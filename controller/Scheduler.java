/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import model.Event;
import model.PersonalEvent;
import model.TeamEvent;
import model.User;
import persistence.CsvStore;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;


public class Scheduler {
    private User current;
    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // Konstruktor
    public Scheduler(User u) { 
        this.current= u; 
    }
    
    // Selektor
    public User getUser(){
        return current;
    }  
    
    // Autentikasi Login User
    public static User authenticate(String email, String password) {
        try {
            for (User u : CsvStore.loadUsers()) {
                if (u.getEmail().equalsIgnoreCase(email) && u.getPassword().equals(password)) {
                    return u;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


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
                  .sorted(Comparator.comparing(Event::getDate))
                  .toList();
    }

    public void addPersonalEvent(String id, String title, 
                                 java.time.LocalDate date, 
                                 java.time.LocalTime time) throws IOException {
        PersonalEvent pe = new PersonalEvent(id, title, date, time);
        CsvStore.appendEventToUser(current.getId(), pe);
    }

    public void addTeamEvent(String teamId, String id, String title,
                              java.time.LocalDate date, 
                              java.time.LocalTime time) throws IOException {
        TeamEvent te = new TeamEvent(id, title, date, time, teamId);
        CsvStore.appendEventToTeam(teamId, te);
        for (String memberId : CsvStore.loadTeamMembers(teamId)) {
            // Salin event untuk setiap anggota tim
            TeamEvent copy = new TeamEvent(id, title, date, time, teamId);
            CsvStore.appendEventToUser(memberId, copy);
        }
    }
    
    public void setCurrentUser(User updatedUser) {
    this.current = updatedUser;
    }
    
    public void addTeam(String teamName, List<String> memberEmails) throws IOException {
        // Ambil semua user dari CSV
        List<User> allUsers = CsvStore.loadUsers();
        List<User> anggota = new ArrayList<>();

        // Validasi email dan cari usernya
        for (String em : memberEmails) {
            allUsers.stream()
                    .filter(u -> u.getEmail().equalsIgnoreCase(em))
                    .findFirst()
                    .ifPresentOrElse(anggota::add,
                        () -> System.out.println("! email tidak ditemukan: " + em));
        }

        if (anggota.isEmpty()) {
            throw new IllegalArgumentException("Gagal: tidak ada anggota valid.");
        }

        // Generate ID tim
        String tid = "T" + UUID.randomUUID().toString().substring(0, 4);
        List<String> memberIds = anggota.stream().map(User::getId).toList();

        // Simpan tim dan anggota ke file CSV
        CsvStore.appendTeam(tid, teamName, current.getId(), memberIds);

        // Update data user (masukkan id tim ke list teamIds)
        for (User u : anggota) {
            if (!u.getTeamIds().contains(tid)) {
                u.getTeamIds().add(tid);
            }
        }

        // Simpan ulang semua user
        CsvStore.overwriteUsers(allUsers);

        // Update current user agar tim langsung terbaca di sesi ini
        Optional<User> updated = allUsers.stream()
                .filter(u -> u.getId().equals(current.getId()))
                .findFirst();
        updated.ifPresent(u -> this.current = u);
    }   
    
}

