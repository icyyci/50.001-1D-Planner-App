package com.example.onedee;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ListOfUsers {
    public static ArrayList<User> listOfUsers = new ArrayList<User>();


    public ListOfUsers(){

    }

    public static ArrayList<String> listOfUsernames(){
        ArrayList<String> list = new ArrayList<String>();
        for(User user: listOfUsers){
            list.add(user.toString());
        }
        return list;
    }

    public static User findUser(String s){
        ArrayList<String> list = listOfUsernames();
        int i = list.indexOf(s);
        if(i!=-1){
            return listOfUsers.get(i);
        }
        else{
            return null;
        }
    }


}
