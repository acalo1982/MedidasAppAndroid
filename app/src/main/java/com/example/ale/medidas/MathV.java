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

    //Modulo de 2 vectores uno a uno
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
        float[] v1 = new float[N/2];
        float[] v2 = new float[N/2];
        int idx = 0;
        for (int i = 0; i < 1 * N; i += 2) {
            v1[idx] = Float.parseFloat(s11[i]);//parte real
            v2[idx] = Float.parseFloat(s11[i + 1]);//parte imag
            idx += 1;
        }
        MathDatos v3 = new MathDatos(v1, v2);
        return v3;
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