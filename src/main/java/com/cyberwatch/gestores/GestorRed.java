package com.cyberwatch.gestores;

import com.cyberwatch.util.Log;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GestorRed {

    private final Log log;
    private final String rutaTraffic = "logs/traffic.log";

    public GestorRed(Log log) {
        this.log = log;
    }

    public void crearTrafico() {
        Random r = new Random();
        log.registrarLinea("logs/log_sdas.txt", "[INFO] Creando IPs aleatorias");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaTraffic, true))) {
            for (int i = 0; i < 10; i++) {
                bw.write("[2024-01-01 12:00:0" + i + "] 192.168.1." + r.nextInt(10) + ":" + (r.nextBoolean() ? 4444 : 80));
                bw.newLine();
            }
        } catch (IOException e) {
        }
    }

    public void analizar() {
        log.registrarLinea("logs/log_sdas.txt", "[INFO] Gestor de tráfico iniciado");
        try (BufferedReader br = new BufferedReader(new FileReader(rutaTraffic))) {
            String l;
            List<String> ips = new ArrayList<>();
            while ((l = br.readLine()) != null) {
                if (l.contains(":4444")) {
                    log.registrarLinea("logs/log_sdas.txt", "[RED] Conexión sospechosa detectada: " + l);
                }
            }
        } catch (IOException e) {
        }
        log.registrarLinea("logs/log_sdas.txt", "[INFO] Gestor de tráfico detenido");
    }
}
