package org.activiti.demo.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * @author herong
 * 2021/7/17
 */
@Data
public class TaskDto {

    @NotNull
    private String taskId;

    private List<String> targetActivityIds;

    private Map<String, Object> variables;

    private String comment;

}
