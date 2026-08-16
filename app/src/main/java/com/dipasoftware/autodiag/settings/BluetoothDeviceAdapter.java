package com.dipasoftware.autodiag.settings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dipasoftware.autodiag.R;
import com.dipasoftware.autodiag.bluetooth.BluetoothDeviceInfo;

import java.util.ArrayList;
import java.util.List;

/******************************************************************************
 *
 * Classe.....: BluetoothDeviceAdapter
 *
 * Tipo.......: RecyclerView Adapter
 *
 * Package....: com.dipasoftware.autodiag.settings
 *
 * Descrizione:
 *
 * Adapter utilizzato per visualizzare i dispositivi Bluetooth
 * associati.
 *
 * L'adapter non gestisce la connessione Bluetooth.
 *
 * Quando l'utente seleziona un dispositivo, l'adapter comunica
 * la selezione tramite BluetoothDeviceClickListener.
 *
 ******************************************************************************/
public class BluetoothDeviceAdapter
        extends RecyclerView.Adapter<BluetoothDeviceAdapter.DeviceViewHolder> {

    /**
     * Lista dei dispositivi.
     */
    private final List<BluetoothDeviceInfo> devices =
            new ArrayList<>();

    /**
     * Listener utilizzato per notificare la selezione
     * di un dispositivo.
     */
    private BluetoothDeviceClickListener listener;

    /**
     * Imposta il listener della selezione.
     *
     * @param listener listener da utilizzare.
     */
    public void setBluetoothDeviceClickListener(
            BluetoothDeviceClickListener listener) {

        this.listener = listener;
    }

    /**
     * Sostituisce la lista dei dispositivi.
     *
     * @param newDevices nuova lista.
     */
    public void setDevices(
            @NonNull List<BluetoothDeviceInfo> newDevices) {

        devices.clear();

        devices.addAll(
                newDevices
        );

        notifyDataSetChanged();
    }

    /**
     * Crea un ViewHolder.
     *
     * @param parent ViewGroup padre.
     * @param viewType tipo della View.
     *
     * @return ViewHolder.
     */
    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view =
                LayoutInflater.from(
                        parent.getContext()
                ).inflate(
                        R.layout.item_bluetooth_device,
                        parent,
                        false
                );

        return new DeviceViewHolder(
                view
        );
    }

    /**
     * Associa un dispositivo al ViewHolder.
     *
     * @param holder ViewHolder.
     * @param position posizione.
     */
    @Override
    public void onBindViewHolder(
            @NonNull DeviceViewHolder holder,
            int position) {

        BluetoothDeviceInfo device =
                devices.get(position);

        holder.textViewName.setText(
                device.getName()
        );

        holder.textViewAddress.setText(
                device.getAddress()
        );

        /*
         * Gestisce la selezione del dispositivo.
         */
        holder.itemView.setOnClickListener(
                view -> {

                    if (listener != null) {

                        listener.onBluetoothDeviceSelected(
                                device
                        );
                    }
                }
        );
    }

    /**
     * Restituisce il numero di dispositivi.
     *
     * @return numero elementi.
     */
    @Override
    public int getItemCount() {

        return devices.size();
    }

    /**
     * ViewHolder di un dispositivo Bluetooth.
     */
    static class DeviceViewHolder
            extends RecyclerView.ViewHolder {

        private final TextView textViewName;

        private final TextView textViewAddress;

        DeviceViewHolder(
                @NonNull View itemView) {

            super(itemView);

            textViewName =
                    itemView.findViewById(
                            R.id.textViewBluetoothDeviceName
                    );

            textViewAddress =
                    itemView.findViewById(
                            R.id.textViewBluetoothDeviceAddress
                    );
        }
    }

    /**
     * Listener per la selezione di un dispositivo Bluetooth.
     */
    public interface BluetoothDeviceClickListener {

        /**
         * Notifica la selezione di un dispositivo.
         *
         * @param device dispositivo selezionato.
         */
        void onBluetoothDeviceSelected(
                BluetoothDeviceInfo device
        );
    }
}
