package com.example.onedee;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.time.LocalDateTime;
import java.util.ArrayList;

/** MeetingAdapter for our ListView to accept our MeetingClass **/
public class MeetingAdapter extends BaseAdapter {
    ArrayList<EventClass> meetingList;
    Context meetingContext;
    public MeetingAdapter(Context context, ArrayList<EventClass> meetingList) {
        //meetingList contains all the meetings that this user has
        this.meetingList = meetingList;
        this.meetingContext = context;
    }
    @Override
    public int getCount() {
        //getCount returns a int that represents the number of items that will be populated in the
        //list view. Since we want to show all the meetings, we return meetingList.size
        return meetingList.size();
    }

    @Override
    public EventClass getItem(int position) {
        //To get the specific meeting from the meetingList. Used in the event of clicks
        return meetingList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater meetingInflater = (LayoutInflater) meetingContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            view = meetingInflater.inflate(R.layout.meeting_adapter_layout, null);
        }

        TextView meetingTitle = view.findViewById(R.id.meeting_title);
        TextView meetingDate = view.findViewById(R.id.meeting_date);
        TextView meetingMembers = view.findViewById(R.id.meeting_members);
        /** Below is the code to represent the MeetingClass details in the listview using a format
         * that we want
         */
        String members = "Members: ";
        MeetingClass meeting = (MeetingClass)meetingList.get(i);
        ArrayList<String> membersList = meeting.getGroupMembers();
        Log.i("memberList", "memberList in adapter is " + membersList);
        int counter = 0;
        for(String member: membersList) {
            if (counter < membersList.size()) {
                members += member + ", ";
                counter++;
            }
            else {
                members += member;
                counter = 0;
            }

        }

        meetingTitle.setText(meetingList.get(i).name);
        if (meetingList.get(i).startTime == null || meetingList.get(i).startTime.equals(LocalDateTime.of(0,1,1,0,0))) {
            meetingDate.setText("to be optimised");
        }
        else {
            meetingDate.setText(meetingList.get(i).startTime.toString());
        }
        meetingMembers.setText(members);

        return view;
    }
}
