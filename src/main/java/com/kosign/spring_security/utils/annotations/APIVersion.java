package com.kosign.spring_security.utils.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface APIVersion {
    String value();
    String since() default "";
    String deprecated() default "";
    boolean breaking() default false;
    String description() default "";
    String migrationGuide() default "";
    String backwardCompatibilityDuration() default "";
    Class<?> requestExample() default Void.class;
    Class<?> responseExample() default Void.class;
}