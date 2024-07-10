package mb.pie.task.java;

import org.immutables.value.Value;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PACKAGE, ElementType.TYPE})
@Retention(RetentionPolicy.CLASS) // Class retention for incremental compilation.
@Value.Style(
    jdkOnly = true,
    typeImmutableEnclosing = "*Data",
    deepImmutablesDetection = true
) @interface ImmutableStyle {}
