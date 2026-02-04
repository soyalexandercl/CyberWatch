/* Guarda los datos de cada archivo de forma individual para el control de integridad */
package entidad;

public class ArchivoIntegridad {

    /* Estas variables se inicializan por medio del constructor al crear el registro */
    private String nombreArchivo;
    private String firmaDigitalHash;

    public ArchivoIntegridad(String nombreArchivo, String firmaDigitalHash) {
        this.nombreArchivo = nombreArchivo;
        this.firmaDigitalHash = firmaDigitalHash;
    }

    public String getNombre() {
        return nombreArchivo;
    }

    public String getHash() {
        return firmaDigitalHash;
    }

    public void setHash(String nuevoHash) {
        this.firmaDigitalHash = nuevoHash;
    }
}
