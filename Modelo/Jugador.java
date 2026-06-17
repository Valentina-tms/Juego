package Modelo;

public class Jugador {

    private String nombre;
    private int    idUsuario;   // -1 = invitado (no logueado)

    private volatile int x, y;
    private volatile int puntaje;
    private volatile int vidas = 3;
    
    private volatile long tiempoInicio;
    private volatile long tiempoFinal;

    public void iniciarCronometro() {
    this.tiempoInicio = System.currentTimeMillis();
    }

    public void detenerCronometro() {
    this.tiempoFinal = System.currentTimeMillis();
    }

    /** Tiempo transcurrido en milisegundos (en vivo si aún no terminó). */
    public long getTiempoTranscurrido() {
    long fin = (tiempoFinal > 0) ? tiempoFinal : System.currentTimeMillis();
    return fin - tiempoInicio;
    }

    /** Tiempo formateado como MM:SS.mmm */
    public String getTiempoFormateado() {
    long ms = getTiempoTranscurrido();
    long minutos = (ms / 60000);
    long segundos = (ms % 60000) / 1000;
    long milis    = ms % 1000;
    return String.format("%02d:%02d.%03d", minutos, segundos, milis);
    }

    public Jugador(String nombre, int idUsuario) {
        this.nombre    = nombre;
        this.idUsuario = idUsuario;
        this.puntaje   = 0;
    }

    public Jugador(String nombre) { this(nombre, -1); }

    public boolean estaLogueado() { return idUsuario > 0; }

    // ── Getters / Setters ─────────────────────────────────────────────────

    public String getNombre()          { return nombre; }
    public void   setNombre(String n)  { this.nombre = n; }

    public int  getIdUsuario()         { return idUsuario; }
    public void setIdUsuario(int id)   { this.idUsuario = id; }

    public int  getX()                 { return x; }
    public void setX(int x)           { this.x = x; }

    public int  getY()                 { return y; }
    public void setY(int y)           { this.y = y; }

    public synchronized int  getPuntaje()      { return puntaje; }
    public synchronized void setPuntaje(int p) { this.puntaje = p; }

    public synchronized int  getVidas()        { return vidas; }
    public synchronized void setVidas(int v)   { this.vidas = v; }
    
    private String personaje = "burbuja"; // valor por defecto

    public String getPersonaje()            { return personaje; }
    public void   setPersonaje(String p)    { this.personaje = p; }

    /** Resta una vida y devuelve true si ya no quedan vidas. */
    public synchronized boolean perderVida() {
        if (vidas > 0) vidas--;
        return vidas == 0;
    }
}
