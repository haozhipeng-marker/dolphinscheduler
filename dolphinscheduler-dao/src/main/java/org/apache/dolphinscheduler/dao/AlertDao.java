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
package org.apache.dolphinscheduler.dao;


import com.alibaba.fastjson.JSON;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.AlertStatus;
import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.common.enums.ShowType;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.datasource.ConnectionFactory;
import org.apache.dolphinscheduler.dao.entity.*;
import org.apache.dolphinscheduler.dao.mapper.AlertGroupMapper;
import org.apache.dolphinscheduler.dao.mapper.AlertMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.UserAlertGroupMapper;
import org.apache.dolphinscheduler.dao.utils.HttpClientUtils;
import org.apache.dolphinscheduler.dao.utils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class AlertDao extends AbstractBaseDao {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AlertMapper alertMapper;
    @Autowired
    private UserAlertGroupMapper userAlertGroupMapper;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private AlertGroupMapper alertGroupMapper;

    @Override
    protected void init() {
        alertMapper = ConnectionFactory.getInstance().getMapper(AlertMapper.class);
        userAlertGroupMapper = ConnectionFactory.getInstance().getMapper(UserAlertGroupMapper.class);
        projectMapper = ConnectionFactory.getInstance().getMapper(ProjectMapper.class);
        alertGroupMapper = ConnectionFactory.getInstance().getMapper(AlertGroupMapper.class);
    }

    /**
     * insert alert
     *
     * @param alert alert
     * @return add alert result
     */
    public int addAlert(Alert alert){
        alert.setId(alertMapper.getMaxId());
        return alertMapper.insert(alert);
    }

    /**
     * update alert
     *
     * @param alertStatus alertStatus
     * @param log log
     * @param id id
     * @return update alert result
     */
    public int updateAlert(AlertStatus alertStatus,String log,int id){
        Alert alert = alertMapper.selectById(id);
        alert.setAlertStatus(alertStatus);
        alert.setUpdateTime(new Date());
        alert.setLog(log);
        return alertMapper.updateById(alert);
    }

    /**
     * 根据告警组id获取用户集合
     *
     * @param alerGroupId alerGroupId
     * @return user list
     */
    public List<User> queryUserByAlertGroupId(int alerGroupId){
        return userAlertGroupMapper.listUserByAlertgroupId(alerGroupId);
    }

    /**
     * MasterServer or WorkerServer停止
     *
     * @param alertGroupId alertgroupId
     * @param host host
     * @param serverType serverType
     */
    public void sendServerStopedAlert(int alertGroupId,String host,String serverType){
        Alert alert = new Alert();
        String content = String.format("[{'type':'%s','host':'%s','event':'server down','warning level':'serious'}]", serverType, host);
        alert.setTitle("Fault tolerance warning");
        saveTaskTimeoutAlert(alert, content, alertGroupId, null, null);
    }

    /**
     * 发送流程超时邮件告警
     *
     * @param processInstance processInstance
     * @param processDefinition processDefinition
     */
    public void sendProcessTimeoutAlert(ProcessInstance processInstance, ProcessDefinition processDefinition){
        int alertGroupId = processInstance.getWarningGroupId();
        String receivers = processDefinition.getReceivers();
        String receiversCc = processDefinition.getReceiversCc();
        Alert alert = new Alert();
        String content = String.format("[{'id':'%d','name':'%s','event':'timeout','warnLevel':'middle'}]",
                processInstance.getId(), processInstance.getName());
        alert.setTitle("Process Timeout Warn");
        saveTaskTimeoutAlert(alert, content, alertGroupId, receivers, receiversCc);
    }

    /**
     * 发送任务超时邮件告警
     *
     * @param alertGroupId alertGroupId
     * @param receivers receivers
     * @param receiversCc receiversCc
     * @param processInstanceId processInstanceId
     * @param processInstanceName processInstanceName
     * @param taskId taskId
     * @param taskName taskName
     */
    public void sendTaskTimeoutAlert(int alertGroupId, String receivers, String receiversCc, int processInstanceId,
                                     String processInstanceName, int taskId, String taskName){
        Alert alert = new Alert();
        String content = String.format("[{'process instance id':'%d','task name':'%s','task id':'%d','task name':'%s'," + "'event':'timeout','warnLevel':'middle'}]", processInstanceId, processInstanceName, taskId, taskName);
        alert.setTitle("Task Timeout Warn");
        saveTaskTimeoutAlert(alert, content, alertGroupId, receivers, receiversCc);
    }

    private void  saveTaskTimeoutAlert(Alert alert, String content, int alertGroupId,
                                       String receivers,  String receiversCc){
        alert.setShowType(ShowType.TABLE);
        alert.setContent(content);
        alert.setAlertType(AlertType.EMAIL);
        alert.setAlertGroupId(alertGroupId);
        if (StringUtils.isNotEmpty(receivers)) {
            alert.setReceivers(receivers);
        }
        if (StringUtils.isNotEmpty(receiversCc)) {
            alert.setReceiversCc(receiversCc);
        }
        alert.setCreateTime(new Date());
        alert.setUpdateTime(new Date());
        alert.setId(alertMapper.getMaxId());
        alertMapper.insert(alert);
    }

    /**
     * list the alert information of waiting to be executed
     * @return alert list
     */
    public List<Alert> listWaitExecutionAlert(){
        return alertMapper.listAlertByStatus(AlertStatus.WAIT_EXECUTION);
    }

    /**
     * list user information by alert group id
     * @param alertGroupId alertGroupId
     * @return user list
     */
    public List<User> listUserByAlertgroupId(int alertGroupId){
        return userAlertGroupMapper.listUserByAlertgroupId(alertGroupId);
    }

    /**
     * for test
     * @return AlertMapper
     */
    public AlertMapper getAlertMapper() {
        return alertMapper;
    }

    /**
     * 根据项目id获取项目信息
     *
     * @param projectId projectId
     * @return project
     */
    public Project queryProjectById(int projectId){
        return projectMapper.queryDetailById(projectId);
    }

    /**
     * 发送服务节点宕机短信告警
     *
     * @param alertGroupId alertGroupId
     * @param host host
     * @param serverType serverType
     */
    public void sendSmsServerStopedAlert(int alertGroupId, String host, String serverType){
        //获取告警组名称
        AlertGroup alertGroup = alertGroupMapper.selectById(alertGroupId);
        //获取告警组下用户信息
        List<User> userList = listUserByAlertgroupId(alertGroupId);
        //组装短信结构
        WarnResult warnResult = new WarnResult();
        warnResult.setTaskType("DS调度任务");
        String message = "【服务节点告警】"
                       + "节点类型：" + serverType + "，节点路径：" + host
                       + "，告警事件：server down"
                       + "，告警组：" + alertGroup.getGroupName()
                       + "，告警级别：严重";
        warnResult.setMessage(message);
        String users = "";
        String telephones = "";
        if(null != userList && !userList.isEmpty()){
            for (User user: userList) {
                if(null != user.getPhone() && user.getPhone() != ""){
                    users += user.getUserName() + ",";
                    telephones += user.getPhone() + ",";
                }
            }
        }
        if(null != telephones && telephones != ""){
            warnResult.setWarnUsers(users.substring(0, users.length()-1));
            warnResult.setTelephone(telephones.substring(0, telephones.length()-1));
            logger.info("send sms alert server stoped message, message: {}", warnResult.toString());
            //发送短信告警到message服务
            sendSmsAlertToMessageServer(warnResult);
        }
    }

    /**
     * 发送流程超时短信告警
     *
     * @param processInstance processInstance
     * @param processDefinition processDefinition
     */
    public void sendSmsProcessTimeoutAlert(ProcessInstance processInstance, ProcessDefinition processDefinition){
        //组装短信结构
        Project project = queryProjectById(processDefinition.getProjectId());
        WarnResult warnResult = new WarnResult();
        warnResult.setTaskType("DS调度任务");
        warnResult.setMessage(
                getAlertMessage(project, processInstance, processDefinition, Constants.PROCESS_TIME_OUT, null));
        warnResult.setTaskDate(processInstance.getStartTime());
        warnResult.setTelephone(project.getPhone());
        warnResult.setWarnUsers(project.getUserName());
        logger.info("send sms alert process timeout message, message: {}", warnResult.toString());
        if(StringUtils.isNotEmpty(project.getPhone())){
            //发送短信告警到message服务
            sendSmsAlertToMessageServer(warnResult);
        }
    }

    /**
     * 发送任务超时短信告警
     *
     * @param processInstance processInstance
     * @param processInstance taskInstance
     */
    public void sendSmsTaskTimeoutAlert(ProcessInstance processInstance,
                                        ProcessDefinition processDefinition,
                                        TaskInstance taskInstance){
        //组装短信结构
        Project project = queryProjectById(processDefinition.getProjectId());
        WarnResult warnResult = new WarnResult();
        warnResult.setTaskType("DS调度任务");
        warnResult.setMessage(getAlertMessage(project, processInstance, processDefinition, Constants.TASK_TIME_OUT, taskInstance));
        warnResult.setTaskDate(processInstance.getStartTime());
        warnResult.setTelephone(project.getPhone());
        warnResult.setWarnUsers(project.getUserName());
        logger.info("send sms alert task timeout message, message : {}", warnResult.toString());
        if(StringUtils.isNotEmpty(project.getPhone())){
            //发送短信告警到message服务
            sendSmsAlertToMessageServer(warnResult);
        }
    }

    /**
     * 获取流程或任务超时短信告警信息内容
     * @param project project
     * @return processInstance processInstance
     */
    public String getAlertMessage(Project project, ProcessInstance processInstance,
                                  ProcessDefinition processDefinition, String type, TaskInstance taskInstance){
        String message = "";
        if(Constants.PROCESS_TIME_OUT.equals(type)){
            message = "【流程超时告警】"
                    + "项目名称：" + project.getName()
                    + "，工作流定义名称：" + processDefinition.getName()
                    + "，流程实例名称：" + processInstance.getName()
                    + "，告警级别：中级";
        }else if(Constants.TASK_TIME_OUT.equals(type)){
            message = "【任务超时告警】"
                    + "项目名称：" + project.getName()
                    + "，工作流定义名称：" + processDefinition.getName()
                    + "，流程实例名称：" + processInstance.getName()
                    + "，告警级别：中级"
                    + "，任务信息：【任务ID：" + taskInstance.getId()
                    + "，任务名称：" + taskInstance.getName() + "】";
        }
        return message;
    }

    /**
     * 发送短信告警到message服务
     *
     * @param warnResult warnResult
     */
    public void sendSmsAlertToMessageServer(WarnResult warnResult) {
        try {
            //发送告警
            String result = HttpClientUtils.doPost(PropertyUtils.getString(Constants.MESSAGE_SERVER_URL), JSON.toJSONString(warnResult));
            logger.info("send sms alert to message server, return info: {}", result);
        } catch (Exception e) {
            logger.error("send sms alert to message server error, error: {}", e);
        }
    }
}
