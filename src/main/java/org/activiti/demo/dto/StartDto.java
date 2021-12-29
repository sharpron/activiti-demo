package org.activiti.demo.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * @author ron 2021/12/29
 */
@Data
public class StartDto {

    @NotNull
    private String processDefinitionKey;

    private Map<String, Object> vars;
}
