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
 * La classe identifica il profilo ECU presente nel catalogo
 * dell'applicazione.
 *
 * Una ECU può utilizzare contemporaneamente:
 *
 * - dataset OBD-II standard;
 * - dataset PID proprietari OEM;
 * - dataset UDS/DID;
 * - altri dataset diagnostici futuri.
 *
 * La classe NON contiene i PID.
 *
 * I PID sono contenuti nei DiagnosticDataset associati alla ECU.
 *
 * Gli identificativi utilizzati per il riconoscimento automatico
 * della ECU sono contenuti in EcuDefinitionIdentifier.
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
 *          +-- identificativo ECU
 *          +-- protocollo
 *          +-- identificativi compatibili
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
     * Nei nuovi cataloghi il campo può essere lasciato vuoto
     * quando vengono utilizzati esclusivamente i dataset
     * definiti nell'elenco datasets.
     */
    @NonNull
    private final String pidFile;

    /**
     * Identificativi utilizzabili per il riconoscimento
     * automatico della ECU.
     *
     * Contiene:
     *
     * - hardware numbers;
     * - software numbers;
     * - part numbers;
     * - suppliers;
     * - VIN patterns.
     */
    @NonNull
    private final EcuDefinitionIdentifier identifiers;

    /**
     * Elenco dei dataset diagnostici associati all'ECU.
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
     * Costruttore compatibile con la versione originale.
     *
     * Non definisce identificativi specifici.
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
                new EcuDefinitionIdentifier(),
                Collections.emptyList()
        );
    }

    /**
     * Costruttore compatibile con la versione già utilizzata
     * dal catalogo ECU.
     *
     * @param brand marca.
     * @param model modello.
     * @param engine motore.
     * @param ecu centralina.
     * @param protocol protocollo.
     * @param pidFile dataset principale.
     * @param datasets dataset diagnostici.
     */
    public EcuDefinition(
            @NonNull String brand,
            @NonNull String model,
            @NonNull String engine,
            @NonNull String ecu,
            @NonNull String protocol,
            @NonNull String pidFile,
            @NonNull List<DiagnosticDataset> datasets) {

        this(
                brand,
                model,
                engine,
                ecu,
                protocol,
                pidFile,
                new EcuDefinitionIdentifier(),
                datasets
        );
    }

    /**
     * Costruttore completo.
     *
     * Permette di definire sia gli identificativi utilizzabili
     * per il matching automatico sia i dataset diagnostici.
     *
     * @param brand marca.
     * @param model modello.
     * @param engine motore.
     * @param ecu centralina.
     * @param protocol protocollo.
     * @param pidFile dataset principale.
     * @param identifiers identificativi ECU.
     * @param datasets dataset diagnostici.
     */
    public EcuDefinition(
            @NonNull String brand,
            @NonNull String model,
            @NonNull String engine,
            @NonNull String ecu,
            @NonNull String protocol,
            @NonNull String pidFile,
            @NonNull EcuDefinitionIdentifier identifiers,
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

        this.identifiers =
                identifiers;

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
     * Restituisce gli identificativi catalogati
     * per il riconoscimento automatico della ECU.
     *
     * @return identificativi ECU.
     */
    @NonNull
    public EcuDefinitionIdentifier getIdentifiers() {

        return identifiers;
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
     * Cerca un dataset in base al tipo.
     *
     * @param type tipo dataset.
     *
     * @return dataset trovato oppure null.
     */
    public DiagnosticDataset findDataset(
            @NonNull String type) {

        String normalizedType =
                type.trim()
                        .toUpperCase();

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
     * Indica se l'ECU contiene un dataset OBD-II standard.
     *
     * @return true se presente.
     */
    public boolean hasStandardObdDataset() {

        return findDataset(
                "STANDARD_OBD"
        ) != null;
    }

    /**
     * Restituisce true quando sono presenti identificativi
     * utilizzabili per il matching automatico.
     *
     * @return true se almeno un identificativo è disponibile.
     */
    public boolean hasIdentifiers() {

        return !identifiers.isEmpty();
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
                ", identifiers=" +
                identifiers +
                ", datasets=" +
                datasets.size() +
                '}';
    }
}