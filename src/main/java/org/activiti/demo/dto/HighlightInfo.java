package org.activiti.demo.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * @author herong
 * 2021/7/19
 */
@Builder
@Getter
public class HighlightInfo {

    List<String> activeActivityIds;
    List<String> activeFlowIds;

    List<String> finishedActivityIds;
    List<String> finishedFlowIds;
}
