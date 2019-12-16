package com.example.onedee;

/** Data type to be passed on to database. Since you LocalDateTime has no "no arg constructor", we need
 * to parse the time as int and later reconstruct it to LocalDateTime when we retrieved the data*/
public class DatabaseEvent{
    public String name;
    public boolean flexible;
    public int start_year = 0;
    public int start_day = 1;
    public int start_month = 1;
    public int start_hour = 0;
    public int start_minute = 0;

    public int end_year = 0;
    public int end_day = 1;
    public int end_month = 1;
    public int end_hour = 0;
    public int end_minute = 0;

    public int due_year = 0;
    public int due_day = 1;
    public int due_month = 1;
    public int due_hour = 0;
    public int due_minute = 0;

    //    public long end;

    public long duration = 0; //in minutes
    public String difficulty = "";

    public DatabaseEvent(){
    }

    public int getDue_year() {
        return due_year;
    }

    public int getDue_day() {
        return due_day;
    }

    public int getDue_month() {
        return due_month;
    }

    public int getDue_hour() {
        return due_hour;
    }

    public int getDue_minute() {
        return due_minute;
    }

    public DatabaseEvent(EventClass eventClass){
        if (eventClass.getFlexible() == false){
            if(eventClass.getStartTime()!=null && eventClass.getEndTime()!=null) {
                start_year = eventClass.getStartTime().getYear();
                start_day = eventClass.getStartTime().getDayOfMonth();
                start_month = eventClass.getStartTime().getMonthValue();
                start_hour = eventClass.getStartTime().getHour();
                start_minute = eventClass.getStartTime().getMinute();

                end_year = eventClass.getEndTime().getYear();
                end_day = eventClass.getEndTime().getDayOfMonth();
                end_month = eventClass.getEndTime().getMonthValue();
                end_hour = eventClass.getEndTime().getHour();
                end_minute = eventClass.getEndTime().getMinute();
            }
            if(eventClass.getDueDate()!=null){
                due_year= eventClass.getDueDate().getYear();
                due_day = eventClass.getDueDate().getDayOfMonth();
                due_month = eventClass.getDueDate().getMonthValue();
                due_hour = eventClass.getDueDate().getHour();
                due_minute = eventClass.getDueDate().getMinute();
            }
        }

        /** handle fixed event, flexible event attributes*/
        if(eventClass.getDueDate() != null){
            if(eventClass.getFlexible() == true){
                due_year= eventClass.getDueDate().getYear();
                due_day = eventClass.getDueDate().getDayOfMonth();
                due_month = eventClass.getDueDate().getMonthValue();
                due_hour = eventClass.getDueDate().getHour();
                due_minute = eventClass.getDueDate().getMinute();
                if(eventClass.getChecked() == true){
                    start_year = eventClass.getStartTime().getYear();
                    start_day = eventClass.getStartTime().getDayOfMonth();
                    start_month = eventClass.getStartTime().getMonthValue();
                    start_hour = eventClass.getStartTime().getHour();
                    start_minute = eventClass.getStartTime().getMinute();

                    end_year= eventClass.getEndTime().getYear();
                    end_day = eventClass.getEndTime().getDayOfMonth();
                    end_month = eventClass.getEndTime().getMonthValue();
                    end_hour = eventClass.getEndTime().getHour();
                    end_minute = eventClass.getEndTime().getMinute();
                }
            }
        }

        name = eventClass.getName();
        difficulty = eventClass.getDifficulty();
        flexible = eventClass.getFlexible();
        duration = eventClass.getDuration();
    }

    /** getter methods require for retrieving data*/
    public String getName() {
        return name;
    }


    public String getDifficulty() {
        return difficulty;
    }

    public boolean getFlexible() {
        return flexible;
    }

    public int getStart_year() {
        return start_year;
    }

    public int getStart_day() {
        return start_day;
    }

    public int getStart_month() {
        return start_month;
    }

    public int getStart_hour() {
        return start_hour;
    }

    public int getStart_minute() {
        return start_minute;
    }

    public int getEnd_year() {
        return end_year;
    }

    public int getEnd_day() {
        return end_day;
    }

    public int getEnd_month() {
        return end_month;
    }

    public int getEnd_hour() {
        return end_hour;
    }

    public int getEnd_minute() {
        return end_minute;
    }

    public long getDuration() {
        return duration;
    }
}
