package org.apache.dolphinscheduler.dao.entity;

import java.util.Date;

/**
 * warn_result
 */
public class WarnResult {

    /**
     * warn_result id
     */
    private String id;

    /**
     * warn_result taskId
     */
    private String taskId;

    /**
     * warn_result taskName
     */
    private String taskName;

    /**
     * warn_result taskDate
     */
    private Date taskDate;

    /**
     * warn_result createDAte
     */
    private Date createDAte;

    /**
     * warn_result taskType
     */
    private String taskType;

    /**
     * warn_result message
     */
    private String message;

    /**
     * warn_result isSend
     */
    private String isSend;

    /**
     * warn_result telephone
     */
    private String telephone;

    /**
     * warn_result warnUsers
     */
    private String warnUsers;

    /**
     * warn_result execId
     */
    private Integer execId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Date getTaskDate() {
        return taskDate;
    }

    public void setTaskDate(Date taskDate) {
        this.taskDate = taskDate;
    }

    public Date getCreateDAte() {
        return createDAte;
    }

    public void setCreateDAte(Date createDAte) {
        this.createDAte = createDAte;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getIsSend() {
        return isSend;
    }

    public void setIsSend(String isSend) {
        this.isSend = isSend;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getWarnUsers() {
        return warnUsers;
    }

    public void setWarnUsers(String warnUsers) {
        this.warnUsers = warnUsers;
    }

    public Integer getExecId() {
        return execId;
    }

    public void setExecId(Integer execId) {
        this.execId = execId;
    }


    @Override
    public String toString() {
        return "WarnResult{" +
                "id='" + id + '\'' +
                ", taskId='" + taskId + '\'' +
                ", taskName='" + taskName + '\'' +
                ", taskDate=" + taskDate +
                ", createDAte=" + createDAte +
                ", taskType='" + taskType + '\'' +
                ", message='" + message + '\'' +
                ", isSend='" + isSend + '\'' +
                ", telephone='" + telephone + '\'' +
                ", warnUsers='" + warnUsers + '\'' +
                ", execId=" + execId +
                '}';
    }
}
