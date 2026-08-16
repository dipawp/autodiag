package com.dipasoftware.autodiag.dashboard;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.dipasoftware.autodiag.dpf.DpfFragment;
import com.dipasoftware.autodiag.dtc.DtcFragment;
import com.dipasoftware.autodiag.history.HistoryFragment;
import com.dipasoftware.autodiag.home.HomeFragment;
import com.dipasoftware.autodiag.live.LiveFragment;

/******************************************************************************
 *
 * Classe.....: DashboardPagerAdapter
 *
 * Tipo.......: Adapter
 *
 * Package....: com.dipasoftware.autodiag.dashboard
 *
 * Descrizione:
 *
 * Gestisce le cinque pagine principali della dashboard.
 *
 * Ordine delle pagine:
 *
 * 0 - Home
 * 1 - Live
 * 2 - DPF
 * 3 - DTC
 * 4 - History
 *
 ******************************************************************************/
public class DashboardPagerAdapter extends FragmentStateAdapter {

    /**
     * Numero totale delle pagine della dashboard.
     */
    private static final int PAGE_COUNT = 5;

    /**
     * Costruttore dell'adapter.
     *
     * @param fragmentActivity Activity che ospita il ViewPager2.
     */
    public DashboardPagerAdapter(
            @NonNull FragmentActivity fragmentActivity) {

        super(fragmentActivity);
    }

    /**
     * Restituisce il numero totale delle pagine.
     *
     * @return numero delle pagine.
     */
    @Override
    public int getItemCount() {

        return PAGE_COUNT;
    }

    /**
     * Crea il Fragment associato alla posizione richiesta.
     *
     * @param position posizione della pagina.
     *
     * @return Fragment corrispondente.
     *
     * @throws IllegalArgumentException se la posizione
     * non è supportata.
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position) {

            case 0:
                return new HomeFragment();

            case 1:
                return new LiveFragment();

            case 2:
                return new DpfFragment();

            case 3:
                return new DtcFragment();

            case 4:
                return new HistoryFragment();

            default:
                throw new IllegalArgumentException(
                        "Posizione pagina non supportata: " + position
                );
        }
    }
}