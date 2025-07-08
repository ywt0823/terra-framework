package com.terra.framework.strata.annoation;

import org.apache.ibatis.annotations.Mapper;

import java.lang.annotation.*;

/**
 * @author Zeus
 * @date 2025年07月08日 15:47
 * @description TerraMapper
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Mapper
public @interface TerraMapper {

    String datasourceName() default "mysql";

}
