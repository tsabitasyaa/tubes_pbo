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

    // Menambahkan user baru ke CsvStore
    public void addUser(String email, String password, String nama) throws IOException {
        if (CsvStore.findUserByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email sudah terdaftar!");
        }
        
        String id = "U" + UUID.randomUUID().toString().substring(0, 4);
        User userBaru = new User(id, email, password, nama, "");
        CsvStore.appendUser(userBaru);
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

    // Memuat semua event dari CsvStore
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

    // Mendapatkan nama tim
    public Map<String, String> getTeamNames() throws IOException {
        Map<String, String> teamNames = new HashMap<>();
        for (String[] row : CsvStore.loadTeams()) {
            if (row.length >= 2) {
                teamNames.put(row[0], row[1]); // key: teamId, value: teamName
            }
        }
        return teamNames;
    }  

    // menambahkan event personal
    public void addPersonalEvent(String id, String title, 
                                 java.time.LocalDate date, 
                                 java.time.LocalTime time) throws IOException {
        PersonalEvent pe = new PersonalEvent(id, title, date, time);
        CsvStore.appendEventToUser(current.getId(), pe);
    }

    // Menambahkan event team
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

    // mendapatkan tim dari user
    public List<String[]> getUserTeams() throws IOException {
        List<String[]> result = new ArrayList<>();
        List<String[]> allTeams = CsvStore.loadTeams(); // raw team data

        for (String teamId : current.getTeamIds()) {
            for (String[] row : allTeams) {
                if (row.length >= 2 && row[0].equals(teamId)) {
                    result.add(row); // row[0]=id, row[1]=name, ...
                }
            }
        }
        return result;
    }    

    // menambahkan tim
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

        // Update current user
        Optional<User> updated = allUsers.stream()
                .filter(u -> u.getId().equals(current.getId()))
                .findFirst();
        updated.ifPresent(u -> this.current = u);
    }
    
}

