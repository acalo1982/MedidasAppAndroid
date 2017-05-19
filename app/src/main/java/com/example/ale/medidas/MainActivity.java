package com.example.ale.medidas;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import fastandroid.neoncore.collection.FaCollection;

public class MainActivity extends AppCompatActivity {

    private TCPClient mTcpClient; //objeto que recivirá y enviará msg al servidor!
    private TCPClientv2 mTcpClientv2;
    private int conectado = 0;//monitoriza el estado de conexión al VNA
    private LineGraphSeries<DataPoint> mSeries2;
    private LineGraphSeries<DataPoint> mSeries3;
    private GraphView graph;
    private static double c = 0.3;//veloc luz lista para dividir por GHz y obtener metros: "distancia =c/fGHz" (m)
    private String S11;
    private MathDatos S11m = null;
    private MathDatos S11back = null;
    private MathDatos S11ref = null;
    private MathDatos S11std3 = null;
    private MathDatos Rcoef = null;//coef. S11 calibrado (con filtrado mediante FFT)
    private ArrayList<MathDatos> Rlist = new ArrayList<>();//array con las medidas realizadas hasta ahora
    private MathDatos Rmedia;
    private MathDatosD Rmedia2;
    private confFreq confF;//param para pintar las graf en freq
    private String[] NumAreas;
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

        //Inicializamos la lista de areas disponibles a mostrar en el desplegable de la Actionbar
//        String[] datos = new String[]{"A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8", "A9", "A10"};
//        ArrayAdapter<String> adaptador = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, datos);//Adaptador: contiene los datos a mostrar
//        Spinner cmbOpciones = (Spinner)findViewById(R.id.CmbToolbar);
//        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        cmbOpciones.setAdapter(adaptador);
//        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(getSupportActionBar().getThemedContext(),android.R.layout.appbar_filter_title,datos);
//        //adaptador.setDropDownViewResource(R.layout.appbar_filter_list);
//
//        cmbOpciones.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                //... Acciones al seleccionar una opción de la lista
//                Log.i("Toolbar 3", "Seleccionada opción " + i);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//                //... Acciones al no existir ningún elemento seleccionado
//            }
//        });


        //CREAMOS UNOS EJES Y UNA CURVA DE EJEMPLO
        graph = (GraphView) findViewById(R.id.graph);
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


        //EVENTO: PULSAR SOBRE EL EJE (LO TRATAREMOS COMO UN OBJETO VIEW)
        graph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //lecturaS11(null);//debug en Modo Offline
                new connectTask().execute(":CALC:DATA:SDAT?");//debug en Modo Online
            }
        });

        //EVENTO: PULSACIÓN LARGA SOBRE EL EJE
        graph.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                borrarRlist();
                Toast.makeText(getApplicationContext(), "Borrar Medida!", Toast.LENGTH_SHORT).show();
                return true;
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

    //Actualiza el titulo del item "Nuevo" con el num de medidas actuales:a modo de contador
    public void setContMedItem() {
        invalidateOptionsMenu();//esto hace una llamada a la función "onPrepareOptionsMenu()" q tiene acceso a los items de la actionbar

    }

    //Borramos la última medida almacenada
    public void borrarRlist() {
        int Nmeas = Rlist.size();
        if (Nmeas > 1) {
            Rmedia2 = MathV.mediaDel(Rcoef, Rmedia2, Rlist.size());//actualizamos la media actual (tras el borrado)
            Rlist.remove(Nmeas - 1);//eliminamos el último elemento (1er elemento, indice i=0!)
            Rcoef = Rlist.get(Rlist.size() - 1);//actualizamos la última medida como la actual
            Log.e("borrarList", "alej: Nmeas = " + Rlist.size());
            //Grafica: medida filtrada
            graph.removeAllSeries();//borramos las series de los ejes
            MathV.pintarSerie(graph, Color.BLUE, Rcoef, confF.df, confF.xlimF, confF.ylimF, confF.fini, confF.N);
            MathV.pintarSerie(graph, Color.BLACK, Rmedia2, confF.df, confF.xlimF, confF.ylimF, confF.fini, confF.N);
            setContMedItem();//set contador de medidas en la actionbar
        }
        if (Nmeas == 1) {
            Rlist.remove(Nmeas - 1);//eliminamos el único elemento
            Rcoef = null;
            Rmedia = null;
            graph.removeAllSeries();//borramos los ejes
            setContMedItem();//set contador de medidas en la actionbar
        }
    }

    //Cada petición de lectura: Leer S11, Calibrarlo con Filtrado con FFT y Pintar Media actual y Medida
    public void lecturaS11(String msg) {
        //LECTURA de los datos del VNA
        //Modo Offline para Testeo: Cogemos siempre un valor de medida almacenados en fichero de conf. /data/data/com.example.ale.medidas/shared_prefs/*.xml
        //msg = PreferenceManager.getDefaultSharedPreferences(this).getString("medida", ""); //Modo Offline
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
            //Toast.makeText(getApplicationContext(), "(Num de puntos, filtro) = (" + N + "," + dR + ")", Toast.LENGTH_SHORT).show();
        }
        double df = (double) (fstop - fini) / (double) (N - 1);
        double[] xlimF = new double[]{fini, fstop};
        double[] ylimF = new double[]{-35, 5};
        double dx = 1 / (df * Nfft) * c;//retardo de ida y vuelta (dividimos entre 2 la distancia!)
        double dmax = 1 / df * c;//retardo de ida y vuelta (dividimos entre 2 la distancia!)
        double[] xlim = new double[]{0, dmax / 2};
        double[] ylim = new double[]{-20, 40};
        confF = new confFreq(df, xlimF, ylimF, fini, fstop, N);//guardamos la conf de medida

        //Toast.makeText(getApplicationContext(), "(Num de puntos,df) = (" + N + "," + df + ")", Toast.LENGTH_SHORT).show();

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
        if (!std3.equals("")) {
            S11std3 = MathV.vna2ReIm(std3);
        }
        //String to Vectors
        S11m = MathV.vna2ReIm(S11);
        S11back = MathV.vna2ReIm(back);
        S11ref = MathV.vna2ReIm(ref);

        //Testeo
//        Toast.makeText(getApplicationContext(), "CaL S11back N = "+ Nfft +" S11 = " + back, Toast.LENGTH_SHORT).show();
//        Toast.makeText(getApplicationContext(), "CaL S11ref N = "+ Nfft +" S11 = " + ref, Toast.LENGTH_SHORT).show();
//        Toast.makeText(getApplicationContext(), "CaL S11m N = "+ Nfft +" S11 = " + S11, Toast.LENGTH_SHORT).show();


        //Zero padding para la IFFT
        float[] Sb_Re = Arrays.copyOf(S11back.v1(), Nfft);//Background
        float[] Sb_Im = Arrays.copyOf(S11back.v2(), Nfft);
        float[] Sr_Re = Arrays.copyOf(S11ref.v1(), Nfft); //Reference
        float[] Sr_Im = Arrays.copyOf(S11ref.v2(), Nfft);
        float[] Sm_Re = Arrays.copyOf(S11m.v1(), Nfft);   //Medida
        float[] Sm_Im = Arrays.copyOf(S11m.v2(), Nfft);


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
        //MathV.pintarSerie(graph, Color.RED, Sb_t, dx/2, xlim, ylim);//retardo de ida y vuelta
        //MathV.pintarSerie(graph, Color.BLUE, Sr_t, dx/2, xlim, ylim);
        //MathV.pintarSerie(graph, Color.GREEN, Sm_t, dx/2, xlim, ylim);

        //Filtrado y CaL
        MathDatos[] Scal_t = MathV.filtrar(Sparam, dR, dx);

        //Realizamos "IFFT/Nfft"
        MathDatos[] S2 = MathV.calBackRef(Scal_t, N);
        Rcoef = S2[0];//S11 de la medida calibrado!


        //Añadimos la nueva medida a la lista de medidas
        Rlist.add(Rcoef);
        Log.e("lecturaS11", "alej: Nmeas = " + Rlist.size());
        MathV.pintarSerie(graph, Color.BLUE, Rcoef, df, xlimF, ylimF, fini, N);
        Rmedia2 = MathV.mediaAdd(Rcoef, Rmedia2, Rlist.size());//última media
        setContMedItem();//actualizamos el num de medidas en el menu

        //Grafica: medida filtrada
        graph.removeAllSeries();//borramos las series de los ejes
        MathV.pintarSerie(graph, Color.BLUE, Rcoef, df, xlimF, ylimF, fini, N);
        MathV.pintarSerie(graph, Color.BLACK, Rmedia2, df, xlimF, ylimF, fini, N);

        //Pintamos la operación inversa para ver si obtenemos la curva original "FFT(IFFT)": Para que sea correcto es necesario hacer "1/Nfft*FFT(IFFT)"
        //(sólo pintamos los N primeros valores, pq los restantes hasta llegar a Nfft serán 0: zero padding!!)
//        FaCollection.fft_float32(Sr_t, Si_t);

    }

    //Sobreescribir el comportamiento pulsar el botón "hacia atrás" en la actividad principal: evitar que se cierre la app y perder los datos no guardados!
    //Además, parece que se puede capturar el evento "right click" de un ratón conectado por micro-usb/bluetooth
    @Override
    public void onBackPressed() {
        //Log.e("MainActivity","alej: Botón dcho ratón o Botón Atrás");
        Toast.makeText(getApplicationContext(), "Pulsado Botón dcho ratón o Botón Atrás", Toast.LENGTH_SHORT).show();
        // your code.
    }

    //Actualiza la actionbar tras una llamada a "invalidateOptionsMenu()" (aprovecharemos para cambiar el título del item de la actionbar q usaremos para mostrar el num. medidas actual
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_nuevo);
        int Nmed = Rlist.size();
        String txt = "M: " + Nmed;
        item.setTitle(txt);
        //Log.i("ActionBar.Nuevo", "alej: Nmed = "+Nmed);
        return true;
    }

    //Asociamos el menu a la ActionBar por defecto de la App
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        //Spinner: Asociamos el Adaptador con el contenido
        MenuItem item = menu.findItem(R.id.CmbToolbar);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        int Nareas;
        if (pref.getString("Narea", "").equals("")){
            Nareas=7;
        }else{
            Nareas=Integer.parseInt(pref.getString("Narea", ""));
        }


        int[] datos2= new int[Nareas];
        Log.e("OnCreateOptionMenu","alej: [Filtro, Narea, datos2] = "+Nareas+", "+pref.getString("filtro", "")+", "+datos2[0]+"]");
//        for (int i=0;i<Nareas;i+=1){
//            datos[0]=""+i;
//        }
        String[] datos = new String[]{"A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8", "A9", "A10"};
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(getSupportActionBar().getThemedContext(), android.R.layout.simple_spinner_item, android.R.id.text1,datos);//Adaptador: contiene los datos a mostrar
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adaptador);

        return true;
    }

    //Detecta qué botón de la ActionBar se ha pulsado
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_nuevo:
                return true;
            case R.id.action_buscar: //Pulsado sobre el icono de configuracion
                Log.i("ActionBar", "Settings!");
                //Intent intent = new Intent(this, Configuracion.class); // Abrimos la pantalla de configuracion: Implementada como una secundaria Activity
                Intent intent = new Intent(this, PantallaConf.class); // Abrimos Activity secundaria que hace uso del fragment XML como su propia UI
                startActivity(intent);
                return true;
            case R.id.action_settings: //Icono que muestra si estamos online (conectados al VNA)
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


