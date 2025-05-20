/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author tsabi
 */

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Event {
    private final String id;
    private String title;
    private LocalDateTime start;
    private LocalDateTime end;
    private boolean teamEvent;
    private String teamId; // null if personal

    public Event(String id, String title, LocalDateTime start, LocalDateTime end, boolean teamEvent, String teamId) {
        this.id = id;
        this.title = title;
        this.start = start;
        this.end = end;
        this.teamEvent = teamEvent;
        this.teamId = teamId;
    }
    
        // --- GETTER ---
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }
    
    public boolean getTeamEvent(){
        return teamEvent;
    }
    
    public String getTeamId() {
        return teamId;
    }

    public Event(Event other) { // copy
        this(other.id, other.title, other.start, other.end, other.teamEvent, other.teamId);
    }

    public String toCsv() {
        return String.join(",",
            id, title,
            start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            String.valueOf(teamEvent),
            teamId == null ? "" : teamId);
    }

    public static Event fromCsv(String line) {
        String[] t = line.split(",", -1);
        return new Event(
            t[0], t[1],
            LocalDateTime.parse(t[2]),
            LocalDateTime.parse(t[3]),
            Boolean.parseBoolean(t[4]),
            t[5].isBlank() ? null : t[5]);
    }
}