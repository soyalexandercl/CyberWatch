package com.cyberwatch.ui;

import com.cyberwatch.service.*;
import com.cyberwatch.util.Log;
import javax.swing.*;
import java.awt.*;
import java.io.File;

public class VentanaPrincipal extends JFrame {
    private JTextArea areaLogs;
    private Log logger;
    private GestorIntegridad gIntegridad;
    private GestorProcesos gProcesos;
    private GestorRed gRed;

    public VentanaPrincipal() {
        setTitle("CyberWatch");
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        areaLogs = new JTextArea();
        areaLogs.setEditable(false);
        logger = new Log(areaLogs);
        gRed = new GestorRed(logger);

        add(new JScrollPane(areaLogs), BorderLayout.CENTER);

        JPanel panelBotones = new JPanel();
        JButton btnIntegridad = new JButton("Monitorear Carpeta");
        JButton btnTrafico = new JButton("Analizar Red");
        JButton btnProcesos = new JButton("Simular Procesos");

        btnIntegridad.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                gIntegridad = new GestorIntegridad(chooser.getSelectedFile().getAbsolutePath(), logger);
                gIntegridad.start();
            }
        });

        btnTrafico.addActionListener(e -> {
            gRed.generarTrafico();
            gRed.analizarTrafico();
        });

        btnProcesos.addActionListener(e -> {
            gProcesos = new GestorProcesos(logger);
            gProcesos.start();
        });

        panelBotones.add(btnIntegridad);
        panelBotones.add(btnTrafico);
        panelBotones.add(btnProcesos);
        add(panelBotones, BorderLayout.NORTH);
    }
}