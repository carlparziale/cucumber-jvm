package cucumber.api.java;


import cucumber.runtime.java.StepDefAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@StepDefAnnotation
public @interface FeatureScope {
    /**
     * @return a regular expression
     */
    String value();

    /**
     * @return max amount of time this is allowed to run for. 0 (default) means no restriction.
     */
    int timeout() default 0;
}

