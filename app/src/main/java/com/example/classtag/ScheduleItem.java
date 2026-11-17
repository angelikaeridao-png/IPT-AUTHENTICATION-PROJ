package com.example.classtag;

public class ScheduleItem {

    private final String time;
    private final String subject;
    private final String room;
    private final int accentColorRes;

    public ScheduleItem(String time, String subject, String room, int accentColorRes) {
        this.time = time;
        this.subject = subject;
        this.room = room;
        this.accentColorRes = accentColorRes;
    }

    public String getTime() {
        return time;
    }

    public String getSubject() {
        return subject;
    }

    public String getRoom() {
        return room;
    }

    public int getAccentColorRes() {
        return accentColorRes;
    }
}
