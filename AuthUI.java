/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package app;

/**
 *
 * @author tsabi
 */
import service.Scheduler;
import persistence.CsvStore;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.util.UUID;
import java.util.Optional;
import java.io.IOException;

public class AuthUI extends JFrame {

    private final CardLayout cards = new CardLayout();
    private final JPanel mainPanel = new JPanel(cards);
    private final Scheduler sch = new Scheduler();

    public AuthUI() {
        setTitle("Scheduler");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(360, 640));   // ukuran ponsel
        setResizable(false);

        /* -------- style helper -------- */
        int fieldW = 300, fieldH = 32;
        Font font = new Font("SansSerif", Font.PLAIN, 14);

        /* ========== LOGIN ========== */
        JPanel login = new JPanel();
        login.setLayout(new BoxLayout(login, BoxLayout.Y_AXIS));
        login.setBorder(BorderFactory.createEmptyBorder(40, 20, 20, 20));

        JTextField inEmailL = new JTextField();   stylize(inEmailL, fieldW, fieldH, font);
        JPasswordField inPassL = new JPasswordField(); stylize(inPassL, fieldW, fieldH, font);
        JButton btnLogin = new JButton("Login"); stylize(btnLogin, fieldW, 40, font);
        JLabel linkReg = linkLabel("Belum punya akun? Daftar");

        login.add(makeTitle("LOGIN"));
        login.add(Box.createVerticalStrut(30));
        login.add(center(new JLabel("Email")));    login.add(inEmailL);
        login.add(Box.createVerticalStrut(10));
        login.add(center(new JLabel("Password"))); login.add(inPassL);
        login.add(Box.createVerticalStrut(20));
        login.add(btnLogin);
        login.add(Box.createVerticalStrut(10));
        login.add(linkReg);

        /* ========== REGISTER ========== */
        JPanel reg = new JPanel();
        reg.setLayout(new BoxLayout(reg, BoxLayout.Y_AXIS));
        reg.setBorder(BorderFactory.createEmptyBorder(40, 20, 20, 20));

        JTextField inEmailR = new JTextField();   stylize(inEmailR, fieldW, fieldH, font);
        JPasswordField inPassR = new JPasswordField(); stylize(inPassR, fieldW, fieldH, font);
        JTextField inNameR = new JTextField();    stylize(inNameR, fieldW, fieldH, font);
        JButton btnReg = new JButton("Daftar");   stylize(btnReg, fieldW, 40, font);
        JLabel linkLogin = linkLabel("Sudah punya akun? Login");

        reg.add(makeTitle("DAFTAR"));
        reg.add(Box.createVerticalStrut(30));
        reg.add(center(new JLabel("Email")));    reg.add(inEmailR);
        reg.add(Box.createVerticalStrut(10));
        reg.add(center(new JLabel("Password"))); reg.add(inPassR);
        reg.add(Box.createVerticalStrut(10));
        reg.add(center(new JLabel("Nama Lengkap"))); reg.add(inNameR);
        reg.add(Box.createVerticalStrut(20));
        reg.add(btnReg);
        reg.add(Box.createVerticalStrut(10));
        reg.add(linkLogin);

        /* ========== add to cards ========== */
        mainPanel.add(login, "login");
        mainPanel.add(reg, "reg");
        add(mainPanel);

        /* ========== listeners ========== */
        linkReg.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                cards.show(mainPanel, "reg");
            }
        });
        linkLogin.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                cards.show(mainPanel, "login");
            }
        });

    btnLogin.addActionListener(e -> {
        String email = inEmailL.getText().trim();
        String pass  = String.valueOf(inPassL.getPassword()).trim();

        if (email.isBlank() || pass.isBlank()) {
            JOptionPane.showMessageDialog(this,
                    "Email dan password wajib diisi!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (sch.authenticate(email, pass)) {
            new MenuUI(sch).setVisible(true);  // buka halaman menu
            dispose();                         // tutup layar login
        } else {
            JOptionPane.showMessageDialog(this,
                    "Email atau password salah!",
                    "Login Gagal", JOptionPane.ERROR_MESSAGE);
        }
    });

    btnReg.addActionListener(e -> {
        String email = inEmailR.getText().trim();
        String pass  = String.valueOf(inPassR.getPassword()).trim();
        String name  = inNameR.getText().trim();

        if (email.isBlank() || pass.isBlank() || name.isBlank()) {
            JOptionPane.showMessageDialog(this,
                    "Semua kolom wajib diisi!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // cek email sudah ada?
            if (CsvStore.findUserByEmail(email).isPresent()) {
                JOptionPane.showMessageDialog(this,
                        "Email sudah terdaftar. Silakan login.",
                        "Gagal", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String id = "U" + UUID.randomUUID().toString().substring(0,4);
            User u = new User(id, email, pass, name, "");   // belum ada tim
            CsvStore.appendUser(u);
            
            JOptionPane.showMessageDialog(this,
                    "Registrasi sukses! Silakan login.",
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);

            // otomatis kembali ke panel login & isi email
            cards.show(mainPanel, "login");
            inEmailL.setText(email);
            inPassL.setText("");
            inPassR.setText("");
            inNameR.setText("");

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Terjadi kesalahan penyimpanan: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    });

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /* ---------- util ---------- */
    private static void stylize(JComponent c, int w, int h, Font f) {
        c.setMaximumSize(new Dimension(w, h));
        c.setAlignmentX(Component.CENTER_ALIGNMENT);
        c.setFont(f);
        if (c instanceof JButton b) b.setFocusPainted(false);
    }
    private static JLabel linkLabel(String text) {
        JLabel l = new JLabel("<html><u>" + text + "</u></html>");
        l.setForeground(new Color(0x0066CC));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }
    private static Component center(JLabel l) {
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }
    private static Component center(JLabel l, int align, Font f) {
        l.setHorizontalAlignment(align);
        l.setFont(f);
        return center(l);
    }
    
    private static JLabel makeTitle(String text) {
    JLabel lbl = new JLabel(text);
    lbl.setFont(new Font("SansSerif", Font.BOLD, 18));
    lbl.setHorizontalAlignment(SwingConstants.CENTER);
    lbl.setAlignmentX(Component.CENTER_ALIGNMENT);   // untuk BoxLayout
    return lbl;
}


    public static void main(String[] args) {
        SwingUtilities.invokeLater(AuthUI::new);
    }
}

