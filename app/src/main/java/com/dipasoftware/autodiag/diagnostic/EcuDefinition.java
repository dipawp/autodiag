package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.List;

/**
 * ****************************************************************************
 *
 * Classe.....: EcuDefinition
 *
 * Tipo.......: Model
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Rappresenta una specifica ECU diagnostica associata a un veicolo.
 *
 * La classe identifica il contesto nel quale devono essere utilizzati
 * uno o più dataset diagnostici.
 *
 * Una ECU può utilizzare contemporaneamente:
 *
 * - dataset OBD-II standard;
 * - dataset PID proprietari;
 * - dataset UDS/DID;
 * - altri dataset specifici del costruttore.
 *
 * La classe NON contiene i PID.
 * I PID sono contenuti nei DiagnosticDataset associati alla ECU.
 *
 * Struttura concettuale:
 *
 * Veicolo
 *     |
 *     +-- marca
 *     +-- modello
 *     +-- motore
 *     |
 *     +-- ECU
 *          |
 *          +-- costruttore ECU
 *          +-- modello ECU
 *          +-- protocollo
 *          |
 *          +-- dataset diagnostici
 *
 * ****************************************************************************
 */
public class EcuDefinition {

    /**
     * Marca commerciale del veicolo.
     *
     * Esempi:
     *
     * Fiat
     * Alfa Romeo
     * BMW
     * Volkswagen
     */
    @NonNull
    private final String brand;

    /**
     * Modello del veicolo.
     */
    @NonNull
    private final String model;

    /**
     * Motore o variante propulsiva.
     *
     * Esempi:
     *
     * 1.9 JTD
     * 1.6 Multijet
     * 2.0 TDI
     */
    @NonNull
    private final String engine;

    /**
     * Identificativo della centralina.
     *
     * Esempi:
     *
     * Bosch EDC16C39
     * Bosch EDC17C49
     * Marelli MJD
     */
    @NonNull
    private final String ecu;

    /**
     * Protocollo diagnostico principale.
     *
     * Esempi:
     *
     * CAN
     * ISO_15765_4_CAN
     * KWP2000
     * ISO_14230
     * UDS
     */
    @NonNull
    private final String protocol;

    /**
     * Percorso del dataset PID principale.
     *
     * Questo campo viene mantenuto per compatibilità
     * con la struttura precedente.
     *
     * Nei nuovi dataset può essere utilizzato come
     * percorso del dataset predefinito.
     */
    @NonNull
    private final String pidFile;

    /**
     * Elenco dei dataset diagnostici associati all'ECU.
     *
     * La lista permette di associare più dataset alla stessa ECU.
     *
     * Esempi:
     *
     * STANDARD_OBD
     * OEM_PID
     * UDS_DID
     */
    @NonNull
    private final List<DiagnosticDataset> datasets;

    /**
     * Costruttore compatibile con la versione precedente.
     *
     * Viene mantenuto per non rompere il codice già esistente.
     *
     * Il parametro pidFile rimane il riferimento al dataset principale.
     *
     * @param brand marca.
     * @param model modello.
     * @param engine motore.
     * @param ecu centralina.
     * @param protocol protocollo.
     * @param pidFile dataset PID principale.
     */
    public EcuDefinition(
            @NonNull String brand,
            @NonNull String model,
            @NonNull String engine,
            @NonNull String ecu,
            @NonNull String protocol,
            @NonNull String pidFile) {

        this(
                brand,
                model,
                engine,
                ecu,
                protocol,
                pidFile,
                Collections.emptyList()
        );
    }

    /**
     * Costruttore completo.
     *
     * @param brand marca.
     * @param model modello.
     * @param engine motore.
     * @param ecu centralina.
     * @param protocol protocollo.
     * @param pidFile dataset principale.
     * @param datasets dataset diagnostici associati.
     */
    public EcuDefinition(
            @NonNull String brand,
            @NonNull String model,
            @NonNull String engine,
            @NonNull String ecu,
            @NonNull String protocol,
            @NonNull String pidFile,
            @NonNull List<DiagnosticDataset> datasets) {

        this.brand =
                brand.trim();

        this.model =
                model.trim();

        this.engine =
                engine.trim();

        this.ecu =
                ecu.trim();

        this.protocol =
                protocol.trim();

        this.pidFile =
                pidFile.trim();

        this.datasets =
                Collections.unmodifiableList(
                        datasets
                );
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
     * Restituisce il motore.
     *
     * @return motore.
     */
    @NonNull
    public String getEngine() {

        return engine;
    }

    /**
     * Restituisce l'identificativo ECU.
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
     * Restituisce il percorso del dataset principale.
     *
     * @return percorso dataset.
     */
    @NonNull
    public String getPidFile() {

        return pidFile;
    }

    /**
     * Restituisce tutti i dataset associati alla ECU.
     *
     * @return lista immutabile dei dataset.
     */
    @NonNull
    public List<DiagnosticDataset> getDatasets() {

        return datasets;
    }

    /**
     * Cerca un dataset in base al suo tipo.
     *
     * @param type tipo dataset.
     *
     * @return dataset trovato oppure null.
     */
    public DiagnosticDataset findDataset(
            @NonNull String type) {

        String normalizedType =
                type.trim().toUpperCase();

        for (
                DiagnosticDataset dataset :
                datasets
        ) {

            if (dataset.getType()
                    .equalsIgnoreCase(
                            normalizedType
                    )) {

                return dataset;
            }
        }

        return null;
    }

    /**
     * Indica se l'ECU contiene almeno un dataset OEM.
     *
     * @return true se presente.
     */
    public boolean hasOemDataset() {

        return findDataset(
                "OEM_PID"
        ) != null;
    }

    /**
     * Indica se l'ECU contiene almeno un dataset UDS.
     *
     * @return true se presente.
     */
    public boolean hasUdsDataset() {

        return findDataset(
                "UDS_DID"
        ) != null;
    }

    /**
     * Restituisce la rappresentazione testuale della ECU.
     *
     * @return descrizione ECU.
     */
    @NonNull
    @Override
    public String toString() {

        return "EcuDefinition{" +
                "brand='" +
                brand +
                '\'' +
                ", model='" +
                model +
                '\'' +
                ", engine='" +
                engine +
                '\'' +
                ", ecu='" +
                ecu +
                '\'' +
                ", protocol='" +
                protocol +
                '\'' +
                ", pidFile='" +
                pidFile +
                '\'' +
                ", datasets=" +
                datasets.size() +
                '}';
    }
}