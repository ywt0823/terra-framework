package com.terra.framework.strata.annoation;

import java.lang.annotation.*;

/**
 * @author Zeus
 * @date 2025年07月08日 15:47
 * @description TerraMapper
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface TerraDatasource {

    String datasourceName() default "mysql";

}
