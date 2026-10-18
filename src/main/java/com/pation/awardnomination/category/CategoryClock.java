package com.pation.awardnomination.category;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** Module 2 - single source of "now" for every deadline check (Module 2, the Module 3 lock, Voting). */
@Component
public class CategoryClock {

    public LocalDateTime now() {
        return LocalDateTime.now().withNano(0);
    }
}
