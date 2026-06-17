package Controlador;

import Modelo.DatabaseManager;
import Vista.LoginVista;

public class LoginController {

    private LoginVista vista;

    public LoginController(LoginVista vista) {
        this.vista = vista;
        vista.getBtnLogin().addActionListener(e     -> intentarLogin());
        vista.getBtnRegistrar().addActionListener(e -> intentarRegistro());
        vista.getBtnCancelar().addActionListener(e  -> vista.dispose());
    }

    private void intentarLogin() {
        String nombre   = vista.getNombre();
        String password = vista.getPassword();

        if (nombre.isEmpty() || password.isEmpty()) {
            vista.mostrarError("Completa todos los campos.");
            return;
        }

        int idUsuario = DatabaseManager.validarLogin(nombre, password);
        if (idUsuario < 0) {
            vista.mostrarError("Usuario o contraseña incorrectos.");
            return;
        }

        vista.setLoginExitoso(nombre, idUsuario);
        vista.dispose();
    }

    private void intentarRegistro() {
        String nombre   = vista.getNombre();
        String correo   = vista.getCorreo();
        String password = vista.getPassword();

        if (nombre.isEmpty() || password.isEmpty()) {
            vista.mostrarError("Usuario y contraseña son obligatorios.");
            return;
        }
        if (nombre.length() < 3) {
            vista.mostrarError("El nombre debe tener al menos 3 caracteres.");
            return;
        }
        if (password.length() < 4) {
            vista.mostrarError("La contraseña debe tener al menos 4 caracteres.");
            return;
        }

        String error = DatabaseManager.registrarUsuario(nombre, correo, password);
        if (error != null) {
            vista.mostrarError(error);
            return;
        }

        vista.mostrarExito("¡Registro exitoso! Ahora inicia sesión.");
    }
}
