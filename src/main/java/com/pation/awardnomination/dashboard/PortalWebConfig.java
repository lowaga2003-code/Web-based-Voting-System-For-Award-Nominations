package com.pation.awardnomination.dashboard;

import com.pation.awardnomination.policy.PolicyConsentGate;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * [INTEGRATION] Plugs the single sign-on bridges in front of each member's pages.
 *
 * (සිංහලෙන්: /nominee/** request එකකට කලින් NomineeSessionBridge එකත්, /dispute/** සහ /auditor/**
 *  request එකකට කලින් DisputeSessionBridge එකත් run වෙන්න register කරනවා. Module 6 එකේ
 *  PolicyConsentGate එක හැම module page එකකටම කලින් run වෙනවා - policy එකට agree නැත්නම් consent page එකට.
 *  Module 2 එකේ NominationWindowGate එක register වෙන්නේ category.CategoryWebConfig එකේ.)
 */
@Configuration
public class PortalWebConfig implements WebMvcConfigurer {

    private final NomineeSessionBridge nomineeSessionBridge;
    private final DisputeSessionBridge disputeSessionBridge;
    private final PolicyConsentGate policyConsentGate;

    public PortalWebConfig(NomineeSessionBridge nomineeSessionBridge, DisputeSessionBridge disputeSessionBridge,
                           PolicyConsentGate policyConsentGate) {
        this.nomineeSessionBridge = nomineeSessionBridge;
        this.disputeSessionBridge = disputeSessionBridge;
        this.policyConsentGate = policyConsentGate;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Module 6 - first: accept the current Privacy Policy / Terms before using any module page
        registry.addInterceptor(policyConsentGate).addPathPatterns(
                "/dashboard", "/admin/**", "/nominee/**", "/dispute/**", "/auditor/**", "/legal/**", "/manager/**", "/voter/**",
                "/evaluation/**", "/timeline/**");   // [INTEGRATION v5] /timeline = Module 2 award calendar
        registry.addInterceptor(nomineeSessionBridge).addPathPatterns("/nominee/**");   // Module 3
        registry.addInterceptor(disputeSessionBridge).addPathPatterns("/dispute/**", "/auditor/**"); // Module 4
    }
}
