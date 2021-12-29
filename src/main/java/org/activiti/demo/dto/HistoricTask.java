package org.activiti.demo.dto;

import lombok.Value;

import java.util.Date;

/**
 * @author herong
 * 2021/7/20
 */
@Value
public class HistoricTask {
    String assignee;
    Date startTime;
    Date endTime;
    String advise;
}
