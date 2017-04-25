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
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private TCPClient mTcpClient; //objeto que recivirá y enviará msg al servidor!
    private TCPClientv2 mTcpClientv2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // OPCIONES RELACIONADAS CON LA ACTIONBAR
        setContentView(R.layout.activity_main);
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
            points[i] = new DataPoint(i, Math.sin(i*0.5) * 20*(Math.random()*10+1));
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

        //CREAMOS UN SOCKET DE COMUNICACIÓN
        mTcpClientv2 = new TCPClientv2(); //creamos objeto y asiganamos una referencia a él
        //new connectTaskv2().execute(); //abrimos socket (v2)


        //EVENTO DE PULSAR SOBRE EL EJE (LO TRATAREMOS COMO UN OBJETO VIEW)
        graph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //sends the message to the server

                //Cliente v1: Se abre y cierra un socket para comando
                new connectTask().execute("Datos");//en el nuevo hilo se envía 1 comando y se espera su respuesta

                //Cliente v2: Se abre un socket y se deja abierto para enviar todos los comandos
                //mTcpClientv2.sendMessage("Ale"); //enviamos datos al servidor
                //new connectTaskv3().execute();//leemos datos del servidor (v2)
                //Log.e("MainActivity", "alej: Antes de llamar a mTcpClient.sendMessage()");




            }
        });
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
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class connectTask extends AsyncTask<String,String,TCPClient> {

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
            mTcpClient.run(message[0]); //abrimos el socket de comunicacion con el servidor
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            String msg=values[0]; //recuperamos el texto como parámetro de entrada
            Toast.makeText(getApplicationContext(), "Rxdo msg="+msg, Toast.LENGTH_SHORT).show();
            //mTcpClient.stopClient();
        }
    }

    public class connectTaskv2 extends AsyncTask<Void,String,Boolean> {

        @Override
        protected Boolean doInBackground(Void... values) {
            mTcpClientv2.abrirSocket();
            return null;
        }
    }

    public class connectTaskv3 extends AsyncTask<Void,String,Boolean> {

        @Override
        protected Boolean doInBackground(Void... values) {
            String msg=mTcpClientv2.leerMessage();
            publishProgress(msg);
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            String msg=values[0]; //recuperamos el texto como parámetro de entrada
            Toast.makeText(getApplicationContext(), "Rxdo msg="+msg, Toast.LENGTH_SHORT).show();
            //mTcpClient.stopClient();
        }
    }


}


