package com.cyberwatch.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class Log {

    private final JTextArea campoLogs;

    public Log(JTextArea campoLogs) {
        this.campoLogs = campoLogs;
    }

    public synchronized void registrarLinea(String nombreArchivo, String informacion) {
        String fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String informacionCompleta = "[" + fechaHora + "] " + informacion;

        try {
            // Asegurar que la carpeta logs existe
            Files.createDirectories(Paths.get("logs"));
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(nombreArchivo, true))) {
                bw.write(informacionCompleta);
                bw.newLine();
            }
            // Actualizar interfaz
            SwingUtilities.invokeLater(() -> campoLogs.append(informacionCompleta + "\n"));
        } catch (IOException e) {
            System.out.println("Error al escribir el archivo de log.");
        }
    }
}
