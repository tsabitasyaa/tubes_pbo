package model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class PersonalEvent extends Event {

    // Konstruktor
    public PersonalEvent(String id, String title, LocalDate date, LocalTime time) {
        super(id, title, date, time);
    }

    @Override
    public boolean isTeamEvent() {
        return false;
    }

    @Override
    public String getTeamId() {
        return null;
    }

    @Override
    public String toCsv() {
        return String.join(",", id, title,
            date.format(DateTimeFormatter.ISO_LOCAL_DATE),
            time.format(DateTimeFormatter.ISO_LOCAL_TIME),
            "false", "");
    }
}
