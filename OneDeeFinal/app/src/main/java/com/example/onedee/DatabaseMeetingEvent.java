package com.example.onedee;

import java.time.LocalDateTime;
import java.util.ArrayList;

/** Data type for meetingEvent to  be passed on to Firebase*/
public class DatabaseMeetingEvent extends DatabaseEvent {
    public ArrayList<String> membersList;

    DatabaseMeetingEvent(){}

    DatabaseMeetingEvent(MeetingClass meeting){
        super(meeting);
        this.membersList = meeting.getGroupMembers();
    }

    public ArrayList<String> getMembersList() {
        return membersList;
    }
}
