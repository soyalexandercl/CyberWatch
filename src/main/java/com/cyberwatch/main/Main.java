package com.cyberwatch.main;

import com.cyberwatch.ui.VentanaPrincipal;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal v = new VentanaPrincipal();
            v.setLocationRelativeTo(null);
            v.setVisible(true);
        });
    }
}
