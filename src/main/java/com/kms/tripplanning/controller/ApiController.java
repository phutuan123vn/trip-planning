package com.kms.tripplanning.controller;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * Custom annotation to combine @RestController to unify API Rendering for all controllers in the application. 
 * This is useful for future enhancements, such as adding common exception handling, logging, or response formatting for all API controllers.
 */
@RestController
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiController {

    @AliasFor(annotation = RestController.class)
	String value() default "";
}
