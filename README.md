// =========================================================================
// SECCIÓN DE IMPORTS: Aquí traemos las herramientas de las librerías de Java
// =========================================================================

// LIBRERÍA: javax.swing (Java Swing - Interfaz Gráfica Moderna)
import javax.swing.JFrame; // Clase que crea la ventana principal del sistema operativo (con borde, título, botón cerrar).
import javax.swing.JPanel; // Clase que funciona como un "lienzo" o panel invisible donde dibujaremos el juego.
import javax.swing.Timer;  // Clase que ejecuta un bloque de código repetidamente cada X milisegundos (el bucle del juego).

// LIBRERÍA: java.awt (Abstract Window Toolkit - Gráficos y Diseño Básico)
import java.awt.Color;     // Clase que define colores estándar (Color.red, Color.black) y permite crear nuevos RGB.
import java.awt.Dimension; // Clase objeto que guarda un ancho y un alto (width, height) para dimensionar la ventana.
import java.awt.Font;      // Clase para definir la tipografía (fuente, estilo, tamaño) de los textos.
import java.awt.FontMetrics; // Clase técnica que sirve para medir cuántos píxeles ocupa un texto en pantalla (para centrarlo).
import java.awt.Graphics;  // La clase más importante para dibujar: es el "pincel" que pinta líneas, óvalos y cuadrados.
import java.awt.Toolkit;   // (Opcional) Herramienta para conectar con el sistema nativo (ej: para suavizar animaciones).

// LIBRERÍA: java.awt.event (Manejo de Eventos - Interacción Usuario)
import java.awt.event.ActionEvent; // Clase que representa el "evento" de que pasó el tiempo (un tick del reloj).
import java.awt.event.ActionListener; // Interfaz ("contrato") que nos obliga a tener un método para recibir los eventos del reloj.
import java.awt.event.KeyAdapter; // Clase base que facilita escuchar el teclado (para no tener que implementar todos los métodos).
import java.awt.event.KeyEvent;   // Clase que contiene las constantes de las teclas (ej: KeyEvent.VK_UP es la flecha arriba).

// LIBRERÍA: java.util (Utilidades Generales)
import java.util.Random; // Clase matemática para generar números aleatorios (para poner la manzana en lugares al azar).


// =========================================================================
// CLASE PRINCIPAL
// Hereda de JPanel (es un panel) e implementa ActionListener (reacciona al tiempo)
// =========================================================================
public class SnakeJuego extends JPanel implements ActionListener {

    // Constantes de configuración (final significa que no cambian)
    private final int ANCHO = 600;
    private final int ALTO = 600;
    private final int TAMANO_UNITARIO = 25; // Tamaño de cada cuadrito de la serpiente
    // Cálculo total de unidades posibles en pantalla
    private final int UNIDADES_JUEGO = (ANCHO * ALTO) / (TAMANO_UNITARIO * TAMANO_UNITARIO);
    private final int RETRASO = 75; // Velocidad del juego en milisegundos (Controlado por javax.swing.Timer)

    // Arrays para guardar las coordenadas (x, y) de todas las partes del cuerpo
    // Vienen de la sintaxis base de Java (no requieren import)
    private final int x[] = new int[UNIDADES_JUEGO];
    private final int y[] = new int[UNIDADES_JUEGO];

    private int partesCuerpo = 6;  // Longitud inicial
    private int manzanasComidas;
    private int manzanaX;
    private int manzanaY;
    private char direccion = 'D';  // 'D'erecha, 'I'zquierda, 'A'rriba, 'B'ajo
    private boolean enJuego = false;

    // Declaración de objetos de las librerías importadas
    private Timer timer;    // Objeto de javax.swing
    private Random random;  // Objeto de java.util

    // Constructor: Se ejecuta al crear el juego
    public SnakeJuego() {
        random = new Random(); // Inicializamos la utilidad de aleatoriedad
        
        // Métodos de la clase padre (JPanel - javax.swing)
        this.setPreferredSize(new Dimension(ANCHO, ALTO)); // Dimension viene de java.awt
        this.setBackground(Color.black); // Color viene de java.awt
        this.setFocusable(true); // Permite que el panel reciba "foco" para detectar teclas
        
        // Agregamos nuestro "escucha" de teclas (definido más abajo)
        this.addKeyListener(new MiAdaptadorDeTeclas()); 
        
        iniciarJuego();
    }

    public void iniciarJuego() {
        nuevaManzana();
        enJuego = true;
        // Timer(velocidad, quién_escucha). "this" es esta clase porque implementa ActionListener
        timer = new Timer(RETRASO, this); 
        timer.start();
    }

    // Método de la clase JPanel (javax.swing). Se llama automáticamente para pintar la pantalla.
    public void paintComponent(Graphics g) {
        super.paintComponent(g); // Limpia la pantalla anterior
        dibujar(g); // Pasamos el objeto Graphics (java.awt) para que pinte
    }

    // Método propio para organizar el dibujo
    public void dibujar(Graphics g) {
        if (enJuego) {
            // Dibujar Manzana (java.awt.Graphics)
            g.setColor(Color.red); // Definimos el color del pincel a rojo (java.awt.Color)
            g.fillOval(manzanaX, manzanaY, TAMANO_UNITARIO, TAMANO_UNITARIO); // Dibujamos círculo relleno

            // Dibujar Serpiente
            for (int i = 0; i < partesCuerpo; i++) {
                if (i == 0) { // Cabeza
                    g.setColor(Color.green);
                    g.fillRect(x[i], y[i], TAMANO_UNITARIO, TAMANO_UNITARIO); // Rectángulo relleno
                } else { // Cuerpo
                    g.setColor(new Color(45, 180, 0)); // Creamos un color verde personalizado (R, G, B)
                    g.fillRect(x[i], y[i], TAMANO_UNITARIO, TAMANO_UNITARIO);
                }
            }
            
            // Dibujar Puntuación
            g.setColor(Color.red);
            g.setFont(new Font("Ink Free", Font.BOLD, 40)); // Fuente Ink Free, Negrita, tamaño 40 (java.awt.Font)
            // FontMetrics (java.awt) nos ayuda a centrar el texto matemáticamente
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString
