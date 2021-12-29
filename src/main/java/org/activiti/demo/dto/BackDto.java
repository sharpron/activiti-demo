package org.activiti.demo.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author herong
 * 2021/7/17
 */
@Data
public class BackDto {

    @NotNull
    private String taskId;

    @NotEmpty
    private List<String> backActivityIds;

    @NotBlank
    private String reason;

}
