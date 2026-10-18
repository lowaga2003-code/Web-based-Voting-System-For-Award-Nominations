package com.pation.awardnomination.policy;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * MODULE 6 - consent records ("Create" and "Read" parts of the CRUD).
 *
 *  recordConsent -> INSERT into consent_records (+ a row in Module 4's transparency ledger)
 *  history       -> SELECT ... WHERE user_id = ? ORDER BY agreed_at DESC
 *  mustAccept    -> which documents the user still has to accept before using the system
 *  withdrawAll   -> UPDATE ... SET status = 'WITHDRAWN' (after an approved request)
 */
@Service
public class ConsentService {

    private final ConsentRecordRepository consents;
    private final PolicyService policyService;
    private final PolicyAuditLogger auditLogger;

    public ConsentService(ConsentRecordRepository consents, PolicyService policyService, PolicyAuditLogger auditLogger) {
        this.consents = consents;
        this.policyService = policyService;
        this.auditLogger = auditLogger;
    }

    /**
     * Saves a timestamped consent to the CURRENT version of each document.
     * @param types which documents (null or empty = Privacy Policy and Terms & Conditions)
     */
    @Transactional
    public List<ConsentRecord> recordConsent(Long userId, ConsentType consentType, Collection<PolicyType> types,
                                             String ipAddress, String userAgent) {
        if (userId == null || consentType == null) {
            throw new IllegalArgumentException("Unknown user or consent type.");
        }
        Collection<PolicyType> documents = (types == null || types.isEmpty()) ? EnumSet.allOf(PolicyType.class) : types;
        LocalDateTime now = LocalDateTime.now();
        List<ConsentRecord> saved = new ArrayList<>();
        for (PolicyType type : documents) {
            PolicyDocument doc = policyService.current(type)
                    .orElseThrow(() -> new IllegalStateException("There is no published " + type.getDisplayName() + " yet."));
            saved.add(consents.save(new ConsentRecord(userId, doc, consentType, now,
                    PolicyHtml.limit(ipAddress, 45), PolicyHtml.limit(userAgent, 255))));
        }
        auditLogger.consentRecorded(userId, consentType, saved);
        return saved;
    }

    /** Consent history of one user, newest first. */
    public List<ConsentRecord> history(Long userId) {
        return consents.findByUserIdOrderByAgreedAtDesc(userId);
    }

    /** The latest ACTIVE consent per document = what the user agrees to right now. */
    public Map<PolicyType, ConsentRecord> currentConsents(Long userId) {
        return latestPerDocument(userId, null);
    }

    /** Same, but only consents given in one place - e.g. ConsentType.VOTING for the voting page. */
    public Map<PolicyType, ConsentRecord> currentConsents(Long userId, ConsentType consentType) {
        return latestPerDocument(userId, consentType);
    }

    /** Documents the user MUST accept before using the system (empty list = nothing to do). */
    public List<PolicyDocument> mustAccept(Long userId) {
        return missing(currentConsents(userId));
    }

    /** Documents the user still has to accept on the voting page before voting (empty list = ready to vote). */
    public List<PolicyDocument> missingVotingConsent(Long userId) {
        return missing(currentConsents(userId, ConsentType.VOTING));
    }

    private Map<PolicyType, ConsentRecord> latestPerDocument(Long userId, ConsentType onlyType) {
        Map<PolicyType, ConsentRecord> map = new EnumMap<>(PolicyType.class);
        for (ConsentRecord record : consents.findByUserIdAndStatus(userId, ConsentStatus.ACTIVE)) {
            if (onlyType != null && record.getConsentType() != onlyType) {
                continue;
            }
            ConsentRecord best = map.get(record.getPolicyType());
            if (best == null || isNewer(record, best)) {
                map.put(record.getPolicyType(), record);
            }
        }
        return map;
    }

    /** Current documents for which the given consents are missing or older than the oldest version still accepted. */
    private List<PolicyDocument> missing(Map<PolicyType, ConsentRecord> agreed) {
        List<PolicyDocument> result = new ArrayList<>();
        for (PolicyType type : PolicyType.values()) {
            Optional<PolicyDocument> latest = policyService.current(type);
            if (latest.isEmpty()) {
                continue;
            }
            int minimum = policyService.minimumAccepted(type).map(PolicyDocument::versionOrder)
                    .orElse(latest.get().versionOrder());
            ConsentRecord record = agreed.get(type);
            if (record == null || PolicyDocument.versionOrder(record.getPolicyVersion()) < minimum) {
                result.add(latest.get());
            }
        }
        return result;
    }

    /** Newer versions the user MAY accept but doesn't have to (minor updates). */
    public List<PolicyDocument> optionalUpdates(Long userId) {
        Map<PolicyType, ConsentRecord> agreed = currentConsents(userId);
        List<PolicyDocument> result = new ArrayList<>();
        for (PolicyType type : PolicyType.values()) {
            Optional<PolicyDocument> latest = policyService.current(type);
            ConsentRecord record = agreed.get(type);
            if (latest.isEmpty() || record == null) {
                continue;
            }
            int accepted = PolicyDocument.versionOrder(record.getPolicyVersion());
            int minimum = policyService.minimumAccepted(type).map(PolicyDocument::versionOrder).orElse(0);
            if (accepted >= minimum && accepted < latest.get().versionOrder()) {
                result.add(latest.get());
            }
        }
        return result;
    }

    /**
     * Marks every active consent of the user as WITHDRAWN (rows are kept as proof).
     * @param removeDeviceDetails true = also clear the IP address and browser (personal data) from all rows
     * @return how many active records were withdrawn
     */
    @Transactional
    public int withdrawAll(Long userId, boolean removeDeviceDetails) {
        int withdrawn = 0;
        LocalDateTime now = LocalDateTime.now();
        for (ConsentRecord record : consents.findByUserIdOrderByAgreedAtDesc(userId)) {
            boolean changed = false;
            if (record.isActive()) {
                record.setStatus(ConsentStatus.WITHDRAWN);
                record.setWithdrawnAt(now);
                withdrawn++;
                changed = true;
            }
            if (removeDeviceDetails && (record.getIpAddress() != null || record.getUserAgent() != null)) {
                record.setIpAddress(null);
                record.setUserAgent(null);
                changed = true;
            }
            if (changed) {
                consents.save(record);
            }
        }
        return withdrawn;
    }

    public List<ConsentRecord> recent() {
        return consents.findTop50ByOrderByAgreedAtDesc();
    }

    public long usersOnVersion(Long policyId) {
        return policyId == null ? 0 : consents.countUsersOnPolicy(policyId, ConsentStatus.ACTIVE);
    }

    public long count() {
        return consents.count();
    }

    public long countByStatus(ConsentStatus status) {
        return consents.countByStatus(status);
    }

    private static boolean isNewer(ConsentRecord a, ConsentRecord b) {
        int va = PolicyDocument.versionOrder(a.getPolicyVersion());
        int vb = PolicyDocument.versionOrder(b.getPolicyVersion());
        if (va != vb) {
            return va > vb;
        }
        return a.getAgreedAt() != null && b.getAgreedAt() != null && a.getAgreedAt().isAfter(b.getAgreedAt());
    }
}
