package com.example.onedee;

import java.time.LocalDateTime;
import java.util.ArrayList;

/** Meeting class used to store information about the meetings. Is a subclass of EventClass **/

public class MeetingClass extends EventClass{
    private ArrayList<String> groupMembers;

    MeetingClass(String name, boolean flexible, LocalDateTime startTime, LocalDateTime endTime, String difficulty, LocalDateTime dueDate, long duration, ArrayList<String> groupMembers) {
        super(name,flexible, startTime, endTime,difficulty,dueDate,duration);
        this.groupMembers = groupMembers;
    }

    public ArrayList<String> getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(ArrayList<String> groupMembers) {
        this.groupMembers = groupMembers;
    }
}
