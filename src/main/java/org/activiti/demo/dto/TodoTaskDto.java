package org.activiti.demo.dto;

import lombok.Data;
import org.activiti.engine.task.Task;

import java.util.Date;

/**
 * @author herong
 * 2021/7/17
 */
@Data
public class TodoTaskDto {

    private String id;
    private String name;
    private String description;
    private int priority;

    private Date dueDate;
    private Integer version;
    private boolean suspended;


    public static TodoTaskDto ofTask(Task task) {
        final TodoTaskDto todoTaskDto = new TodoTaskDto();
        todoTaskDto.setId(task.getId());
        todoTaskDto.setName(task.getName());
        todoTaskDto.setDescription(task.getDescription());
        todoTaskDto.setPriority(task.getPriority());
        todoTaskDto.setDueDate(task.getDueDate());
        todoTaskDto.setVersion(task.getAppVersion());
        todoTaskDto.setSuspended(task.isSuspended());

        return todoTaskDto;
    }
}
