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
package org.apache.dolphinscheduler.server.utils;


import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.ShowType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.DaoFactory;
import org.apache.dolphinscheduler.dao.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * alert manager
 */
public class AlertManager {

    /**
     * logger of AlertManager
     */
    private static final Logger logger = LoggerFactory.getLogger(AlertManager.class);

    /**
     * alert dao
     */
    private AlertDao alertDao = DaoFactory.getDaoInstance(AlertDao.class);


    /**
     * command type convert chinese
     *
     * @param commandType command type
     * @return command name
     */
    private String getCommandCnName(CommandType commandType) {
        switch (commandType) {
            case RECOVER_TOLERANCE_FAULT_PROCESS:
                return "recover tolerance fault process";
            case RECOVER_SUSPENDED_PROCESS:
                return "recover suspended process";
            case START_CURRENT_TASK_PROCESS:
                return "start current task process";
            case START_FAILURE_TASK_PROCESS:
                return "start failure task process";
            case START_PROCESS:
                return "start process";
            case REPEAT_RUNNING:
                return "repeat running";
            case SCHEDULER:
                return "scheduler";
            case COMPLEMENT_DATA:
                return "complement data";
            case PAUSE:
                return "pause";
            case STOP:
                return "stop";
            default:
                return "unknown type";
        }
    }

    /**
     * 流程实例格式化
     */
    private static final String PROCESS_INSTANCE_FORMAT =
            "\"id:%d\"," +
            "\"name:%s\"," +
            "\"job type: %s\"," +
            "\"state: %s\"," +
            "\"recovery:%s\"," +
            "\"run time: %d\"," +
            "\"start time: %s\"," +
            "\"end time: %s\"," +
            "\"host: %s\"" ;

    /**
     * 获取流程实例邮件告警内容
     *
     * @param processInstance   process instance
     * @param taskInstances     task instance list
     * @return process instance format content
     */
    public String getContentProcessInstance(ProcessInstance processInstance, List<TaskInstance> taskInstances){
        String res = "";
        if(processInstance.getState().typeIsSuccess()){
            res = String.format(PROCESS_INSTANCE_FORMAT,
                    processInstance.getId(),
                    processInstance.getName(),
                    getCommandCnName(processInstance.getCommandType()),
                    processInstance.getState().toString(),
                    processInstance.getRecovery().toString(),
                    processInstance.getRunTimes(),
                    DateUtils.dateToString(processInstance.getStartTime()),
                    DateUtils.dateToString(processInstance.getEndTime()),
                    processInstance.getHost()
            );
            res = "[" + res + "]";
        }else if(processInstance.getState().typeIsFailure()){
            List<LinkedHashMap> failedTaskList = new ArrayList<>();
            for(TaskInstance task : taskInstances){
                if(task.getState().typeIsSuccess()){
                    continue;
                }
                LinkedHashMap<String, String> failedTaskMap = new LinkedHashMap();
                failedTaskMap.put("process instance id", String.valueOf(processInstance.getId()));
                failedTaskMap.put("process instance name", processInstance.getName());
                failedTaskMap.put("task id", String.valueOf(task.getId()));
                failedTaskMap.put("task name", task.getName());
                failedTaskMap.put("task type", task.getTaskType());
                failedTaskMap.put("task state", task.getState().toString());
                failedTaskMap.put("task start time", DateUtils.dateToString(task.getStartTime()));
                failedTaskMap.put("task end time", DateUtils.dateToString(task.getEndTime()));
                failedTaskMap.put("host", task.getHost());
                failedTaskMap.put("log path", task.getLogPath());
                failedTaskList.add(failedTaskMap);
            }
            res = JSONUtils.toJson(failedTaskList);
        }
        return res;
    }

    /**
     * 获取流程实例短信告警信息
     *
     * @param processInstance   process instance
     * @param taskInstances     task instance list
     * @return process instance format sms message
     */
    public String getMessageProcessInstance(ProcessInstance processInstance, List<TaskInstance> taskInstances, Project project){
        String processDefinitionName = processInstance.getProcessDefinition().getName();
        String processInstanceName = processInstance.getName();
        String cmdName = getCommandCnName(processInstance.getCommandType());
        String message = "【流程实例告警】"
                       + "项目名称：" + project.getName()
                       + "，工作流定义名称：" + processDefinitionName
                       + "，流程实例名称：" + processInstanceName
                       + "，命令类型：" + cmdName;
        if(processInstance.getState().typeIsFailure()){
            if(taskInstances.size() > 0 && !taskInstances.isEmpty()){
                StringBuffer taskMessage = new StringBuffer("，任务信息：");
                for(TaskInstance task : taskInstances){
                    if(task.getState().typeIsSuccess()){continue;}
                    taskMessage.append("【任务ID：" + task.getId()
                            + ",任务名称：" + task.getName()
                            + ",任务类型：" + task.getTaskType()
                            + ",任务状态：" + task.getState().toString()
                            + ",任务开始时间：" + DateUtils.dateToString(task.getStartTime())
                            + ",任务结束时间：" + DateUtils.dateToString(task.getEndTime()) + "】"
                    );
                }
                message += taskMessage;
            }
        }
        return message;
    }

    /**
     * 获取worker容错邮件告警内容
     *
     * @param processInstance   process instance
     * @param toleranceTaskList tolerance task list
     * @return worker tolerance content
     */
    private String getWorkerToleranceContent(ProcessInstance processInstance, List<TaskInstance> toleranceTaskList){
        List<LinkedHashMap<String, String>> toleranceTaskInstanceList =  new ArrayList<>();
        for(TaskInstance taskInstance: toleranceTaskList){
            LinkedHashMap<String, String> toleranceWorkerContentMap = new LinkedHashMap();
            toleranceWorkerContentMap.put("process name", processInstance.getName());
            toleranceWorkerContentMap.put("task name", taskInstance.getName());
            toleranceWorkerContentMap.put("host", taskInstance.getHost());
            toleranceWorkerContentMap.put("task retry times", String.valueOf(taskInstance.getRetryTimes()));
            toleranceTaskInstanceList.add(toleranceWorkerContentMap);
        }
        return JSONUtils.toJson(toleranceTaskInstanceList);
    }

    /**
     * 根据告警类型返回相应发送告警类型
     *
     * @param processInstance   process instance
     */
    public boolean getSendWarningStatus(ProcessInstance processInstance){
        boolean sendWarning = false;
        WarningType warningType = processInstance.getWarningType();
        switch (warningType){
            case ALL:
                if(processInstance.getState().typeIsFinished()){
                    sendWarning = true;
                }
                break;
            case SUCCESS:
                if(processInstance.getState().typeIsSuccess()){
                    sendWarning = true;
                }
                break;
            case FAILURE:
                if(processInstance.getState().typeIsFailure()){
                    sendWarning = true;
                }
                break;
            default:
        }
        return sendWarning;
    }

    /**
     * 发送worker容错邮件告警
     *
     * @param processInstance   process instance
     * @param toleranceTaskList tolerance task list
     */
    public void sendAlertWorkerToleranceFault(ProcessInstance processInstance, List<TaskInstance> toleranceTaskList){
        try{
            Alert alert = new Alert();
            alert.setTitle("worker fault tolerance");
            alert.setShowType(ShowType.TABLE);
            String content = getWorkerToleranceContent(processInstance, toleranceTaskList);
            alert.setContent(content);
            alert.setAlertType(AlertType.EMAIL);
            alert.setCreateTime(new Date());
            alert.setAlertGroupId(processInstance.getWarningGroupId() == null ? 1:processInstance.getWarningGroupId());
            alert.setReceivers(processInstance.getProcessDefinition().getReceivers());
            alert.setReceiversCc(processInstance.getProcessDefinition().getReceiversCc());
            alertDao.addAlert(alert);
            logger.info("add alert to db , alert : {}", alert.toString());
        }catch (Exception e){
            logger.error("send alert failed:{} ", e.getMessage());
        }

    }

    /**
     * 发送流程实例邮件告警
     *
     * @param processInstance   process instance
     * @param taskInstances     task instance list
     */
    public void sendAlertProcessInstance(ProcessInstance processInstance, List<TaskInstance> taskInstances){
        //如果告警类型为NONE，则不发送信息
        if(!getSendWarningStatus(processInstance)) return;
        Alert alert = new Alert();
        String cmdName = getCommandCnName(processInstance.getCommandType());
        String success = processInstance.getState().typeIsSuccess() ? "success" :"failed";
        alert.setTitle(cmdName + " " + success);
        ShowType showType = processInstance.getState().typeIsSuccess() ? ShowType.TEXT : ShowType.TABLE;
        alert.setShowType(showType);
        String content = getContentProcessInstance(processInstance, taskInstances);
        alert.setContent(content);
        alert.setAlertType(AlertType.EMAIL);
        alert.setAlertGroupId(processInstance.getWarningGroupId());
        alert.setCreateTime(new Date());
        alert.setReceivers(processInstance.getProcessDefinition().getReceivers());
        alert.setReceiversCc(processInstance.getProcessDefinition().getReceiversCc());
        alertDao.addAlert(alert);
        logger.info("add alert to db , alert: {}", alert.toString());
    }

    /**
     * 发送worker容错短信告警
     *
     * @param processInstance   process instance
     * @param toleranceTaskList tolerance task list
     */
    public void sendSmsAlertWorkerToleranceFault(ProcessInstance processInstance, List<TaskInstance> toleranceTaskList){
        try {
            sendSmsAlert(processInstance, toleranceTaskList);
        }catch (Exception e){
            logger.error("send sms alert WorkerToleranceFault, error: {}", e);
        }
    }

    /**
     * 发送流程实例运行短信告警
     *
     * @param processInstance   process instance
     * @param taskInstances     task instance list
     */
    public void sendSmsAlertProcessInstance(ProcessInstance processInstance, List<TaskInstance> taskInstances){
        //如果告警类型为NONE，则不发送信息
        if(!getSendWarningStatus(processInstance)) return;
        //如果流程实例状态为success，则不发送信息
        if("success".equals(processInstance.getState().typeIsSuccess() ? "success" :"failed")) return;
        try {
            sendSmsAlert(processInstance, taskInstances);
        } catch (Exception e) {
            logger.error("send sms alert processInstance, error: {}", e);
        }
    }

    /**
     * 封装发送流程或容错实例
     *
     * @param processInstance
     * @param taskList
     */
    public void sendSmsAlert(ProcessInstance processInstance, List<TaskInstance> taskList){
        //组装短信结构
        Integer projectId = processInstance.getProcessDefinition().getProjectId();
        Project project = alertDao.queryProjectById(projectId);
        WarnResult warnResult = new WarnResult();
        warnResult.setTaskType("DS调度任务");
        warnResult.setMessage(getMessageProcessInstance(processInstance, taskList, project));
        warnResult.setTaskDate(processInstance.getEndTime());
        warnResult.setTelephone(project.getPhone());
        warnResult.setWarnUsers(project.getUserName());
        logger.info("send sms alert to message server, message: {}", warnResult.toString());
        if(StringUtils.isNotEmpty(project.getPhone())){
            //发送短信告警到message服务
            alertDao.sendSmsAlertToMessageServer(warnResult);
        }else {
            logger.info("send sms alert to message server, error: phone is null");
        }
    }

    /**
     * 发送流程超时邮件告警
     *
     * @param processInstance   process instance
     * @param processDefinition process definition
     */
    public void sendProcessTimeoutAlert(ProcessInstance processInstance, ProcessDefinition processDefinition) {
        alertDao.sendProcessTimeoutAlert(processInstance, processDefinition);
    }

    /**
     * 发送流程超时短信告警
     *
     * @param processInstance   process instance
     * @param processDefinition process definition
     */
    public void sendSmsProcessTimeoutAlert(ProcessInstance processInstance, ProcessDefinition processDefinition) {
        alertDao.sendSmsProcessTimeoutAlert(processInstance, processDefinition);
    }
}
