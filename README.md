Aquí tienes el código completo para un juego de Snake (la viborita) en Java. He diseñado este código para que sea un solo archivo fácil de copiar y ejecutar.

Todo el código está comentado línea por línea en español para que entiendas exactamente qué función cumple cada parte, desde el bucle del juego hasta el dibujo de los gráficos.

Cómo ejecutarlo
Crea un archivo llamado JuegoSnake.java.

Pega el siguiente código.

Compílalo y ejecútalo (si usas terminal: javac JuegoSnake.java y luego java JuegoSnake).

java
import javax.swing.*; // Importamos librerías para ventanas y componentes visuales
import java.awt.*;    // Importamos librerías para gráficos (colores, dibujo)
import java.awt.event.*; // Importamos librerías para manejar eventos (teclado)
import java.util.Random; // Importamos para generar números aleatorios (comida)

// La clase principal hereda de JPanel para poder dibujar en ella
// Implementa ActionListener para el bucle del juego (reloj)
// Implementa KeyListener para escuchar las flechas del teclado
public class JuegoSnake extends JPanel implements ActionListener, KeyListener {

    // --- CONFIGURACIÓN DEL JUEGO ---
    private final int ANCHO_PANTALLA = 600;  // Ancho de la ventana en píxeles
    private final int ALTO_PANTALLA = 600;   // Alto de la ventana en píxeles
    private final int TAMANO_CUADRO = 25;    // Tamaño de cada cuadradito del juego
    // Calculamos cuántos cuadros caben en total (área total / área de un cuadro)
    private final int UNIDADES = (ANCHO_PANTALLA * ALTO_PANTALLA) / (TAMANO_CUADRO * TAMANO_CUADRO);
    private final int VELOCIDAD = 75;        // Velocidad del juego (milisegundos entre actualizaciones)

    // --- ARRAYS PARA GUARDAR LA POSICIÓN DE LA SERPIENTE ---
    // x[] guarda las coordenadas horizontales del cuerpo
    // y[] guarda las coordenadas verticales del cuerpo
    private final int x[] = new int[UNIDADES];
    private final int y[] = new int[UNIDADES];

    // --- VARIABLES DEL ESTADO DEL JUEGO ---
    private int partesCuerpo = 6;  // La serpiente empieza con 6 partes
    private int manzanasComidas;   // Contador de puntuación
    private int manzanaX;          // Posición X de la comida actual
    private int manzanaY;          // Posición Y de la comida actual
    private char direccion = 'R';  // Dirección actual: 'R' (derecha), 'L' (izquierda), 'U' (arriba), 'D' (abajo)
    private boolean enJuego = false; // Indica si el juego está corriendo
    
    private Timer timer;           // El reloj que controla la velocidad del juego
    private Random random;         // Generador de números aleatorios

    // --- CONSTRUCTOR ---
    public JuegoSnake() {
        random = new Random(); // Inicializamos el generador aleatorio
        
        // Configuración básica del panel
        this.setPreferredSize(new Dimension(ANCHO_PANTALLA, ALTO_PANTALLA)); // Tamaño
        this.setBackground(Color.black); // Fondo negro
        this.setFocusable(true);         // Permitir que el panel reciba teclas
        this.addKeyListener(this);       // Agregamos el "oído" para el teclado
        
        iniciarJuego(); // Llamamos al método que arranca todo
    }

    // Método para preparar el inicio
    public void iniciarJuego() {
        nuevaManzana();     // Colocamos la primera comida
        enJuego = true;     // Activamos la bandera de "jugando"
        timer = new Timer(VELOCIDAD, this); // Creamos el reloj con la velocidad definida
        timer.start();      // Arrancamos el reloj
    }

    // Método principal de dibujo (se llama automáticamente por Swing)
    public void paintComponent(Graphics g) {
        super.paintComponent(g); // Limpia la pantalla
        dibujar(g); // Llamamos a nuestro método de dibujo personalizado
    }

    // Método donde dibujamos los gráficos del juego
    public void dibujar(Graphics g) {
        if (enJuego) {
            // Opcional: Dibujar una cuadrícula para ver mejor las celdas (comentado para estilo retro limpio)
            /*
            for(int i=0; i<ANCHO_PANTALLA/TAMANO_CUADRO; i++) {
                g.drawLine(i*TAMANO_CUADRO, 0, i*TAMANO_CUADRO, ALTO_PANTALLA);
                g.drawLine(0, i*TAMANO_CUADRO, ANCHO_PANTALLA, i*TAMANO_CUADRO);
            }
            */

            // --- DIBUJAR LA MANZANA ---
            g.setColor(Color.red); // Color rojo
            // Dibujamos un óvalo en la posición de la manzana
            g.fillOval(manzanaX, manzanaY, TAMANO_CUADRO, TAMANO_CUADRO);

            // --- DIBUJAR LA SERPIENTE ---
            for(int i = 0; i < partesCuerpo; i++) {
                if(i == 0) { // Si es la cabeza (índice 0)
                    g.setColor(Color.green); // Color verde brillante
                    g.fillRect(x[i], y[i], TAMANO_CUADRO, TAMANO_CUADRO);
                } else { // Si es el cuerpo
                    g.setColor(new Color(45, 180, 0)); // Un verde un poco más oscuro
                    g.fillRect(x[i], y[i], TAMANO_CUADRO, TAMANO_CUADRO);
                }
            }
            
            // --- DIBUJAR PUNTUACIÓN ---
            g.setColor(Color.red);
            g.setFont(new Font("Ink Free", Font.BOLD, 40));
            FontMetrics metrics = getFontMetrics(g.getFont());
            // Centramos el texto de puntuación arriba
            g.drawString("Puntos: " + manzanasComidas, 
                         (ANCHO_PANTALLA - metrics.stringWidth("Puntos: " + manzanasComidas))/2, 
                         g.getFont().getSize());
        } else {
            finDelJuego(g); // Si no estamos jugando, mostramos pantalla de Game Over
        }
    }

    // Método para generar una nueva manzana en posición aleatoria
    public void nuevaManzana() {
        // Genera una coordenada X aleatoria dentro de la cuadrícula
        manzanaX = random.nextInt((int)(ANCHO_PANTALLA / TAMANO_CUADRO)) * TAMANO_CUADRO;
        // Genera una coordenada Y aleatoria dentro de la cuadrícula
        manzanaY = random.nextInt((int)(ALTO_PANTALLA / TAMANO_CUADRO)) * TAMANO_CUADRO;
    }

    // Método para mover la serpiente
    public void mover() {
        // Recorremos el cuerpo de atrás hacia adelante
        // Cada parte del cuerpo toma la posición de la parte anterior
        for(int i = partesCuerpo; i > 0; i--) {
            x[i] = x[i-1];
            y[i] = y[i-1];
        }

        // Movemos la cabeza (índice 0) según la dirección actual
        switch(direccion) {
            case 'U': // Arriba (Up)
                y[0] = y[0] - TAMANO_CUADRO;
                break;
            case 'D': // Abajo (Down)
                y[0] = y[0] + TAMANO_CUADRO;
                break;
            case 'L': // Izquierda (Left)
                x[0] = x[0] - TAMANO_CUADRO;
                break;
            case 'R': // Derecha (Right)
                x[0] = x[0] + TAMANO_CUADRO;
                break;
        }
    }

    // Método para verificar si comimos una manzana
    public void verificarManzana() {
        // Si la cabeza (x[0], y[0]) está en la misma posición que la manzana
        if((x[0] == manzanaX) && (y[0] == manzanaY)) {
            partesCuerpo++;  // La serpiente crece
            manzanasComidas++; // Sumamos puntos
            nuevaManzana();  // Generamos nueva comida
        }
    }

    // Método para verificar choques (con bordes o consigo misma)
    public void verificarColisiones() {
        // Verificamos si la cabeza choca con el cuerpo
        for(int i = partesCuerpo; i > 0; i--) {
            if((x[0] == x[i]) && (y[0] == y[i])) {
                enJuego = false; // Fin del juego
            }
        }

        // Verificamos si choca con los bordes
        if(x[0] < 0) enJuego = false; // Borde izquierdo
        if(x[0] > ANCHO_PANTALLA) enJuego = false; // Borde derecho
        if(y[0] < 0) enJuego = false; // Borde superior
        if(y[0] > ALTO_PANTALLA) enJuego = false; // Borde inferior

        if(!enJuego) {
            timer.stop(); // Si perdimos, detenemos el reloj
        }
    }

    // Método para mostrar pantalla de Game Over
    public void finDelJuego(Graphics g) {
        // Texto de Puntuación Final
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 40));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        g.drawString("Puntos: " + manzanasComidas, 
                     (ANCHO_PANTALLA - metrics1.stringWidth("Puntos: " + manzanasComidas))/2, 
                     g.getFont().getSize());
        
        // Texto de "Game Over"
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 75));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("Game Over", 
                     (ANCHO_PANTALLA - metrics2.stringWidth("Game Over"))/2, 
                     ALTO_PANTALLA / 2);
    }

    // --- BUCLE DEL JUEGO (ActionListener) ---
    // Este método se ejecuta cada vez que el Timer cumple su ciclo (velocidad)
    @Override
    public void actionPerformed(ActionEvent e) {
        if(enJuego) {
            mover();             // Calculamos nueva posición
            verificarManzana();  // Verificamos si comió
            verificarColisiones(); // Verificamos si murió
        }
        repaint(); // Redibujamos la pantalla con los nuevos datos
    }

    // --- CONTROLES (KeyListener) ---
    @Override
    public void keyPressed(KeyEvent e) {
        // Detectamos qué tecla se presionó
        switch(e.getKeyCode()) {
            case KeyEvent.VK_LEFT: // Flecha Izquierda
                // Solo giramos si no vamos a la derecha (para no suicidarnos 180 grados)
                if(direccion != 'R') {
                    direccion = 'L';
                }
                break;
            case KeyEvent.VK_RIGHT: // Flecha Derecha
                if(direccion != 'L') {
                    direccion = 'R';
                }
                break;
            case KeyEvent.VK_UP: // Flecha Arriba
                if(direccion != 'D') {
                    direccion = 'U';
                }
                break;
            case KeyEvent.VK_DOWN: // Flecha Abajo
                if(direccion != 'U') {
                    direccion = 'D';
                }
                break;
        }
    }

    // Métodos obligatorios de KeyListener que no usamos pero deben estar presentes
    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyReleased(KeyEvent e) {}

    // --- MÉTODO MAIN PARA EJECUTAR ---
    public static void main(String[] args) {
        // Creamos la ventana principal (JFrame)
        JFrame frame = new JFrame();
        JuegoSnake juego = new JuegoSnake(); // Creamos instancia de nuestro juego
        
        frame.add(juego); // Agregamos el panel del juego a la ventana
        frame.setTitle("Juego Snake en Java");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Cerrar al salir
        frame.setResizable(false); // No permitir cambiar tamaño
        frame.pack(); // Ajustar ventana al tamaño preferido del panel
        frame.setVisible(true); // Mostrar ventana
        frame.setLocationRelativeTo(null); // Centrar en pantalla
    }
}
