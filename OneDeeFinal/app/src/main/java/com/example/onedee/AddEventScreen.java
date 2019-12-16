package com.example.onedee;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

import static com.example.onedee.LoginScreen.username;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.MINUTES;


public class AddEventScreen extends AppCompatActivity {

    private EditText etEventTitle;
    private TimePickerDialog timePickerDialog;
    int currentHour;
    int currentMinute;
    String amPm;
    public Button addEventButton;
    String notificationTimeRange;
    String date;
    private TextView etSelectDate, etStartTime, etEndTime;
    int checkHourStart;
    int checkHourEnd;

    public static final String NOTIFICATION_CHANNEL_ID = "1001";
    private final static String default_notification_channel_id = "default";

    static FirebaseDatabase userDatabase = FirebaseDatabase.getInstance();
    static DatabaseReference userRef = userDatabase.getReference(username);
    static DatabaseReference userFixedEvent = userRef.child("FixedEvents");
    static DatabaseReference userAllEvent = userRef.child("AllEvents");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event_screen);
        etSelectDate = (TextView) findViewById(R.id.etSelectDate);
        etStartTime = (TextView) findViewById(R.id.etStartTime);
        etEndTime = (TextView) findViewById(R.id.etEndTime);
        etEventTitle = (EditText) findViewById(R.id.etEventTitle);

        //Instantiating the calender to show the user the current date
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        /** when user clicks on select date, we show a date picker to allow them to choose a date **/
        etSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(AddEventScreen.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month = month+1;
                        //If month is less than 10, we add a 0 in front to make it 01, 02.. etc so as to follow
                        //Local Date Time format
                        if (month < 10) {
                            date = dayOfMonth+"/0"+month+"/"+year;
                        }
                        else {
                            //Adding the "/" to make it follow Local Date Time format
                            date = dayOfMonth + "/" + month + "/" + year;
                        }
                        if (dayOfMonth < 10) {
                            //Do the same thing for days that are less than 10
                            date = 0 + date;
                        }
                        //Display the modified date to show the user
                        etSelectDate.setText(date);
                    }
                },year,month,dayOfMonth);
                datePickerDialog.show();
            }
        });

        /** When the user clicks on the start time, we show a time picker to allow the user to choose the time **/
        etStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Instantiating calendar to show the user the current time
                Calendar calendar = Calendar.getInstance();
                currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                currentMinute = calendar.get(Calendar.MINUTE);

                timePickerDialog = new TimePickerDialog(AddEventScreen.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (hourOfDay >= 12) {
                            amPm = "PM";
                        }else {
                            amPm = "AM";
                        }
                        checkHourStart = hourOfDay;
                        etStartTime.setText(String.format("%02d:%02d",hourOfDay, minute));
                    }
                }, currentHour, currentMinute, false);

                timePickerDialog.show();
            }
        });

        /** When the user clicks on the end time, we show a time picker to allow the user to choose the time **/
        etEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                currentMinute = calendar.get(Calendar.MINUTE);

                timePickerDialog = new TimePickerDialog(AddEventScreen.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (hourOfDay >= 12) {
                            amPm = "PM";
                        }else {
                            amPm = "AM";
                        }
                        etEndTime.setText(String.format("%02d:%02d",hourOfDay, minute));
                        checkHourEnd = hourOfDay;
                    }
                }, currentHour, currentMinute, false);

                timePickerDialog.show();
            }
        });

        /** Setting up the spinner for the user to choose the notification settings **/
        Spinner notificationSpinner = findViewById(R.id.spinnerReminder);
        ArrayAdapter<CharSequence> notificationAdapter = ArrayAdapter.createFromResource(this, R.array.reminders, android.R.layout.simple_spinner_item);
        notificationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        notificationSpinner.setAdapter(notificationAdapter);
        notificationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                notificationTimeRange = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        /** To store the events on the add event button press **/

        addEventButton = (Button) findViewById(R.id.btnAddEvent);
        addEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String eventTitle = etEventTitle.getText().toString();
                String startTime = etStartTime.getText().toString();
                String endTime = etEndTime.getText().toString();
                String selectDate = etSelectDate.getText().toString();
                //If the user did not key in any of the blanks
                if (eventTitle.equals("")) {
                    Toast.makeText(AddEventScreen.this, "Please enter valid event name", Toast.LENGTH_LONG).show();
                }
                else if (startTime.equals("")) {
                    Toast.makeText(AddEventScreen.this, "Please enter valid start time", Toast.LENGTH_LONG).show();
                }
                else if (endTime.equals("")) {
                    Toast.makeText(AddEventScreen.this, "Please enter valid end time", Toast.LENGTH_LONG).show();

                }
                else if (selectDate.equals("")) {
                    Toast.makeText(AddEventScreen.this, "Please enter valid date", Toast.LENGTH_LONG).show();
                }
                else if (checkHourStart < 8 || checkHourEnd < 8) {
                    Toast.makeText(AddEventScreen.this, "Please enter a time from 8am to 12am", Toast.LENGTH_SHORT).show();
                }

                else {
                    //Modifying the start and end time string to fit into Local Date Time format
                    startTime = selectDate + " " + startTime;
                    endTime = selectDate + " " + endTime;
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                    //Changing from String to LocalDateTime
                    LocalDateTime eventStart = LocalDateTime.parse(startTime, dateFormatter);
                    LocalDateTime eventEnd = LocalDateTime.parse(endTime, dateFormatter);
                    //If the event end time is earlier than the start time
                    if (eventStart.until(eventEnd,MINUTES) < 0) {
                        Toast.makeText(AddEventScreen.this, "End Time is earlier than start time", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        //Create a new EventClass for the event
                        EventClass newEvent = new EventClass(eventTitle, false, eventStart, eventEnd, null, null, 0);
                        //Add the event to be stored in CalendarClass
                        CalendarClass.addEvent(newEvent);
                        //Push the event to FireBase
                        userFixedEvent.push().setValue(new DatabaseEvent(newEvent));
                        userAllEvent.push().setValue(new DatabaseEvent(newEvent));
                        //Notify the user that the event has been added successfully
                        Toast.makeText(AddEventScreen.this,"Added to Events List", Toast.LENGTH_LONG).show();
                        //Schedule the notifications based on the settings set by the user
                        LocalDateTime nowTime = LocalDateTime.now();
                        /** Sets up notification when fixed event is created*/
                        Long toEventLong = nowTime.until(newEvent.getStartTime(), MILLIS);
                        if (notificationTimeRange.equals("At time of event")) {
                            scheduledNotification(getNotification(eventTitle), toEventLong);
                        } else if (notificationTimeRange.equals("5 minutes before")) {
                            toEventLong = toEventLong - 300000;
                            scheduledNotification(getNotification(eventTitle), toEventLong);
                        } else if (notificationTimeRange.equals("1 hour before")) {
                            toEventLong = toEventLong - 3600000;
                            scheduledNotification(getNotification(eventTitle), toEventLong);
                        } else if (notificationTimeRange.equals("1 day before")) {
                            toEventLong = toEventLong - 86400000;
                            scheduledNotification(getNotification(eventTitle), toEventLong);
                        } else if (notificationTimeRange.equals("2 days before")) {
                            toEventLong = toEventLong - 172800000;
                            scheduledNotification(getNotification(eventTitle), toEventLong);
                        }
                        //Once the event has been added successfully, we return to the main Calendar page
                        Intent intent = new Intent(AddEventScreen.this, DisplayCalendar.class);
                        startActivity(intent);
                    }
                }
            }
        });
    }

    /** Method to create the notifications **/
    private Notification getNotification(String title){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, default_notification_channel_id);
        builder.setContentText("Start doing your assignment: " + title);
        builder.setContentTitle("Assignment Notification");
        builder.setAutoCancel(true);
        builder.setChannelId(NOTIFICATION_CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);
        return builder.build();
    }

    /** Method to schedule the notifications*/
    public void scheduledNotification (Notification notification, long delay){
        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        long futureInMills = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMills, pendingIntent);
    }

}