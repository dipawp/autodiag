package com.dipasoftware.autodiag.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dipasoftware.autodiag.R;
import com.dipasoftware.autodiag.settings.BluetoothSettingsFragment;

/******************************************************************************
 *
 * Classe.....: SettingsFragment
 *
 * Tipo.......: Fragment
 *
 * Package....: com.dipasoftware.autodiag.settings
 *
 * Descrizione:
 *
 * Schermata principale delle impostazioni di AutoDiag.
 *
 ```java
 package com.dipasoftware.autodiag.settings;

 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;

 import androidx.annotation.NonNull;
 import androidx.annotation.Nullable;
 import androidx.fragment.app.Fragment;

 import com.dipasoftware.autodiag.R;

 /******************************************************************************
 *
 * Classe.....: SettingsFragment
 *
 * Tipo.......: Fragment
 *
 * Package....: com.dipasoftware.autodiag.settings
 *
 * Descrizione:
 *
 * Schermata principale delle impostazioni di AutoDiag.
 *
 * Contiene l'accesso alle impostazioni Bluetooth.
 *
 * Le schermate secondarie vengono visualizzate nel
 * contenitore interno del Fragment.
 *
 ******************************************************************************/
public class SettingsFragment extends Fragment {

    /**
     * Contenuto principale della schermata Settings.
     */
    private View settingsMainContent;

    /**
     * Costruttore obbligatorio del Fragment.
     */
    public SettingsFragment() {

        super(R.layout.fragment_settings);
    }

    /**
     * Crea la View del Fragment.
     *
     * @param inflater inflater Android.
     * @param container container padre.
     * @param savedInstanceState stato precedente.
     *
     * @return View del Fragment.
     */
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_settings,
                container,
                false
        );
    }

    /**
     * Inizializza la schermata.
     *
     * @param view View del Fragment.
     * @param savedInstanceState stato precedente.
     */
    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(
                view,
                savedInstanceState
        );

        settingsMainContent =
                view.findViewById(
                        R.id.settingsMainContent
                );

        View bluetoothSettings =
                view.findViewById(
                        R.id.buttonBluetoothSettings
                );

        bluetoothSettings.setOnClickListener(
                v -> openBluetoothSettings()
        );
    }

    /**
     * Apre la schermata Bluetooth.
     *
     * Il contenuto principale di Settings viene
     * temporaneamente nascosto per evitare che rimanga
     * sovrapposto al Fragment Bluetooth.
     */
    private void openBluetoothSettings() {

        settingsMainContent.setVisibility(
                View.GONE
        );

        getChildFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.settingsContainer,
                        new BluetoothSettingsFragment()
                )
                .addToBackStack(null)
                .commit();
    }

    /**
     * Ripristina il contenuto principale quando
     * il Fragment Bluetooth viene rimosso dal back stack.
     */
    @Override
    public void onResume() {

        super.onResume();

        if (settingsMainContent == null) {
            return;
        }

        if (getChildFragmentManager()
                .getBackStackEntryCount() == 0) {

            settingsMainContent.setVisibility(
                    View.VISIBLE
            );
        }
    }
}
