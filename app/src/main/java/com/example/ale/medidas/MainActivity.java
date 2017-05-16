package com.example.ale.medidas;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.*;

import android.content.pm.ActivityInfo;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.Arrays;

import fastandroid.neoncore.collection.FaCollection;

public class MainActivity extends AppCompatActivity {

    private TCPClient mTcpClient; //objeto que recivirá y enviará msg al servidor!
    private TCPClientv2 mTcpClientv2;
    private int conectado = 0;//monitoriza el estado de conexión al VNA
    private LineGraphSeries<DataPoint> mSeries2;
    private LineGraphSeries<DataPoint> mSeries3;
    private GraphView graph;
    private static double c = 0.3;
    private String S11;
    private MathDatos S11m = null;
    private MathDatos S11back = null;
    private MathDatos S11ref = null;
    private MathDatos S11std3 = null;

//    @Override
//    protected void onResume(){
//        super.onResume();  // Always call the superclass method first
//        //Comprueba cuando vengamos de vuelta de la pantalla de configuración, q tenemos la calibración
//        if (Sback==null){}
//        if (Sref==null){}
//        if (S3std==null){}
//
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // OPCIONES RELACIONADAS CON LA ACTIONBAR
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//evitamos que el salvapantallas aparezca en esta Actividad
        //int flg=View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY; //en modo inmersivo: fullscreen + actionbar y softbar aparece/desaparece
        //flg=View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        //flg=View.SYSTEM_UI_FLAG_FULLSCREEN;
        //getWindow().getDecorView().setSystemUiVisibility(flg);
        //getSupportActionBar().show();

//        getSupportActionBar().setDisplayShowTitleEnabled(true);
//        getSupportActionBar().setTitle("Ale");
//        getSupportActionBar().setIcon(R.drawable.dani_icon);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        //getSupportActionBar().setLogo(R.drawable.dani_icon);
        //getSupportActionBar().setDisplayUseLogoEnabled(true);

        //CREAMOS UNOS EJES Y UNA CURVA DE EJEMPLO
        graph = (GraphView) findViewById(R.id.graph);
//        DataPoint[] points = new DataPoint[100]; // array de objetos DataPoint
//        for (int i = 0; i < points.length; i++) { //inicializamos el array de DataPoints con valores (x,y)
//            points[i] = new DataPoint(i, Math.sin(i * 0.5) * 10 * (Math.random() * 10 + 1));
//        }
        //LineGraphSeries<DataPoint> series = new LineGraphSeries<>(points); //Creamos una curva a partir del array de puntos
//        mSeries2 = new LineGraphSeries<>();//creamos una serie vacia (la serie puede contener multiples curvas: DataPoints)
//        graph.addSeries(mSeries2);//añadimos la serie a los ejes
        // set manual Y bounds
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(-35);
        graph.getViewport().setMaxY(2);
        // set manual X bounds
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(2);
        graph.getViewport().setMaxX(18);
        // enable scaling and scrolling
        graph.getViewport().setScalable(false);
        graph.getViewport().setScalableY(false);
        //graph.addSeries(curvas); //Dibuamos la curva sobre los ejes


        //EVENTO: PULSAR SOBRE EL EJE (LO TRATAREMOS COMO UN OBJETO VIEW)
        graph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lecturaS11(null);//debug en Modo Offline
                //new connectTask().execute(":CALC:DATA:SDAT?");//debug en Modo Offline
            }
        });
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            if (hasFocus) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
        }
    }

    public void lecturaS11(String msg) {
        //LECTURA de los datos del VNA
        //Modo Offline para Testeo: Cogemos siempre un valor de medida almacenados en fichero de conf. /data/data/com.example.ale.medidas/shared_prefs/*.xml
        msg = PreferenceManager.getDefaultSharedPreferences(this).getString("medida", "");
        S11 = msg;

        //Recuperamos la configuración
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        //Guardamos la medida en la preferencia (fase de testeo: para ir probando cosas con datos correctos)
//        SharedPreferences.Editor editor = pref.edit();
//        editor.putString("medida", msg);
//        editor.commit();
        //Toast.makeText(getApplicationContext(), "Freq range: [" + pref.getString("freq1", "") + "," + pref.getString("freqEnd", "") + "] Filtro: " + pref.getString("filtro", ""), Toast.LENGTH_SHORT).show();


        //VAlores de configuración
        float fini = 2;
        float fstop = 18;
        double dR = 0.2;//así se declara un número como float (32 bits)
        int N = 201;
        int Nfft = (int) Math.pow(2, 10);//long del vector S11 (201 puntos) + zero padding (ceros hasta los 1024 puntos: 10 bits)
        if (pref.getString("freq1", "") != null) {
            fini = Float.parseFloat(pref.getString("freq1", ""));
            fstop = Float.parseFloat(pref.getString("freqEnd", ""));
            dR = Float.parseFloat(pref.getString("filtro", ""));
            N = (int) Float.parseFloat(pref.getString("npoint", ""));
            Toast.makeText(getApplicationContext(), "(Num de puntos,filtro) = (" + N + "," + dR + ")", Toast.LENGTH_SHORT).show();
        }
        double df = (double) (fstop - fini) / (N - 1);
        double[] xlimF = new double[]{fini, fstop};
        double[] ylimF = new double[]{-35, 5};

        Toast.makeText(getApplicationContext(), "(Num de puntos,df) = (" + N + "," + df + ")", Toast.LENGTH_SHORT).show();

        //Medida y Recuperar CaL: Back, ref y 3erStd
        String back = pref.getString("background", "");
        String ref = pref.getString("reference", "");
        String std3 = pref.getString("plex2mm", "");
        //String med = pref.getString("medida", "");
        String txt = "";
        if (back.equals("")) {
            txt += "Back";
        }
        if (ref.equals("")) {
            txt += ".Ref";
        }
        if (!txt.equals("")) {
            Toast.makeText(getApplicationContext(), "CaL Incompleta! Falta: " + txt, Toast.LENGTH_SHORT).show();
            return;
        }
        if (std3.equals("")) {
            S11std3 = MathV.vna2ReIm(std3);
        }
        //String to Vectors
        S11m = MathV.vna2ReIm(S11);
        S11back = MathV.vna2ReIm(back);
        S11ref = MathV.vna2ReIm(ref);
        //Testeo
//        Toast.makeText(getApplicationContext(), "CaL S11back N = "+ Nfft +" S11 = " + back, Toast.LENGTH_SHORT).show();
//        Toast.makeText(getApplicationContext(), "CaL S11ref N = "+ Nfft +" S11 = " + ref, Toast.LENGTH_SHORT).show();
//        Toast.makeText(getApplicationContext(), "CaL S11m N = "+ Nfft +" S11 = " + med, Toast.LENGTH_SHORT).show();


        //Zero padding para la IFFT
        float[] Sb_Re = Arrays.copyOf(S11back.v1(), Nfft);//Background
        float[] Sb_Im = Arrays.copyOf(S11back.v2(), Nfft);
        float[] Sr_Re = Arrays.copyOf(S11ref.v1(), Nfft); //Reference
        float[] Sr_Im = Arrays.copyOf(S11ref.v2(), Nfft);
        float[] Sm_Re = Arrays.copyOf(S11m.v1(), Nfft);   //Medida
        float[] Sm_Im = Arrays.copyOf(S11m.v2(), Nfft);

        //Test: funciona la librería de la FFT
        //String test_neon = FaCollection.test_fft();
        //Toast.makeText(getApplicationContext(), "FFT test con FaCollection: "+test_neon, Toast.LENGTH_SHORT).show();
        //Log.e("MainActivity", "alej: FFT FaCollection: "+ test_neon);

        //Realizamos la IFFT
        float[] Sb_Re_t = Sb_Re;//copia de los valores en freq y tras la fft será rellenado cn los valores en el tiempo
        float[] Sb_Im_t = Sb_Im;
        float[] Sr_Re_t = Sr_Re;
        float[] Sr_Im_t = Sr_Im;
        float[] Sm_Re_t = Sm_Re;
        float[] Sm_Im_t = Sm_Im;
        FaCollection.ifft_float32(Sb_Re_t, Sb_Im_t);//esta función modifica el contenido de las variables "Sr_t" y "Si_t" (como si fueran punteros!)
        FaCollection.ifft_float32(Sr_Re_t, Sr_Im_t);
        FaCollection.ifft_float32(Sm_Re_t, Sm_Im_t);
        MathDatos Sb_t = new MathDatos(Sb_Re_t, Sb_Im_t);
        MathDatos Sr_t = new MathDatos(Sr_Re_t, Sr_Im_t);
        MathDatos Sm_t = new MathDatos(Sm_Re_t, Sm_Im_t);
        MathDatos[] Sparam = new MathDatos[]{Sb_t, Sr_t, Sm_t};//array de objetos MathDatos (CaL y Medida)

        //Pintamos la IFFT
        //graph.removeAllSeries();//borramos las series de los ejes
        //mSeries3 = new LineGraphSeries<>();
        //LineGraphSeries<DataPoint> mSeries4 = new LineGraphSeries<>();
        double dx = 1 / (df * Nfft) * c;//retardo de ida y vuelta (dividimos entre 2 la distancia!)
        double dmax = 1 / df * c;//retardo de ida y vuelta (dividimos entre 2 la distancia!)
        double[] xlim = new double[]{0, dmax / 2};
        double[] ylim = new double[]{-20, 40};
        //MathV.pintarSerie(graph, Color.RED, Sb_t, dx, xlim, ylim);
        //MathV.pintarSerie(graph, Color.BLUE, Sr_t, dx, xlim, ylim);//pinta la curva sobre los ejes
        //MathV.pintarSerie(graph, Color.GREEN, Sm_t, dx, xlim, ylim);

        //Filtrado y CaL
        MathDatos[] Scal_t = MathV.filtrar(Sparam, dR, dx);

        //Realizamos "IFFT/Nfft"
        MathDatos R = MathV.calBackRef(Scal_t);
//        float[] SmRe2 = R.v1();
//        float[] SmIm2 = R.v2();
//        FaCollection.fft_float32(SmRe2, SmIm2);
//        MathDatos Sm2 = new MathDatos(SmRe2, SmIm2);

        //Grafica: medida filtrada
        MathV.pintarSerie(graph, Color.BLUE, R, df, xlimF, ylimF);


        //Pintamos la operación inversa para ver si obtenemos la curva original "FFT(IFFT)": Para que sea correcto es necesario hacer "1/Nfft*FFT(IFFT)"
        //(sólo pintamos los N primeros valores, pq los restantes hasta llegar a Nfft serán 0: zero padding!!)
//        FaCollection.fft_float32(Sr_t, Si_t);

    }

    //Asociamos el menu a la ActionBar por defecto de la App
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //Detecta qué botón de la ActionBar se ha pulsado
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_nuevo:
                Log.i("ActionBar", "Nuevo!");
                return true;
            case R.id.action_buscar: //Pulsado sobre el icono de configuracion
                Log.i("ActionBar", "Settings!");
                //Intent intent = new Intent(this, Configuracion.class); // Abrimos la pantalla de configuracion: Implementada como una secundaria Activity
                Intent intent = new Intent(this, PantallaConf.class); // Abrimos Activity secundaria que hace uso del fragment XML como su propia UI
                startActivity(intent);
                return true;
            case R.id.action_settings:
                Log.i("ActionBar", "Settings!");
                if (conectado == 0) {
                    conectado = 1;
                    Log.e("ActionBar", "alej: Conectado: boton en rojo");
                    //Configuración del VNA
                    String[] cmd_conf = {":INST \"NA\"", ":SOUR:POW:ALC HIGH", ":INIT:CONT 1", ":FREQ:STAR 2e9", ":FREQ:STOP 18e9", ":SWE:POIN 201", ":BWID 1000", ":AVER:COUN 1"};
                    new connectTask().execute(cmd_conf);
                    //item.setIcon(android.R.drawable.presence_online);
                    item.setIcon(android.R.drawable.ic_notification_overlay);
                } else {
                    conectado = 0;
                    item.setIcon(android.R.drawable.presence_invisible);
                    Log.e("ActionBar", "alej: Desconectado: boton en gris");
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Cliente v1: Se abre/cierra un socket en el envío de cada comando (si el comando es de request, se espera a la respuesta del VNA)
    public class connectTask extends AsyncTask<String, String, TCPClient> {
        @Override
        protected TCPClient doInBackground(String... message) {
            //we create a TCPClient object and
            mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            });
            if (message.length == 1) { //Enviamos 1 comando
                mTcpClient.run(message[0]); //abrimos el socket de comunicacion con el servidor
            } else { //Varios comandos a las vez
                mTcpClient.run(message);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            String msg = values[0];
            //Toast.makeText(getApplicationContext(), "Num de puntos: " + msg.length() + " Leído S11=" + msg, Toast.LENGTH_SHORT).show();
            lecturaS11(msg);//Tras recibir un msg, llamamos a la función que lo procesa
            //mTcpClient.stopClient();
        }
    }


//    //Cliente v2: Comparte un socket para enviar varios comandos y escuchar las respuestas enviadas por el servidor
//    public class connectTaskv2 extends AsyncTask<Void, String, Boolean> {
//        @Override
//        protected Boolean doInBackground(Void... values) {
//            mTcpClientv2.abrirSocket();
//            return null;
//        }
//    }
//    public class connectTaskv3 extends AsyncTask<Void, String, Boolean> {
//        @Override
//        protected Boolean doInBackground(Void... values) {
//            String msg = mTcpClientv2.leerMessage();
//            publishProgress(msg);
//            return null;
//        }
//
//        @Override
//        protected void onProgressUpdate(String... values) {
//            super.onProgressUpdate(values);
//            String msg = values[0]; //recuperamos el texto como parámetro de entrada
//            Toast.makeText(getApplicationContext(), "Num de puntos: " + msg.length() + " Rxdo msg=" + msg, Toast.LENGTH_SHORT).show();
//        }
//    }


}


