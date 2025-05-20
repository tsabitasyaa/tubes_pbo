/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author tsabi
 */

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Team {
    private final String id;
    private String name;
    private final List<String> memberIds = new ArrayList<>();

    public Team(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public void addMember(String userId) {
        if (!memberIds.contains(userId)) memberIds.add(userId);
    }

    public String getTeamName(){
        return name;
    }
    // other getters / setters
}
