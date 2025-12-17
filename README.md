// =========================================================================
// SECCIÓN DE IMPORTS: Aquí traemos las herramientas de las librerías de Java
// =========================================================================

// LIBRERÍA: javax.swing (Java Swing - Interfaz Gráfica Moderna)
import javax.swing.JFrame; // Clase que crea la ventana principal del sistema operativo.
import javax.swing.JPanel; // Clase que funciona como un "lienzo" invisible donde dibujaremos.
import javax.swing.Timer;  // Clase que ejecuta el bucle del juego (reloj).

// LIBRERÍA: java.awt (Abstract Window Toolkit - Gráficos y Diseño Básico)
import java.awt.Color;     // Clase que define colores estándar.
import java.awt.Dimension; // Clase objeto que guarda ancho y alto.
import java.awt.Font;      // Clase para definir la tipografía.
import java.awt.FontMetrics; // Clase técnica para medir textos en pantalla.
import java.awt.Graphics;  // El "pincel" que pinta líneas, óvalos y cuadrados.

// LIBRERÍA: java.awt.event (Manejo de Eventos - Interacción Usuario)
import java.awt.event.ActionEvent; // Representa el "evento" de un tick del reloj.
import java.awt.event.ActionListener; // Interfaz para escuchar al reloj.
import java.awt.event.KeyAdapter; // Clase base para escuchar el teclado.
import java.awt.event.KeyEvent;   // Contiene los códigos de las teclas (VK_W, VK_UP, etc).

// LIBRERÍA: java.util (Utilidades Generales)
import java.util.Random; // Clase para generar números aleatorios (manzana).

// =========================================================================
// CLASE PRINCIPAL
// =========================================================================
public class SnakeJuego extends JPanel implements ActionListener {

    private final int ANCHO = 600;
    private final int ALTO = 600;
    private final int TAMANO_UNITARIO = 25;
    private final int UNIDADES_JUEGO = (ANCHO * ALTO) / (TAMANO_UNITARIO * TAMANO_UNITARIO);
    private final int RETRASO = 75; 

    private final int x[] = new int[UNIDADES_JUEGO];
    private final int y[] = new int[UNIDADES_JUEGO];

    private int partesCuerpo = 6;
    private int manzanasComidas;
    private int manzanaX;
    private int manzanaY;
    private char direccion = 'D';  // 'D'erecha, 'I'zquierda, 'A'rriba, 'B'ajo
    private boolean enJuego = false;

    private Timer timer;    
    private Random random;  

    public SnakeJuego() {
        random = new Random(); 
        
        this.setPreferredSize(new Dimension(ANCHO, ALTO)); 
        this.setBackground(Color.black); 
        this.setFocusable(true); 
        
        this.addKeyListener(new MiAdaptadorDeTeclas()); 
        
        iniciarJuego();
    }

    public void iniciarJuego() {
        nuevaManzana();
        enJuego = true;
        timer = new Timer(RETRASO, this); 
        timer.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g); 
        dibujar(g); 
    }

    public void dibujar(Graphics g) {
        if (enJuego) {
            g.setColor(Color.red); 
            g.fillOval(manzanaX, manzanaY, TAMANO_UNITARIO, TAMANO_UNITARIO);

            for (int i = 0; i < partesCuerpo; i++) {
                if (i == 0) { 
                    g.setColor(Color.green);
                    g.fillRect(x[i], y[i], TAMANO_UNITARIO, TAMANO_UNITARIO); 
                } else { 
                    g.setColor(new Color(45, 180, 0)); 
                    g.fillRect(x[i], y[i], TAMANO_UNITARIO, TAMANO_UNITARIO);
                }
            }
            
            g.setColor(Color.red);
            g.setFont(new Font("Ink Free", Font.BOLD, 40)); 
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("Puntos: " + manzanasComidas, (ANCHO - metrics.stringWidth("Puntos: " + manzanasComidas))/2, g.getFont().getSize());
        } else {
            finDelJuego(g);
        }
    }

    public void nuevaManzana() {
        manzanaX = random.nextInt((int)(ANCHO / TAMANO_UNITARIO)) * TAMANO_UNITARIO;
        manzanaY = random.nextInt((int)(ALTO / TAMANO_UNITARIO)) * TAMANO_UNITARIO;
    }

    public void mover() {
        for (int i = partesCuerpo; i > 0; i--) {
            x[i] = x[i-1];
            y[i] = y[i-1];
        }
        switch(direccion) {
            case 'A': y[0] = y[0] - TAMANO_UNITARIO; break;
            case 'B': y[0] = y[0] + TAMANO_UNITARIO; break;
            case 'I': x[0] = x[0] - TAMANO_UNITARIO; break;
            case 'D': x[0] = x[0] + TAMANO_UNITARIO; break;
        }
    }

    public void comprobarManzana() {
        if ((x[0] == manzanaX) && (y[0] == manzanaY)) {
            partesCuerpo++;
            manzanasComidas++;
            nuevaManzana();
        }
    }

    public void comprobarColisiones() {
        for (int i = partesCuerpo; i > 0; i--) {
            if ((x[0] == x[i]) && (y[0] == y[i])) {
                enJuego = false;
            }
        }
        if (x[0] < 0 || x[0] > ANCHO || y[0] < 0 || y[0] > ALTO) {
            enJuego = false;
        }
        if (!enJuego) timer.stop();
    }

    public void finDelJuego(Graphics g) {
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 75));
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString("Game Over", (ANCHO - metrics.stringWidth("Game Over"))/2, ALTO/2);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (enJuego) {
            mover();
            comprobarManzana();
            comprobarColisiones();
        }
        repaint(); 
    }

    // =========================================================================
    // SECCIÓN MODIFICADA: CONTROLES WASD + FLECHAS
    // =========================================================================
    public class MiAdaptadorDeTeclas extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            // e.getKeyCode() nos da el número interno de la tecla presionada
            // VK_W, VK_A, VK_S, VK_D vienen de la librería java.awt.event.KeyEvent
            
            switch(e.getKeyCode()) {
                // CASO IZQUIERDA: Flecha Izquierda O tecla 'A'
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_A:
                    if (direccion != 'D') direccion = 'I';
                    break;

                // CASO DERECHA: Flecha Derecha O tecla 'D'
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_D:
                    if (direccion != 'I') direccion = 'D';
                    break;

                // CASO ARRIBA: Flecha Arriba O tecla 'W'
                case KeyEvent.VK_UP:
                case KeyEvent.VK_W:
                    if (direccion != 'B') direccion = 'A';
                    break;

                // CASO ABAJO: Flecha Abajo O tecla 'S'
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_S:
                    if (direccion != 'A') direccion = 'B';
                    break;
            }
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame(); 
        SnakeJuego juego = new SnakeJuego();
        
        frame.add(juego);
        frame.setTitle("Snake WASD + Flechas");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }
}
