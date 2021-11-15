/* Luis Bodart A01635000 */

/* Mersenne Twister de 64 bits (MT19937-64) */

/**
 * w - Rango de valores generados (bits), 2^w
 * n - Grado de recurrencia (tamaño de la reserva de bytes)
 * m - Desplazamiento utilizado en la relación de recurrencia que define la serie x, 1 ≤ m < n
 * r - Número de bits de la lower bit-mask (twist value/valor de giro), 0 ≤ r ≤ w - 1
 * a - Coeficientes de la matriz de giro de forma normal racional
 * u - Componente 1 de la matriz de bit-scrambling (templado)
 * d - Componente 2 de la matriz de bit-scrambling (templado)
 * s - Componente 3 de la matriz de bit-scrambling (templado)
 * b - Componente 4 de la matriz de bit-scrambling (templado)
 * t - Componente 5 de la matriz de bit-scrambling (templado)
 * c - Componente 6 de la matriz de bit-scrambling (templado)
 * l - Componente 7 de la matriz de bit-scrambling (templado)
 * f - Multiplicador de inicialización
 * lower_mask - 31 bits menos significativos
 * upper_mask - 33 bits más significativos
 * MT - arreglo de estado interno
 * index - Índice actual en la reserva de bytes
 */

/**
 * Referencias:
 * 
 * Mersenne Twister
 * https://en.wikipedia.org/wiki/Mersenne_Twister
 * 
 * The Mersenne Twister
 * http://www.quadibloc.com/crypto/co4814.htm
 * 
 * Mersenne Twister Engine
 * https://en.cppreference.com/w/cpp/numeric/random/mersenne_twister_engine
 * 
 * mt19937-64
 * https://cplusplus.com/reference/random/mt19937_64/
 * 
 * Mersenne Twister: A 623-Dimensionally Equidistributed Uniform Pseudo-Random Number Generator
 * http://www.math.sci.hiroshima-u.ac.jp/~m-mat/MT/ARTICLES/mt.pdf
 */

public class MersenneTwister {

    private static final int w = 64, n = 312, m = 156, r = 31;
    private static final long a = 0xB5026F5AA96619E9l;
    private static final int u = 29, s = 17, t = 37;
    private static final long d = 0x5555555555555555l, b = 0x71d67fffeda60000l, c = 0xfff7eee000000000l;
    private static final int l = 43;
    private static final long f = 6364136223846793005l;
    private static final long lower_mask = 0x7FFFFFFFl, upper_mask = 0xFFFFFFFF80000000l;
    public static final int default_seed = 5489;

    private final long[] MT;
    private int index;

    /**
     * Constructor con una semilla aleatoria
     */
    public MersenneTwister() {
        this.MT = new long[n];
        this.setSeed(System.currentTimeMillis());
    }

    /**
     * Cnstructor con una semilla definida
     * 
     * @param seed semilla inicial
     */
    public MersenneTwister(long seed) {
        this.MT = new long[n];
        this.setSeed(seed);
    }

    /**
     * Inicializa el generador a partir de una semilla
     * 
     * @param seed semilla para la creación de los números aleatorios
     */
    private void setSeed(long seed) {
        this.MT[0] = seed;
        this.index = n;
        for (int i = 1; i < n; i++) {
            this.MT[i] = (f * (this.MT[i - 1] ^ (this.MT[i - 1] >> (w - 2))) + i);
        }
    }

    /**
     * Genera los siguientes valores n de la serie x_i
     */
    private void twist() {
        for (int i = 0; i < n - 1; i++) {
            long x = (this.MT[i] & upper_mask) + (this.MT[(i + 1) % n] & lower_mask);
            long xA = x >> 1;
            if (x % 2 != 0) { // bit más bajo de x es 1
                xA ^= a;
            }
            this.MT[i] = this.MT[(i + m) % n] ^ xA;
        }
        this.index = 0;
    }

    /**
     * Genera un valor templado basado en MT[index] llamando a twist() cada n
     */
    private long extract_number() {
        if (this.index >= n) {
            this.twist();
        }
        long y = this.MT[this.index++];
        // templando
        y ^= (y >> u) & d;
        y ^= (y << s) & b;
        y ^= (y << t) & c;
        y ^= (y >> l);
        return y;
    }

    /**
     * Genera un valor double pseudoaleatorio y uniformemente distribuido entre [0.0, 1.0)
     */
    public double random() {
        return (this.extract_number() >> 1) / (double) Long.MAX_VALUE;
    }

    /**
     * Genera un valor entero pseudoaleatorio entre [0, x)
     * 
     * @param x Número entero límite, x > 0
     * @return Valor entero entre [0, x)
     * @throws IllegalArgumentException Si x <= 0
     */
    public int randint(int x) {
        if (x <= 0) {
            throw new IllegalArgumentException("n debe ser mayor a 0");
        }
        int bits, val;
        do {
            bits = (int) (extract_number() >> (w - r));
            val = bits % x;
        } while (bits - val + (x - 1) < 0);
        return val;
    }

    /**
     * Genera un valor float pseudoaleatorio entre [0.0, x)
     * 
     * @param x Número float límite, x > 0.0
     * @return Valor float entre [0.0, x)
     * @throws IllegalArgumentException Si x <= 0
     */
    public float randfloat(float x) {
        if (x <= 0.0f) {
            throw new IllegalArgumentException("n debe ser mayor a 0.0");
        }
        return (float) this.random() * x;
    }

    /**
     * Genera un valor double pseudoaleatorio entre [0.0, x)
     * 
     * @param x Número double límite, x > 0.0
     * @return Valor double entre [0.0, x)
     * @throws IllegalArgumentException Si x <= 0.0
     */
    public double randdouble(double x) {
        if (x <= 0.0d) {
            throw new IllegalArgumentException("n debe ser mayor a 0.0");
        }
        return this.random() * x;
    }

    /**
     * Genera un número entero aleatorio entre [min, max - 1]
     * 
     * @param min Número entero incial, min < max
     * @param max Número entero final
     * @return Valor entero entre [min, max - 1]
     * @throws IllegalArgumentException Si min >= max
     */
    public int randrange(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("min debe ser menor que max");
        }
        return this.randint(max - min) + min;
    }

    /**
     * Obtener la semilla inicial
     * 
     * @return semilla inicial
     */
    public long getSeed() {
        return this.MT[0];
    }

    public static void main(String[] args) {
        System.out.println("Mersenne Twister 64 bit");
        MersenneTwister mt = new MersenneTwister();
        System.out.println("seed: " + mt.getSeed());

        int size = 8;

        System.out.println("random");
        double[] mtList = new double[size];
        for (int i = 0; i < size; i++) {
            mtList[i] = mt.random();
        }
        for (int i = 0; i < size; i++) {
            System.out.println(mtList[i]);
        }

        System.out.println("random int");
        int[] mtInt = new int[size];
        for (int i = 0; i < size; i++) {
            mtInt[i] = mt.randint(8);
        }
        for (int i = 0; i < size; i++) {
            System.out.println(mtInt[i]);
        }

        System.out.println("random float");
        float[] mtFloat = new float[size];
        for (int i = 0; i < size; i++) {
            mtFloat[i] = mt.randfloat(size);
        }
        for (int i = 0; i < size; i++) {
            System.out.println(mtFloat[i]);
        }

        System.out.println("random double");
        double[] mtDouble = new double[size];
        for (int i = 0; i < size; i++) {
            mtDouble[i] = mt.randdouble(size);
        }
        for (int i = 0; i < size; i++) {
            System.out.println(mtDouble[i]);
        }

        System.out.println("random range");
        int[] mtRange = new int[size];
        for (int i = 0; i < size; i++) {
            mtRange[i] = mt.randrange(-size, size);
        }
        for (int i = 0; i < size; i++) {
            System.out.println(mtRange[i]);
        }
    }
}
