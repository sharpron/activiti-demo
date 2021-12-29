package org.activiti.demo;

import org.activiti.engine.*;
import org.activiti.engine.repository.Deployment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author herong
 * 2021/7/17
 */
@Configuration
public class ActivitiConfig {


    @Bean
    public ProcessEngine buildProcessEngine() {
        return ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration()
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE)
//                .setJdbcUrl("jdbc:h2:mem:my-own-db;DB_CLOSE_DELAY=1000")
                .setJdbcUrl("jdbc:mysql://localhost:3306/activiti_demo?nullCatalogMeansCurrent=true")
                .setJdbcUsername("remote_root")
                .setJdbcPassword("123456!")
                .setJdbcDriver("com.mysql.cj.jdbc.Driver")
                .setAsyncExecutorActivate(false)
                .buildProcessEngine();
    }

    @Bean
    public TaskService taskService(ProcessEngine processEngine) {
        return processEngine.getTaskService();
    }

    @Bean
    public HistoryService historyService(ProcessEngine processEngine) {
        return processEngine.getHistoryService();
    }

    @Bean
    public ManagementService managementService(ProcessEngine processEngine) {
        return processEngine.getManagementService();
    }

    @Bean
    public RepositoryService repositoryService(ProcessEngine processEngine) {
        return processEngine.getRepositoryService();
    }

    @Bean
    public RuntimeService runtimeService(ProcessEngine processEngine) {
        return processEngine.getRuntimeService();
    }

    @Bean
    Deployment deployment(RepositoryService repositoryService) {
        return repositoryService.createDeployment()//创建部署对象
                .addClasspathResource("test.bpmn")
                .name("test")
                .deploy();
    }


}
