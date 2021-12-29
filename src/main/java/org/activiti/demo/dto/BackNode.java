package org.activiti.demo.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author herong
 * 2021/7/17
 */
@Data
public class BackNode {

    private String activityId;

    private String taskName;

    private Date startTime;

    private Date endTime;
}
