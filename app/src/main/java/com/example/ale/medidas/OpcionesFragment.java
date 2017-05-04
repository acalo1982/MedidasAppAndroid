package com.example.ale.medidas;
import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.Preference;
import android.widget.Toast;

/**
 * Created by ale on 19/04/2017.
 */

public class OpcionesFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.opciones);
        //Añadimos una respuesta al clic para los objetos Preferences de la pantalla de Conf
        //Boton Background
        Preference back = findPreference("background");
        back.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Toast.makeText(getActivity(), "Lectura del Background", Toast.LENGTH_SHORT).show();

                return true;
            }
        });

        //Boton Reference
        Preference ref = findPreference("reference");
        ref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Toast.makeText(getActivity(), "Lectura del Reference", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        //Boton 3er Estandar de calibración
        Preference cal3 = findPreference("plex2mm");
        cal3.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Toast.makeText(getActivity(), "Lectura del 3er Estándar", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }
}