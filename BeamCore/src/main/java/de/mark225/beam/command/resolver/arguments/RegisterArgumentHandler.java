package de.mark225.beam.command.resolver.arguments;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface RegisterArgumentHandler {
    String value();
    String[] usageIndicators() default {"<", ">"};
}
