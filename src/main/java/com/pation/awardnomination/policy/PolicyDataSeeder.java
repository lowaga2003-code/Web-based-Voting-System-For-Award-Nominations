package com.pation.awardnomination.policy;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * MODULE 6 - on the very first start (empty policy_documents table) publishes version 1.0 of the
 * Privacy Policy and of the Terms & Conditions, so users always have something to agree to.
 * The Legal & Governance Officer can then edit them in the Legal console.
 */
@Component
public class PolicyDataSeeder implements CommandLineRunner {

    private final PolicyService policyService;

    public PolicyDataSeeder(PolicyService policyService) {
        this.policyService = policyService;
    }

    @Override
    public void run(String... args) {
        if (policyService.count() > 0) {
            return;
        }
        policyService.seedFirstVersion(PolicyType.PRIVACY_POLICY, "Privacy Policy", PRIVACY_POLICY_V1);
        policyService.seedFirstVersion(PolicyType.TERMS_AND_CONDITIONS, "Terms & Conditions", TERMS_V1);
    }

    static final String PRIVACY_POLICY_V1 =
            "<p>This Privacy Policy explains how the <strong>Award Nomination System</strong> collects, uses and protects "
            + "personal data when you register, submit a nomination, vote or raise a dispute.</p>"
            + "<h2>1. Information we collect</h2>"
            + "<ul>"
            + "<li><strong>Account details</strong> &ndash; your username, e-mail address and an encrypted password.</li>"
            + "<li><strong>Nomination details</strong> &ndash; nominee profiles, NIC numbers, company details and the evidence files you upload.</li>"
            + "<li><strong>Voting records</strong> &ndash; the award categories you voted in. Each vote is counted once per category.</li>"
            + "<li><strong>Dispute tickets</strong> &ndash; the descriptions and evidence you send to the Compliance Auditor.</li>"
            + "<li><strong>Technical logs</strong> &ndash; sign-in times, IP address and browser details, used to keep voting secure.</li>"
            + "</ul>"
            + "<h2>2. Why we use it</h2>"
            + "<ul>"
            + "<li>To run the nomination, evaluation and voting process.</li>"
            + "<li>To stop duplicate or fraudulent votes.</li>"
            + "<li>To investigate disputes and keep a transparent audit trail.</li>"
            + "<li>To contact you about your account, nomination or dispute.</li>"
            + "</ul>"
            + "<h2>3. Your consent</h2>"
            + "<p>We use your data on the basis of your consent. When you register or vote you tick <em>I agree</em>, and the "
            + "system stores the date, the time and the exact version of this policy you accepted. If we make an important "
            + "change, you will be asked to accept the new version before you continue.</p>"
            + "<h2>4. Who can see your data</h2>"
            + "<p>Only authorised staff &ndash; the System Administrator, Programme Manager, Selection Committee, Compliance "
            + "Auditor and Legal &amp; Governance Officer &ndash; and only what their role needs. We never sell your data.</p>"
            + "<h2>5. How long we keep it</h2>"
            + "<p>Account and nomination data is kept while your account is active and for the length of the award "
            + "programme. Consent records are kept as proof of consent, without personal details once your data is deleted.</p>"
            + "<h2>6. Your rights</h2>"
            + "<ul>"
            + "<li>See every consent you have given on the <em>My privacy &amp; consent</em> page.</li>"
            + "<li>Withdraw your consent.</li>"
            + "<li>Ask us to delete your personal data. The Legal &amp; Governance Officer reviews every request.</li>"
            + "</ul>"
            + "<h2>7. Contact</h2>"
            + "<p>Questions about this policy can be sent to the Legal &amp; Governance Officer of the Award Nomination System.</p>";

    static final String TERMS_V1 =
            "<p>These Terms &amp; Conditions apply to everyone who uses the Award Nomination System. By creating an account "
            + "or voting you agree to them.</p>"
            + "<h2>1. Accounts</h2>"
            + "<ul>"
            + "<li>Give true and complete information when you register.</li>"
            + "<li>Keep your password and one-time codes private. You are responsible for what happens on your account.</li>"
            + "<li>Staff accounts are created only by the System Administrator.</li>"
            + "</ul>"
            + "<h2>2. Nominations</h2>"
            + "<ul>"
            + "<li>Nominee details and evidence must be accurate and must not break anyone else's rights.</li>"
            + "<li>Evidence files must be PDF, PNG or JPG and no larger than 5 MB.</li>"
            + "<li>The Programme Manager and the Selection Committee may reject nominations that do not meet the eligibility rules.</li>"
            + "</ul>"
            + "<h2>3. Voting</h2>"
            + "<ul>"
            + "<li>One vote per person in each award category.</li>"
            + "<li>Automated, duplicate or bought votes are not allowed and will be removed.</li>"
            + "<li>Results are final once the Selection Committee publishes them, subject to the dispute process.</li>"
            + "</ul>"
            + "<h2>4. Disputes</h2>"
            + "<p>If you believe something went wrong, raise a dispute ticket. The Compliance Auditor investigates it and "
            + "records the outcome in the transparency ledger.</p>"
            + "<h2>5. Suspension</h2>"
            + "<p>Accounts that break these terms &ndash; for example by trying to vote more than once &ndash; may be suspended.</p>"
            + "<h2>6. Changes to these terms</h2>"
            + "<p>When the terms change we publish a new version. For important changes you will be asked to accept the new "
            + "version before you can continue.</p>";
}
