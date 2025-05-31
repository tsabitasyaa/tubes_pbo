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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TeamEvent extends Event {
    private final String teamId;

    // Konstruktor
    public TeamEvent(String id, String title, LocalDate date, LocalTime time, String teamId) {
        super(id, title, date, time);
        this.teamId = teamId;
    }
    
    @Override
    public String getTeamId() {
        return teamId;
    }    

    @Override
    public boolean isTeamEvent() {
        return true;
    }

    @Override
    public String toCsv() {
        return String.join(",", id, title,
            date.format(DateTimeFormatter.ISO_LOCAL_DATE),
            time.format(DateTimeFormatter.ISO_LOCAL_TIME),
            "true", teamId);
    }
}
