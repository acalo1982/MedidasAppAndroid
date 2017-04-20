package com.example.ale.medidas;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

public class PantallaConf extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_pantalla_conf); // Para asociar a esta actividad su layout

        //Sustituir el layout de la actividad por el del fragment que hemos definido
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new OpcionesFragment())
                .commit();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//activamos el botón en la ActionBar para volver hacia atrás

    }

    //Sobreescribe el metodo q se ejecuta al pulsar un icono de la ActionBar mostrada en esta actividad
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // API 5+ solution
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

