package com.example.ale.medidas;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class PantallaConf extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_pantalla_conf);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new OpcionesFragment())
                .commit();
    }
}

