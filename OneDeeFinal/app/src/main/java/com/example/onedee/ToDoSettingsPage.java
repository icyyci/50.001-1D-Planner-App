package com.example.onedee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.EventLog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.onedee.ui.todolist.TodoListFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class ToDoSettingsPage extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    EditText enterEventTitle, enterHours, enterMinutes;
    TextView EventTitle, Duration, Difficulty, DueDate, enterDueDate, enterDueTime;
    Button btnAddToDoList;
    Spinner spinnerDifficulty;
    int currentHour;
    int currentMinute;
    String amPm;
    int[] datePicked = new int[5];
    TimePickerDialog timePickerDialog;
    boolean deleted;
    EventClass newEvent;

    static FirebaseDatabase userDatabase = FirebaseDatabase.getInstance();
    static DatabaseReference userRef = userDatabase.getReference(LoginScreen.getUsername());
    static DatabaseReference userFlexibleEvents = userRef.child("FlexibleEvents");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_settings_page);

        enterEventTitle = (EditText)findViewById(R.id.etEventTitleToDo);
        enterHours = (EditText)findViewById(R.id.etEnterHours);
        enterMinutes = (EditText)findViewById(R.id.etEnterMinutes);
        enterDueDate = (TextView)findViewById(R.id.etDueDate);
        EventTitle = (TextView)findViewById(R.id.tvEventTitle);
        Duration = (TextView)findViewById(R.id.tvDuration);
        Difficulty = (TextView) findViewById(R.id.tvDifficulty);
        DueDate = (TextView) findViewById(R.id.tvDueDate);
        btnAddToDoList = (Button) findViewById(R.id.btnPlus);
        spinnerDifficulty = (Spinner)findViewById(R.id.spinnerDifficulty);
        enterDueTime = (TextView) findViewById(R.id.tvEnterDueTime);

        Intent intent1 = getIntent();
        String title = intent1.getStringExtra(TodoListFragment.TITLE);
        enterEventTitle.setText(title);
        final int index = intent1.getIntExtra("INDEX",-1);
        String time = intent1.getStringExtra("DUE_TIME");
        enterDueTime.setText(time);
        String date = intent1.getStringExtra("DUE_DATE");
        enterDueDate.setText(date);
        final long duration = intent1.getLongExtra("DURATION",0);
        long hours = duration/60;
        long minutes = duration%60;
//        enterHours.setText("hours: "+hours);
//        enterMinutes.setText("minutes: "+minutes);



        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        enterDueTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                currentMinute = calendar.get(Calendar.MINUTE);

                timePickerDialog = new TimePickerDialog(ToDoSettingsPage.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (hourOfDay >= 12) {
                            amPm = " (PM)";
                        }else {
                            amPm = " (AM)";
                        }
                        datePicked[3] = hourOfDay;
                        datePicked[4] = minute;
                        enterDueTime.setText(String.format("%02d:%02d",hourOfDay, minute));
                    }
                }, currentHour, currentMinute, false);

                timePickerDialog.show();
            }
        });



        enterDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(ToDoSettingsPage.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month = month+1;
                        String date;
                        if (month < 10) {
                            date = dayOfMonth+"/0"+month+"/"+year;
                        }
                        else {
                            date = dayOfMonth + "/" + month + "/" + year;
                        }
                        if (dayOfMonth < 10) {
                            date = 0 + date;
                        }
                        enterDueDate.setText(date);
                        datePicked[0] = year;
                        datePicked[1] = month;
                        datePicked[2] = dayOfMonth;
                    }
                },year,month,dayOfMonth);
                datePickerDialog.show();
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.difficulty, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(adapter);
        spinnerDifficulty.setOnItemSelectedListener(this);

        //Store to-do list upon pressing add to-do button
        btnAddToDoList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(enterEventTitle.getText().toString().equals("")||enterHours.getText().toString().equals("")||
                        enterMinutes.getText().toString().equals("")||enterDueDate.getText().toString().equals("")||
                        enterDueTime.getText().toString().equals("Click to pick Due Time")){
                    Toast.makeText(ToDoSettingsPage.this, "Please fill in the blanks", Toast.LENGTH_LONG).show();

                }

                else {
                    if(index>=0){
                        deleted = false;
                        Log.i("delete","deleting from calendar class");

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                        Query EventsQuery;
                        EventsQuery = ref.child(LoginScreen.getUsername()).child("FlexibleEvents").orderByChild("name").equalTo(TodoListFragment.eventList.get(index).getName());

                        EventsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot eventSnapshot: dataSnapshot.getChildren()) {
                                    eventSnapshot.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Log.i("todo_edit","deleted");
                                            if(!deleted){
                                                userFlexibleEvents.push().setValue(new DatabaseEvent(newEvent));
                                            }
                                            deleted = true;
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e("hans", "onCancelled", databaseError.toException());
                            }
                        });

                        CalendarClass.deleteEvent(TodoListFragment.eventList.get(index));
                        TodoListFragment.eventList.remove(index);
                        TodoListFragment.arrayList.remove(index);
                    }
                    String ToDoTitle = enterEventTitle.getText().toString();
                    BigDecimal duration = new BigDecimal(enterHours.getText().toString()).multiply(new BigDecimal("60")).add(new BigDecimal(enterMinutes.getText().toString()));
                    String difficulty = spinnerDifficulty.getSelectedItem().toString();
                    String deadlineDate = enterDueDate.getText().toString();
                    String deadlineTime = enterDueTime.getText().toString();
                    String deadline = deadlineDate + " " + deadlineTime;

                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                    LocalDateTime dueDate = LocalDateTime.parse(deadline, dateFormatter);

                    //LocalDateTime dueDate = LocalDateTime.of(datePicked[0], datePicked[1], datePicked[2], datePicked[3], datePicked[4]);

                    newEvent = new EventClass(ToDoTitle, true, null, null, difficulty, dueDate, duration.longValue());
                    //CalendarClass.addEvent(newEvent);
                    TodoListFragment.eventList.add(newEvent);
                    //TodoListFragment.arrayList.add(newEvent.toString());
                    userFlexibleEvents.push().setValue(new DatabaseEvent(newEvent));
                    CalendarClass.addEvent(newEvent);
                    Intent intent = new Intent(ToDoSettingsPage.this, TodoListFragment.class);
                    intent.putExtra("test", ToDoTitle);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }

        });

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
