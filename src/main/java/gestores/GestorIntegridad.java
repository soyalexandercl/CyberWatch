package gestores;

/* Módulo 1: Monitor de Integridad de Archivos
   El sistema deberá:
   Calcular el hash SHA-256 de cada archivo de una carpeta.
   Detectar cambios, eliminaciones o creaciones nuevas.
   Registrar alertas en log_sdas.txt.
*/

/* En el módulo 1 se crea un hilo debido a que se debe estar realizando consultas para 
   identificar si hubieron cambios, eliminaciones o creaciones.
*/

import entidad.ArchivoIntegridad;
import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import util.Log;

/* Se utiliza extends Thread para darle el poder de ser un hilo, para esto debe tener la clase + extends + Thread */
public class GestorIntegridad extends Thread {

    /* Siempre las variables de clase se crean al inicio */
    private final String rutaCarpeta; 
    /* Aquí es donde voy a almacenar la ruta de la carpeta que se va a gestionar. Final indica una variable constante */
    private final List<ArchivoIntegridad> archivosRegistrados; 
    /* Esto es un ArrayList donde se almacenan todos los archivos procesados */
    private final Log registroLog; 
    /* Nombre del tipo de dato (Clase) - gestiona los registros de eventos */
    private boolean enEjecucion; 

    /* El constructor se crea al inicio y siempre va a ser public, aquí se inicializan las variables creadas */
    public GestorIntegridad(String rutaCarpeta, Log registroLog) {
        this.rutaCarpeta = rutaCarpeta; /* Referencia a la variable de la clase con this */
        this.registroLog = registroLog;
        this.archivosRegistrados = new ArrayList<>();
        this.enEjecucion = true;
    }

    /* ==================================================== */
    /* CALCULAR HASH SHA-256                 */
    /* ==================================================== */
    
    private String generarFirmaDigital(File archivo) {
        /* El try sirve para manejar los errores de lectura o algoritmo */
        try {
            /* toPath() entrega la dirección y readAllBytes lee el contenido en bytes */
            byte[] contenidoBytes = Files.readAllBytes(archivo.toPath());
            /* Se declara el algoritmo SHA-256 y se procesan los datos */
            byte[] hashBytes = MessageDigest.getInstance("SHA-256").digest(contenidoBytes);
            
            /* StringBuilder permite construir el texto hexadecimal de manera eficiente */
            StringBuilder constructorHex = new StringBuilder();
            for (byte b : hashBytes) {
                /* Convierte cada byte en un formato hexadecimal de dos caracteres */
                constructorHex.append(String.format("%02x", b));
            }
            return constructorHex.toString();
        } catch (Exception e) {
            return null; /* En caso de error se retorna nulo */
        }
    }

    @Override
    /* El @Override sobreescribe el método run para ejecutar la lógica del hilo */
    public void run() {
        File directorio = new File(rutaCarpeta); /* Se crea el objeto de tipo File */
        
        /* Verifica si la ruta proporcionada representa realmente una carpeta */
        if (directorio.isDirectory()) {
            registroLog.registrarLinea("logs/log_sdas.txt", "[INFO] Gestor de archivos iniciado");

            /* Carga inicial de archivos existentes en el directorio */
            File[] listaInicial = directorio.listFiles();
            if (listaInicial != null) {
                for (File f : listaInicial) {
                    if (f.isFile()) {
                        /* Agrega el nombre y su firma digital a la lista de registros */
                        archivosRegistrados.add(new ArchivoIntegridad(f.getName(), generarFirmaDigital(f)));
                    }
                }
            }
            registroLog.registrarLinea("logs/log_sdas.txt", "[INFO] Los archivos existentes fueron cargados");

            /* Bucle constante para monitorear cambios mientras el hilo esté activo */
            while (enEjecucion) {
                File[] archivosActuales = directorio.listFiles();
                
                if (archivosActuales != null) {
                    /* Detectar nuevos archivos y/o modificados */
                    for (File archivoFisico : archivosActuales) {
                        if (!archivoFisico.isFile()) {
                            continue; /* Si no es un archivo, pasa a la siguiente iteración */
                        }

                        boolean encontrado = false;
                        String firmaActual = generarFirmaDigital(archivoFisico);
                        
                        int indice = 0;
                        while (indice < archivosRegistrados.size()) {
                            ArchivoIntegridad registro = archivosRegistrados.get(indice);
                            
                            /* Compara el nombre del archivo con los registros guardados */
                            if (registro.getNombre().equals(archivoFisico.getName())) {
                                encontrado = true;
                                /* Si el hash cambió, el archivo fue modificado */
                                if (!registro.getHash().equals(firmaActual)) {
                                    registroLog.registrarLinea("logs/log_sdas.txt", "[INTEGRIDAD] El archivo " + archivoFisico.getName() + " ha sido modificado.");
                                    registro.setHash(firmaActual);
                                }
                            }
                            indice++;
                        }

                        /* Si no estaba en la lista, es un archivo nuevo */
                        if (!encontrado) {
                            archivosRegistrados.add(new ArchivoIntegridad(archivoFisico.getName(), firmaActual));
                            registroLog.registrarLinea("logs/log_sdas.txt", "[INTEGRIDAD] El archivo " + archivoFisico.getName() + " ha sido creado.");
                        }
                    }

                    /* Detectar archivos eliminados comparando la lista con el directorio real */
                    int k = 0;
                    while (k < archivosRegistrados.size()) {
                        boolean todaviaExiste = false;
                        String nombreRegistrado = archivosRegistrados.get(k).getNombre();
                        
                        for (File f : archivosActuales) {
                            if (f.getName().equals(nombreRegistrado)) {
                                todaviaExiste = true;
                            }
                        }

                        if (!todaviaExiste) {
                            registroLog.registrarLinea("logs/log_sdas.txt", "[INTEGRIDAD] El archivo " + nombreRegistrado + " ha sido eliminado.");
                            archivosRegistrados.remove(k);
                        } else {
                            k++;
                        }
                    }
                }
            }
        }
    } /* Lo que está dentro del run es lo que se ejecuta al iniciar el hilo */
}