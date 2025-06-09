package model;

import java.time.LocalDate;
import java.time.LocalTime;

public abstract class Event {
    protected final String id;
    protected String title;
    protected LocalDate date;
    protected LocalTime time;

    // konstruktor
    public Event(String id, String title, LocalDate date, LocalTime time) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.time = time;
    }

    // Selektor
    public String getId() { 
        return id; 
    }
    public String getTitle() { 
        return title; 
    }
    public LocalDate getDate() { 
        return date;
    }
    public LocalTime getTime() { 
        return time; 
    }

    // Metode polimorfik
    public abstract boolean isTeamEvent();
    public abstract String getTeamId();
    public abstract String toCsv();

    public static Event fromCsv(String line) {
        String[] t = line.split(",", -1);
        String id = t[0];
        String title = t[1];
        LocalDate date = LocalDate.parse(t[2]);
        LocalTime time = LocalTime.parse(t[3]);
        boolean isTeam = Boolean.parseBoolean(t[4]);
        String teamId = t[5].isBlank() ? null : t[5];

        return isTeam
            ? new TeamEvent(id, title, date, time, teamId)
            : new PersonalEvent(id, title, date, time);
    }
}
