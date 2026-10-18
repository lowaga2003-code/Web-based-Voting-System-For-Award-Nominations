package com.pation.awardnomination.evaluation;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * [INTEGRATION - new] Only for the Module 5 controllers (basePackageClasses): an unknown judge / rubric /
 * candidate id goes back to the Module 5 home with a message instead of an error page.
 */
@ControllerAdvice(basePackageClasses = EvaluationController.class)
public class EvaluationErrorAdvice {

    @ExceptionHandler(ResourceNotFoundException.class)
    public String notFound() {
        return "redirect:/evaluation?notFound";
    }
}
