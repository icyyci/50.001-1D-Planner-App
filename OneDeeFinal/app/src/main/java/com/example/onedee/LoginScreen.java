package com.example.onedee;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDateTime;

public class LoginScreen extends AppCompatActivity {
    public static final String EXTRA_EMAIL = "com.example.onedee.EMAIL";
    public static final String EXTRA_PASSWORD = "com.example.onedee.PASSWORD";
    public static String username = "";
    //Add button references
    private Button Login;
    TextView CreateNewAccount;
    EditText Email;
    EditText Password;

    static FirebaseDatabase userDatabase = FirebaseDatabase.getInstance();
    static DatabaseReference userDatabaseRef = userDatabase.getReference("userDatabase");
    static DatabaseReference userEventReference;
    static DatabaseReference userFixedEventsReference;
    static DatabaseReference userFlexibleEventsReference;
    static DatabaseReference userAllEventsReference;
    static DatabaseReference userMeetingEventsReference;
    ChildEventListener fixedEventListener;
    ChildEventListener flexibleEventListener;
    ChildEventListener allEventListener;
    ChildEventListener meetingEventListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("creation","hi from create login screen");
        setContentView(R.layout.activity_main);
        //We clear any stored events from the calendar class
        //This is so that if one user logs out, they will be directed to the log in page
        //If another user logs in, the previous user details is still stored in CalendarClass
        CalendarClass.getAllEvents().clear();
        CalendarClass.getFlexibleEvents().clear();
        CalendarClass.getFixedEvents().clear();
        CalendarClass.getGroupMeetingList().clear();
        //Add findViewByIds for widgets
        Login = findViewById(R.id.Login);
        CreateNewAccount = findViewById(R.id.tvSignUp);
        Email = findViewById(R.id.Email);
        Password = findViewById(R.id.Password);
        /** When the user clicks on the login button **/
        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String tryEmail = Email.getText().toString();
                final String tryPassword = Password.getText().toString();
                username = tryEmail;

                //if the user did not enter any username or password
                if (tryEmail.equals("") || tryPassword.equals("")){
                    Toast.makeText(LoginScreen.this, "Please enter email/password", Toast.LENGTH_LONG).show();
                }
                else {
                        userDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            // if the username exist
                            if (dataSnapshot.hasChild(tryEmail)){
                                userDatabaseRef.child(tryEmail).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        //if the password is correct
                                        if(tryPassword.equals(dataSnapshot.getValue())){
                                            userEventReference = userDatabase.getReference(username);
                                            userFixedEventsReference = userEventReference.child("FixedEvents");
                                            userFlexibleEventsReference = userEventReference.child("FlexibleEvents");
                                            userAllEventsReference = userEventReference.child("AllEvents");
                                            userMeetingEventsReference = userEventReference.child("MeetingEvents");

                                            /** To retrieve the user's allEvents from firebase**/
                                            allEventListener = new ChildEventListener() {
                                                @Override
                                                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                                    //We pull the user's data from firebase and re-represent it as an EventClass
                                                    DatabaseEvent toaddEvent = dataSnapshot.getValue(DatabaseEvent.class);
                                                    EventClass newEvent = new EventClass(toaddEvent.getName(), toaddEvent.getFlexible(),
                                                            LocalDateTime.of(toaddEvent.getStart_year(),toaddEvent.getStart_month(),toaddEvent.getStart_day(),
                                                                    toaddEvent.getStart_hour(),toaddEvent.getStart_minute()), LocalDateTime.of(toaddEvent.getEnd_year(),toaddEvent.getEnd_month(),toaddEvent.getEnd_day(),
                                                            toaddEvent.getEnd_hour(),toaddEvent.getEnd_minute()), toaddEvent.getDifficulty() ,LocalDateTime.of(toaddEvent.getDue_year(),toaddEvent.getDue_month(),toaddEvent.getDue_day(),
                                                            toaddEvent.getDue_hour(),toaddEvent.getDue_minute()), toaddEvent.getDuration());
                                                    //Add it to the respective list in Calendar Class
                                                    CalendarClass.getAllEvents().add(newEvent);
                                                    Log.i("crash", "event's show all stats: " + newEvent.ShowAllStats());
                                                }

                                                @Override
                                                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
                                                @Override
                                                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }
                                                @Override
                                                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) { }
                                            };
                                            userAllEventsReference.addChildEventListener(allEventListener);
                                            /**To retrieve the user's fixed events from firebase**/
                                            fixedEventListener = new ChildEventListener() {
                                                @Override
                                                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                                    DatabaseEvent toaddEvent = dataSnapshot.getValue(DatabaseEvent.class);
                                                    Log.i("crash","database event: " + toaddEvent);
                                                    CalendarClass.getFixedEvents().add(new EventClass(toaddEvent.getName(), toaddEvent.getFlexible(),
                                                            LocalDateTime.of(toaddEvent.getStart_year(),toaddEvent.getStart_month(),toaddEvent.getStart_day(),
                                                                    toaddEvent.getStart_hour(),toaddEvent.getStart_minute()), LocalDateTime.of(toaddEvent.getEnd_year(),toaddEvent.getEnd_month(),toaddEvent.getEnd_day(),
                                                            toaddEvent.getEnd_hour(),toaddEvent.getEnd_minute()), toaddEvent.getDifficulty() ,LocalDateTime.of(toaddEvent.getDue_year(),toaddEvent.getDue_month(),toaddEvent.getDue_day(),
                                                            toaddEvent.getDue_hour(),toaddEvent.getDue_minute()), toaddEvent.getDuration()));
                                                }

                                                @Override
                                                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
                                                @Override
                                                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }
                                                @Override
                                                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) { }
                                            };
                                            userFixedEventsReference.addChildEventListener(fixedEventListener);

                                            /**To retrieve the user's flexible events from firebase**/
                                            flexibleEventListener = new ChildEventListener() {
                                                @Override
                                                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                                    DatabaseEvent toaddEvent = dataSnapshot.getValue(DatabaseEvent.class);
                                                    CalendarClass.getFlexibleEvents().add(new EventClass(toaddEvent.getName(), toaddEvent.getFlexible(),
                                                            LocalDateTime.of(toaddEvent.getStart_year(),toaddEvent.getStart_month(),toaddEvent.getStart_day(),
                                                                    toaddEvent.getStart_hour(),toaddEvent.getStart_minute()), LocalDateTime.of(toaddEvent.getEnd_year(),toaddEvent.getEnd_month(),toaddEvent.getEnd_day(),
                                                            toaddEvent.getEnd_hour(),toaddEvent.getEnd_minute()), toaddEvent.getDifficulty() ,LocalDateTime.of(toaddEvent.getDue_year(),toaddEvent.getDue_month(),toaddEvent.getDue_day(),
                                                            toaddEvent.getDue_hour(),toaddEvent.getDue_minute()), toaddEvent.getDuration()));
                                                }
                                                @Override
                                                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
                                                @Override
                                                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }
                                                @Override
                                                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) { }
                                            };
                                            userFlexibleEventsReference.addChildEventListener(flexibleEventListener);

                                            /**To retrieve the user's meetings from firebase **/
                                            meetingEventListener = new ChildEventListener() {
                                                @Override
                                                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                                    DatabaseMeetingEvent toaddEvent = dataSnapshot.getValue(DatabaseMeetingEvent.class);
                                                    CalendarClass.getGroupMeetingList().add(new MeetingClass(toaddEvent.getName(), toaddEvent.getFlexible(),
                                                            LocalDateTime.of(toaddEvent.getStart_year(),toaddEvent.getStart_month(),toaddEvent.getStart_day(),
                                                                    toaddEvent.getStart_hour(),toaddEvent.getStart_minute()), LocalDateTime.of(toaddEvent.getEnd_year(),toaddEvent.getEnd_month(),toaddEvent.getEnd_day(),
                                                            toaddEvent.getEnd_hour(),toaddEvent.getEnd_minute()), toaddEvent.getDifficulty() ,LocalDateTime.of(toaddEvent.getDue_year(),toaddEvent.getDue_month(),toaddEvent.getDue_day(),
                                                            toaddEvent.getDue_hour(),toaddEvent.getDue_minute()), toaddEvent.getDuration(),toaddEvent.getMembersList()));
                                                }
                                                @Override
                                                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
                                                @Override
                                                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }
                                                @Override
                                                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) { }
                                            };
                                            userMeetingEventsReference.addChildEventListener(meetingEventListener);
                                            //Bring the user to the calendar page
                                            Intent intent = new Intent(LoginScreen.this, DisplayCalendar.class);
                                            startActivity(intent);
                                        } else {
                                            Toast.makeText(LoginScreen.this, "Incorrect password", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                                });
                            } else {
                                Toast.makeText(LoginScreen.this, "Username is not registered", Toast.LENGTH_LONG).show();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });
                }

            }
        });
        /**If the create account button is pressed**/
        CreateNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginScreen.this, CreateAccount.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(fixedEventListener!=null && flexibleEventListener!=null && allEventListener!=null && meetingEventListener!=null) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(fixedEventListener!=null && flexibleEventListener!=null && allEventListener!=null && meetingEventListener!=null) {
            userFixedEventsReference.removeEventListener(fixedEventListener);
            userFlexibleEventsReference.removeEventListener(flexibleEventListener);
            userAllEventsReference.removeEventListener(allEventListener);
            userMeetingEventsReference.removeEventListener(meetingEventListener);
        }
    }


    public static String getUsername(){
        Log.i("delete","username: " + username);
        return username;
    }
    /** Called when the user taps the Send button */
    public void calendarLogin(View view) {
        // Do something in response to button
    }

}
