package com.example.computergallery.alarm;

/**
 * Created by Makdia Hussain on 2/6/2018.
 */

public class FriendListModel {
    String user_id;
    String user_name;
    String key;
    String status;
    String user_profile_pic;
    String user_timezone;

    public FriendListModel(){
        //this constructor is required
    }


    public FriendListModel(String user_id, String user_name, String key, String status, String user_profile_pic, String user_timezone) {
        this.user_id = user_id;
        this.user_name = user_name;
        this.key = key;
        this.status = status;
        this.user_profile_pic = user_profile_pic;
        this.user_timezone = user_timezone;
    }


    public String getUserId() {
        return user_id;
    }
    public void setUserId(String user_id) {
        this.user_id = user_id;
    }

    public String getUserName() {
        return user_name;
    }
    public void setUserName(String user_name) {
        this.user_name = user_name;
    }

    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }


    public String getUserProfilePic() {
        return user_profile_pic;
    }
    public void setUserProfilePic(String user_profile_pic) {
        this.user_profile_pic = user_profile_pic;
    }


    public String getUserTimezone() {
        return user_timezone;
    }
    public void setUserTimezone(String user_timezone) {
        this.user_timezone = user_timezone;
    }


}
