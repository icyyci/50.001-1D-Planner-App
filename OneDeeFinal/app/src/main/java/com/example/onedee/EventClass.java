package com.example.onedee;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import static java.time.temporal.ChronoUnit.MINUTES;

/** This is the EventClass that we use to represent the user's event
 *  It has the start time, end time, duration, due date, name and flexibility
 **/

public class EventClass implements Comparable<EventClass>{
    //I know we should change all this to private since we got getter and setter
    public String name;
    public boolean flexible;
    public LocalDateTime startTime = null;
    public LocalDateTime endTime = null;
    public long duration=0; //in minutes
    public String difficulty = null;
    public LocalDateTime dueDate = null;
    public boolean checked = false;
    //But we left it as public for convenience during the coding

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getFlexible() {
        return flexible;
    }

    public void setFlexible(boolean flexible) {
        this.flexible = flexible;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    @Override
    public int compareTo(EventClass e) {
        return this.dueDate.compareTo(e.dueDate);
    }

    @Override
    public String toString(){
        if(flexible){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            String formatDateTime = dueDate.format(formatter);

            String s = "Title: "+name+"\n";
            s+="Duration: ";
            if(duration>=60){
                long hours = duration/60;
                long minutes = duration%60;
                s+=hours+" h "+minutes+" min\n";
            }
            else{s+=duration+" minutes\n";}
            s+="Deadline: "+formatDateTime+"\n";
            s+="Difficulty: "+difficulty;
            return s;
        }
        else{return this.name;}
    }

    public String ShowAllStats(){
        return "Start Time: " + this.startTime + " End Time: " + this.endTime + " name: " + this.name + " duration: " + this.duration + " flexible: " + this.flexible + " difficulty: " + this.difficulty + " due date: " + this.dueDate + " checked: " + this.checked;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean getChecked(){
        return this.checked;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public long getDuration() {
        return duration;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public EventClass() {
        this.name = null;
    }
    // The constructor for our EventClass... We should have used Static Factory method.... or maybe a Builder
    public EventClass(String name, boolean flexible, LocalDateTime startTime, LocalDateTime endTime, String difficulty, LocalDateTime dueDate, long duration){
        this.name = name;
        this.flexible = flexible;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.difficulty = difficulty;
        this.dueDate = dueDate;

    }

    public EventClass(String name, boolean flexible, long duration, String difficulty, LocalDateTime dueDate){
        this.name = name;
        this.flexible=flexible;
        this.duration = duration;
        this.difficulty=difficulty;
        this.dueDate=dueDate;
    }

    public void editEvent(boolean flexible, LocalDateTime startTime, long duration, String difficulty, LocalDateTime dueDate) {
        if (!flexible) {
            this.flexible = false;
            this.startTime = startTime;
            this.duration = duration;
            this.endTime = startTime.plusMinutes(duration);
        } else if (flexible) {
            this.flexible = true;
            this.duration = duration;
            this.difficulty = difficulty;
            this.dueDate = dueDate;
        }


    }




}
