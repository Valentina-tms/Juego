package Modelo;

public class Obstaculo implements Runnable {
    private volatile int x, y;
    private volatile int velocidad;
    private final int velocidadY;   // nueva — movimiento vertical
    private final int ancho, alto;

    private Thread hilo;
    private volatile boolean corriendo = false;

    public Obstaculo(int x, int y, int velocidad, int velocidadY) {
        this.x          = x;
        this.y          = y;
        this.velocidad  = velocidad;
        this.velocidadY = velocidadY;

        // Tamaño aleatorio entre 25 y 70
        int tam   = 25 + (int)(Math.random() * 46);
        this.ancho = tam;
        this.alto  = tam;
    }

    public void iniciarHilo() {
        corriendo = true;
        hilo = new Thread(this, "Hilo-Obstaculo-" + x);
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
            mover();
            try { Thread.sleep(16); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
        }
    }

    public void mover() {
        if (velocidad > 0) {
            x -= velocidad;   // siempre va hacia la izquierda
            y += velocidadY;  // sube, baja o recto según velocidadY
        }
    }

    public boolean fueraDePantalla() {
        return x + ancho < 0 || y > 800 || y + alto < 0;
    }

    public java.awt.Rectangle getBounds() {
        return new java.awt.Rectangle(x, y, ancho, alto);
    }

    public void        setVelocidad(int v) { this.velocidad = v; }
    public int         getX()              { return x; }
    public int         getY()              { return y; }
    public int         getAncho()          { return ancho; }
    public int         getAlto()           { return alto; }
    public int         getVelocidad()      { return velocidad; }
}