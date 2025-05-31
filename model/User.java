/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author tsabi
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class User {
    private final String id;
    private final String email;
    private String password;
    private String name;
    private List<String> teamIds = new ArrayList<>();

    // Konstruktor
    public User(String id, String email, String password, String name, String teamsPipe) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        if (!teamsPipe.isBlank()) {
            teamIds.addAll(Arrays.asList(teamsPipe.split("\\|")));
        }
    }

    // Selektor
    public String getId() { 
        return id; 
    }
    
    public String getEmail() { 
        return email; 
    }
    
    public String getPassword() { 
        return password; 
    }
    
    public String getName() { 
        return name; 
    }
    
    public List<String> getTeamIds() { 
        return teamIds; 
    }
}
