package org.activiti.demo.web;

import lombok.RequiredArgsConstructor;
import org.activiti.demo.dto.BackDto;
import org.activiti.demo.dto.HighlightInfo;
import org.activiti.demo.dto.StartDto;
import org.activiti.demo.dto.TaskDto;
import org.activiti.demo.service.WorkflowService;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.util.Collections;

/**
 * 工作流公用功能
 *
 * @author herong
 * 2021/7/17
 */
@RestController
@RequestMapping("api/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @GetMapping("todos")
    public ResponseEntity<?> getTodos() {
        return ResponseEntity.ok(workflowService.findTodos());
    }


    @PostMapping
    public ResponseEntity<?> start(@Validated @RequestBody StartDto startDto) {
        String businessKey = workflowService.start(startDto);
        return ResponseEntity.ok(businessKey);
    }


    @GetMapping("backs")
    public ResponseEntity<?> getCanBackNodes(@RequestParam String taskId) {
        return ResponseEntity.ok(workflowService.getCanBacks(taskId));
    }

    @GetMapping("approvals")
    public ResponseEntity<?> getApprovalNodes(@RequestParam String taskId,
                                              @RequestParam String targetId) {
        return ResponseEntity.ok(workflowService.getCanApprovals(taskId, targetId));
    }

    @GetMapping("approval-direction-info")
    public ResponseEntity<?> getCanApprovalDirInfo(@RequestParam String taskId) {
        return ResponseEntity.ok(workflowService.getCanApprovalDirections(taskId));
    }


    @PostMapping("tasks")
    public ResponseEntity<?> complete(@RequestBody @Validated TaskDto taskDto) {
        workflowService.complete(taskDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("backs")
    public ResponseEntity<?> back(@RequestBody @Validated BackDto backDto) {
        workflowService.back(backDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("diagrams")
    public ResponseEntity<StreamingResponseBody> getDiagram(@RequestParam String processInstanceId) {
        final InputStream diagram = workflowService.getDiagram(processInstanceId);
        return ResponseEntity.ok(outputStream -> IOUtils.copy(diagram, outputStream));
    }

    @GetMapping("instance-highlights")
    public ResponseEntity<?> getInstanceHighlights(@RequestParam String processInstanceId) {
        final HighlightInfo instanceHighlights = workflowService.getInstanceHighlights(processInstanceId);
        return ResponseEntity.ok(instanceHighlights);
    }

    @GetMapping("activity-tasks")
    public ResponseEntity<?> getActivityTasks(@RequestParam String processInstanceId,
                                              @RequestParam String taskDefinitionKey) {
        return ResponseEntity.ok(workflowService.getActivityTasks(processInstanceId, taskDefinitionKey));
    }
}
