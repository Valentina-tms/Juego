package Modelo;

import java.sql.*;
import java.util.*;

/**
 * Gestiona la conexión y operaciones con MySQL (WAMP).
 *
 * Requiere mysql-connector-j en el classpath.
 * Base de datos: juego  |  Puerto: 3306
 */
public class DatabaseManager {

    private static final String URL    = "jdbc:mysql://127.0.0.1:3306/juego?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER   = "root";
    private static final String PASS   = "";          // WAMP por defecto no tiene contraseña

    // ── Conexión ─────────────────────────────────────────────────────────────

    public static Connection getConexion() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL no encontrado. Agrega mysql-connector-j al proyecto.", e);
        }
        return DriverManager.getConnection(URL, USER, PASS);
    }

    /** Verifica que la conexión funcione. Devuelve null si OK, mensaje de error si falla. */
    public static String probarConexion() {
        try (Connection c = getConexion()) {
            return null; // OK
        } catch (SQLException e) {
            return e.getMessage();
        }
    }

    // ── Usuarios ─────────────────────────────────────────────────────────────

    /**
     * Registra un nuevo usuario con correo opcional.
     * Devuelve null si OK, o mensaje de error si falla.
     */
    public static String registrarUsuario(String nombre, String correo, String password) {
        String sql = "INSERT INTO usuarios (nombre_usuario, correo, contraseña) VALUES (?, ?, ?)";
        try (Connection c = getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, correo.isEmpty() ? null : correo);
            ps.setString(3, hashPassword(password));
            ps.executeUpdate();
            return null;
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate") || e.getErrorCode() == 1062) {
                return "El usuario o correo ya existe.";
            }
            return "Error al registrar: " + e.getMessage();
        }
    }

    /**
     * Valida credenciales. Devuelve el id_usuario si OK, -1 si falla.
     */
    public static int validarLogin(String nombre, String password) {
        String sql = "SELECT id_usuario, contraseña FROM usuarios WHERE nombre_usuario = ?";
        try (Connection c = getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String hash = rs.getString("contraseña");
                if (hash.equals(hashPassword(password))) {
                    return rs.getInt("id_usuario");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en login: " + e.getMessage());
        }
        return -1;
    }

    // ── Partidas ─────────────────────────────────────────────────────────────

    /** Guarda el puntaje de una partida para el usuario dado. */
    public static void guardarPuntaje(int idUsuario, int tiempoMs) {
        String sql = "INSERT INTO partidas (id_usuario, puntaje) VALUES (?, ?)";
        try (Connection c = getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, tiempoMs);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error guardando puntaje: " + e.getMessage());
        }
    }

    // ── Ranking ──────────────────────────────────────────────────────────────

    /**
     * Obtiene el top N desde la vista `ranking`.
     * Cada entrada es String[]{ nombre_usuario, mejor_puntaje }.
     */
    public static List<String[]> getTopRanking(int top) {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT nombre_usuario, mejor_puntaje FROM ranking LIMIT ?";
        try (Connection c = getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, top);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(new String[]{
                    rs.getString("nombre_usuario"),
                    String.valueOf(rs.getInt("mejor_puntaje"))
                });
            }
        } catch (SQLException e) {
            System.err.println("Error cargando ranking: " + e.getMessage());
        }
        return lista;
    }

    // ── Hash ─────────────────────────────────────────────────────────────────

    /**
     * SHA-256 del password. En producción usar BCrypt,
     * pero SHA-256 es suficiente sin deps externas.
     */
    public static String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            // SHA-256 siempre está disponible en Java
            throw new RuntimeException(e);
        }
    }
}
