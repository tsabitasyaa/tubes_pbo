/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package app;

import service.Scheduler;

import javax.swing.*;
import java.awt.*;

public class MenuUI extends JFrame {

    private final Scheduler sch;

    public MenuUI(Scheduler scheduler) {
        this.sch = scheduler;

        setTitle("Scheduler – Menu");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(360, 640));
        setResizable(false);

        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        pane.setBorder(BorderFactory.createEmptyBorder(40, 20, 20, 20));

        Font btnFont = new Font("SansSerif", Font.BOLD, 16);

        JLabel lblWelcome = new JLabel("Halo, " + sch.currentUser().getName());
        lblWelcome.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblWelcome.setFont(new Font("SansSerif", Font.PLAIN, 18));

        JButton btnProfile = makeBigButton("Lihat Profil", btnFont);
        JButton btnSchedule = makeBigButton("Lihat Jadwal", btnFont);
        JButton btnTeams = makeBigButton("Lihat Tim", btnFont);
        JButton btnLogout = makeBigButton("Logout", btnFont);
        
        pane.add(lblWelcome);
        pane.add(Box.createVerticalStrut(40));
        pane.add(btnProfile);
        pane.add(Box.createVerticalStrut(20));
        pane.add(btnSchedule);
        pane.add(Box.createVerticalStrut(20));
        pane.add(btnTeams);
        pane.add(Box.createVerticalStrut(40));
        pane.add(btnLogout);

        /* ---- listeners ---- */
        btnProfile.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "Email : " + sch.currentUser().getEmail() + "\n"
                  + "Nama  : " + sch.currentUser().getName(),
                    "Profil", JOptionPane.INFORMATION_MESSAGE);
        });

//        btnSchedule.addActionListener(e -> {
//            new ScheduleUI(sch).setVisible(true); // buat layar jadwal
//            dispose();                            // tutup menu
//        });
//
//        btnTeams.addActionListener(e -> {
//            new TeamUI(sch).setVisible(true);     // buat layar tim
//            dispose();
//        });
        btnLogout.addActionListener(e -> {
            new AuthUI().setVisible(true);
            dispose();
        });
        
        add(pane);
        pack();
        setLocationRelativeTo(null);
    }

    private static JButton makeBigButton(String text, Font f) {
        JButton b = new JButton(text);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(280, 50));
        b.setFont(f);
        b.setFocusPainted(false);
        return b;
    }
}