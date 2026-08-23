package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.List;

/**
 * ****************************************************************************
 *
 * Classe.....: PidDatasetDefinition
 *
 * Tipo.......: Model
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Rappresenta un intero dataset diagnostico.
 *
 * Un dataset contiene:
 *
 * - informazioni identificative;
 * - origine del dataset;
 * - costruttore;
 * - marca;
 * - modello;
 * - centralina;
 * - protocollo;
 * - elenco dei PID.
 *
 * La classe permette di separare le informazioni relative al dataset
 * dalle informazioni relative al singolo PidDefinition.
 *
 * Esempio:
 *
 * FIAT
 *   Giulietta
 *      Bosch EDC17C49
 *          |
 *          +-- PID DPF 1
 *          +-- PID DPF 2
 *          +-- PID DPF 3
 *
 * ****************************************************************************
 */
public class PidDatasetDefinition {

    /**
     * Versione del dataset.
     */
    @NonNull
    private final String version;

    /**
     * Origine del dataset.
     *
     * Valori previsti:
     *
     * STANDARD
     * OEM
     */
    @NonNull
    private final String source;

    /**
     * Costruttore del veicolo/ECU.
     *
     * Esempio:
     *
     * FIAT
     */
    @NonNull
    private final String manufacturer;

    /**
     * Marca commerciale.
     *
     * Esempio:
     *
     * Alfa Romeo
     */
    @NonNull
    private final String brand;

    /**
     * Modello.
     *
     * Esempio:
     *
     * Giulietta
     */
    @NonNull
    private final String model;

    /**
     * Famiglia o identificativo della centralina.
     *
     * Esempio:
     *
     * Bosch EDC17C49
     */
    @NonNull
    private final String ecu;

    /**
     * Protocollo diagnostico utilizzato dal dataset.
     *
     * Esempi:
     *
     * OBD-II
     * CAN
     * KWP2000
     * ISO 14230
     */
    @NonNull
    private final String protocol;

    /**
     * Lista dei PID contenuti nel dataset.
     */
    @NonNull
    private final List<PidDefinition> pids;

    /**
     * Costruttore.
     *
     * @param version versione dataset.
     * @param source origine dataset.
     * @param manufacturer costruttore.
     * @param brand marca.
     * @param model modello.
     * @param ecu centralina.
     * @param protocol protocollo.
     * @param pids lista PID.
     */
    public PidDatasetDefinition(
            @NonNull String version,
            @NonNull String source,
            @NonNull String manufacturer,
            @NonNull String brand,
            @NonNull String model,
            @NonNull String ecu,
            @NonNull String protocol,
            @NonNull List<PidDefinition> pids) {

        this.version =
                version.trim();

        this.source =
                source.trim().toUpperCase();

        this.manufacturer =
                manufacturer.trim();

        this.brand =
                brand.trim();

        this.model =
                model.trim();

        this.ecu =
                ecu.trim();

        this.protocol =
                protocol.trim();

        this.pids =
                Collections.unmodifiableList(
                        pids
                );
    }

    /**
     * Restituisce la versione.
     *
     * @return versione.
     */
    @NonNull
    public String getVersion() {

        return version;
    }

    /**
     * Restituisce l'origine.
     *
     * @return origine.
     */
    @NonNull
    public String getSource() {

        return source;
    }

    /**
     * Restituisce il costruttore.
     *
     * @return costruttore.
     */
    @NonNull
    public String getManufacturer() {

        return manufacturer;
    }

    /**
     * Restituisce la marca.
     *
     * @return marca.
     */
    @NonNull
    public String getBrand() {

        return brand;
    }

    /**
     * Restituisce il modello.
     *
     * @return modello.
     */
    @NonNull
    public String getModel() {

        return model;
    }

    /**
     * Restituisce la centralina.
     *
     * @return ECU.
     */
    @NonNull
    public String getEcu() {

        return ecu;
    }

    /**
     * Restituisce il protocollo.
     *
     * @return protocollo.
     */
    @NonNull
    public String getProtocol() {

        return protocol;
    }

    /**
     * Restituisce i PID del dataset.
     *
     * @return lista immutabile dei PID.
     */
    @NonNull
    public List<PidDefinition> getPids() {

        return pids;
    }

    /**
     * Indica se il dataset è standard.
     *
     * @return true se STANDARD.
     */
    public boolean isStandard() {

        return "STANDARD".equals(
                source
        );
    }

    /**
     * Indica se il dataset è OEM.
     *
     * @return true se OEM.
     */
    public boolean isOem() {

        return "OEM".equals(
                source
        );
    }

    /**
     * Restituisce una descrizione leggibile del dataset.
     *
     * @return descrizione.
     */
    @NonNull
    @Override
    public String toString() {

        return "PidDatasetDefinition{" +

                "version='" +
                version +
                '\'' +

                ", source='" +
                source +
                '\'' +

                ", manufacturer='" +
                manufacturer +
                '\'' +

                ", brand='" +
                brand +
                '\'' +

                ", model='" +
                model +
                '\'' +

                ", ecu='" +
                ecu +
                '\'' +

                ", protocol='" +
                protocol +
                '\'' +

                ", pids=" +
                pids.size() +

                '}';

    }
}