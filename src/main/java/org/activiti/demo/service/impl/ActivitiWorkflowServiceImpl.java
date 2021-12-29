package org.activiti.demo.service.impl;

import lombok.RequiredArgsConstructor;
import org.activiti.bpmn.model.*;
import org.activiti.demo.dto.*;
import org.activiti.demo.service.WorkflowService;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author herong 2021/7/17
 */
@Service
@RequiredArgsConstructor
public class ActivitiWorkflowServiceImpl implements WorkflowService {

    private final TaskService taskService;
    private final HistoryService historyService;

    private final ManagementService managementService;
    private final RepositoryService repositoryService;

    private final RuntimeService runtimeService;


    @Override
    public List<TodoTaskDto> findTodos() {
        return taskService.createTaskQuery()
                .orderByTaskCreateTime().desc()
                .list()
                .stream()
                .map(TodoTaskDto::ofTask)
                .collect(Collectors.toList());
    }

    @Override
    public String start(StartDto startDto) {
        String processDefinitionKey = startDto.getProcessDefinitionKey();
        String businessKey = processDefinitionKey + ":" + UUID.randomUUID();
        runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, startDto.getVars());
        return businessKey;
    }

    @Override
    public void pause(String processInstanceId) {
        runtimeService.suspendProcessInstanceById(processInstanceId);
    }

    @Override
    public void resume(String processInstanceId) {
        runtimeService.activateProcessInstanceById(processInstanceId);
    }

    @Override
    public void complete(TaskDto taskDto) {
        if (StringUtils.isNotBlank(taskDto.getComment())) {
            taskService.addComment(taskDto.getTaskId(), null, taskDto.getComment());
        }
    }

    @Override
    public List<ApprovalNode> getCanApprovals(String taskId, String targetId) {
        final Task task = taskOf(taskId);
        final UserTask userTask = nodeOf(task);

        return findTarget(userTask, targetId, new ArrayList<>()).stream()
                .map(e -> {
                    final ApprovalNode approvalNode = new ApprovalNode();
                    approvalNode.setActivityId(e.getId());
                    approvalNode.setName(e.getName());
                    return approvalNode;
                }).collect(Collectors.toList());
    }

    private List<FlowNode> findTarget(FlowNode flowNode, String targetId, List<FlowNode> result) {
        for (SequenceFlow outgoingFlow : flowNode.getOutgoingFlows()) {
            final FlowNode target = (FlowNode) outgoingFlow.getTargetFlowElement();
            if (target instanceof UserTask && target.getId().equals(targetId)) {
                result.add(target);
                return result;
            } else {
                findTarget(target, targetId, result);
            }
        }
        return result;
    }

    private Task taskOf(String taskId) {
        return taskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();
    }

    @Override
    public void back(BackDto backDto) {
        final BackCmd backCmd = new BackCmd(backDto);
        managementService.executeCommand(backCmd);
    }

    @Override
    public List<BackNode> getCanBacks(String taskId) {
        final Task task = taskOf(taskId);

        return getBackUserTasks(task).stream().map(e -> {
            final BackNode backNode = new BackNode();
            backNode.setActivityId(e.getTaskDefinitionKey());
            backNode.setStartTime(e.getStartTime());
            backNode.setEndTime(e.getEndTime());
            backNode.setTaskName(e.getName());
            return backNode;
        }).collect(Collectors.toList());
    }

    @Override
    public ApprovalDirInfo getCanApprovalDirections(String taskId) {
        final UserTask userTask = nodeOf(taskOf(taskId));

        final List<SequenceFlow> outgoingFlows = userTask.getOutgoingFlows();
        if (outgoingFlows.isEmpty()) {
            return ApprovalDirInfo.ofEmpty();
        }

        if (outgoingFlows.size() == 1) {
            final SequenceFlow sequenceFlow = outgoingFlows.get(0);
            final FlowNode target = (FlowNode) sequenceFlow.getTargetFlowElement();
            if (target instanceof InclusiveGateway) {
                return ApprovalDirInfo.of(SelectOption.MULTI, target.getOutgoingFlows());
            } else if (target instanceof ParallelGateway) {
                return ApprovalDirInfo.of(SelectOption.ALL, target.getOutgoingFlows());
            } else if (target instanceof ExclusiveGateway) {
                return ApprovalDirInfo.of(SelectOption.SINGLE, target.getOutgoingFlows());
            }
        }

        return ApprovalDirInfo.ofEmpty();
    }

    @Override
    public InputStream getDiagram(String processInstanceId) {
        final HistoricProcessInstance historicProcessInstance = historyService
                .createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        if (historicProcessInstance == null) {
            throw new RuntimeException("没有找到对应的流程实例");
        }
        return repositoryService.getProcessModel(historicProcessInstance.getProcessDefinitionId());
    }

    @Override
    public HighlightInfo getInstanceHighlights(String processInstanceId) {
        final List<HistoricActivityInstance> historicActivityInstances = historyService
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .list();

        return HighlightInfo.builder()
                .activeActivityIds(getActiveActivityIds(historicActivityInstances))
                .finishedActivityIds(getFinishedActivityIds(historicActivityInstances))
                .finishedFlowIds(getHighLightedFlows(historicActivityInstances))
                .build();
    }

    @Override
    public List<HistoricTask> getActivityTasks(String processInstanceId, String taskDefinitionKey) {
        return historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .taskDefinitionKey(taskDefinitionKey)
                .list()
                .stream()
                .map(e -> new HistoricTask(e.getAssignee(), e.getStartTime(),
                        e.getEndTime(), e.getDeleteReason()))
                .collect(Collectors.toList());
    }

    private List<String> getHighLightedFlows(
            List<HistoricActivityInstance> historicActivityInstances) {
        if (historicActivityInstances.isEmpty()) {
            throw new IllegalStateException("历史记录为空");
        }
        // process definition id 全部一样
        final String processDefinitionId = historicActivityInstances.get(0).getProcessDefinitionId();
        final BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);

        Map<String, List<HistoricActivityInstance>> activityInstanceMap = new HashMap<>();
        for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
            activityInstanceMap.computeIfAbsent(historicActivityInstance.getActivityId(),
                            e -> new ArrayList<>(1))
                    .add(historicActivityInstance);
        }

        List<String> flowIds = new ArrayList<>();
        for (HistoricActivityInstance sourceActivityInstance : historicActivityInstances) {
            final FlowNode source = (FlowNode) bpmnModel.getFlowElement(
                    sourceActivityInstance.getActivityId());
            for (SequenceFlow outgoingFlow : source.getOutgoingFlows()) {
                final FlowNode target = (FlowNode) outgoingFlow.getTargetFlowElement();
                final List<HistoricActivityInstance> targets = activityInstanceMap.get(target.getId());
                if (targets == null) {
                    continue;
                }
                for (HistoricActivityInstance targetActivityInstance : targets) {
                    if (target.getId().equals(targetActivityInstance.getActivityId()) &&
                            lineIsComplete(sourceActivityInstance, targetActivityInstance)) {
                        flowIds.add(outgoingFlow.getId());
                    }
                }
            }
        }

        return flowIds;
    }

    private static boolean lineIsComplete(HistoricActivityInstance source,
                                          HistoricActivityInstance target) {
        assert source != null;
        if (target == null || source.getEndTime() == null) {
            return false;
        }
        // 两秒的误差
        return source.getEndTime().getTime() - target.getStartTime().getTime() < 2000;
    }


    private List<String> getActiveActivityIds(
            List<HistoricActivityInstance> historicActivityInstances) {
        return historicActivityInstances.stream()
                .filter(e -> Objects.isNull(e.getEndTime()))
                .map(HistoricActivityInstance::getActivityId)
                .collect(Collectors.toList());
    }

    private List<String> getFinishedActivityIds(
            List<HistoricActivityInstance> historicActivityInstances) {
        return historicActivityInstances.stream()
                .filter(e -> Objects.nonNull(e.getEndTime()))
                .map(HistoricActivityInstance::getActivityId)
                .collect(Collectors.toList());
    }


    private static List<UserTask> findPreUserTasks(
            FlowNode userTask, List<UserTask> result) {
        List<SequenceFlow> sequenceFlows = userTask.getIncomingFlows();
        for (SequenceFlow sequenceFlow : sequenceFlows) {
            FlowNode flowNode = (FlowNode) sequenceFlow.getSourceFlowElement();
            if (flowNode instanceof UserTask) {
                result.add((UserTask) flowNode);
            } else {
                findPreUserTasks(flowNode, result);
            }
        }
        return result;
    }

    private UserTask nodeOf(Task task) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        return (UserTask) bpmnModel.getFlowElement(task.getTaskDefinitionKey());
    }

    public List<HistoricTaskInstance> getBackUserTasks(Task task) {
        final UserTask userTask = nodeOf(task);
        final List<UserTask> preUserTasks = findPreUserTasks(userTask, new ArrayList<>());

        final Set<String> sequenceId = preUserTasks.stream()
                .map(BaseElement::getId).collect(Collectors.toSet());

        return historyService
                .createHistoricTaskInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .finished()
                .orderByTaskCreateTime().desc()
                .list()
                .stream()
                .filter(e -> sequenceId.contains(e.getTaskDefinitionKey()))
                .collect(Collectors.toList());
    }
}
