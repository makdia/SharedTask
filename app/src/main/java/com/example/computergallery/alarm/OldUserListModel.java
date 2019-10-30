package com.example.computergallery.alarm;

/**
 * Created by Makdia Hussain on 2/16/2018.
 */

public class OldUserListModel {
    String user_id;
    String user_name;
    String key;
    String password;
    String device_imei;
    String user_profile_pic;

    public OldUserListModel(){
        //this constructor is required
    }


    public OldUserListModel(String user_id, String user_name, String key, String password, String device_imei, String user_profile_pic) {
        this.user_id = user_id;
        this.user_name = user_name;
        this.key = key;
        this.password = password;
        this.device_imei = device_imei;
        this.user_profile_pic = user_profile_pic;
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

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getDeviceImei() {
        return device_imei;
    }
    public void setDeviceImei(String device_imei) {
        this.device_imei = device_imei;
    }


    public String getUserProfilePic() {
        return user_profile_pic;
    }
    public void setUserProfilePic(String user_profile_pic) {
        this.user_profile_pic = user_profile_pic;
    }

}
