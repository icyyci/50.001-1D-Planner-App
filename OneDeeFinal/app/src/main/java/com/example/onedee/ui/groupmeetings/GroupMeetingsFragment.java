package com.example.onedee.ui.groupmeetings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.onedee.AddMeetingScreen;
import com.example.onedee.CalendarClass;
import com.example.onedee.EventClass;
import com.example.onedee.MeetingAdapter;
import com.example.onedee.MeetingClass;
import com.example.onedee.R;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.time.temporal.ChronoUnit.MINUTES;

public class GroupMeetingsFragment extends Fragment {

    private GroupMeetingsViewModel GroupMeetingsViewModel;
    Button addMeeting;
    static ListView groupMeetingListView;
    static MeetingAdapter meetingAdapter;
    static View root;
    static ArrayList<EventClass> memberFixedEvents = AddMeetingScreen.memberFixedEvents;
    //static ArrayList<EventClass> toBeOptimizedEvents = AddMeetingScreen.toBeOptimizedEvents;
    static ArrayList<String> memberList = AddMeetingScreen.memberList;
    EventClass meetingSelected;
    Button cancelButton;
    Button deleteButton;
    Button optimiseButton;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GroupMeetingsViewModel =
                ViewModelProviders.of(this).get(GroupMeetingsViewModel.class);
        root = inflater.inflate(R.layout.fragment_group_meetings, container, false);
        addMeeting = root.findViewById(R.id.add_group_meetings);
        groupMeetingListView = root.findViewById(R.id.group_meeting_list);

        /**If the user click on add meeting, we bring the user to the add meeting screen**/
        addMeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), AddMeetingScreen.class);
                startActivity(intent);
            }
        });

        //creating the adapter that we will set the listview with
        meetingAdapter = new MeetingAdapter(root.getContext(), CalendarClass.getGroupMeetingList());
        for(EventClass i: CalendarClass.getGroupMeetingList()){
            Log.i("group_optimize","event: " + i.ShowAllStats());
        }
        groupMeetingListView.setAdapter(meetingAdapter);

        //Initialising a Popup window that will be displayed when the user clicks on an item in
        //the listview
        LayoutInflater popupInflater = (LayoutInflater) root.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View popupView = popupInflater.inflate(R.layout.activity_remove_group_meetings,null);
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = false;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        //When the user click an item on the list view
        groupMeetingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //show the popup window
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
                //get the meeting that the user selected
                meetingSelected = meetingAdapter.getItem(position);
                cancelButton = popupView.findViewById(R.id.group_cancel_button);
                deleteButton = popupView.findViewById(R.id.group_delete_button);
                optimiseButton = popupView.findViewById(R.id.meetingbtnoptimise);
                //if user clicks on cancel, dismiss the popup
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });
                //if user clicks on delete
                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //if the meeting has not been optimised, we can just delete it from the
                        //user's calendar class
                        if (meetingSelected.startTime == null) {
                            CalendarClass.removeGroupMeeting((MeetingClass) meetingSelected);
                        }
                        //else if it has been optimised, means that it has been uploaded to firebase
                        //and all the members of that meeting have an instance of that meeting in their database
                        else {
                            MeetingClass meetingSelectedDownCast = (MeetingClass) meetingSelected;
                            //call a method in CalendarClass to delete that meeting for all members and the user
                            CalendarClass.deleteMeetingstoMembers(meetingSelectedDownCast, meetingSelectedDownCast.getGroupMembers());
                        }
                        //call method to refresh the listview to display the changes
                        setListView();
                        popupWindow.dismiss();
                    }
                });
                //if user clicks on the optimise button
                optimiseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /** algo to scan through all the member's schedule and find a common free time **/
                        List<EventClass> inputFixedEvents = new ArrayList<EventClass>();
                        ArrayList<EventClass> toBeOptimizedEvents = new ArrayList<>();
                        toBeOptimizedEvents.add(meetingSelected);
                        LocalDateTime Start_time;
                        if (LocalDateTime.now().getHour() < 8) {
                            Start_time = LocalDateTime.of(LocalDateTime.now().getYear(), LocalDateTime.now().getMonth(), LocalDateTime.now().getDayOfMonth(),
                                    8, 0);
                        } else {
                            Start_time = LocalDateTime.now(); //get current date-time
                        }
                        Log.i("meeting_optimize", memberFixedEvents.toString());
                        CalendarClass.sortEvents(memberFixedEvents);
                        for (EventClass i : memberFixedEvents) {
                            if (Start_time.until(i.getEndTime(), MINUTES) > 0) {
                                int start_index = memberFixedEvents.indexOf(i);
                                inputFixedEvents = memberFixedEvents.subList(start_index, memberFixedEvents.size());
                                break;
                            }
                        }
                        Log.i("meeting_optimize", "input fixed events: " + inputFixedEvents.toString());
                        if (!memberFixedEvents.isEmpty()) {
                            Log.i("meeting_optimize", "details of first event: " + memberFixedEvents.get(0).toString());
                        }
                        Log.i("meeting_optimize", toBeOptimizedEvents.toString());
                        String message = CalendarClass.optimiseGroupMeeting(inputFixedEvents, toBeOptimizedEvents, Start_time, memberList);
                        AddMeetingScreen.toBeOptimizedEvents.clear();
                        AddMeetingScreen.memberFixedEvents.clear();
                        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                        setListView();
                        popupWindow.dismiss();
                    }
                });

            }
        });


        return root;
    }

    //Method to set the list view again. We use this to refresh the list to reflect changes
    public static void setListView() {
        Log.i("group_optimize","in set list view: " + CalendarClass.getGroupMeetingList().toString());
        meetingAdapter = new MeetingAdapter(root.getContext(), CalendarClass.getGroupMeetingList());
        groupMeetingListView.setAdapter(meetingAdapter);
    }

}