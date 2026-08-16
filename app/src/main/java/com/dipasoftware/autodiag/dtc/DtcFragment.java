package com.dipasoftware.autodiag.dtc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dipasoftware.autodiag.databinding.FragmentDtcBinding;

/******************************************************************************
 *
 * Classe.....: DtcFragment
 *
 * Tipo.......: Fragment
 *
 * Package....: com.dipasoftware.autodiag.dtc
 *
 * Descrizione:
 *
 * Rappresenta la schermata dedicata ai Diagnostic Trouble Codes.
 *
 * In questa schermata verranno successivamente visualizzati:
 *
 * - DTC presenti
 * - DTC memorizzati
 * - descrizione degli errori
 * - stato degli errori
 * - possibilità di cancellazione degli errori
 *
 ******************************************************************************/
public class DtcFragment extends Fragment {

    /**
     * ViewBinding associato al layout fragment_dtc.xml.
     */
    private FragmentDtcBinding binding;

    /**
     * Costruttore vuoto richiesto dal framework Android.
     */
    public DtcFragment() {
    }

    /**
     * Crea la View del Fragment.
     *
     * @param inflater inflater Android.
     * @param container contenitore padre.
     * @param savedInstanceState stato precedente.
     *
     * @return View principale del Fragment.
     */
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        binding = FragmentDtcBinding.inflate(
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