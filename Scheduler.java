/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import model.Event;
import model.User;
import persistence.CsvStore;

import java.io.IOException;
import java.util.*;
import java.util.List;


public class Scheduler {
    private User current;

    public boolean authenticate(String email, String pwd) {
        try {
            Optional<User> opt = CsvStore.findUserByEmail(email);
            if (opt.isPresent() && pwd.equals(opt.get().getPassword())) {   // plain compare
                current = opt.get();
                return true;
            }
        } catch (IOException ignored) {}
        return false;
    }

    public User currentUser(){ return current; }

    public List<Event> loadAllEvents() throws IOException {
        Map<String, Event> map = new LinkedHashMap<>();

        // event pribadi
        for (Event e : CsvStore.loadUserEvents(current.getId())) {
            map.put(e.getId(), e);
        }

        // event tim
        for (String tid : current.getTeamIds()) {
            for (Event e : CsvStore.loadTeamEvents(tid)) {
                map.putIfAbsent(e.getId(), e);
            }
        }

        return map.values()
                  .stream()
                  .sorted(Comparator.comparing(Event::getStart))
                  .toList();
    }



    public void addPersonalEvent(Event e) throws IOException {
        CsvStore.appendEventToUser(current.getId(), e);
    }
    public void addTeamEvent(String teamId, Event e) throws IOException {
        CsvStore.appendEventToTeam(teamId, e);
        for (String member : CsvStore.loadTeamMembers(teamId)) {
            Event copy = new Event(e.getId(), e.getTitle(), e.getStart(), e.getEnd(), true, teamId);
            CsvStore.appendEventToUser(member, copy);
        }
    }

}

