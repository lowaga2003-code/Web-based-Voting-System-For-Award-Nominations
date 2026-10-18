package com.pation.awardnomination.policy;

import com.pation.awardnomination.admin.User;
import com.pation.awardnomination.admin.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * MODULE 6 - withdrawal / data deletion requests ("Delete" part of the CRUD).
 *
 *  submit  -> INSERT a PENDING request
 *  cancel  -> DELETE the user's own pending request
 *  approve -> UPDATE status = APPROVED, then:
 *               WITHDRAW_CONSENT : the user's consent records are marked WITHDRAWN
 *               DELETE_MY_DATA   : consents withdrawn + IP / browser removed + sign-in account disabled
 *                                  (the System Administrator can then delete it for good in User Management)
 *  reject  -> UPDATE status = REJECTED (a reason is required)
 */
@Service
public class DeletionRequestService {

    private final DeletionRequestRepository requests;
    private final ConsentService consentService;
    private final UserRepository accounts;
    private final PolicyAuditLogger auditLogger;

    public DeletionRequestService(DeletionRequestRepository requests, ConsentService consentService,
                                  UserRepository accounts, PolicyAuditLogger auditLogger) {
        this.requests = requests;
        this.consentService = consentService;
        this.accounts = accounts;
        this.auditLogger = auditLogger;
    }

    @Transactional
    public DeletionRequest submit(Long userId, DeletionRequestType type, String reason) {
        if (userId == null || type == null) {
            throw new IllegalArgumentException("Choose what you would like us to do.");
        }
        if (requests.existsByUserIdAndRequestTypeAndStatus(userId, type, RequestStatus.PENDING)) {
            throw new IllegalStateException("You already have a pending \"" + type.getDisplayName() + "\" request.");
        }
        String text = PolicyHtml.limit(PolicyHtml.trim(reason), 1000);
        DeletionRequest saved = requests.save(new DeletionRequest(userId, type, text.isEmpty() ? null : text));
        auditLogger.requestSubmitted(saved);
        return saved;
    }

    /** The user cancels their own request while it is still pending (the row is deleted). */
    @Transactional
    public DeletionRequest cancel(Long requestId, Long userId) {
        DeletionRequest request = find(requestId);
        if (!request.getUserId().equals(userId)) {
            throw new IllegalStateException("You can only cancel your own requests.");
        }
        if (!request.isPending()) {
            throw new IllegalStateException("Only pending requests can be cancelled.");
        }
        requests.delete(request);
        return request;
    }

    @Transactional
    public DeletionRequest approve(Long requestId, Long reviewerId, String reviewerName, String note) {
        DeletionRequest request = pendingForReview(requestId, reviewerId);
        String outcome;
        if (request.getRequestType() == DeletionRequestType.WITHDRAW_CONSENT) {
            int withdrawn = consentService.withdrawAll(request.getUserId(), false);
            outcome = withdrawn + " active consent record(s) withdrawn. The user must agree again before using the system.";
        } else {
            int withdrawn = consentService.withdrawAll(request.getUserId(), true);
            outcome = withdrawn + " consent record(s) withdrawn and IP address / browser details removed. "
                    + disableAccount(request.getUserId());
        }
        request.setStatus(RequestStatus.APPROVED);
        request.setReviewedBy(PolicyHtml.limit(reviewerName, 100));
        request.setReviewedAt(LocalDateTime.now());
        String text = PolicyHtml.limit(PolicyHtml.trim(note), 1000);
        request.setReviewNote(text.isEmpty() ? null : text);
        request.setOutcome(PolicyHtml.limit(outcome, 500));
        DeletionRequest saved = requests.save(request);
        auditLogger.requestReviewed(saved);
        return saved;
    }

    @Transactional
    public DeletionRequest reject(Long requestId, Long reviewerId, String reviewerName, String note) {
        String text = PolicyHtml.limit(PolicyHtml.trim(note), 1000);
        if (text.isEmpty()) {
            throw new IllegalArgumentException("Please give a reason when you reject a request - the user will see it.");
        }
        DeletionRequest request = pendingForReview(requestId, reviewerId);
        request.setStatus(RequestStatus.REJECTED);
        request.setReviewedBy(PolicyHtml.limit(reviewerName, 100));
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewNote(text);
        request.setOutcome("No changes were made.");
        DeletionRequest saved = requests.save(request);
        auditLogger.requestReviewed(saved);
        return saved;
    }

    public List<DeletionRequest> forUser(Long userId) {
        return requests.findByUserIdOrderByRequestedAtDesc(userId);
    }

    /** Request types the user already has waiting for a decision. */
    public Set<DeletionRequestType> pendingTypes(Long userId) {
        Set<DeletionRequestType> types = EnumSet.noneOf(DeletionRequestType.class);
        for (DeletionRequest request : forUser(userId)) {
            if (request.isPending()) {
                types.add(request.getRequestType());
            }
        }
        return types;
    }

    /** Newest first; pending requests are listed oldest first so the longest-waiting one is on top. */
    public List<DeletionRequest> byStatus(RequestStatus status) {
        List<DeletionRequest> list = new ArrayList<>();
        for (DeletionRequest request : requests.findAllByOrderByRequestedAtDesc()) {
            if (request.getStatus() == status) {
                list.add(request);
            }
        }
        if (status == RequestStatus.PENDING) {
            java.util.Collections.reverse(list);
        }
        return list;
    }

    public List<DeletionRequest> all() {
        return requests.findAllByOrderByRequestedAtDesc();
    }

    public long count() {
        return requests.count();
    }

    public long countByStatus(RequestStatus status) {
        return requests.countByStatus(status);
    }

    private DeletionRequest find(Long requestId) {
        return requests.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("This request no longer exists."));
    }

    private DeletionRequest pendingForReview(Long requestId, Long reviewerId) {
        DeletionRequest request = find(requestId);
        if (!request.isPending()) {
            throw new IllegalStateException("This request was already " + request.getStatus().getLabel().toLowerCase() + ".");
        }
        if (request.getUserId().equals(reviewerId)) {
            throw new IllegalStateException("You can't review your own request. Another Legal Officer or the System Administrator must decide.");
        }
        return request;
    }

    /** Disables the main sign-in account (Module 1) - except administrator accounts. */
    private String disableAccount(Long userId) {
        Optional<User> account = accounts.findById(userId);
        if (account.isEmpty()) {
            return "The sign-in account no longer exists.";
        }
        User user = account.get();
        if (PolicyAccounts.isAdministrator(user)) {
            return "Administrator accounts are not disabled automatically - another administrator must remove it in User Management.";
        }
        if (user.isActive()) {
            user.setActive(false);
            accounts.save(user);
        }
        return "Sign-in account disabled - the System Administrator can now delete it permanently in User Management.";
    }
}
