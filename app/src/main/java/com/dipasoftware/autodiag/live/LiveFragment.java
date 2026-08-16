package com.dipasoftware.autodiag.live;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dipasoftware.autodiag.databinding.FragmentLiveBinding;

/******************************************************************************
 *
 * Classe.....: LiveFragment
 *
 * Tipo.......: Fragment
 *
 * Package....: com.dipasoftware.autodiag.live
 *
 * Descrizione:
 *
 * Rappresenta la schermata dei dati diagnostici in tempo reale.
 *
 * In questa schermata verranno successivamente visualizzati
 * i parametri letti dalla ECU, ad esempio:
 *
 * - RPM
 * - temperatura liquido refrigerante
 * - pressione MAP
 * - pressione turbo
 * - MAF
 * - pressione rail
 * - velocità veicolo
 *
 * Il Fragment non comunicherà direttamente con Bluetooth.
 *
 ******************************************************************************/
public class LiveFragment extends Fragment {

    /**
     * ViewBinding associato al layout fragment_live.xml.
     */
    private FragmentLiveBinding binding;

    /**
     * Costruttore vuoto richiesto dal framework Android.
     */
    public LiveFragment() {
    }

    /**
     * Crea la View del Fragment.
     *
     * @param inflater inflater Android.
     * @param container contenitore padre.
     * @param savedInstanceState stato precedente del Fragment.
     *
     * @return View principale del Fragment.
     */
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        binding = FragmentLiveBinding.inflate(
                inflater,
                container,
                false
        );

        return binding.getRoot();
    }

    /**
     * Libera il ViewBinding quando la View del Fragment
     * viene distrutta.
     */
    @Override
    public void onDestroyView() {

        super.onDestroyView();

        binding = null;
    }
}