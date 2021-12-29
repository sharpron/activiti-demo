package org.activiti.demo.service;

import org.activiti.demo.dto.*;

import java.io.InputStream;
import java.util.List;

/**
 * @author herong 2021/7/17
 */
public interface WorkflowService {

    List<TodoTaskDto> findTodos();

    /**
     * 启动流程
     *
     * @param startDto startDto
     * @return 业务key
     */
    String start(StartDto startDto);

    /**
     * 暂停流程
     *
     * @param processInstanceId 流程实例id
     */
    void pause(String processInstanceId);

    void resume(String processInstanceId);

    void complete(TaskDto taskDto);

    List<ApprovalNode> getCanApprovals(String taskId, String targetId);

    void back(BackDto backDto);

    List<BackNode> getCanBacks(String taskId);

    ApprovalDirInfo getCanApprovalDirections(String taskId);

    InputStream getDiagram(String processInstanceId);

    HighlightInfo getInstanceHighlights(String processInstanceId);

    List<HistoricTask> getActivityTasks(String processInstanceId, String taskDefinitionKey);
}
