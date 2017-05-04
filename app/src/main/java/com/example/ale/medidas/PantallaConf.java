package com.example.ale.medidas;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.jjoe64.graphview.series.DataPoint;


public class PantallaConf extends AppCompatActivity {

    private TCPClient mTcpClient; //objeto que recivirá y enviará msg al servidor!
    private DataPoint[] S11back=null;
    private DataPoint[] S11ref=null;
    private DataPoint[] S113std=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_pantalla_conf); // Para asociar a esta actividad su layout

        //Sustituye el layout de la actividad por el del fragment que hemos definido
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new OpcionesFragment()).commit();

        //Activa el botón en la ActionBar para volver hacia atrás
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    //Sobreescribe el metodo q se ejecuta al pulsar un icono de la ActionBar mostrada en esta actividad
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: //clic en el boton de ir para atrás!
                // API 5+ solution
                onBackPressed(); //volvemos a la Actividad principal
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void lecturaS11(String msg) {
        //LECTURA de los datos del VNA
        String S11 = msg;
        //Toast.makeText(getApplicationContext(), "Num de puntos: " + msg.length() + " Leído S11=" + msg, Toast.LENGTH_SHORT).show();

        //Convertimos el String en DataPoint
        String[] S11_list = S11.split(",");
        int N = S11_list.length / 2;//string array con la parte real y compleja del S11 para cada freq
        double df = (double) (18 - 2) / (N - 1);//incremento en freq
        Toast.makeText(getApplicationContext(), "alej: (Num de puntos,S11(1),df) = (" + N + "," + S11_list[0] + "," + df + ")", Toast.LENGTH_SHORT).show();

        DataPoint[] points = new DataPoint[N];
        for (int i = 0; i < N; i += 2) {
            double yR = Float.parseFloat(S11_list[i]);
            double yI = Float.parseFloat(S11_list[i + 1]);
            double M = Math.sqrt(Math.pow(yR, 2) + Math.pow(yI, 2));//modulo
            M = 20 * Math.log10(M);//modulo en dB
            double x = (double) 2 + i * df;
            points[i] = new DataPoint(x, M);
            Log.e("DataPoint", "alej: (x,y)=(" + (x) + "," + (M) + ")");
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
        }
    }
}
