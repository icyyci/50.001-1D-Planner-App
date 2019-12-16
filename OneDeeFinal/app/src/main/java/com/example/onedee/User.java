package com.example.onedee;

import java.util.ArrayList;

/** User Class used to hold the user account details**/

public class User {
    String username;
    String password;


    public User(String username, String password){
        this.username = username;
        this.password = password;
        ListOfUsers.listOfUsers.add(this);
    }

    public String toString(){
        return username;
    }

    public void editPassword(String newPW){
        this.password = newPW;
    }

    public boolean checkUser(String tryUsername, String tryPassword){
        if(tryUsername==this.username && tryPassword==this.password){
            return true;
        }
        else{
            return false;
        }
    }
}

