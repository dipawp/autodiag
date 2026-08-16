package com.dipasoftware.autodiag.dpf;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dipasoftware.autodiag.databinding.FragmentDpfBinding;

/******************************************************************************
 *
 * Classe.....: DpfFragment
 *
 * Tipo.......: Fragment
 *
 * Package....: com.dipasoftware.autodiag.dpf
 *
 * Descrizione:
 *
 * Rappresenta la schermata dedicata al filtro antiparticolato
 * Diesel Particulate Filter (DPF).
 *
 * Questa sarà una delle sezioni principali di AutoDiag 1.0.
 *
 * In futuro visualizzerà:
 *
 * - massa particolato
 * - saturazione DPF
 * - pressione differenziale
 * - temperature DPF
 * - distanza dall'ultima rigenerazione
 * - tempo dall'ultima rigenerazione
 * - stato della rigenerazione
 *
 ******************************************************************************/
public class DpfFragment extends Fragment {

    /**
     * ViewBinding associato al layout fragment_dpf.xml.
     */
    private FragmentDpfBinding binding;

    /**
     * Costruttore vuoto richiesto dal framework Android.
     */
    public DpfFragment() {
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

        binding = FragmentDpfBinding.inflate(
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