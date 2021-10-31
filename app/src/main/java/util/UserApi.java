package util;

import android.app.Application;

public class UserApi extends Application{
    private String username;
    private String userId;
    private String userRole;
    private String userEmail;
    private static UserApi instance;

    public static UserApi getInstance(){
        if(instance == null)
            instance = new UserApi();
        return instance;

    }

    public UserApi(){}

    public String getUsername(){
        return username;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public String getUserId(){
        return userId;
    }

    public void setUserId(String userId){
        this.userId = userId;
    }

    public String getUserRole(){
        return userRole;
    }

    public void setUserRole(String userRole){
        this.userRole = userRole;
    }

    public String getUserEmail(){
        return userEmail;
    }

    public void setUserEmail(String userEmail){
        this.userEmail = userEmail;
    }
}
