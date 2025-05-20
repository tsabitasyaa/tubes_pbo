/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package app;

import model.Event;
import model.User;
import persistence.CsvStore;
import service.Scheduler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.UUID;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


public class Main {

    private static final DateTimeFormatter F =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /* ---------- main entry ---------- */
    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        Scheduler sch = new Scheduler();

        /* ===== LOOP LOGIN / DAFTAR ===== */
        while (true) {
            System.out.println("===== LOGIN =====");
            System.out.print("[L]ogin / [D]aftar: ");
            String mode = sc.nextLine().trim().toUpperCase();

            if (mode.equals("D")) {                     // ----- DAFTAR -----
                System.out.print("Email baru   : ");
                String email = sc.nextLine().trim();
                if (CsvStore.findUserByEmail(email).isPresent()) {
                    System.out.println("Email sudah terdaftar!\n");
                    continue;
                }
                System.out.print("Password     : ");
                String pw = sc.nextLine().trim();
                System.out.print("Nama lengkap : ");
                String nama = sc.nextLine().trim();

                String id = "U" + UUID.randomUUID().toString().substring(0, 4);
                User baru = new User(id, email, pw, nama, "");
                CsvStore.appendUser(baru);
                System.out.println("Registrasi sukses! Silakan login.\n");
                continue;
            }

            if (mode.equals("L")) {                     // ----- LOGIN -----
                System.out.print("Email    : ");
                String email = sc.nextLine().trim();
                System.out.print("Password : ");
                String pw = sc.nextLine().trim();

                if (sch.authenticate(email, pw)) {
                    System.out.println("Selamat datang, "
                            + sch.currentUser().getName() + "!\n");
                    menuLoop(sc, sch);                  // → menu utama
                } else {
                    System.out.println("Email / password salah!\n");
                }
            }
        }
    }

    /* ---------- MENU UTAMA PER USER ---------- */
    private static void menuLoop(Scanner sc, Scheduler sch) throws IOException {
        while (true) {
            System.out.println("1) Lihat jadwal");
            System.out.println("2) Tambah jadwal");
            System.out.println("3) Lihat tim");
            System.out.println("4) Tambah tim");
            System.out.println("0) Logout");
            System.out.print("Pilih: ");
            switch (sc.nextLine().trim()) {
                case "1" -> viewSchedule(sch);
                case "2" -> addSchedule(sc, sch);
                case "3" -> listTeams(sc, sch);
                case "4" -> addTeam(sc, sch);
                case "0" -> { System.out.println("Logout...\n"); return; }
                default -> System.out.println("Pilihan tidak valid!\n");
            }
        }
    }

    /* ---------- TAMPILKAN JADWAL ---------- */
private static void viewSchedule(Scheduler sch) throws IOException {
    System.out.println("\n--- Jadwal ---");

    // Load semua tim ke map <teamId, teamName>
    Map<String, String> teamNames = new HashMap<>();
    for (String[] t : CsvStore.loadTeams()) {
        teamNames.put(t[0], t[1]);
    }

    for (Event e : sch.loadAllEvents()) {
        String teamLabel;
        if (e.getTeamEvent()) {
            String teamId = e.getTeamId();
            String teamName = teamNames.getOrDefault(teamId, "Unknown Team");
            teamLabel = String.format("TIM %s (%s)", teamId, teamName);
        } else {
            teamLabel = "PERSONAL";
        }

        System.out.printf("%s  %s  (%s)%n",
            e.getStart().format(F), e.getTitle(), teamLabel);
    }
    System.out.println();
}


    /* ---------- TAMBAH EVENT ---------- */
    private static void addSchedule(Scanner sc, Scheduler sch) throws IOException {
        System.out.print("Judul event : ");
        String title = sc.nextLine();
        System.out.print("Mulai (yyyy-MM-dd HH:mm): ");
        LocalDateTime start = LocalDateTime.parse(sc.nextLine(), F);
        System.out.print("Selesai (yyyy-MM-dd HH:mm): ");
        LocalDateTime end = LocalDateTime.parse(sc.nextLine(), F);

        System.out.print("Pribadi / Tim? (P/T): ");
        String kind = sc.nextLine().trim().toUpperCase();

        if (kind.equals("P")) {
            Event e = new Event(UUID.randomUUID().toString(), title,
                    start, end, false, null);
            sch.addPersonalEvent(e);
        } else {
            System.out.print("Masukkan teamId: ");
            String tid = sc.nextLine().trim();
            Event e = new Event(UUID.randomUUID().toString(), title,
                    start, end, true, tid);
            sch.addTeamEvent(tid, e);
        }
        System.out.println("Event berhasil ditambah!\n");
    }
    
    private static void viewTeams(Scheduler sch) throws IOException {
        var teams = CsvStore.loadTeams();
        System.out.println("\n--- Tim Anda ---");
        for (String tid : sch.currentUser().getTeamIds()) {
            teams.stream()
                 .filter(t -> t[0].equals(tid))
                 .findFirst()
                 .ifPresent(t -> {
                     System.out.printf("%s  %s  Anggota: %s%n",
                             t[0], t[1], t[2].replace("|", ", "));
                 });
        }
        System.out.println();
    }

    private static void addTeam(Scanner sc, Scheduler sch) throws IOException {
        System.out.print("Nama tim baru : ");
        String nama = sc.nextLine().trim();

        System.out.print("Email anggota (pisah koma, wajib termasuk Anda): ");
        String[] emails = sc.nextLine().trim().split("\\s*,\\s*");

        // validasi email → userId
        List<User> allUsers = CsvStore.loadUsers();
        List<User> anggota  = new ArrayList<>();
        for (String em : emails) {
            allUsers.stream()
                    .filter(u -> u.getEmail().equalsIgnoreCase(em))
                    .findFirst()
                    .ifPresentOrElse(anggota::add,
                         () -> System.out.println("! email tidak ditemukan: " + em));
        }
        if (anggota.isEmpty()) {
            System.out.println("Gagal: tidak ada anggota valid.\n");
            return;
        }

        String tid = "T" + UUID.randomUUID().toString().substring(0, 4);
        List<String> memberIds = anggota.stream().map(User::getId).toList();
        CsvStore.appendTeam(tid, nama, memberIds);

        /* update teamIds setiap user anggota */
        for (User u : anggota) {
            if (!u.getTeamIds().contains(tid)) u.getTeamIds().add(tid);
        }
        CsvStore.overwriteUsers(allUsers);

        System.out.println("Tim berhasil dibuat dengan ID " + tid + "\n");
    }

    /* ---------- DAFTAR TIM ---------- */
    private static void listTeams(Scanner sc, Scheduler sch) throws IOException {
        List<String[]> teams = CsvStore.loadTeams();
        List<String[]> owned = new ArrayList<>();

    int idx = 1;
    System.out.println("\n--- Daftar Tim ---");

    for (String tid : sch.currentUser().getTeamIds()) {
        // cari baris tim dengan id = tid
        String[] t = teams.stream()
                          .filter(row -> row[0].equals(tid))
                          .findFirst()
                          .orElse(null);

        if (t != null) {
            owned.add(t);
            System.out.printf("%d) %s%n", idx++, t[1]);   // tampilkan nama tim
        }
    }
        if (owned.isEmpty()) { System.out.println("(Tidak ada tim)\n"); return; }

        System.out.print("Pilih nomor tim (atau Enter untuk keluar): ");
        String sel = sc.nextLine().trim();
        if (sel.isBlank()) return;

        int n;
        try { n = Integer.parseInt(sel); }
        catch (NumberFormatException e){ System.out.println("Input salah.\n"); return; }

        if (n < 1 || n > owned.size()) { System.out.println("Nomor tidak ada.\n"); return; }

        showTeamDetail(sc, sch, owned.get(n-1));   // → detail
    }    
    
    /* ---------- DETAIL TIM ---------- */
    private static void showTeamDetail(Scanner sc, Scheduler sch, String[] team) throws IOException {
        var allUsers = CsvStore.loadUsers();
        System.out.printf("\n=== %s (%s) ===%n", team[1], team[0]);

        // tampilkan anggota
        System.out.println("Anggota:");
        List<String> memberIds = team[2].isBlank() ? List.of()
                                 : Arrays.asList(team[2].split("\\|"));
        memberIds.forEach(id -> allUsers.stream()
                          .filter(u -> u.getId().equals(id))
                          .findFirst()
                          .ifPresent(u -> System.out.println("- " + u.getEmail())));

        /* submenu */
        System.out.print("\n[T] Tambah anggota / [K] Kembali: ");
        String opt = sc.nextLine().trim().toUpperCase();
        if (opt.equals("T")) {
            addMembersToTeam(sc, team, allUsers);   // helper di bawah
        }
        // apapun selain T akan kembali ke daftar tim
        System.out.println();
        listTeams(sc, sch);        // kembali & refresh
    }

    private static void addMembersToTeam(Scanner sc, String[] team,
                                         List<User> allUsers) throws IOException
    {
        System.out.print("Email anggota baru (pisah koma): ");
        String[] emails = sc.nextLine().trim().split("\\s*,\\s*");

        List<User> baru = new ArrayList<>();
        for (String em : emails) {
            allUsers.stream()
                    .filter(u -> u.getEmail().equalsIgnoreCase(em))
                    .findFirst()
                    .ifPresentOrElse(baru::add,
                         () -> System.out.println("! email tidak ditemukan: " + em));
        }
        if (baru.isEmpty()) { System.out.println("Tidak ada anggota valid."); return; }

        List<String> newIds = baru.stream().map(User::getId).toList();
        CsvStore.addMembersToTeam(team[0], newIds);     // update teams.csv

        /* tambahkan teamId ke setiap user baru */
        for (User u : baru) {
            if (!u.getTeamIds().contains(team[0])) u.getTeamIds().add(team[0]);
        }
        CsvStore.overwriteUsers(allUsers);              // tulis ulang users.csv

        System.out.println("✅ Anggota berhasil ditambahkan.");
    }
}
