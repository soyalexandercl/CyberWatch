package com.cyberwatch.ui;

import com.cyberwatch.gestores.GestorIntegridad;
import com.cyberwatch.gestores.GestorRed;
import com.cyberwatch.gestores.GestorProcesos;
import com.cyberwatch.util.Log;
import javax.swing.*;
import java.awt.*;

public class VentanaPrincipal extends JFrame {

    private final JTextArea areaLogs;
    private final Log logger;

    public VentanaPrincipal() {
        setTitle("CyberWatch");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        areaLogs = new JTextArea();
        areaLogs.setEditable(false);
        areaLogs.setForeground(Color.BLUE);
        logger = new Log(areaLogs);

        add(new JScrollPane(areaLogs), BorderLayout.CENTER);

        JPanel panelBotones = new JPanel();
        JButton btnArchivos = new JButton("Monitorear Carpeta");
        JButton btnRed = new JButton("Analizar Red");
        JButton btnProcesos = new JButton("Simular Procesos");

        btnArchivos.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                new GestorIntegridad(fc.getSelectedFile().getAbsolutePath(), logger).start();
            }
        });

        btnRed.addActionListener(e -> {
            GestorRed gr = new GestorRed(logger);
            gr.crearTrafico();
            gr.analizar();
        });

        btnProcesos.addActionListener(e -> new GestorProcesos(logger).start());

        panelBotones.add(btnArchivos);
        panelBotones.add(btnRed);
        panelBotones.add(btnProcesos);
        add(panelBotones, BorderLayout.NORTH);
    }
}
