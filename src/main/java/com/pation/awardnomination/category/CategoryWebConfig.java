package com.pation.awardnomination.category;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Module 2 wiring: turns on the timeline engine's schedule and puts the deadline gate
 * in front of the Module 3 nominee pages.
 */
@Configuration
@EnableScheduling
public class CategoryWebConfig implements WebMvcConfigurer {

    private final NominationWindowGate nominationWindowGate;

    public CategoryWebConfig(NominationWindowGate nominationWindowGate) {
        this.nominationWindowGate = nominationWindowGate;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(nominationWindowGate).addPathPatterns("/nominee/**");
    }
}
