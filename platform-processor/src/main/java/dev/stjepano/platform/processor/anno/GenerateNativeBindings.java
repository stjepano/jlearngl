package dev.stjepano.platform.processor.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface GenerateNativeBindings {
    String className();
    String classAccess() default "public";
    String targetPackage() default "";
}
