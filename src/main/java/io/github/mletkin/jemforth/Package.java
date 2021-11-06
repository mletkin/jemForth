package io.github.mletkin.jemforth;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ TYPE, FIELD, METHOD, CONSTRUCTOR })
@Retention(RetentionPolicy.SOURCE)
public @interface Package {
    String cause();
}
