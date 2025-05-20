/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package persistence;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import model.Event;
import model.User;

public class CsvStore {
    private static final Path DATA = Paths.get("data");
    private static final Path USERS = DATA.resolve("users.csv");
    private static final Path TEAMS = DATA.resolve("teams.csv");
     // id,nama,memberIds|pipa

    /* ---------- users ---------- */
    public static List<User> loadUsers() throws IOException {
        if (!Files.exists(USERS)) return List.of();

        try (BufferedReader br = Files.newBufferedReader(USERS)) {
            return br.lines()
                     .map(String::trim)
                     .filter(l -> !l.isBlank())          // skip baris kosong
                     .map(l -> l.split(",", -1))
                     .filter(t -> t.length >= 4)         // pastikan kolom minimal 4
                     .map(t -> new User(
                             t[0],                       // id
                             t[1],                       // email
                             t[2],                       // password
                             t[3],                       // nama
                             t.length > 4 ? t[4] : ""))  // teamIds (opsional)
                     .collect(Collectors.toList());
        }
    }

    public static Optional<User> findUserByEmail(String email) throws IOException {
        return loadUsers().stream()
                          .filter(u -> u.getEmail().equalsIgnoreCase(email))
                          .findFirst();
    }

    /* ---------- events ---------- */
    private static Path userEventFile(String userId){
        return DATA.resolve("events_" + userId + ".csv");
    }
    private static Path teamEventFile(String teamId){
        return DATA.resolve("events_team_" + teamId + ".csv");
    }

    public static List<Event> loadEventsForUser(String uid) throws IOException {
        Path p = userEventFile(uid);
        if (!Files.exists(p)) return List.of();
        try (BufferedReader br = Files.newBufferedReader(p)) {
            return br.lines().map(Event::fromCsv).collect(Collectors.toList());
        }
    }
    public static List<Event> loadEventsForTeam(String tid) throws IOException {
        Path p = teamEventFile(tid);
        if (!Files.exists(p)) return List.of();
        try (BufferedReader br = Files.newBufferedReader(p)) {
            return br.lines().map(Event::fromCsv).collect(Collectors.toList());
        }
    }

    public static void appendUser(model.User u) throws IOException {
        Files.createDirectories(DATA);
        try (BufferedWriter bw = Files.newBufferedWriter(
                USERS, StandardOpenOption.CREATE, StandardOpenOption.APPEND))
        {
            // format: userId,email,password,nama,teamIds
            String line = String.join(",", u.getId(),
                                       u.getEmail(),
                                       u.getPassword(),
                                       u.getName(),
                                       String.join("|", u.getTeamIds()));
            bw.write(line);
            bw.newLine();
        }
    }
    public static void appendEventToUser(String uid, Event e) throws IOException {
        Files.createDirectories(DATA);
        try (BufferedWriter bw = Files.newBufferedWriter(
                userEventFile(uid), StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            bw.write(e.toCsv());
            bw.newLine();
        }
    }
    public static void appendEventToTeam(String tid, Event e) throws IOException {
        Files.createDirectories(DATA);
        try (BufferedWriter bw = Files.newBufferedWriter(
                teamEventFile(tid), StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            bw.write(e.toCsv());
            bw.newLine();
        }
    }

    /* ---------- team helpers ---------- */
    public static List<String> loadTeamMembers(String teamId) throws IOException {
        if (!Files.exists(TEAMS)) return List.of();
        try (BufferedReader br = Files.newBufferedReader(TEAMS)) {
            return br.lines()
                     .map(l -> l.split(",", -1))
                     .filter(t -> t[0].equals(teamId))
                     .map(t -> t[2].split("\\|"))
                     .findFirst()
                     .map(arr -> Arrays.asList(arr))
                     .orElse(List.of());
        }
    }
    
    public static List<String[]> loadTeams() throws IOException {
        if (!Files.exists(TEAMS)) return List.of();
        try (BufferedReader br = Files.newBufferedReader(TEAMS)) {
            return br.lines()
                     .map(String::trim)
                     .filter(l -> !l.isBlank())
                     .map(l -> l.split(",", -1)) // [0]=id,[1]=nama,[2]=memberIds|
                     .collect(Collectors.toList());
        }
    }
    /* simpan tim baru */
    public static void appendTeam(String id, String name, List<String> memberIds) throws IOException {
        Files.createDirectories(DATA);
        try (BufferedWriter bw = Files.newBufferedWriter(
                TEAMS, StandardOpenOption.CREATE, StandardOpenOption.APPEND))
        {
            bw.write(String.join(",", id, name, String.join("|", memberIds)));
            bw.newLine();
        }
    }

    /* update kolom teamIds milik user (write‑all) */
    public static void overwriteUsers(List<User> newList) throws IOException {
        Files.createDirectories(DATA);
        try (BufferedWriter bw = Files.newBufferedWriter(USERS, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (User u : newList) {
                bw.write(String.join(",", u.getId(), u.getEmail(),
                                     u.getPassword(), u.getName(),
                                     String.join("|", u.getTeamIds())));
                bw.newLine();
            }
        }
    }
    
    public static void addMembersToTeam(String teamId, List<String> newMemberIds) throws IOException {
        List<String> lines = Files.readAllLines(TEAMS);
        List<String> updatedLines = new ArrayList<>();

        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length < 3) {
                updatedLines.add(line);
                continue;
            }

            if (parts[0].equals(teamId)) {
                String members = parts[2].trim();
                Set<String> memberSet = new LinkedHashSet<>();

                if (!members.isEmpty()) {
                    memberSet.addAll(Arrays.asList(members.split("\\|")));
                }
                memberSet.addAll(newMemberIds);

                // update kolom anggota (index 2)
                parts[2] = String.join("|", memberSet);

                String updatedLine = String.join(",", parts);
                updatedLines.add(updatedLine);
            } else {
                updatedLines.add(line);
            }
        }

        Files.write(TEAMS, updatedLines);
    }
    
    public static List<Event> loadUserEvents(String userId) throws IOException {
        Path file = DATA.resolve("events_" + userId + ".csv");
        if (!Files.exists(file)) return List.of();

        try (BufferedReader br = Files.newBufferedReader(file)) {
            return br.lines()
                     .map(String::trim)
                     .filter(l -> !l.isBlank())
                     .map(CsvStore::toEvent)   // helper di bawah
                     .toList();
        }
    }

    public static List<Event> loadTeamEvents(String teamId) throws IOException {
        Path file = DATA.resolve("events_team_" + teamId + ".csv");
        if (!Files.exists(file)) return List.of();

        try (BufferedReader br = Files.newBufferedReader(file)) {
            return br.lines()
                     .map(String::trim)
                     .filter(l -> !l.isBlank())
                     .map(CsvStore::toEvent)
                     .toList();
        }
    }

    /* ---- helper konversi satu baris CSV menjadi objek Event ---- */
    private static Event toEvent(String line) {
        // format: id,title,start,end,isTeam,teamId
        String[] t = line.split(",", -1);
        return new Event(
            t[0],
            t[1],
            LocalDateTime.parse(t[2]),
            LocalDateTime.parse(t[3]),
            Boolean.parseBoolean(t[4]),
            t[5].isBlank() ? null : t[5]
        );
    }
}


