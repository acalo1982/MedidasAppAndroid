package com.example.ale.medidas;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.*;
import android.content.pm.ActivityInfo;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_main);
//        getSupportActionBar().setDisplayShowTitleEnabled(true);
//        getSupportActionBar().setTitle("Ale");
//        getSupportActionBar().setIcon(R.drawable.dani_icon);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        //getSupportActionBar().setLogo(R.drawable.dani_icon);
        //getSupportActionBar().setDisplayUseLogoEnabled(true);

        GraphView graph = (GraphView) findViewById(R.id.graph);
        DataPoint[] points = new DataPoint[100];
        for (int i = 0; i < points.length; i++) {
            points[i] = new DataPoint(i, Math.sin(i*0.5) * 20*(Math.random()*10+1));
        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(points);
        // set manual X bounds
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(-150);
        graph.getViewport().setMaxY(150);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(4);
        graph.getViewport().setMaxX(80);

        // enable scaling and scrolling
        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(true);

        graph.addSeries(series);

        //        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
//                new DataPoint(0, 1),
//                new DataPoint(1, 5),
//                new DataPoint(2, 3)
//        });
//        graph.addSeries(series);

    }


    //Asociamos el menu a la ActionBar por defecto de la App
    //ale
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
}
