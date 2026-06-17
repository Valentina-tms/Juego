package Modelo;

public class BalaEnemigo implements Runnable {
    private volatile int x, y;
    private final int velocidadX;
    private final int velocidadY;  // nueva
    public static final int ANCHO = 12, ALTO = 8;

    private Thread hilo;
    private volatile boolean corriendo = false;

    public BalaEnemigo(int x, int y, int velocidadX, int velocidadY) {
        this.x = x;
        this.y = y;
        this.velocidadX = velocidadX;
        this.velocidadY = velocidadY;
    }

    public void iniciarHilo() {
        corriendo = true;
        hilo = new Thread(this, "Hilo-BalaEnemigo");
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
            x -= velocidadX;
            y += velocidadY;
            try { Thread.sleep(16); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
        }
    }

    public boolean fueraDePantalla() {
        return x + ANCHO < 0 || y > 800 || y + ALTO < 0;
    }

    public int getX() { return x; }
    public int getY() { return y; }
}