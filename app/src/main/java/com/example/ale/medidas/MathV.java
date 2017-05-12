package com.example.ale.medidas;

/**
 * Created by ale on 11/05/2017.
 */


// Utility classes should always be final (no se pueden heredar y extender) and have a private constructor (no se pueden instanciar): conjunto de métodos estáticos (librería)
public final class MathV {

    private MathV() {
        // Utility classes should always be final and have a private constructor
    }


    //Producto de 2 vectores uno a uno
    public static double[] prodV(double[] v1, double[] v2) {
        double[] v3 = null;
        if (v1.length == v2.length) {
            int N = v1.length;
            for (int i = 0; i < N; i += 1) {
                v3[i] = v1[i] * v2[2];
            }
            return v3;
        } else {
            return null;
        }
    }

    //Suma de vectores componente a componente
    public static double[] sumV(double[] v1, double[] v2) {
        double[] v3 = null;
        if (v1.length == v2.length) {
            int N = v1.length;
            for (int i = 0; i < N; i += 1) {
                v3[i] = v1[i] + v2[2];
            }
            return v3;
        } else {
            return null;
        }
    }

    //Suma ponderada de vectores componente a componente (puede tambien ser una resta: w1=1 y w2=-1 o al revés)
    public static double[] sum_wV(double[] v1, double[] v2, double w1, double w2) {
        double[] v3 = null;
        if (v1.length == v2.length) {
            int N = v1.length;
            for (int i = 0; i < N; i += 1) {
                v3[i] = w1 * v1[i] + w2 * v2[2];
            }
            return v3;
        } else {
            return null;
        }
    }

    //Modulo de 2 vectores uno a uno
    public static double[] absdBV(double[] v1, double[] v2) {
        double[] v3 = null;
        if (v1.length == v2.length) {
            int N = v1.length;
            for (int i = 0; i < N; i += 1) {
                double M = Math.sqrt(Math.pow(v1[i], 2) + Math.pow(v2[i], 2));//modulo
                v3[i] = 20 * Math.log10(M);//modulo en dB
            }
            return v3;
        } else {
            return null;
        }
    }

    //Modulo de 2 vectores uno a uno (float)
    public static float[] absdBV(float[] v1, float[] v2) {
        float[] v3 = null;
        if (v1.length == v2.length) {
            int N = v1.length;
            double M;
            for (int i = 0; i < N; i += 1) {
                M = Math.sqrt(Math.pow(v1[i], 2) + Math.pow(v2[i], 2));//modulo
                v3[i] = (float) (20 * Math.log10(M));//modulo en dB
            }
            return v3;
        } else {
            return null;
        }
    }

    //Modulo de cada una de las componentes de un vector complejo: MathDatos
    public static float[] absdBV(MathDatos a1) {
        float[] v3 = null;
        float[] v1 = a1.v1();
        float[] v2 = a1.v2();
        return absdBV(v1, v2);
    }
    //Modulo de la combinación de 2 vectores
    public static double[] absdB_wV(MathDatos a1, MathDatos a2, double w1, double w2) {
        double[] v3 = null;
        float[] v1Re = a1.v1();
        float[] v1Im = a1.v2();
        float[] v2Re = a2.v1();
        float[] v2Im = a2.v2();
        double M1,M2;
        if (v1Re.length == v2Re.length) {
            int N = v1Re.length;
            for (int i = 0; i < N; i += 1) {
                M1 = Math.sqrt(Math.pow(v1Re[i], 2) + Math.pow(v1Im[i], 2));//modulo1
                M2 = Math.sqrt(Math.pow(v2Re[i], 2) + Math.pow(v2Im[i], 2));//modulo2
                v3[i] = Math.abs(M1-M2);//diferencia entre los modulos para cada componente del vector
            }
        }
        return v3;
    }

    //Modulo de 2 vectores en unidades naturales
    public static double[] absV(double[] v1, double[] v2) {
        double[] v3 = null;
        if (v1.length == v2.length) {
            int N = v1.length;
            for (int i = 0; i < N; i += 1) {
                double M = Math.sqrt(Math.pow(v1[i], 2) + Math.pow(v2[i], 2));//modulo
                v3[i] = 20 * Math.log10(M);//modulo en dB
            }
            return v3;
        } else {
            return null;
        }
    }

    //Convertimos un string con los datos devueltos por el VNA a un vector 2D de double
    public static MathDatos vna2ReIm(String S) {
        String[] s11 = S.split(",");//se divide el string en arrays d string con ls digitos en el formato "2.3E-2"
        int N = s11.length;//long del doble de elementos: los partes reales y imaginarias
        float[] v1 = new float[N / 2];
        float[] v2 = new float[N / 2];
        int idx = 0;
        for (int i = 0; i < 1 * N; i += 2) {
            v1[idx] = Float.parseFloat(s11[i]);//parte real
            v2[idx] = Float.parseFloat(s11[i + 1]);//parte imag
            idx += 1;
        }
        MathDatos v3 = new MathDatos(v1, v2);
        return v3;
    }

    public static float[] w_hamming(int N) {
        float[] v3 = new float[N];
        for (int i = 0; i < N; i += 1) {
            v3[i] = (float) (0.54 - 0.46 * Math.cos(2 * Math.PI / N * i));
        }
        return v3;
    }

    public static MathDatos[] filtrar(MathDatos[] Scal, double dR) {
        MathDatos[] Sfil = new MathDatos[3];
        //CaL y Medida
        MathDatos Sb_t = Scal[0];//back
        MathDatos Sr_t = Scal[1];//ref
        MathDatos Sm_t = Scal[2];//med

        //Posicion de filtrado: distancia a la que ocurre la reflex.
        double[] dist=absdB_wV(Sb_t, Sr_t, 1, -1);

        return Sfil;
    }
}

class MathDatos {
    public float[] a1 = null;
    public float[] a2 = null;

    public MathDatos(float[] a, float[] b) {
        a1 = a;
        a2 = b;
    }

    public float[] v1() {
        return a1;
    }

    public float[] v2() {
        return a2;
    }
}