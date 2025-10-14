package config;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Clase Logger para registrar mensajes, advertencias y errores
 * en consola y en un archivo log.txt dentro del proyecto.
 *
 * @author Milanes
 */
public class Logger {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    // Ruta del archivo de log (en la raíz del proyecto)
    private static final String LOG_FILE = "log.txt";

    /**
     * Muestra un mensaje informativo y lo guarda en el log.
     * @param mensaje Contenido del mensaje.
     */
    public static void info(String mensaje) {
        registrar("INFO", mensaje);
    }

    /**
     * Muestra un mensaje de advertencia y lo guarda en el log.
     * @param mensaje Contenido del mensaje.
     */
    public static void warning(String mensaje) {
        registrar("WARNING", mensaje);
    }

    /**
     * Muestra un mensaje de error y lo guarda en el log.
     * @param mensaje Contenido del mensaje.
     */
    public static void error(String mensaje) {
        registrar("ERROR", mensaje);
    }

    /**
     * Registra una excepción completa con su traza en consola y log.
     * @param mensaje Mensaje descriptivo.
     * @param e Excepción capturada.
     */
    public static void exception(String mensaje, Exception e) {
        registrar("EXCEPTION", mensaje);
        try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            e.printStackTrace(out);
        } catch (IOException ioEx) {
            System.err.println("Error al escribir la excepción en el archivo de log: " + ioEx.getMessage());
        }
        e.printStackTrace(System.err);
    }

    /**
     * Método genérico para formatear, mostrar y guardar mensajes.
     */
    private static void registrar(String tipo, String mensaje) {
        String texto = formatear(tipo, mensaje);

        // Mostrar en consola
        if (tipo.equals("ERROR") || tipo.equals("EXCEPTION")) {
            System.err.println(texto);
        } else {
            System.out.println(texto);
        }

        // Guardar en archivo
        escribirEnArchivo(texto);
    }

    /**
     * Formatea el mensaje con fecha, hora y tipo.
     */
    private static String formatear(String tipo, String mensaje) {
        return "[" + FORMATTER.format(LocalDateTime.now()) + "] "
                + "[" + tipo + "] " + mensaje;
    }

    /**
     * Escribe el mensaje en el archivo log.txt
     */
    private static void escribirEnArchivo(String texto) {
        try {
            Path path = Paths.get(LOG_FILE);
            if (!Files.exists(path)) {
                Files.createFile(path);
            }

            try (FileWriter fw = new FileWriter(LOG_FILE, true);
                 PrintWriter pw = new PrintWriter(fw)) {
                pw.println(texto);
            }

        } catch (IOException e) {
            System.err.println("Error al escribir en el archivo log: " + e.getMessage());
        }
    }
}
