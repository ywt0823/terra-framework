package com.terra.framework.crust.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface IgnoreResponseAdvice {

    boolean ignore() default true;

}
