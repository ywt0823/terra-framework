package com.terra.framework.strata.exception;

import com.terra.framework.common.exception.TerraBaseException;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ValhallaDataVersionException extends TerraBaseException {

    private String sql;
}
