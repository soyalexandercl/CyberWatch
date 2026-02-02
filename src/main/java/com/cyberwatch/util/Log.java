package com.cyberwatch.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class Log {
    private JTextArea areaTexto;
    private String rutaArchivo = "log_sdas.txt";

    public Log(JTextArea areaTexto) {
        this.areaTexto = areaTexto;
    }

    public void registrar(String modulo, String mensaje) {
        String fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String lineaLog = "[" + fechaHora + "] [" + modulo.toUpperCase() + "] " + mensaje;

        System.out.println(lineaLog);

        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(rutaArchivo, true))) {
            escritor.write(lineaLog);
            escritor.newLine();
        } catch (IOException e) {
            System.out.println("Error al escribir log");
        }

        SwingUtilities.invokeLater(() -> {
            areaTexto.append(lineaLog + "\n");
        });
    }
}