package org.yellowteam.models;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Kuznetsov Illia
 * @since 30/10/2022
 */
@Retention(RUNTIME)
@Target({FIELD,METHOD})
public @interface JsonElement {
    public String name() default "";
}
