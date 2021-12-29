package org.activiti.demo.service.impl;

import org.activiti.bpmn.model.*;
import org.activiti.demo.dto.BackDto;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.cmd.NeedsActiveTaskCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntityManager;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.activiti.engine.task.DelegationState;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BackCmd extends NeedsActiveTaskCmd<Void> {
    private static final long serialVersionUID = 1L;

    // 当前任务

    private final BackDto backDto;

    public BackCmd(BackDto backDto) {
        super(backDto.getTaskId());
        this.backDto = backDto;
    }


    private List<FlowNode> getBackFlowNodes(BpmnModel bpmnModel) {
        return backDto.getBackActivityIds().stream()
                .map(bpmnModel::getFlowElement)
                .map(e -> (FlowNode) e)
                .collect(Collectors.toList());
    }

    @Override
    protected Void execute(CommandContext commandContext, TaskEntity task) {
        BpmnModel bpmnModel = ProcessDefinitionUtil.getBpmnModel(task.getProcessDefinitionId());
        UserTask userTask = (UserTask) bpmnModel.getFlowElement(task.getTaskDefinitionKey());

        // 检测是否可以回退 流入的结点是并行结点
        if (preNodeIsParallel(userTask)) {
            throw new ActivitiException("Current node is parallel gateway, can not execute back cmd");
        }

        TaskEntityManager taskEntityManager = commandContext.getTaskEntityManager();

        // 挂起的任务需要先激活
        if (task.getDelegationState() == DelegationState.PENDING) {
            throw new ActivitiException("A delegated task cannot be completed, but should be resolved instead.");
        }

        // 完成当前任务
        taskEntityManager.deleteTask(task, backDto.getReason(), false, false);
        ExecutionEntity executionEntity = commandContext.getExecutionEntityManager()
                .findById(task.getExecutionId());

        getBackFlowNodes(bpmnModel).forEach(e -> {
            SequenceFlow backStepSequenceFlow = new SequenceFlow();
            backStepSequenceFlow.setId(UUID.randomUUID().toString());
            backStepSequenceFlow.setSourceFlowElement(userTask);
            backStepSequenceFlow.setTargetFlowElement(e);

            executionEntity.setCurrentFlowElement(backStepSequenceFlow);
            Context.getAgenda().planTakeOutgoingSequenceFlowsOperation(executionEntity, false);
        });
        return null;
    }

    public static boolean preNodeIsParallel(FlowNode userTask) {
        List<SequenceFlow> sequenceFlows = userTask.getIncomingFlows();
        for (SequenceFlow sequenceFlow : sequenceFlows) {
            FlowNode flowNode = (FlowNode) sequenceFlow.getSourceFlowElement();
            if (flowNode instanceof InclusiveGateway || flowNode instanceof ParallelGateway) {
                return true;
            }
        }
        return false;
    }

}