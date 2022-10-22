package de.mark225.beam.command.resolver;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface RegisterBeamCommand {
    String label();
    String[] aliases() default {};
    String description() default "";
    String permission() default "";
    String[] arguments() default {};
    String[] argumentLabels() default {};
}
