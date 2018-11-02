package mb.fs.api;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;
import java.lang.annotation.*;

@Documented
@Nonnull
@TypeQualifierDefault(
    {
        ElementType.ANNOTATION_TYPE,
        ElementType.CONSTRUCTOR,
        ElementType.FIELD,
        ElementType.LOCAL_VARIABLE,
        ElementType.METHOD,
        ElementType.PACKAGE,
        ElementType.PARAMETER,
        ElementType.TYPE
    })
@Retention(RetentionPolicy.RUNTIME)
public @interface NonNullByDefault {

}
