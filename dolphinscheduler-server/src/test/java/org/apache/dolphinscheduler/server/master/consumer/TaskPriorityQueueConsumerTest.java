/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.server.master.consumer;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.dao.entity.*;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.dispatch.ExecutorDispatcher;
import org.apache.dolphinscheduler.server.master.dispatch.executor.NettyExecutorManager;
import org.apache.dolphinscheduler.server.registry.DependencyConfig;
import org.apache.dolphinscheduler.server.registry.ZookeeperNodeManager;
import org.apache.dolphinscheduler.server.registry.ZookeeperRegistryCenter;
import org.apache.dolphinscheduler.server.zk.SpringZKServer;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.queue.TaskPriorityQueue;
import org.apache.dolphinscheduler.service.queue.TaskPriorityQueueImpl;
import org.apache.dolphinscheduler.service.zk.ZookeeperCachedOperator;
import org.apache.dolphinscheduler.service.zk.ZookeeperConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={DependencyConfig.class, SpringApplicationContext.class, SpringZKServer.class,
        NettyExecutorManager.class, ExecutorDispatcher.class, ZookeeperRegistryCenter.class, TaskPriorityQueueConsumer.class,
        ZookeeperNodeManager.class, ZookeeperCachedOperator.class, ZookeeperConfig.class, MasterConfig.class})
public class TaskPriorityQueueConsumerTest {


    @Autowired
    private TaskPriorityQueue taskPriorityQueue;

    @Autowired
    private TaskPriorityQueueConsumer taskPriorityQueueConsumer;

    @Autowired
    private ProcessService processService;

    @Autowired
    private ExecutorDispatcher dispatcher;

    @Before
    public void init(){

        Tenant tenant = new Tenant();
        tenant.setId(1);
        tenant.setTenantCode("journey");
        tenant.setTenantName("journey");
        tenant.setDescription("journey");
        tenant.setQueueId(1);
        tenant.setCreateTime(new Date());
        tenant.setUpdateTime(new Date());

        Mockito.when(processService.getTenantForProcess(1,2)).thenReturn(tenant);

        Mockito.when(processService.queryUserQueueByProcessInstanceId(1)).thenReturn("default");
    }


    @Test
    public void testSHELLTask() throws Exception {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setTaskType("SHELL");
        taskInstance.setProcessDefinitionId(1);
        taskInstance.setProcessInstanceId(1);
        taskInstance.setState(ExecutionStatus.KILL);
        taskInstance.setTaskJson("{\"conditionResult\":\"{\\\"successNode\\\":[\\\"\\\"],\\\"failedNode\\\":[\\\"\\\"]}\",\"conditionsTask\":false,\"depList\":[],\"dependence\":\"{}\",\"forbidden\":false,\"id\":\"tasks-55201\",\"maxRetryTimes\":0,\"name\":\"测试任务\",\"params\":\"{\\\"rawScript\\\":\\\"echo \\\\\\\"测试任务\\\\\\\"\\\",\\\"localParams\\\":[],\\\"resourceList\\\":[]}\",\"preTasks\":\"[]\",\"retryInterval\":1,\"runFlag\":\"NORMAL\",\"taskInstancePriority\":\"MEDIUM\",\"taskTimeoutParameter\":{\"enable\":false,\"interval\":0},\"timeout\":\"{\\\"enable\\\":false,\\\"strategy\\\":\\\"\\\"}\",\"type\":\"SHELL\",\"workerGroup\":\"default\"}");
        taskInstance.setProcessInstancePriority(Priority.MEDIUM);
        taskInstance.setWorkerGroup("default");
        taskInstance.setExecutorId(2);

        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setTenantId(1);
        processInstance.setCommandType(CommandType.START_PROCESS);
        taskInstance.setProcessInstance(processInstance);

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setUserId(2);
        processDefinition.setProjectId(1);
        taskInstance.setProcessDefine(processDefinition);

        Mockito.when(processService.getTaskInstanceDetailByTaskId(1)).thenReturn(taskInstance);
        taskPriorityQueue.put("2_1_2_1_default");

        Thread.sleep(10000);
    }


    @Test
    public void testSQLTask() throws Exception {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setTaskType("SQL");
        taskInstance.setProcessDefinitionId(1);
        taskInstance.setProcessInstanceId(1);
        taskInstance.setState(ExecutionStatus.KILL);
        taskInstance.setTaskJson("{\"conditionsTask\":false,\"depList\":[],\"dependence\":\"{}\",\"forbidden\":false,\"id\":\"tasks-3655\",\"maxRetryTimes\":0,\"name\":\"UDF测试\",\"params\":\"{\\\"postStatements\\\":[],\\\"connParams\\\":\\\"\\\",\\\"receiversCc\\\":\\\"\\\",\\\"udfs\\\":\\\"1\\\",\\\"type\\\":\\\"HIVE\\\",\\\"title\\\":\\\"test\\\",\\\"sql\\\":\\\"select id,name,ds,zodia(ds) from t_journey_user\\\",\\\"preStatements\\\":[],\\\"sqlType\\\":0,\\\"receivers\\\":\\\"825193156@qq.com\\\",\\\"datasource\\\":3,\\\"showType\\\":\\\"TABLE\\\",\\\"localParams\\\":[]}\",\"preTasks\":\"[]\",\"retryInterval\":1,\"runFlag\":\"NORMAL\",\"taskInstancePriority\":\"MEDIUM\",\"taskTimeoutParameter\":{\"enable\":false,\"interval\":0},\"timeout\":\"{\\\"enable\\\":false,\\\"strategy\\\":\\\"\\\"}\",\"type\":\"SQL\"}");
        taskInstance.setProcessInstancePriority(Priority.MEDIUM);
        taskInstance.setWorkerGroup("default");
        taskInstance.setExecutorId(2);

        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setTenantId(1);
        processInstance.setCommandType(CommandType.START_PROCESS);
        taskInstance.setProcessInstance(processInstance);

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setUserId(2);
        processDefinition.setProjectId(1);
        taskInstance.setProcessDefine(processDefinition);

        Mockito.when(processService.getTaskInstanceDetailByTaskId(1)).thenReturn(taskInstance);
        taskPriorityQueue.put("2_1_2_1_default");

        DataSource dataSource = new DataSource();
        dataSource.setId(1);
        dataSource.setName("sqlDatasource");
        dataSource.setType(DbType.MYSQL);
        dataSource.setUserId(2);
        dataSource.setConnectionParams("{\"address\":\"jdbc:mysql://192.168.221.185:3306\",\"database\":\"dolphinscheduler_qiaozhanwei\",\"jdbcUrl\":\"jdbc:mysql://192.168.221.185:3306/dolphinscheduler_qiaozhanwei\",\"user\":\"root\",\"password\":\"root@123\"}");
        dataSource.setCreateTime(new Date());
        dataSource.setUpdateTime(new Date());

        Mockito.when(processService.findDataSourceById(1)).thenReturn(dataSource);

        Thread.sleep(10000);
    }


    @Test
    public void testDataxTask() throws Exception {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setTaskType("DATAX");
        taskInstance.setProcessDefinitionId(1);
        taskInstance.setProcessInstanceId(1);
        taskInstance.setState(ExecutionStatus.KILL);
        taskInstance.setTaskJson("{\"conditionResult\":\"{\\\"successNode\\\":[\\\"\\\"],\\\"failedNode\\\":[\\\"\\\"]}\",\"conditionsTask\":false,\"depList\":[],\"dependence\":\"{}\",\"forbidden\":false,\"id\":\"tasks-97625\",\"maxRetryTimes\":0,\"name\":\"MySQL数据相互导入\",\"params\":\"{\\\"targetTable\\\":\\\"pv2\\\",\\\"postStatements\\\":[],\\\"jobSpeedRecord\\\":1000,\\\"customConfig\\\":0,\\\"dtType\\\":\\\"MYSQL\\\",\\\"dsType\\\":\\\"MYSQL\\\",\\\"jobSpeedByte\\\":0,\\\"dataSource\\\":80,\\\"dataTarget\\\":80,\\\"sql\\\":\\\"SELECT dt,count FROM pv\\\",\\\"preStatements\\\":[]}\",\"preTasks\":\"[]\",\"retryInterval\":1,\"runFlag\":\"NORMAL\",\"taskInstancePriority\":\"MEDIUM\",\"taskTimeoutParameter\":{\"enable\":false,\"interval\":0},\"timeout\":\"{\\\"enable\\\":false,\\\"strategy\\\":\\\"\\\"}\",\"type\":\"DATAX\",\"workerGroup\":\"default\"}");
        taskInstance.setProcessInstancePriority(Priority.MEDIUM);
        taskInstance.setWorkerGroup("default");
        taskInstance.setExecutorId(2);

        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setTenantId(1);
        processInstance.setCommandType(CommandType.START_PROCESS);
        taskInstance.setProcessInstance(processInstance);

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setUserId(2);
        processDefinition.setProjectId(1);
        taskInstance.setProcessDefine(processDefinition);

        Mockito.when(processService.getTaskInstanceDetailByTaskId(1)).thenReturn(taskInstance);
        taskPriorityQueue.put("2_1_2_1_default");



        DataSource dataSource = new DataSource();
        dataSource.setId(80);
        dataSource.setName("datax");
        dataSource.setType(DbType.MYSQL);
        dataSource.setUserId(2);
        dataSource.setConnectionParams("{\"address\":\"jdbc:mysql://192.168.221.185:3306\",\"database\":\"dolphinscheduler_qiaozhanwei\",\"jdbcUrl\":\"jdbc:mysql://192.168.221.185:3306/dolphinscheduler_qiaozhanwei\",\"user\":\"root\",\"password\":\"root@123\"}");
        dataSource.setCreateTime(new Date());
        dataSource.setUpdateTime(new Date());

        Mockito.when(processService.findDataSourceById(80)).thenReturn(dataSource);

        Thread.sleep(10000);
    }


    @Test
    public void testSqoopTask() throws Exception {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setTaskType("SQOOP");
        taskInstance.setProcessDefinitionId(1);
        taskInstance.setProcessInstanceId(1);
        taskInstance.setState(ExecutionStatus.KILL);
        taskInstance.setTaskJson("{\"conditionResult\":\"{\\\"successNode\\\":[\\\"\\\"],\\\"failedNode\\\":[\\\"\\\"]}\",\"conditionsTask\":false,\"depList\":[],\"dependence\":\"{}\",\"forbidden\":false,\"id\":\"tasks-63634\",\"maxRetryTimes\":0,\"name\":\"MySQL数据导入HDSF\",\"params\":\"{\\\"sourceType\\\":\\\"MYSQL\\\",\\\"targetType\\\":\\\"HDFS\\\",\\\"targetParams\\\":\\\"{\\\\\\\"targetPath\\\\\\\":\\\\\\\"/test/datatest\\\\\\\",\\\\\\\"deleteTargetDir\\\\\\\":true,\\\\\\\"fileType\\\\\\\":\\\\\\\"--as-textfile\\\\\\\",\\\\\\\"compressionCodec\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"fieldsTerminated\\\\\\\":\\\\\\\",\\\\\\\",\\\\\\\"linesTerminated\\\\\\\":\\\\\\\"\\\\\\\\\\\\\\\\n\\\\\\\"}\\\",\\\"modelType\\\":\\\"import\\\",\\\"sourceParams\\\":\\\"{\\\\\\\"srcType\\\\\\\":\\\\\\\"MYSQL\\\\\\\",\\\\\\\"srcDatasource\\\\\\\":1,\\\\\\\"srcTable\\\\\\\":\\\\\\\"t_ds_user\\\\\\\",\\\\\\\"srcQueryType\\\\\\\":\\\\\\\"0\\\\\\\",\\\\\\\"srcQuerySql\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"srcColumnType\\\\\\\":\\\\\\\"0\\\\\\\",\\\\\\\"srcColumns\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"srcConditionList\\\\\\\":[],\\\\\\\"mapColumnHive\\\\\\\":[],\\\\\\\"mapColumnJava\\\\\\\":[]}\\\",\\\"localParams\\\":[],\\\"concurrency\\\":1}\",\"preTasks\":\"[]\",\"retryInterval\":1,\"runFlag\":\"NORMAL\",\"taskInstancePriority\":\"MEDIUM\",\"taskTimeoutParameter\":{\"enable\":false,\"interval\":0},\"timeout\":\"{\\\"enable\\\":false,\\\"strategy\\\":\\\"\\\"}\",\"type\":\"SQOOP\",\"workerGroup\":\"default\"}");
        taskInstance.setProcessInstancePriority(Priority.MEDIUM);
        taskInstance.setWorkerGroup("default");
        taskInstance.setExecutorId(2);

        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setTenantId(1);
        processInstance.setCommandType(CommandType.START_PROCESS);
        taskInstance.setProcessInstance(processInstance);

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setUserId(2);
        processDefinition.setProjectId(1);
        taskInstance.setProcessDefine(processDefinition);

        Mockito.when(processService.getTaskInstanceDetailByTaskId(1)).thenReturn(taskInstance);
        taskPriorityQueue.put("2_1_2_1_default");



        DataSource dataSource = new DataSource();
        dataSource.setId(1);
        dataSource.setName("datax");
        dataSource.setType(DbType.MYSQL);
        dataSource.setUserId(2);
        dataSource.setConnectionParams("{\"address\":\"jdbc:mysql://192.168.221.185:3306\",\"database\":\"dolphinscheduler_qiaozhanwei\",\"jdbcUrl\":\"jdbc:mysql://192.168.221.185:3306/dolphinscheduler_qiaozhanwei\",\"user\":\"root\",\"password\":\"root@123\"}");
        dataSource.setCreateTime(new Date());
        dataSource.setUpdateTime(new Date());

        Mockito.when(processService.findDataSourceById(1)).thenReturn(dataSource);
        Thread.sleep(10000);
    }


    @Test
    public void testTaskInstanceIsFinalState(){
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setTaskType("SHELL");
        taskInstance.setProcessDefinitionId(1);
        taskInstance.setProcessInstanceId(1);
        taskInstance.setState(ExecutionStatus.KILL);
        taskInstance.setTaskJson("{\"conditionResult\":\"{\\\"successNode\\\":[\\\"\\\"],\\\"failedNode\\\":[\\\"\\\"]}\",\"conditionsTask\":false,\"depList\":[],\"dependence\":\"{}\",\"forbidden\":false,\"id\":\"tasks-55201\",\"maxRetryTimes\":0,\"name\":\"测试任务\",\"params\":\"{\\\"rawScript\\\":\\\"echo \\\\\\\"测试任务\\\\\\\"\\\",\\\"localParams\\\":[],\\\"resourceList\\\":[]}\",\"preTasks\":\"[]\",\"retryInterval\":1,\"runFlag\":\"NORMAL\",\"taskInstancePriority\":\"MEDIUM\",\"taskTimeoutParameter\":{\"enable\":false,\"interval\":0},\"timeout\":\"{\\\"enable\\\":false,\\\"strategy\\\":\\\"\\\"}\",\"type\":\"SHELL\",\"workerGroup\":\"default\"}");
        taskInstance.setProcessInstancePriority(Priority.MEDIUM);
        taskInstance.setWorkerGroup("default");
        taskInstance.setExecutorId(2);


        Mockito.when( processService.findTaskInstanceById(1)).thenReturn(taskInstance);

        taskPriorityQueueConsumer.taskInstanceIsFinalState(1);
    }


}
