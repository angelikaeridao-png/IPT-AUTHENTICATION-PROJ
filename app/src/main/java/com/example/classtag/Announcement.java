package com.example.classtag;

public class Announcement {

    private final String title;
    private final String meta;

    public Announcement(String title, String meta) {
        this.title = title;
        this.meta = meta;
    }

    public String getTitle() {
        return title;
    }

    public String getMeta() {
        return meta;
    }
}
