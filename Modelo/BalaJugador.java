/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

/**
 *
 * @author User
 */
public class BalaJugador implements Runnable {
    private volatile int x, y;
    private final int velocidad;
    public static final int ANCHO = 12, ALTO = 8;

    private Thread hilo;
    private volatile boolean corriendo = false;

    public BalaJugador(int x, int y, int velocidad) {
        this.x = x; this.y = y; this.velocidad = velocidad;
    }

    public void iniciarHilo() {
        corriendo = true;
        hilo = new Thread(this, "Hilo-BalaJugador");
        hilo.setDaemon(true);
        hilo.start();
    }

    public void detenerHilo() {
        corriendo = false;
        if (hilo != null) hilo.interrupt();
    }

    @Override
    public void run() {
        while (corriendo && !fueraDePantalla()) {
            x += velocidad;
            try { Thread.sleep(16); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
        }
    }

    public boolean fueraDePantalla() { return x > 1200; }
    public int getX() { return x; }
    public int getY() { return y; }
}