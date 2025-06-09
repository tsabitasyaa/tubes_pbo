package persistence;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import model.Event;
import model.User;

public class CsvStore {
    private static final Path DATA = Paths.get("data");
    private static final Path USERS = DATA.resolve("users.csv");
    private static final Path TEAMS = DATA.resolve("teams.csv");

    // Menyimpan user
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

    // Memuat user
    public static List<User> loadUsers() throws IOException {
        if (!Files.exists(USERS)) return List.of();

        try (BufferedReader br = Files.newBufferedReader(USERS)) {
            return br.lines()
                     .map(String::trim)
                     .filter(l -> !l.isBlank())
                     .map(l -> l.split(",", -1))
                     .filter(t -> t.length >= 4)
                     .map(t -> new User(
                             t[0],                       // id
                             t[1],                       // email
                             t[2],                       // password
                             t[3],                       // nama
                             t.length > 4 ? t[4] : ""))  // teamIds (opsional)
                     .collect(Collectors.toList());
        }
    }

    // Mencari user berdasarkan email
    public static Optional<User> findUserByEmail(String email) throws IOException {
        return loadUsers().stream()
                          .filter(u -> u.getEmail().equalsIgnoreCase(email))
                          .findFirst();
    }

    // Membuat file CSV event user
    private static Path userEventFile(String userId){
        return DATA.resolve("events_" + userId + ".csv");
    }

    // Membuat file CSV event tim
    private static Path teamEventFile(String teamId){
        return DATA.resolve("events_team_" + teamId + ".csv");
    }

    // Menyimpan event ke user event file
    public static void appendEventToUser(String uid, Event e) throws IOException {
        Files.createDirectories(DATA);
        try (BufferedWriter bw = Files.newBufferedWriter(
                userEventFile(uid), StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            bw.write(e.toCsv());
            bw.newLine();
        }
    }

    // Menyimpan event ke team event file
    public static void appendEventToTeam(String tid, Event e) throws IOException {
        Files.createDirectories(DATA);
        try (BufferedWriter bw = Files.newBufferedWriter(
                teamEventFile(tid), StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            bw.write(e.toCsv());
            bw.newLine();
        }
    }

    // Memuat event user
    public static List<Event> loadUserEvents(String userId) throws IOException {
        Path file = DATA.resolve("events_" + userId + ".csv");
        if (!Files.exists(file)) return List.of();

        try (BufferedReader br = Files.newBufferedReader(file)) {
            return br.lines()
                     .map(String::trim)
                     .filter(l -> !l.isBlank())
                     .map(Event::fromCsv)   // helper di bawah
                     .toList();
        }
    }

    // Memuat event tim
    public static List<Event> loadTeamEvents(String teamId) throws IOException {
        Path file = DATA.resolve("events_team_" + teamId + ".csv");
        if (!Files.exists(file)) return List.of();

        try (BufferedReader br = Files.newBufferedReader(file)) {
            return br.lines()
                     .map(String::trim)
                     .filter(l -> !l.isBlank())
                     .map(Event::fromCsv)
                     .toList();
        }
    }

    // Menyimpan tim
    public static void appendTeam(String id, String name, String adminId, List<String> memberIds) throws IOException {
        Files.createDirectories(DATA);
        try (BufferedWriter bw = Files.newBufferedWriter(
                TEAMS, StandardOpenOption.CREATE, StandardOpenOption.APPEND))
        {
            // Format: teamId,teamName,adminId,member1|member2|...
            bw.write(String.join(",", id, name, adminId, String.join("|", memberIds)));
            bw.newLine();
        }
    }

    // Memuat tim
    public static List<String[]> loadTeams() throws IOException {
       if (!Files.exists(TEAMS)) return List.of();
       try (BufferedReader br = Files.newBufferedReader(TEAMS)) {
           return br.lines()
                    .map(String::trim)
                    .filter(l -> !l.isBlank())
                    .map(l -> l.split(",", 4)) // [0]=id, [1]=name, [2]=adminId, [3]=memberIds
                    .collect(Collectors.toList());
       }
    }

    // Memuat anggota tim
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

    // update kolom teamIds milik user
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
}
