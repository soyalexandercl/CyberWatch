package com.cyberwatch.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JTextArea;

public class Log {
    
    private final JTextArea campoLogs;
    
    public Log(JTextArea campoLogs) {
        this.campoLogs = campoLogs;
    }

    // Escribir un evento tanto en el archivo de texto como en la pantalla
    public synchronized void registrarLinea(String nombreArchivo, String informacion) {
        String fechaHora = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(nombreArchivo, true))) {
            String informacionCompleta = "[" + fechaHora + "] " + informacion;
            bw.write(informacionCompleta);
            bw.newLine();

            // Actualizar el área de texto en la interfaz gráfica
            javax.swing.SwingUtilities.invokeLater(() -> { campoLogs.append(informacionCompleta + "\n"); });
        } catch (IOException e) {
            System.out.println("Error al escribir el archivo.");
        }
    }
}