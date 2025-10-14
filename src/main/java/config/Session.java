package config;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import models.Usuario;

/**
 * Clase que maneja la sesión actual del usuario logueado. Permite acceder a los
 * datos del usuario desde cualquier parte de la aplicación mientras la sesión
 * esté activa.
 *
 * @author Milanes
 */
public class Session {

    // Usuario actualmente logueado
    private static Usuario usuarioActual;

    /**
     * Inicia la sesión del usuario (se guarda el usuario autenticado).
     *
     * @param usuario Objeto Usuario obtenido tras un login exitoso
     */
    public static void iniciarSesion(Usuario usuario) {
        usuarioActual = usuario;
        Logger.info("Sesión iniciada para el usuario: " + usuario.getNombre());
    }

    /**
     * Devuelve el usuario actualmente logueado.
     *
     * @return Usuario actual o null si no hay sesión activa
     */
    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    /**
     * Comprueba si hay una sesión activa.
     *
     * @return true si hay un usuario logueado, false si no
     */
    public static boolean haySesionActiva() {
        return usuarioActual != null;
    }

    /**
     * Cierra la sesión actual.
     */
    public static void cerrarSesion() {
        if (usuarioActual != null) {
            Logger.info("Sesión cerrada para el usuario: " + usuarioActual.getNombre());
        }
        usuarioActual = null;
    }

    /**
     * Devuelve el ID del usuario actualmente logueado. Si no hay sesión activa,
     * devuelve -1.
     *
     * @return ID del usuario o -1 si no hay sesión activa
     */
    public static int getUsuarioId() {
        if (usuarioActual != null) {
            return usuarioActual.getIdUsuario();
        } else {
            Logger.warning("Intento de obtener el ID de usuario sin sesión activa.");
            return -1;
        }
    }

    /**
     * Genera el hash MD5 de una cadena.
     *
     * @param input Texto plano (por ejemplo, contraseña)
     * @return Hash MD5 en formato hexadecimal
     */
    public static String generarMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : messageDigest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            Logger.error("Error al generar hash MD5: " + e.getMessage());
            return null;
        }
    }
}
