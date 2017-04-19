package com.example.ale.medidas;
import android.preference.PreferenceFragment;
import android.os.Bundle;

/**
 * Created by ale on 19/04/2017.
 */

public class OpcionesFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.opciones);
    }
}