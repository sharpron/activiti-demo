package org.activiti.demo.dto;

import lombok.Value;
import org.activiti.bpmn.model.SequenceFlow;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author herong
 * 2021/7/17
 */
@Value
public class ApprovalDirInfo {

    SelectOption selectOption;
    List<DirectionDto> directions;

    public static ApprovalDirInfo ofEmpty() {
        return new ApprovalDirInfo(SelectOption.NONE, Collections.emptyList());
    }

    public static ApprovalDirInfo of(SelectOption selectOption, List<SequenceFlow> flows) {
        final List<DirectionDto> directions = flows.stream().map(flow -> new DirectionDto(
                flow.getName(), flow.getTargetFlowElement().getId())
        ).collect(Collectors.toList());

        return new ApprovalDirInfo(selectOption, directions);
    }
}
