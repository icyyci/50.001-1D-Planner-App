package com.example.onedee.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.onedee.AddEventScreen;
import com.example.onedee.CalendarClass;
import com.example.onedee.DatabaseEvent;
import com.example.onedee.EventClass;
import com.example.onedee.LoginScreen;
import com.example.onedee.R;
import com.example.onedee.ScheduleAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.Distribution;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private ScheduleAdapter scheduleAdapter;
    Calendar calendar = Calendar.getInstance();
    int monthNumber  = calendar.get(Calendar.MONTH);
    String monthName = MONTHS[monthNumber];
    int year = calendar.get(Calendar.YEAR);
    int weekNumber = calendar.get(Calendar.WEEK_OF_YEAR);
    TextView monthYearTextView;
    TextView dayDateTextView1;
    TextView dayDateTextView2;
    TextView dayDateTextView3;
    TextView dayDateTextView4;
    TextView dayDateTextView5;
    TextView displayTime;
    Button nextButton;
    Button prevButton;
    Button deleteButton;
    Button cancelButton;
    Spinner daySpinner;
    String daySelected;
    ArrayList<String> dates;

    static FirebaseDatabase userEventDatabase = FirebaseDatabase.getInstance();
    static DatabaseReference userEventReference = userEventDatabase.getReference(LoginScreen.getUsername());
    static DatabaseReference userFixedEventsReference = userEventReference.child("FixedEvents");
    static DatabaseReference userFlexibleEventsReference = userEventReference.child("FlexibleEvents");


    public static final String[] MONTHS = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    public static final String[] DAYS = {"Sat", "Sun", "Mon", "Tues", "Wed", "Thurs", "Fri"};

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_home, container, false);


        //This is to link the AddEventScreen using the + button
        FloatingActionButton newEventButton = root.findViewById(R.id.newEventButton);
        newEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newEventIntent = new Intent(getActivity(), AddEventScreen.class);
                startActivity(newEventIntent);
            }
        });



        //MONTH & YEAR LABEL
        String monthnYear = monthName + " " + year;
        monthYearTextView = root.findViewById(R.id.monthYear);
        monthYearTextView.setText(monthnYear);

        //Next and Prev Button
        nextButton = root.findViewById(R.id.next_week);
        prevButton = root.findViewById(R.id.prev_week);

        /**To initialise the calendar and display the date on the calendar **/
        calendar.set(Calendar.WEEK_OF_YEAR, weekNumber);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        final SimpleDateFormat sdfDate = new SimpleDateFormat("dd");
        final SimpleDateFormat fullDate = new SimpleDateFormat("dd/MM/yyyy");
        final SimpleDateFormat sdfMonth = new SimpleDateFormat("MM");
        final SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");
        int dateToDisplay = Integer.parseInt(sdfDate.format(calendar.getTime()));
        String dateToSet = fullDate.format(calendar.getTime());
        dates = new ArrayList<>();
        dates.add(dateToSet);

        dayDateTextView1 = root.findViewById(R.id.day1);
        dayDateTextView1.setText("Mon " + dateToDisplay);

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        dateToSet = fullDate.format(calendar.getTime());
        dateToDisplay = Integer.parseInt(sdfDate.format(calendar.getTime()));
        dates.add(dateToSet);
        dayDateTextView2 = root.findViewById(R.id.day2);
        dayDateTextView2.setText("Tue " + (dateToDisplay));

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        dateToSet = fullDate.format(calendar.getTime());
        dateToDisplay = Integer.parseInt(sdfDate.format(calendar.getTime()));
        dates.add(dateToSet);
        dayDateTextView3 = root.findViewById(R.id.day3);
        dayDateTextView3.setText("Wed " + dateToDisplay);

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
        dateToSet = fullDate.format(calendar.getTime());
        dateToDisplay = Integer.parseInt(sdfDate.format(calendar.getTime()));
        dates.add(dateToSet);
        dayDateTextView4 = root.findViewById(R.id.day4);
        dayDateTextView4.setText("Thurs " + dateToDisplay);

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        dateToSet = fullDate.format(calendar.getTime());
        dateToDisplay = Integer.parseInt(sdfDate.format(calendar.getTime()));
        dates.add(dateToSet);
        dayDateTextView5 = root.findViewById(R.id.day5);
        dayDateTextView5.setText("Fri " + dateToDisplay);


        // SCHEDULE IN LISTVIEW
        scheduleAdapter = new ScheduleAdapter(root.getContext(), CalendarClass.getAllEvents(), dates, monthNumber + 1, year);
        final ListView scheduleView = (ListView) root.findViewById(R.id.schedule_view);
        scheduleView.setAdapter(scheduleAdapter);

        //When the user clicks on the next week button
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //we add 1 to the week number and reinitialise the calendar to display the dates of
                // that week instead
                weekNumber += 1;
                Calendar newCalendar = Calendar.getInstance();
                newCalendar.set(Calendar.WEEK_OF_YEAR, weekNumber);
                newCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                Log.i("corn", newCalendar.getTime().toString());
                int newMonthInt = Integer.parseInt(sdfMonth.format(newCalendar.getTime()));
                Log.i("corn", sdfMonth.format(newCalendar.getTime()));
                String newMonth = MONTHS[newMonthInt-1];
                int newYearInt = Integer.parseInt(sdfYear.format(newCalendar.getTime()));
                String newMonthnYear = newMonth + " " + newYearInt;
                monthYearTextView.setText(newMonthnYear);
                String nextDateToSet = fullDate.format(newCalendar.getTime());
                int nextDateToDisplay = Integer.parseInt(sdfDate.format(newCalendar.getTime()));
                ArrayList<String> newDates = new ArrayList<>();
                newDates.add(nextDateToSet);
                dayDateTextView1.setText("Mon " + nextDateToDisplay);

                newCalendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
                nextDateToSet = fullDate.format(newCalendar.getTime());
                nextDateToDisplay = Integer.parseInt(sdfDate.format(newCalendar.getTime()));
                newDates.add(nextDateToSet);
                dayDateTextView2.setText("Tue " + (nextDateToDisplay));

                newCalendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
                nextDateToSet = fullDate.format(newCalendar.getTime());
                nextDateToDisplay = Integer.parseInt(sdfDate.format(newCalendar.getTime()));
                newDates.add(nextDateToSet);
                dayDateTextView3.setText("Wed " + (nextDateToDisplay));

                newCalendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                nextDateToSet = fullDate.format(newCalendar.getTime());
                nextDateToDisplay = Integer.parseInt(sdfDate.format(newCalendar.getTime()));
                newDates.add(nextDateToSet);
                dayDateTextView4.setText("Thurs " + (nextDateToDisplay));

                newCalendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
                nextDateToSet = fullDate.format(newCalendar.getTime());
                nextDateToDisplay = Integer.parseInt(sdfDate.format(newCalendar.getTime()));
                newDates.add(nextDateToSet);
                dayDateTextView5.setText("Fri " + (nextDateToDisplay));

                //we create a new scheduleAdapter with the new dates so that the events displayed on
                //the calendar will be the right events
                scheduleAdapter = new ScheduleAdapter(root.getContext(), CalendarClass.getAllEvents(),newDates, newMonthInt, newYearInt);
                final ListView scheduleView = (ListView) root.findViewById(R.id.schedule_view);
                scheduleView.setAdapter(scheduleAdapter);
            }
        });

        //if the previous week button is pressed, basically do the same thing as the above
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("corn", "prev button pressed");
                weekNumber -= 1;
                Calendar newCalendar = Calendar.getInstance();
                newCalendar.set(Calendar.WEEK_OF_YEAR, weekNumber);
                newCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                int newMonthInt = Integer.parseInt(sdfMonth.format(newCalendar.getTime()));
                String newMonth = MONTHS[newMonthInt-1];
                int newYearInt = Integer.parseInt(sdfYear.format(newCalendar.getTime()));
                String newMonthnYear = newMonth + " " + newYearInt;
                monthYearTextView.setText(newMonthnYear);
                ArrayList<String> newDates = new ArrayList<>();
                String nextDateToSet = fullDate.format(newCalendar.getTime());
                int nextDateToDisplay = Integer.parseInt(sdfDate.format(newCalendar.getTime()));
                newDates.add(nextDateToSet);
                dayDateTextView1.setText("Mon " + nextDateToDisplay);

                newCalendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
                nextDateToSet = fullDate.format(newCalendar.getTime());
                nextDateToDisplay = Integer.parseInt(sdfDate.format(newCalendar.getTime()));
                newDates.add(nextDateToSet);
                dayDateTextView2.setText("Tue " + (nextDateToDisplay));

                newCalendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
                nextDateToSet = fullDate.format(newCalendar.getTime());
                nextDateToDisplay = Integer.parseInt(sdfDate.format(newCalendar.getTime()));
                newDates.add(nextDateToSet);
                dayDateTextView3.setText("Wed " + (nextDateToDisplay));

                newCalendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                nextDateToSet = fullDate.format(newCalendar.getTime());
                nextDateToDisplay = Integer.parseInt(sdfDate.format(newCalendar.getTime()));
                newDates.add(nextDateToSet);
                dayDateTextView4.setText("Thurs " + (nextDateToDisplay));

                newCalendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
                nextDateToSet = fullDate.format(newCalendar.getTime());
                nextDateToDisplay = Integer.parseInt(sdfDate.format(newCalendar.getTime()));
                newDates.add(nextDateToSet);
                dayDateTextView5.setText("Fri " + (nextDateToDisplay));

                scheduleAdapter = new ScheduleAdapter(root.getContext(), CalendarClass.getAllEvents(), newDates, newMonthInt, newYearInt);
                final ListView scheduleView = (ListView) root.findViewById(R.id.schedule_view);
                scheduleView.setAdapter(scheduleAdapter);
            }
        });


       //To initialise Popup window to delete events when user clicks on the listview
        LayoutInflater popupInflater = (LayoutInflater) root.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = popupInflater.inflate(R.layout.activity_remove_event_screen,null);
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = false;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        displayTime = popupView.findViewById(R.id.delete_event_text);
        daySpinner = (Spinner) popupView.findViewById(R.id.day_spinner);
        cancelButton = popupView.findViewById(R.id.cancel_button);
        deleteButton = popupView.findViewById(R.id.delete_button);

        //when user clicks on the listview
        scheduleView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                //Display popup window
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
                String time = scheduleAdapter.getTime(position);
                displayTime.setText("Delete Event at Time: " + time);
                ArrayList<String> days = new ArrayList<>();
                days.add("Monday");
                days.add("Tuesday");
                days.add("Wednesday");
                days.add("Thursday");
                days.add("Friday");
                //Create a spinner that will allow the user to choose the days they want to delete
                // the event from
                ArrayAdapter<String> spinnerDayAdapter = new ArrayAdapter<String>(root.getContext(), android.R.layout.simple_spinner_dropdown_item, days);
                spinnerDayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                daySpinner.setAdapter(spinnerDayAdapter);
                daySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        daySelected = parent.getItemAtPosition(position).toString();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                /** If the user cancels, just dismiss the popup window */
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });
                /** if the user chooses to delete */
                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /** remove the event from the scheduleAdapter, which will remove the event from
                        the calendar class and firebase as well */
                        scheduleAdapter.removeEvent(position, daySelected);
                        // Set a new schedule adapter to reflect the changes
                        scheduleAdapter = new ScheduleAdapter(root.getContext(), CalendarClass.getAllEvents(), dates, monthNumber + 1, year);
                        final ListView scheduleView = (ListView) root.findViewById(R.id.schedule_view);
                        scheduleView.setAdapter(scheduleAdapter);
                        popupWindow.dismiss();
                    }
                });


            }
        });


        return root;

    }

}