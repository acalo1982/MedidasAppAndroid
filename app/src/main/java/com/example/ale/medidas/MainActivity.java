package com.example.ale.medidas;

import android.os.AsyncTask;
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

public class MainActivity extends AppCompatActivity {

    private TCPClient mTcpClient; //objeto que recivirá y enviará msg al servidor!
    private TCPClientv2 mTcpClientv2;
    private int conectado=0;//monitoriza el estado de conexión al VNA
    private String S11=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // OPCIONES RELACIONADAS CON LA ACTIONBAR
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//evitamos que el salvapantallas aparezca en esta Actividad
//        getSupportActionBar().setDisplayShowTitleEnabled(true);
//        getSupportActionBar().setTitle("Ale");
//        getSupportActionBar().setIcon(R.drawable.dani_icon);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        //getSupportActionBar().setLogo(R.drawable.dani_icon);
        //getSupportActionBar().setDisplayUseLogoEnabled(true);

        //CREAMOS UNOS EJES Y UNA CURVA DE EJEMPLO
        GraphView graph = (GraphView) findViewById(R.id.graph);
        DataPoint[] points = new DataPoint[100]; // array de objetos DataPoint
        for (int i = 0; i < points.length; i++) { //inicializamos el array de DataPoints con valores (x,y)
            points[i] = new DataPoint(i, Math.sin(i * 0.5) * 10 * (Math.random() * 10 + 1));
        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(points); //Creamos una curva a partir del array de puntos
        // set manual Y bounds
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(-150);
        graph.getViewport().setMaxY(150);
        // set manual X bounds
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(4);
        graph.getViewport().setMaxX(80);
        // enable scaling and scrolling
        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(true);
        graph.addSeries(series); //Dibuamos la curva sobre los ejes


        //EVENTO: PULSAR SOBRE EL EJE (LO TRATAREMOS COMO UN OBJETO VIEW)
        graph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lecturaS11(view);
            }
        });
    }

    public void lecturaS11(View v){
        //LECTURA de los datos del VNA
        new connectTask().execute(":CALC:DATA:SDAT?");

        //Convertimos el String en DataPoint
        String[] S11_list=S11.split(",");
        int N=S11_list.length/2;//string array con la parte real y compleja del S11 para cada freq
        int df=(18-2)/(N-1);//incremento en freq
        DataPoint[] points = new DataPoint[N];
        for (int i = 0; i < N; i+=2) {
            double yR=Float.parseFloat(S11_list[i]);
            double yI=Float.parseFloat(S11_list[i+1]);
            double M=Math.sqrt(Math.pow(yR,2)+Math.pow(yI,2));//modulo
            points[i] = new DataPoint(2+i*df,M);
        }

        //Dibujamos la curva leida
        GraphView graph = (GraphView) findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(points); //Creamos una curva a partir del array de puntos
        // set manual Y bounds
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(-35);
        graph.getViewport().setMaxY(5);
        // set manual X bounds
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(2);
        graph.getViewport().setMaxX(18);
        // enable scaling and scrolling
        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(true);
        graph.addSeries(series); //Dibuamos la curva sobre los ejes
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
                    //item.setIcon(android.R.drawable.presence_online);
                    item.setIcon(android.R.drawable.ic_notification_overlay);
                    Log.e("ActionBar", "alej: Conectado: boton en rojo");
                    //Configuración del VNA
                    String[] cmd_conf={":INST \"NA\"",":SOUR:POW:ALC HIGH",":INIT:CONT 1",":FREQ:STAR 2e9",":FREQ:STOP 18e9",":SWE:POIN 201",":BWID 1000",":AVER:COUN 1"};
                    new connectTask().execute(cmd_conf);
                }else{
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
            if (message.length==1) { //Enviamos 1 comando
                mTcpClient.run(message[0]); //abrimos el socket de comunicacion con el servidor
            }else{ //Varios comandos a las vez
                mTcpClient.run(message);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            String msg = values[0]; //recuperamos el texto como parámetro de entrada
            Toast.makeText(getApplicationContext(), "Num de puntos: " + msg.length() + " Rxdo msg=" + msg, Toast.LENGTH_SHORT).show();
            //mTcpClient.stopClient();
        }
    }


    //Cliente v2: Comparte un socket para enviar varios comandos y escuchar las respuestas enviadas por el servidor
    public class connectTaskv2 extends AsyncTask<Void, String, Boolean> {
        @Override
        protected Boolean doInBackground(Void... values) {
            mTcpClientv2.abrirSocket();
            return null;
        }
    }
    public class connectTaskv3 extends AsyncTask<Void, String, Boolean> {
        @Override
        protected Boolean doInBackground(Void... values) {
            String msg = mTcpClientv2.leerMessage();
            publishProgress(msg);
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            String msg = values[0]; //recuperamos el texto como parámetro de entrada
            S11 = values[0];
            Toast.makeText(getApplicationContext(), "Leído S11=" + S11, Toast.LENGTH_SHORT).show();
        }
    }


}


