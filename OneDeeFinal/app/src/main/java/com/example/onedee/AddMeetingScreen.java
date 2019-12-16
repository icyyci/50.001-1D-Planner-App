package com.example.onedee;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.onedee.ui.groupmeetings.GroupMeetingsFragment;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static java.time.temporal.ChronoUnit.MINUTES;

public class AddMeetingScreen extends AppCompatActivity {
    EditText member1;
    EditText member2;
    EditText member3;
    EditText member4;
    String member1ID;
    String member2ID;
    String member3ID;
    String member4ID;
    String dueDate;
    String durationHour;
    String durationMinute;
    String meetingName;
    EditText meetingNameEdit;
    TextView dueDateText;
    EditText durationHourEdit;
    EditText durationMinuteEdit;
    Button arrangeMeeting;
    public static ArrayList<String> memberList = new ArrayList<>();
    public static ArrayList<EventClass> memberFixedEvents = new ArrayList<>();
    boolean numberInputsCorrect;
    public static ArrayList<EventClass> toBeOptimizedEvents = new ArrayList<>();

    int checkDurationMinute;
    int checkDurationHour;

    static boolean check;

    static FirebaseDatabase userDatabase = FirebaseDatabase.getInstance();
    static DatabaseReference userDatabaseRef;
    static DatabaseReference userFixedEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_meeting_screen);
        member1 = findViewById(R.id.member_1);
        member2 = findViewById(R.id.member_2);
        member3 = findViewById(R.id.member_3);
        member4 = findViewById(R.id.member_4);
        meetingNameEdit = findViewById(R.id.group_name_edit_text);
        dueDateText = findViewById(R.id.meeting_due_date);
        durationHourEdit = findViewById(R.id.duration_hours);
        durationMinuteEdit = findViewById(R.id.duration_minutes);
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        /** To display the date picker to allow the user to set a due date for the meeting **/
        dueDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(AddMeetingScreen.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month = month+1;
                        if (month < 10) {
                            dueDate = dayOfMonth+"/0"+month+"/"+year;
                        }
                        else {
                            dueDate = dayOfMonth + "/" + month + "/" + year;
                        }
                        if (dayOfMonth < 10) {
                            dueDate = 0 + dueDate;
                        }
                        dueDateText.setText(dueDate);
                    }
                },year,month,dayOfMonth);
                datePickerDialog.show();
            }
        });

        arrangeMeeting = findViewById(R.id.arrange_meeting);
        /** When the user clicks on arrange meeting button **/
        arrangeMeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                member1ID = member1.getText().toString();
                member2ID = member2.getText().toString();
                member3ID = member3.getText().toString();
                member4ID = member4.getText().toString();
                meetingName = meetingNameEdit.getText().toString();
                durationHour = durationHourEdit.getText().toString();
                durationMinute = durationMinuteEdit.getText().toString();

                //To check that the input entered in the duration field is correct
                try {
                    checkDurationMinute = Integer.parseInt(durationMinute);
                    checkDurationHour = Integer.parseInt(durationHour);
                    numberInputsCorrect = true;
                }

                catch (Exception ex) {
                    numberInputsCorrect = false;
                }
                //Check if any of the fields are empty
                if (meetingName.equals("") || durationHour.equals("") || durationMinute.equals("") || dueDate.equals("")) {
                    Toast.makeText(AddMeetingScreen.this, "Please enter required fields", Toast.LENGTH_SHORT).show();
                }
                //To tell the user if the wrong data type is passed to the duration field
                else if (!numberInputsCorrect) {
                    Toast.makeText(AddMeetingScreen.this,"Enter numbers only", Toast.LENGTH_SHORT).show();
                }
                // Add the members name into a list, unless they are a repeat or blank
                if(!memberList.contains(member1ID) && !member1ID.equals("")) {
                    memberList.add(member1ID);
                }
                if(!memberList.contains(member2ID) && !member2ID.equals("")) {
                    memberList.add(member2ID);
                }
                if(!memberList.contains(member3ID) && !member3ID.equals("")) {
                    memberList.add(member3ID);
                }
                if(!memberList.contains(member4ID) && !member4ID.equals("")) {
                    memberList.add(member4ID);
                }

                //Modifying the due date string to make it into LocalDateTime format
                dueDate += " 08:00";
                DateTimeFormatter dueDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                //Convert the due date from String to Local Date Time
                LocalDateTime meetingDueDate = LocalDateTime.parse(dueDate, dueDateFormatter);

                long duration = checkDurationHour*60 + checkDurationMinute;
                //Create a new meeting class
                MeetingClass newMeeting = new MeetingClass(meetingName,false,null,null,"",meetingDueDate,duration, memberList);
                //This is a method to pull the members schedule from firebase
                getMemberFixedEvents();
                //Add this meeting to a list that contains events that need to be optimised
                toBeOptimizedEvents.add(newMeeting);

                //If the group has more than 1 member, add it to the addGroupMeeting list stored in CalendarClass
                if (memberList.size() > 1) {
                    CalendarClass.addGroupMeeting(newMeeting);
                }
                //We set the ListView in the GroupMeetings Fragment to show the new meeting that was
                // added to the addGroupMeeting list
                GroupMeetingsFragment.setListView();
                //Now we go back to the Group meeting Fragment
                Intent intent = new Intent(AddMeetingScreen.this, GroupMeetingsFragment.class);
                //Idk why but startActivity(intent) dont work when we want to go to a fragment, but
                //this code works
                intent.putExtra("Doing this because", "startActivity dont seem to work when going to fragment");
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    /** Method to check if the group member id exist in our firebase **/
    private boolean checkMembers(){
        AddMeetingScreen.check = true;
        for (final String memberName : memberList) {
            userDatabase = FirebaseDatabase.getInstance();
            userDatabaseRef = userDatabase.getReference("userDatabase");
            userDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.i("checking","is in data change");
                    if(!dataSnapshot.hasChild(memberName)){
                        Log.i("checking","is in has membername");
                        AddMeetingScreen.check = false;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        Log.i("checking","check: " + AddMeetingScreen.check);
        return AddMeetingScreen.check;
    }

    /** Method to pull each of the member schedule from firebase **/
    private void getMemberFixedEvents(){
        for (String memberName : memberList) {
            if (memberName != ""){
                userDatabase = FirebaseDatabase.getInstance();
                userDatabaseRef = userDatabase.getReference(memberName);
                Log.i("hans", memberName);
                userFixedEvents = userDatabaseRef.child("AllEvents");
                userFixedEvents.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Log.i("hans", dataSnapshot.toString());
                        DatabaseEvent toaddEvent = dataSnapshot.getValue(DatabaseEvent.class);
                        memberFixedEvents.add(new EventClass(toaddEvent.getName(), toaddEvent.getFlexible(),
                                LocalDateTime.of(toaddEvent.getStart_year(),toaddEvent.getStart_month(),toaddEvent.getStart_day(),
                                        toaddEvent.getStart_hour(),toaddEvent.getStart_minute()), LocalDateTime.of(toaddEvent.getEnd_year(),toaddEvent.getEnd_month(),toaddEvent.getEnd_day(),
                                toaddEvent.getEnd_hour(),toaddEvent.getEnd_minute()), toaddEvent.getDifficulty() ,LocalDateTime.of(toaddEvent.getDue_year(),toaddEvent.getDue_month(),toaddEvent.getDue_day(),
                                toaddEvent.getDue_hour(),toaddEvent.getDue_minute()), toaddEvent.getDuration()));
                        Log.i("hans", memberFixedEvents.toString());
                    }
                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }
                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
            }
        }
    }
}
