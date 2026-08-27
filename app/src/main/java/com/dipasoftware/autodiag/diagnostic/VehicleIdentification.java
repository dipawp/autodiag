package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

/**
 * ****************************************************************************
 *
 * Classe.....: VehicleIdentification
 *
 * Tipo.......: Model
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Rappresenta le informazioni identificative del veicolo ottenute
 * durante la fase di discovery iniziale.
 *
 * Questa classe NON identifica direttamente la ECU.
 *
 * Il VIN rappresenta un'informazione del veicolo e viene utilizzato
 * successivamente come filtro del catalogo ECU.
 *
 * ****************************************************************************
 */
public class VehicleIdentification {

    /**
     * VIN del veicolo.
     */
    @NonNull
    private final String vin;

    /**
     * Indica se il VIN è stato letto correttamente.
     */
    private final boolean vinAvailable;

    /**
     * Costruttore.
     *
     * @param vin VIN.
     */
    public VehicleIdentification(
            @NonNull String vin) {

        this.vin =
                vin.trim();

        this.vinAvailable =
                !this.vin.isEmpty();
    }

    /**
     * Costruttore completo.
     *
     * @param vin VIN.
     * @param vinAvailable disponibilità VIN.
     */
    public VehicleIdentification(
            @NonNull String vin,
            boolean vinAvailable) {

        this.vin =
                vin.trim();

        this.vinAvailable =
                vinAvailable
                        && !this.vin.isEmpty();
    }

    /**
     * Restituisce il VIN.
     *
     * @return VIN oppure stringa vuota.
     */
    @NonNull
    public String getVin() {

        return vin;
    }

    /**
     * Indica se il VIN è disponibile.
     *
     * @return true se disponibile.
     */
    public boolean hasVin() {

        return vinAvailable;
    }

    /**
     * Restituisce una rappresentazione testuale.
     *
     * @return descrizione.
     */
    @NonNull
    @Override
    public String toString() {

        return "VehicleIdentification{" +
                "vin='" +
                vin +
                '\'' +
                ", vinAvailable=" +
                vinAvailable +
                '}';
    }
}