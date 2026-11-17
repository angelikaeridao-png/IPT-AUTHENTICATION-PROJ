package com.example.classtag;

public class SubjectTag {

    private final String name;
    private final String topicsCount;
    private final int accentColorRes;

    public SubjectTag(String name, String topicsCount, int accentColorRes) {
        this.name = name;
        this.topicsCount = topicsCount;
        this.accentColorRes = accentColorRes;
    }

    public String getName() {
        return name;
    }

    public String getTopicsCount() {
        return topicsCount;
    }

    public int getAccentColorRes() {
        return accentColorRes;
    }
}
