// ==========================================================
// IMPORTS: traemos las clases que necesitamos de cada paquete
// ==========================================================

// Paquete javax.swing: componentes gráficos de alto nivel (ventanas, paneles, temporizador)
import javax.swing.JFrame;  // Ventana principal del sistema (con barra de título, botones, etc.)
import javax.swing.JPanel;  // Zona de dibujo; sobre este panel pintamos el juego.
import javax.swing.Timer;   // Temporizador que llama periódicamente a actionPerformed (bucle del juego).

// Paquete java.awt: utilidades de dibujo 2D y configuración visual
import java.awt.Color;        // Representa colores (predefinidos y personalizados).
import java.awt.Dimension;    // Ancho y alto de un componente (por ejemplo el panel del juego).
import java.awt.Font;         // Tipo de letra (familia, estilo, tamaño).
import java.awt.FontMetrics;  // Permite medir el tamaño en píxeles de textos para poder centrarlos.
import java.awt.Graphics;     // "Pincel" usado para dibujar formas y texto sobre el JPanel.

// Paquete java.awt.event: eventos (acciones del usuario o del sistema)
import java.awt.event.ActionEvent;      // Evento que dispara el Timer en cada "tick".
import java.awt.event.ActionListener;   // Interfaz que obliga a implementar actionPerformed (reacción al Timer).
import java.awt.event.KeyAdapter;       // Clase base para manejar teclado sin implementar todos los métodos.
import java.awt.event.KeyEvent;         // Representa una tecla concreta (flechas, letras, etc.).

// Paquete java.util: utilidades generales
import java.util.Random;  // Generador de números aleatorios (posiciones de la manzana).

// ==========================================================
// CLASE PRINCIPAL DEL JUEGO
// Hereda de JPanel para poder dibujar y mostrar en un JFrame,
// e implementa ActionListener para recibir "ticks" del Timer.
// ==========================================================
public class SnakeJuego extends JPanel implements ActionListener {

    // ---------------------------
    // CONSTANTES DE CONFIGURACIÓN
    // ---------------------------

    // Tamaño de la ventana en píxeles.
    private final int ANCHO = 600;
    private final int ALTO = 600;

    // Tamaño de cada "cuadro" del grid donde se mueve la serpiente.
    private final int TAMANO_UNITARIO = 25;

    // Número máximo de "unidades" (casillas) posibles en el área de juego.
    // Esto determina el tamaño máximo de los arrays x[] e y[].
    private final int UNIDADES_JUEGO = (ANCHO * ALTO) / (TAMANO_UNITARIO * TAMANO_UNITARIO);

    // Retardo en milisegundos entre cada actualización del juego (velocidad de la serpiente).
    private final int RETRASO = 75;

    // ----------------------------------------------------
    // ARRAYS PARA GUARDAR LAS COORDENADAS DEL CUERPO SNAKE
    // x[i] e y[i] guardan la posición de cada segmento.
    // El índice 0 siempre es la cabeza, los demás son el cuerpo.
    // ----------------------------------------------------
    private final int x[] = new int[UNIDADES_JUEGO];
    private final int y[] = new int[UNIDADES_JUEGO];

    // ------------------------
    // ESTADO ACTUAL DEL JUEGO
    // ------------------------

    private int partesCuerpo = 6;  // Longitud inicial de la serpiente (en segmentos).
    private int manzanasComidas;  // Contador de puntos (una manzana = un punto + un segmento extra).
    private int manzanaX;         // Posición X actual de la manzana/comida.
    private int manzanaY;         // Posición Y actual de la manzana/comida.

    // Dirección actual de movimiento:
    // 'A' = Arriba, 'B' = Abajo, 'I' = Izquierda, 'D' = Derecha.
    private char direccion = 'D';

    // Indica si el juego está activo (true) o en Game Over (false).
    private boolean enJuego = false;

    // --------------------------
    // OBJETOS AUXILIARES CLAVE
    // --------------------------

    private Timer timer;   // Objeto Timer de Swing que genera el bucle de juego.
    private Random random; // Generador de posiciones aleatorias para la manzana.

    // ==========================================================
    // CONSTRUCTOR: se ejecuta al crear un nuevo SnakeJuego.
    // Configura el panel, el teclado y arranca la primera partida.
    // ==========================================================
    public SnakeJuego() {
        random = new Random(); // Creamos el generador aleatorio.

        // Fijamos el tamaño preferido del panel (coincide con la ventana).
        this.setPreferredSize(new Dimension(ANCHO, ALTO));

        // Color de fondo del panel (negro).
        this.setBackground(Color.black);

        // Permitimos que el JPanel pueda recibir eventos de teclado.
        this.setFocusable(true);

        // Añadimos nuestro KeyListener personalizado para controlar la serpiente.
        this.addKeyListener(new MiAdaptadorDeTeclas());

        // Iniciamos la primera partida.
        iniciarJuego();
    }

    // =======================================================
    // iniciarJuego(): deja el juego en estado "nuevo comienzo"
    // - Resetea puntuación, tamaño y dirección.
    // - Genera una nueva manzana.
    // - Prepara y arranca el Timer.
    // =======================================================
    public void iniciarJuego() {
        nuevaManzana();   // Colocar la primera manzana en una posición aleatoria.
        enJuego = true;   // Marcamos que el juego está activo.
        partesCuerpo = 6; // Reiniciamos longitud de la serpiente.
        manzanasComidas = 0; // Puntuación a 0.
        direccion = 'D';  // Dirección por defecto: derecha.

        // Situamos los segmentos iniciales todos en (0,0) para evitar "basura".
        for (int i = 0; i < partesCuerpo; i++) {
            x[i] = 0;
            y[i] = 0;
        }

        // Si ya había un Timer de una partida anterior, lo paramos.
        if (timer != null) {
            timer.stop();
        }

        // Creamos un nuevo Timer con el retardo definido y registramos este objeto como oyente.
        timer = new Timer(RETRASO, this);

        // Arrancamos el bucle del juego.
        timer.start();
    }

    // =======================================================
    // reiniciarJuego(): se llama al pulsar 'R'.
    // Solo reinicia si el juego ha terminado (enJuego == false).
    // =======================================================
    public void reiniciarJuego() {
        if (!enJuego) {
            iniciarJuego();
            repaint(); // Volvemos a dibujar el panel con el nuevo estado.
        }
    }

    // =======================================================
    // paintComponent(Graphics g):
    // Método de JPanel llamado automáticamente cuando hay que
    // repintar el contenido (por ejemplo, tras repaint()).
    // =======================================================
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g); // Limpia el fondo.
        dibujar(g);              // Llama a nuestro método que realmente pinta el juego.
    }

    // =======================================================
    // dibujar(Graphics g):
    // Pinta manzana, serpiente y textos según el estado del juego.
    // =======================================================
    public void dibujar(Graphics g) {
        if (enJuego) {
            // 1) Dibujar manzana.
            g.setColor(Color.red); // Color rojo.
            g.fillOval(manzanaX, manzanaY, TAMANO_UNITARIO, TAMANO_UNITARIO); // Círculo relleno.

            // 2) Dibujar la serpiente segmento a segmento.
            for (int i = 0; i < partesCuerpo; i++) {
                if (i == 0) {
                    // Cabeza: color verde más claro.
                    g.setColor(Color.green);
                    g.fillRect(x[i], y[i], TAMANO_UNITARIO, TAMANO_UNITARIO);
                } else {
                    // Cuerpo: verde algo más oscuro.
                    g.setColor(new Color(45, 180, 0));
                    g.fillRect(x[i], y[i], TAMANO_UNITARIO, TAMANO_UNITARIO);
                }
            }

            // 3) Dibujar puntuación en la parte superior.
            g.setColor(Color.red);
            g.setFont(new Font("Ink Free", Font.BOLD, 40)); // Fuente grande y visible.
            FontMetrics metrics = getFontMetrics(g.getFont()); // Para centrar el texto.
            g.drawString(
                "Puntos: " + manzanasComidas,
                (ANCHO - metrics.stringWidth("Puntos: " + manzanasComidas)) / 2,
                g.getFont().getSize()
            );
        } else {
            // Si el juego no está activo, mostramos la pantalla de Game Over.
            finDelJuego(g);
        }
    }

    // =======================================================
    // nuevaManzana():
    // Calcula una nueva posición aleatoria para la manzana
    // dentro de la rejilla de juego.
    // =======================================================
    public void nuevaManzana() {
        // Posición X: número aleatorio de columna * tamaño del cuadro.
        manzanaX = random.nextInt(ANCHO / TAMANO_UNITARIO) * TAMANO_UNITARIO;

        // Posición Y: número aleatorio de fila * tamaño del cuadro.
        manzanaY = random.nextInt(ALTO / TAMANO_UNITARIO) * TAMANO_UNITARIO;
    }

    // =======================================================
    // mover():
    // Desplaza todos los segmentos del cuerpo hacia adelante,
    // copiando la posición del segmento anterior, y luego mueve
    // la cabeza según la dirección actual.
    // =======================================================
    public void mover() {
        // Recorremos desde la cola hacia la cabeza copiando posiciones.
        for (int i = partesCuerpo; i > 0; i--) {
            x[i] = x[i - 1]; // Segmento i toma la posición del segmento i-1 en X.
            y[i] = y[i - 1]; // Segmento i toma la posición del segmento i-1 en Y.
        }

        // Actualizamos la posición de la cabeza según la dirección.
        switch (direccion) {
            case 'A': // Arriba
                y[0] = y[0] - TAMANO_UNITARIO;
                break;
            case 'B': // Abajo
                y[0] = y[0] + TAMANO_UNITARIO;
                break;
            case 'I': // Izquierda
                x[0] = x[0] - TAMANO_UNITARIO;
                break;
            case 'D': // Derecha
                x[0] = x[0] + TAMANO_UNITARIO;
                break;
        }
    }

    // =======================================================
    // comprobarManzana():
    // Comprueba si la cabeza de la serpiente ha llegado a la
    // misma posición que la manzana. Si sí, crece y suma puntos.
    // =======================================================
    public void comprobarManzana() {
        if (x[0] == manzanaX && y[0] == manzanaY) {
            partesCuerpo++;     // Aumenta la longitud de la serpiente.
            manzanasComidas++;  // Suma un punto.
            nuevaManzana();     // Genera una nueva manzana.
        }
    }

    // =======================================================
    // comprobarColisiones():
    // Verifica si la serpiente choca consigo misma o con los
    // bordes de la ventana. Si hay choque, el juego termina.
    // =======================================================
    public void comprobarColisiones() {
        // 1) Choque con el propio cuerpo.
        for (int i = partesCuerpo; i > 0; i--) {
            // Si la cabeza tiene la misma posición que algún segmento:
            if (x[0] == x[i] && y[0] == y[i]) {
                enJuego = false; // Game Over.
            }
        }

        // 2) Choque con los bordes (muerte instantánea al tocar).
        if (x[0] < 0)        enJuego = false; // Sale por la izquierda.
        if (x[0] >= ANCHO)   enJuego = false; // Sale por la derecha.
        if (y[0] < 0)        enJuego = false; // Sale por arriba.
        if (y[0] >= ALTO)    enJuego = false; // Sale por abajo.

        // Si el juego ha terminado, detenemos el Timer.
        if (!enJuego) {
            timer.stop();
        }
    }

    // =======================================================
    // finDelJuego(Graphics g):
    // Dibuja la pantalla de "Game Over" con instrucciones
    // para reiniciar y muestra la puntuación final.
    // =======================================================
    public void finDelJuego(Graphics g) {
        // Texto principal "Game Over".
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 75));
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString(
            "Game Over",
            (ANCHO - metrics.stringWidth("Game Over")) / 2,
            ALTO / 2
        );

        // Instrucción para reiniciar con 'R'.
        g.setColor(Color.white);
        g.setFont(new Font("Ink Free", Font.BOLD, 30));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString(
            "Presiona 'R' para reiniciar",
            (ANCHO - metrics2.stringWidth("Presiona 'R' para reiniciar")) / 2,
            ALTO / 2 + 50
        );

        // Puntuación final.
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 40));
        FontMetrics metrics3 = getFontMetrics(g.getFont());
        g.drawString(
            "Puntos: " + manzanasComidas,
            (ANCHO - metrics3.stringWidth("Puntos: " + manzanasComidas)) / 2,
            g.getFont().getSize()
        );
    }

    // =======================================================
    // actionPerformed(ActionEvent e):
    // Método exigido por ActionListener.
    // El Timer lo llama automáticamente cada RETRASO ms.
    // Aquí actualizamos la lógica y pedimos repintar.
    // =======================================================
    @Override
    public void actionPerformed(ActionEvent e) {
        if (enJuego) {
            mover();             // Actualiza las posiciones de la serpiente.
            comprobarManzana();  // Comprueba si hemos comido.
            comprobarColisiones(); // Comprueba si hemos chocado.
        }
        repaint(); // Solicita redibujar el panel con el estado actualizado.
    }

    // =======================================================
    // Clase interna MiAdaptadorDeTeclas:
    // Extiende KeyAdapter para reaccionar solo a keyPressed.
    // Controla la dirección con flechas y WASD y el reinicio con R.
    // =======================================================
    public class MiAdaptadorDeTeclas extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            // Obtenemos el código de la tecla pulsada.
            switch (e.getKeyCode()) {

                // Izquierda: Flecha izquierda o tecla 'A'.
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_A:
                    if (direccion != 'D') { // Evita girar 180º directamente (no puedes ir derecha -> izquierda de golpe).
                        direccion = 'I';
                    }
                    break;

                // Derecha: Flecha derecha o tecla 'D'.
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_D:
                    if (direccion != 'I') {
                        direccion = 'D';
                    }
                    break;

                // Arriba: Flecha arriba o tecla 'W'.
                case KeyEvent.VK_UP:
                case KeyEvent.VK_W:
                    if (direccion != 'B') {
                        direccion = 'A';
                    }
                    break;

                // Abajo: Flecha abajo o tecla 'S'.
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_S:
                    if (direccion != 'A') {
                        direccion = 'B';
                    }
                    break;

                // Reiniciar partida: tecla 'R'.
                case KeyEvent.VK_R:
                    reiniciarJuego();
                    break;
            }
        }
    }

    // =======================================================
    // main(String[] args):
    // Punto de entrada de la aplicación.
    // Crea la ventana (JFrame), le añade el panel del juego
    // y la muestra centrada en pantalla.
    // =======================================================
    public static void main(String[] args) {
        JFrame frame = new JFrame();        // Ventana principal.
        SnakeJuego juego = new SnakeJuego(); // Instancia de nuestro panel de juego.

        frame.add(juego);                                  // Añadimos el panel a la ventana.
        frame.setTitle("Snake Juego");                     // Título de la ventana.
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Cerrar proceso al cerrar ventana.
        frame.setResizable(false);                         // Evitar cambiar el tamaño (para no romper la rejilla).
        frame.pack();                                      // Ajusta el tamaño de la ventana al del panel.
        frame.setVisible(true);                            // Hace visible la ventana.
        frame.setLocationRelativeTo(null);                 // Centra la ventana en la pantalla.
    }
}
