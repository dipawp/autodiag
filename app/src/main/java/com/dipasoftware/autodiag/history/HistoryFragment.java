package com.dipasoftware.autodiag.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dipasoftware.autodiag.databinding.FragmentHistoryBinding;

/******************************************************************************
 *
 * Classe.....: HistoryFragment
 *
 * Tipo.......: Fragment
 *
 * Package....: com.dipasoftware.autodiag.history
 *
 * Descrizione:
 *
 * Rappresenta la schermata dello storico diagnostico.
 *
 * In futuro conterrà:
 *
 * - storico delle sessioni diagnostiche
 * - storico DTC
 * - storico rigenerazioni DPF
 * - eventi diagnostici
 *
 ******************************************************************************/
public class HistoryFragment extends Fragment {

    /**
     * ViewBinding associato al layout fragment_history.xml.
     */
    private FragmentHistoryBinding binding;

    /**
     * Costruttore vuoto richiesto dal framework Android.
     */
    public HistoryFragment() {
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

        binding = FragmentHistoryBinding.inflate(
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