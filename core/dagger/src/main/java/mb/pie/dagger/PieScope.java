package mb.pie.dagger;

import javax.inject.Scope;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A dependency-injection {@link Scope scope annotation} for instances of {@link PieComponent PIE components}.
 */
@Documented
@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface PieScope {}
