package com.example.onedee;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** ScheduleAdapter is used to allow all our EventClass to be represented on the ListView that is
 * used to populate the main calendar.
 * For this ListView, we have one single ListView that has 6 columns. The columns are Time,
 * Monday, Tuesday, Wednesday, Thursday and Friday, and they have each have their own respective
 * arraylist. (timeList, mondayList, tuesdayList...etc)
 * These 6 columns are aligned in a horizontal layout
 */

public class ScheduleAdapter extends BaseAdapter {

    private Context scheduleContext;
    private List<EventClass> scheduleList;
    private List<EventClass> mondayList = new ArrayList<EventClass>();
    private List<EventClass> tuesdayList = new ArrayList<EventClass>();
    private List<EventClass> wednesdayList = new ArrayList<EventClass>();
    private List<EventClass> thursdayList = new ArrayList<EventClass>();
    private List<EventClass> fridayList = new ArrayList<EventClass>();
    private List<String> timeList = new ArrayList<String>();
    private ArrayList<String> dates = new ArrayList<>();
    private int month;
    private int year;
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    //ScheduleAdapter takes in an arraylist of all the events that the user has
    //It also takes in the dates which is used to tell the adapter which events to display
    //This is because our calendar has arrows to display the previous or next week schedule
    //As such the calendar has to populate the events based on the dates that the user has selected

    public ScheduleAdapter(Context context, List<EventClass> events, ArrayList<String> dates, int month, int year) {
        scheduleContext = context;
        scheduleList = events;
        Log.i("crash", "allevents: " + events);
        this.dates = dates;
        this.month = month;
        this.year = year;
        arrange();
    }

    // Method that we use populate the individual arraylist with the correct events
    private void arrange() {
        //This is to populate the timelist. We want our day to start at 8am and end at midnight
        for (int i = 8; i < 24; i++) {
            //If the hour is a single digit, we add a 0 to the front of it
            //We have 30 minutes interval. time1 is the time at the hour, and time2 is for the time
            //at the 30 mins mark
            if (i < 10) {
                String time1 = "0" + i + "00";
                String time2 = "0" + i + "30";
                timeList.add(time1);
                timeList.add(time2);
            }
            else {
                String time1 = i + "00";
                String time2 = i  + "30";
                timeList.add(time1);
                timeList.add(time2);
            }
        }
        //32 is the number of time slots we have (from 0800,0830,0900...2330)
        for (int i = 0; i < 32; i ++) {
            /**
            *We first add an an empty EventClass to all the timeslots for all the days
            *This is so that the each list will have 32 items in it, thus creating 32 boxes.
            *Since it is an empty event, the box will display nothing inside
            *We have to do this as we need to give listview something to display even if there is
            *no events. Else a list with less than 32 items will have less than 32 boxes, which
             * messes with the alignment with the timeList in the listview
             **/
            mondayList.add(new EventClass());
            tuesdayList.add(new EventClass());
            wednesdayList.add(new EventClass());
            thursdayList.add(new EventClass());
            fridayList.add(new EventClass());
        }
        //for all the events stored in the list
        for (EventClass event: scheduleList) {
            //Convert the LocalDateTime to a String
            String eventDate = event.getStartTime().format(dateFormatter);
            //if the dates of the calendar matches with the event date, we want to display it
            if (dates.contains(eventDate)) {
                //We get the day of the event
                int day = event.getStartTime().getDayOfWeek().getValue();
                //get the hour of the event start
                int startTime = event.startTime.getHour();
                //get the hour of the event end
                int endTime = event.endTime.getHour();
                //Since our day starts at 8, the index of where the event should be located in the
                //list is the start hour - 8. We multiply by 2 as our timetable is in 30 mins
                //interval
                int startIndex = (startTime - 8) * 2;
                int endIndex = ((endTime - 8) * 2) -1;
                //If the startTime minute is more than equal to 30, we shift the index up by 1
                if (event.startTime.getMinute() >= 30) {
                    startIndex += 1;
                }
                if (event.endTime.getMinute() >= 30) {
                    endIndex += 1;
                }
                //This selects the which day list we want to put the event in
                switch (day) {
                    case 1:
                        Log.i("corn", "adding to monday");
                        //We fill the list up with the same event from the event start index to
                        // the event end index
                        for (int i = startIndex; i <= endIndex; i++) {
                            //this replaces the empty EventClass in the list
                            mondayList.set(i, event);
                        }
                        break;
                    case 2:
                        Log.i("corn", "adding to tuesday");
                        for (int i = startIndex; i <= endIndex; i++) {
                            tuesdayList.set(i, event);
                        }
                        break;
                    case 3:
                        Log.i("corn", "adding to wednesday");
                        for (int i = startIndex; i <= endIndex; i++) {
                            wednesdayList.set(i, event);
                        }
                        break;
                    case 4:
                        Log.i("corn", "adding to thursday");
                        for (int i = startIndex; i <= endIndex; i++) {
                            thursdayList.set(i, event);
                        }
                        break;
                    case 5:
                        Log.i("corn", "adding to friday");
                        for (int i = startIndex; i <= endIndex; i++) {
                            fridayList.set(i, event);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    //GetCount represens the number of data we want to display. We hardcode it to be 32 as we have
    //32 time slots to display
    public int getCount() {
        return 32;
    }

    @Override
    public Object getItem(int position) {
        return scheduleList.get(position);
    }
    public long getItemId(int i) {
        return i;
    }

    public String getTime(int position) {
        return timeList.get(position);
    }

    //Method to removeEvent, it takes in a String that is the name of the day we want to remove
    //from, so we know which list to remove the event from
    public void removeEvent(int position, String day) {
        List<EventClass> targetList = new ArrayList<>();
        switch (day) {
            case("Monday"):
                targetList = mondayList;
                break;
            case("Tuesday"):
                targetList = tuesdayList;
                break;
            case("Wednesday"):
                targetList = wednesdayList;
                break;
            case("Thursday"):
                targetList = thursdayList;
                break;
            case("Friday"):
                targetList = fridayList;
                break;
        }
        //Once we know the list, we extract the EventClass from the position that the user clicked at
        EventClass eventToBeDeleted = targetList.get(position);



        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query EventsQuery;
        Query EventsQuery2;
        //We delete that event from firebase
        EventsQuery = ref.child(LoginScreen.getUsername()).child("FixedEvents").orderByChild("name").equalTo(eventToBeDeleted.getName());
        EventsQuery2 = ref.child(LoginScreen.getUsername()).child("AllEvents").orderByChild("name").equalTo(eventToBeDeleted.getName());
        EventsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot eventSnapshot: dataSnapshot.getChildren()) {
                    eventSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("hans", "onCancelled", databaseError.toException());
            }
        });

        EventsQuery2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot eventSnapshot: dataSnapshot.getChildren()) {
                    eventSnapshot.getRef().removeValue();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //We delete that event from the CalendarClass as well
        CalendarClass.deleteEvent(eventToBeDeleted);
        //We then pull the updated list of events from CalendarClass
        scheduleList = CalendarClass.getAllEvents();
        //We arrange the events again to reflect the changes
        arrange();
        notifyDataSetChanged();
    }

    //method to add an event
    public void add(EventClass event) {
        scheduleList.add(event);
        arrange();
        notifyDataSetChanged();
    }

    public View getView(int i , View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater scheduleInflater = (LayoutInflater) scheduleContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            view = scheduleInflater.inflate(R.layout.schedule_adapter_layout, null);
        }
        TextView timeText = (TextView) view.findViewById(R.id.time);
        TextView mondayText = (TextView) view.findViewById(R.id.monday_events);
        TextView tuesdayText = (TextView) view.findViewById(R.id.tuesday_events);
        TextView wednesdayText = (TextView) view.findViewById(R.id.wednesday_events);
        TextView thursdayText = (TextView) view.findViewById(R.id.thursday_events);
        TextView fridayText = (TextView) view.findViewById(R.id.friday_events);

        //This is to set the individual columns in the list view with the respective data
        timeText.setText(timeList.get(i));
        mondayText.setText(mondayList.get(i).name);
        tuesdayText.setText(tuesdayList.get(i).name);
        wednesdayText.setText(wednesdayList.get(i).name);
        thursdayText.setText(thursdayList.get(i).name);
        fridayText.setText(fridayList.get(i).name);
        return view;
    }

}
