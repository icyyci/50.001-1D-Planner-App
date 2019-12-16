package com.example.onedee;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.time.LocalDateTime;
import java.util.List;

import static com.example.onedee.LoginScreen.username;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.temporal.ChronoUnit.MINUTES;

/** CalendarClass where we basically store the list of all the events that belong to this current user
    Code for the optimisation is here as well **/

public class  CalendarClass {
    public static FirebaseDatabase database = FirebaseDatabase.getInstance();
    static ArrayList<EventClass> groupMeetingList = new ArrayList<EventClass>();
    static ArrayList<EventClass> allEvents = new ArrayList<EventClass>();
    static ArrayList<EventClass> fixedEvents = new ArrayList<EventClass>();
    static ArrayList<EventClass> flexibleEvents = new ArrayList<EventClass>();
    static ArrayList<EventClass> assignment = new ArrayList<EventClass>();

    static FirebaseDatabase userDatabase = FirebaseDatabase.getInstance();
    static DatabaseReference userRef = userDatabase.getReference(username);
    static DatabaseReference userAllEvent = userRef.child("AllEvents");
    static boolean deleted;
    static boolean added;
    static ArrayList<EventClass> completedEvents = new ArrayList<EventClass>();

    public static void addEvent(EventClass newEvent) {
        if (newEvent.getFlexible()) {
            flexibleEvents.add(newEvent);
        } else {
            allEvents.add(newEvent);
            fixedEvents.add(newEvent);
        }
    }


    public static void deleteEvent(EventClass eventToDelete) {
        if (eventToDelete.flexible) {
            flexibleEvents.remove(eventToDelete);
        } else {
            fixedEvents.remove(eventToDelete);
        }
        allEvents.remove(eventToDelete);

    }

    public static ArrayList<EventClass> getFixedEvents() {
        return fixedEvents;
    }

    public static ArrayList<EventClass> getAllEvents() {
        return allEvents;
    }

    public static ArrayList<EventClass> getFlexibleEvents() {
        return flexibleEvents;
    }

    public static ArrayList<EventClass> getGroupMeetingList() {
        return groupMeetingList;
    }

    public static void addGroupMeeting(MeetingClass meeting) {
        groupMeetingList.add(meeting);
    }

    public static void removeGroupMeeting(MeetingClass meeting) {groupMeetingList.remove(meeting);}

    public static void sortFixedEvents() {//sort fixedEvents based on start time

        Collections.sort(fixedEvents, new Comparator<EventClass>() {
            public int compare(EventClass e1, EventClass e2) {
                if (e1.startTime.isAfter(e2.startTime)) {
                    return 1;
                } else if (e1.startTime.isEqual(e2.startTime)) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });

    }

    public static ArrayList<EventClass> sortEvents(ArrayList<EventClass> listOfEvents) { //sort input Events based on start time

        Collections.sort(listOfEvents, new Comparator<EventClass>() {
            public int compare(EventClass e1, EventClass e2) {
                if (e1.startTime.isAfter(e2.startTime)) {
                    return 1;
                } else if (e1.startTime.isEqual(e2.startTime)) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });
        return listOfEvents;
    }

    public static int check_name(List<EventClass> list_toCheck, String name) {
        for (EventClass i : list_toCheck) {
            if (name.equals(i.getName())) {
                Log.i("crash", "index: " + list_toCheck.indexOf(i));
                return list_toCheck.indexOf(i);
            }
        }
        return -1;
    }

    public static String optimise() {
        //Initialize boolean that shows whether the event is deleted/added in Firebase to false
        deleted = false;
        added = false;

        //Sort fixed events if it is empty
        if (!fixedEvents.isEmpty()) {
            sortFixedEvents();
        }

        //Create a copy of the flexible events for the use of this optimization algorithm
        assignment = (ArrayList<EventClass>) flexibleEvents.clone();

        //Return if there is no flexible events to be optimized
        if (assignment.isEmpty()) {
            return "No tasks added";
        }

        //Initialize completed events to be returned
        //Set Start Time
        LocalDateTime Start_time;
        if (LocalDateTime.now().getHour() < 8) {
            Start_time = LocalDateTime.of(LocalDateTime.now().getYear(), LocalDateTime.now().getMonth(), LocalDateTime.now().getDayOfMonth(),
                    8, 0);
        } else {
            Start_time = LocalDateTime.now(); //get current date-time
        }

        //Delete all previously implemented flexible events in allEvents list and from Firebase
        deleteFlexible();

        //Get only the relevant fixed events (Remove all fixed events that ends before current time)
        List<EventClass> inputFixedEvents = new ArrayList<EventClass>();
        for (EventClass i : fixedEvents) {
            if (Start_time.until(i.getEndTime(), MINUTES) > 0) {
                int start_index = fixedEvents.indexOf(i);
                inputFixedEvents = fixedEvents.subList(start_index, fixedEvents.size());
                break;
            }
        }

        //Sort the assignments to be optimized based on its due date
        //This is to slot the assignments with earlier due dates first before the others
        Collections.sort(assignment);

        //Check if all assignments could be completed by the deadline
        boolean checkIfCanComplete = canComplete(assignment, inputFixedEvents, Start_time);
        if (!checkIfCanComplete) {
            return "Tasks cannot be completed by deadline";
        }

        //Give all assignments its respective timings and return them to completedEvents
        //Also, add all these events to allEvents list (done in slotting method)
        completedEvents = slotting(assignment, inputFixedEvents, Start_time);

        //Send all completedEvents to Firebase
        for (EventClass i : completedEvents) {
            allEvents.add(i);
            userAllEvent.push().setValue(new DatabaseEvent(i)).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    added = true;
                }
            });
        }
        return "done";
    }

    // Check if can complete by due date
    private static boolean canComplete(ArrayList<EventClass> sortedEvents, List<EventClass> fixedEvents, LocalDateTime Start_time) {
        long totalEventDuration = 0;
        long fixedEventDuration;
        LocalDateTime nowTime = LocalDateTime.now();
        //CALCULATE THE TOTAL DURATION, RETURN FALSE IF CANNOT FINISH THE ASSIGNMENT ON TIME
        for (int i = 0; i < sortedEvents.size(); i++) {
            //GET FIXED DURATION BETWEEN THE PREVIOUS DUEDATE AND THE CURRENT DUEDATE, IF 0 MEANS START FROM STARTTIME
            if (i == 0) {
                fixedEventDuration = getFixedEventDuration(Start_time, sortedEvents.get(i).getDueDate(), fixedEvents);
            } else {
                fixedEventDuration = getFixedEventDuration(sortedEvents.get(i - 1).getDueDate(), sortedEvents.get(i).getDueDate(), fixedEvents);
            }
            totalEventDuration = totalEventDuration + fixedEventDuration + sortedEvents.get(i).getDuration();
            if (totalEventDuration > Start_time.until(sortedEvents.get(i).getDueDate(), MINUTES) || 0 > Start_time.until(sortedEvents.get(i).getDueDate(), MINUTES)) {
                return false;
            }
            nowTime = nowTime.plusMinutes(totalEventDuration);
            if (nowTime.getDayOfWeek() == SATURDAY) {
                totalEventDuration += 2880; //skip to monday
            }
            if (nowTime.getHour() < 8) {
                totalEventDuration = totalEventDuration - fixedEventDuration - sortedEvents.get(i).getDuration();
                LocalDateTime twelve = LocalDateTime.of(nowTime.getYear(), nowTime.getMonth(), nowTime.getDayOfMonth(), 23, 59);

                totalEventDuration = totalEventDuration + nowTime.until(twelve, MINUTES) + 1;
                totalEventDuration = totalEventDuration + 480;
                totalEventDuration = totalEventDuration + fixedEventDuration + sortedEvents.get(i).getDuration();
            }
        }
        return true;
    }

    private static long getFixedEventDuration(LocalDateTime start, LocalDateTime end, List<EventClass> fixedEvents) {
        long duration = 0;
        for (int i = 0; i < fixedEvents.size(); i++) {
            EventClass currentEvent = fixedEvents.get(i);
            if (start.until(currentEvent.getStartTime(), MINUTES) > 0) {
                if (currentEvent.getStartTime().until(end, MINUTES) > 0) {
                    //CATCH THE CASE WHEN THE EVENT STARTS BETWEEN THE START TIME AND ENDS BEFORE THE DUE DATE
                    if (currentEvent.getEndTime().until(end, MINUTES) >= 0) {
                        duration = duration + currentEvent.getDuration();
                        //CATCH THE CASE WHEN THE EVENT STARTS BETWEEN THE START TIME AND DUE DATE AND ENDS AFTER THE DUE DATE
                    } else {
                        duration += currentEvent.getStartTime().until(end, MINUTES);
                    }
                }
            } else {
                if (currentEvent.getEndTime().until(start, MINUTES) > 0) {
                    //CATCH THE CASE WHEN THE EVENT STARTS BEFORE THE START TIME AND ENDS BEFORE THE DUE DATE
                    if (currentEvent.getEndTime().until(end, MINUTES) > 0) {
                        duration += start.until(currentEvent.getEndTime(), MINUTES);
                        //CATCH THE CASE WHEN THE EVENT STARTS BEFORE THE START TIME AND ENDS AFTER THE DUE DATE
                    } else {
                        duration += end.until(start, MINUTES);
                    }
                }
            }
        }
        return duration;
    }

    private static ArrayList slotting(ArrayList<EventClass> sortedEvents, List<EventClass> fixed_events, LocalDateTime Start_time) {
        //Initialize results
        ArrayList<EventClass> completedEvents = new ArrayList<>();

        //Check if flexible event list is empty
        if (sortedEvents.isEmpty()) {
            return completedEvents;
        }

        //Slotting and evening out the flexible events
        for (int i = 0; i < fixed_events.size(); i++) {
            //Initialize duration between fixed events and temp arraylist to store events
            //to be given start time and end time for that duration
            long duration;
            ArrayList<EventClass> temp = new ArrayList<>();

            //Break if all events within the sortedEvent list is checked
            if (allChecked(sortedEvents)) {
                break;
            }

            //Check for weird start time scenarios(Start time is after the start of the 1st fixed event):
            //Scenario 1: Start time is in the middle of the next fixed event (fixed event has a normal end time)
            //Solution: Set the start time to be the end time of the fixed event and skip
            //Scenario 2: Fixed event has the end time of in the middle of the night
            //Solution: Set the start time to be the next day's 8am and skip
            //Scenario 3: Fixed event has an end time which is before the start time
            //Solution: Keep the start time and skip
            //Check if start time is after the start of the next fixed event
            if (Start_time.until(fixed_events.get(i).getStartTime(), MINUTES) <= 0) {
                //Scenario 2
                if (fixed_events.get(i).getEndTime().getHour() < 8) {
                    Start_time = LocalDateTime.of(fixed_events.get(i).getEndTime().getYear(),
                            fixed_events.get(i).getEndTime().getMonth(),
                            fixed_events.get(i).getEndTime().getDayOfMonth(), 8, 0);
                }
                //Scenario 3
                else if (Start_time.until(fixed_events.get(i).getEndTime(), MINUTES) <= 0) {
                }
                //Scenario 1
                else {
                    Start_time = fixed_events.get(i).getEndTime();
                }
            }

            //For normal start time
            else {
                boolean reset = false;
                boolean reset_weekend = false;
                LocalDateTime reset_time = null;
                LocalDateTime reset_time_weekend = null;

                //Check if fixed event is the next day/in the middle of the night
                if (fixed_events.get(i).getStartTime().getDayOfMonth() > Start_time.getDayOfMonth()) {
                    //Check if fixed event is next week
                    //Set the duration until midnight for today
                    //Set the boolean to true so that later can set the start time to be next monday 8am
                    if (fixed_events.get(i).getStartTime().getDayOfMonth() - Start_time.getDayOfMonth() >= 2) {
                        reset_weekend = true;
                        reset_time_weekend = LocalDateTime.of(Start_time.getYear(), Start_time.getMonth(),
                                Start_time.getDayOfMonth() + 3, 8, 0);
                    }
                    //Set the boolean for the next day to true to set the start to be tomorrow 8am
                    else {
                        reset = true;
                        reset_time = LocalDateTime.of(Start_time.getYear(), Start_time.getMonth(),
                                Start_time.getDayOfMonth() + 1, 8, 0);
                    }
                    duration = Start_time.until(LocalDateTime.of(Start_time.getYear(), Start_time.getMonth(),
                            Start_time.getDayOfMonth() + 1, 0, 0), MINUTES);
                }
                //If everything is normal, set the duration to be from start time to the start of the fixed event
                else {
                    duration = Start_time.until(fixed_events.get(i).getStartTime(), MINUTES);
                }

                //Put events into temp list for evening process
                for (EventClass j : sortedEvents) {
                    //Check if the duration of the assignment can be fitted within the duration between start time
                    //and the start of the fixed event
                    //Slot as many assignments as possible within this period of time
                    if (j.getDuration() <= duration && !j.getChecked()) {
                        temp.add(j);
                        j.setChecked(true);
                        duration -= j.getDuration();
                    }
                }

                //Calculate the amount of free time per assignment and give them the free time if possible
                //E.g. with a 3hr slot and 2x 1hr assignment, each assignment has 30min free time
                //Thus there will be a 30min break in after each assignment
                for (EventClass j : temp) {
                    //afa stands for average free time per assignment
                    long afa = duration / temp.size();
                    j.setStartTime(Start_time);
                    j.setEndTime(j.getStartTime().plusMinutes(j.getDuration()));
                    completedEvents.add(j);
                    Start_time = j.getEndTime().plusMinutes(afa);
                }

                //Setting the start time based on the checks above
                //If the fixed event is in the next day set the start time to be 8am the next day
                //If the fixed event is in the weekend, set the start time to be 8am the next monday
                //Also re-check this fixed event by setting i-- if the fixed event cannot be reached
                if (fixed_events.get(i).getEndTime().getHour() < 8) {
                    Start_time = LocalDateTime.of(fixed_events.get(i).getEndTime().getYear(), fixed_events.get(i).getEndTime().getMonth(),
                            fixed_events.get(i).getEndTime().getDayOfMonth(), 8, 0);
                    i--;
                } else if (reset) {
                    Start_time = reset_time;
                    i--;
                } else if (reset_weekend) {
                    Start_time = reset_time_weekend;
                    i--;
                }
                //This is normal case and does not need re-checking of event
                else {
                    Start_time = fixed_events.get(i).getEndTime();
                }
            }
        }

        //If there is no fixed events / to fill in any other missed out flexible events
        for (EventClass i : sortedEvents) {
            if (!i.getChecked()) {
                if (Start_time.until(i.getDueDate(), MINUTES) < 0) {
                    return null;
                } else {
                    i.setChecked(true);
                    i.setStartTime(Start_time);
                    i.setEndTime(i.getStartTime().plusMinutes(i.getDuration()));
                    if (i.getEndTime().getHour() < 8) {
                        if (i.getEndTime().getDayOfWeek() == SATURDAY) {
                            i.setStartTime(LocalDateTime.of(i.getEndTime().getYear(), i.getEndTime().getMonth(),
                                    i.getEndTime().getDayOfMonth() + 2, 8, 0));
                            i.setEndTime(i.getStartTime().plusMinutes(i.getDuration()));
                        } else {
                            i.setStartTime(LocalDateTime.of(i.getEndTime().getYear(), i.getEndTime().getMonth(),
                                    i.getEndTime().getDayOfMonth(), 8, 0));
                            i.setEndTime(i.getStartTime().plusMinutes(i.getDuration()));
                        }
                    }
                    completedEvents.add(i);
                    Start_time = i.getEndTime();
                }
            }
        }
        return completedEvents;
    }

    //Deleting all flexible events from the allEvents list and from Firebase
    private static void deleteFlexible() {
        //Deleting all flexible events from allEvents list
        for (EventClass i : flexibleEvents) {
            if (check_name(allEvents, i.getName()) >= 0) {
                allEvents.remove(check_name(allEvents, i.getName()));
            }
            //Initializing Firebase references
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            final Query EventsQuery;
            EventsQuery = ref.child(LoginScreen.getUsername()).child("AllEvents").orderByChild("name").equalTo(i.getName());

            //Adding listener for Firebase to remove the events
            EventsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        //Removing the value and also when it is complete, add the new events to Firebase
                        eventSnapshot.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (!deleted) {
                                    for (EventClass i : completedEvents) {
                                        userAllEvent.push().setValue(new DatabaseEvent(i)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                added = true;
                                            }
                                        });
                                    }
                                }
                                deleted = true;
                            }
                        });
                    }
                    //Remove the listener afterwards
                    EventsQuery.removeEventListener(this);
                }


                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
        //Reset all checked values in the flexibleEvents list
        for (EventClass i : flexibleEvents) {
            i.setChecked(false);
        }

    }

    //Method to check if all events in the assignments are checked and assigned a timing
    private static boolean allChecked(List<EventClass> assignments) {
        for (EventClass i : assignments) {
            if (!i.getChecked()) {
                return false;
            }
        }
        return true;
    }

    //Optimization for group meetings, same as the above but requires the combination of all User's timetables
    public static String optimiseGroupMeeting(List<EventClass> combinedFixedEvents, ArrayList<EventClass> meetings, LocalDateTime Start_time, ArrayList<String> memberList) {
        //Use the slotting method to give timings to this group meeting
        ArrayList<EventClass> listOfOptimizedMeeting = slotting(meetings, combinedFixedEvents, Start_time);

        //Since there is only 1 group meeting, just get the only item in the list
        MeetingClass optimizedMeeting = (MeetingClass) listOfOptimizedMeeting.get(0);

        //Add the group meeting to all members through Firebase
        pushMeetingstoMembers(optimizedMeeting, memberList);
        allEvents.add(optimizedMeeting);
        return "done";
    }

    //For each group member, add the group meeting event in their Firebase
    private static void pushMeetingstoMembers(MeetingClass meetingEvent, ArrayList<String> memberList) {
        for (String memberName : memberList) {
            DatabaseReference userDatabaseRef = FirebaseDatabase.getInstance().getReference(memberName);
            DatabaseReference userFixedEvents = userDatabaseRef.child("FixedEvents");
            userFixedEvents.push().setValue(new DatabaseMeetingEvent(meetingEvent));
            userFixedEvents = userDatabaseRef.child("AllEvents");
            userFixedEvents.push().setValue(new DatabaseMeetingEvent(meetingEvent));
            userFixedEvents = userDatabaseRef.child("MeetingEvents");
            userFixedEvents.push().setValue(new DatabaseMeetingEvent(meetingEvent));
        }
    }

    //For each group member, delete the group meeting event in their Firebase
    public static void deleteMeetingstoMembers(final MeetingClass meetingEvent, ArrayList<String> memberList) {
        for (String memberName : memberList) {
            final DatabaseReference ref = FirebaseDatabase.getInstance().getReference(memberName);
            final Query EventsQuery;
            final Query EventsQuery2;
            final Query EventsQuery3;
            EventsQuery = ref.child("MeetingEvents").orderByChild("name").equalTo(meetingEvent.getName());
            EventsQuery2 = ref.child("FixedEvents").orderByChild("name").equalTo(meetingEvent.getName());
            EventsQuery3 = ref.child("AllEvents").orderByChild("name").equalTo(meetingEvent.getName());
            EventsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        eventSnapshot.getRef().removeValue();
                    }
                    EventsQuery.removeEventListener(this);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("hans", "onCancelled", databaseError.toException());
                }
            });
            EventsQuery2.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        eventSnapshot.getRef().removeValue();
                    }
                    EventsQuery2.removeEventListener(this);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
            EventsQuery3.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        eventSnapshot.getRef().removeValue();
                    }
                    EventsQuery3.removeEventListener(this);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }
}

