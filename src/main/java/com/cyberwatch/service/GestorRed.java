package com.cyberwatch.service;

import com.cyberwatch.util.Log;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GestorRed {
    private Log log;
    private String rutaTraffic = "traffic.log";

    public GestorRed(Log log) {
        this.log = log;
    }

    public void generarTrafico() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaTraffic))) {
            Random ran = new Random();
            String[] ips = {"192.168.1.10", "10.0.0.5", "172.16.0.1"};
            int i = 0;
            while (i < 20) {
                String ip = ips[ran.nextInt(ips.length)];
                int puerto = (i % 7 == 0) ? 4444 : 80; // Simular puerto sospechoso
                bw.write("IP:" + ip + " Puerto:" + puerto + " Time:" + System.currentTimeMillis());
                bw.newLine();
                i++;
            }
            log.registrar("RED", "Archivo traffic.log generado.");
        } catch (IOException e) {}
    }

    public void analizarTrafico() {
        log.registrar("RED", "Iniciando análisis de tráfico...");
        try (BufferedReader br = new BufferedReader(new FileReader(rutaTraffic))) {
            String linea;
            List<String> ipsDetectadas = new ArrayList<>();
            while ((linea = br.readLine()) != null) {
                if (linea.contains("Puerto:4444") || linea.contains("Puerto:31337")) {
                    log.registrar("RED", "Puerto sospechoso detectado en línea: " + linea);
                }
                
                // Análisis simple de IP repetida
                String ip = linea.split(" ")[0];
                ipsDetectadas.add(ip);
                int contador = 0;
                int j = 0;
                while (j < ipsDetectadas.size()) {
                    if (ipsDetectadas.get(j).equals(ip)) contador++;
                    j++;
                }
                if (contador > 5) {
                    log.registrar("RED", "IP con múltiples conexiones consecutivas: " + ip);
                }
            }
        } catch (IOException e) {
            log.registrar("RED", "Error al leer tráfico.");
        }
    }
}