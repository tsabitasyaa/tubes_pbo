package model;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private final String id;
    private String name;
    private final String adminId;
    private final List<String> memberIds = new ArrayList<>();

    // Konstruktor
    public Team(String id, String name, String adminId) {
        this.id = id;
        this.name = name;
        this.adminId = adminId;
    }

    // Selektor
    public String getTeamName(){
        return name;
    }
    
    public void addMember(String userId) {
        if (!memberIds.contains(userId)) memberIds.add(userId);
    }

}
