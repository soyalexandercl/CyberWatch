package util;

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

    private final JTextArea areaTextoLogs;

    public Log(JTextArea areaTextoLogs) {
        this.areaTextoLogs = areaTextoLogs;
    }

    /* El synchronized se utiliza porque se va a trabajar con hilos; este sirve para que 
       trabajen uno a uno y no al tiempo para evitar errores de concurrencia. */
    public synchronized void registrarLinea(String rutaArchivo, String mensaje) {
        
        /* Esto es para acceder a la fecha y hora actual con el formato indicado */
        String marcaTiempo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        /* Aquí se construye la información tal cual la pide el formato del proyecto */
        String lineaCompleta = "[" + marcaTiempo + "] " + mensaje;

        /* El try sirve para manejar los errores y evitar que el programa finalice bruscamente */
        try {
            /* Crea el directorio de logs si no existe actualmente */
            Files.createDirectories(Paths.get("logs")); 

            /* Se crea un buffer de escritura; el parámetro 'true' indica que se agrega contenido 
               al final en lugar de sobreescribir el archivo */
            try (BufferedWriter escritor = new BufferedWriter(new FileWriter(rutaArchivo, true))) {
                escritor.write(lineaCompleta);
                /* Crea un salto de línea para que cada registro sea independiente */
                escritor.newLine();
            }

            /* Se utiliza para actualizar de forma segura la interfaz gráfica desde otros hilos */
            SwingUtilities.invokeLater(() -> areaTextoLogs.append(lineaCompleta + "\n"));

        } catch (IOException error) {
            /* Si entra en el catch es porque hubo un problema de escritura; se notifica en consola */
            System.out.println("Error al escribir en el archivo de registro: " + error.getMessage());
        }
    }
}