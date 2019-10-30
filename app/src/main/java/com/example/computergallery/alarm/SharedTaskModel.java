package com.example.computergallery.alarm;

/**
 * Created by Makdia Hussain on 1/28/2018.
 */

public class SharedTaskModel {
    String alarm_id;
    String alarm_title;
    String alarm_time;
    String alarm_state;
    String alarm_manager_id;
    String user_id;
    String task_type;
    String task_owner_id;
    String task_seen;
    String task_image;


    public SharedTaskModel(){
        //this constructor is required
    }


    public SharedTaskModel(String alarm_id, String alarm_title, String alarm_time, String alarm_state, String alarm_manager_id, String user_id, String task_type, String task_owner_id, String task_seen, String task_image) {
        this.alarm_id = alarm_id;
        this.alarm_title = alarm_title;
        this.alarm_time = alarm_time;
        this.alarm_state = alarm_state;
        this.alarm_manager_id = alarm_manager_id;
        this.user_id = user_id;
        this.task_type = task_type;
        this.task_owner_id = task_owner_id;
        this.task_seen = task_seen;
        this.task_image = task_image;

    }


    public String getId() {
        return alarm_id;
    }
    public void setId(String alarm_id) {
        this.alarm_id = alarm_id;
    }

    public String getTitle() {
        return alarm_title;
    }
    public void setTitle(String alarm_title) {
        this.alarm_title = alarm_title;
    }

    public String getTime() {
        return alarm_time;
    }
    public void setTime(String alarm_time) {
        this.alarm_time = alarm_time;
    }


    public String getState() {
        return alarm_state;
    }
    public void setState(String alarm_state) {
        this.alarm_state = alarm_state;
    }

    public String getAlarmManagerId() {
        return alarm_manager_id;
    }
    public void setAlarmManagerId(String alarm_manager_id) {
        this.alarm_manager_id = alarm_manager_id;
    }
    public String getUserId() {
        return user_id;
    }
    public void setUserId(String user_id) {
        this.user_id = user_id;
    }
    public String getTaskType() {
        return task_type;
    }
    public void setTaskType(String task_type) {
        this.task_type = task_type;
    }

    public String getTaskOwnerId() {
        return task_owner_id;
    }
    public void setTaskOwnerId(String task_owner_id) {
        this.task_owner_id = task_owner_id;
    }

    public String getTaskSeen() {
        return task_seen;
    }
    public void setTaskSeen(String task_seen) {
        this.task_seen = task_seen;
    }

    public String getTaskImage() {
        return task_image;
    }
    public void setTaskImage(String task_image) {
        this.task_image = task_image;
    }

}
