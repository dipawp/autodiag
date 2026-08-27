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
 * La classe contiene inoltre:
 *
 * - identifiers:
 *     identificativi con cui riconoscere la ECU;
 *
 * - identification:
 *     istruzioni catalogate su quali identificatori diagnostici
 *     leggere durante la discovery automatica.
 *
 * ****************************************************************************
 */
public class EcuDefinition {

    /**
     * Marca commerciale del veicolo.
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
     */
    @NonNull
    private final String engine;

    /**
     * Identificativo della centralina.
     */
    @NonNull
    private final String ecu;

    /**
     * Protocollo diagnostico principale.
     */
    @NonNull
    private final String protocol;

    /**
     * Percorso del dataset PID principale.
     *
     * Mantenuto per compatibilità.
     */
    @NonNull
    private final String pidFile;

    /**
     * Identificativi utilizzabili per il riconoscimento
     * della ECU.
     */
    @NonNull
    private final EcuDefinitionIdentifier identifiers;

    /**
     * Definizioni utilizzabili durante la fase
     * di identificazione automatica.
     *
     * Ogni elemento descrive:
     *
     * - service;
     * - DID;
     * - campo destinazione;
     * - decoder;
     * - obbligatorietà;
     * - offset;
     * - lunghezza.
     */
    @NonNull
    private final List<EcuIdentificationDefinition> identification;

    /**
     * Dataset diagnostici associati all'ECU.
     */
    @NonNull
    private final List<DiagnosticDataset> datasets;

    /**
     * Costruttore compatibile con la versione originale.
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
                Collections.emptyList(),
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
                Collections.emptyList(),
                datasets
        );
    }

    /**
     * Costruttore compatibile con la versione precedente
     * che supporta gli identificativi ECU.
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

        this(
                brand,
                model,
                engine,
                ecu,
                protocol,
                pidFile,
                identifiers,
                Collections.emptyList(),
                datasets
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
     * @param identifiers identificativi ECU.
     * @param identification strategia identificazione.
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
            @NonNull List<EcuIdentificationDefinition> identification,
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

        this.identification =
                Collections.unmodifiableList(
                        identification
                );

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
     * Restituisce gli identificativi catalogati.
     *
     * @return identificativi ECU.
     */
    @NonNull
    public EcuDefinitionIdentifier getIdentifiers() {

        return identifiers;
    }

    /**
     * Indica se esistono identificativi catalogati.
     *
     * @return true se presenti.
     */
    public boolean hasIdentifiers() {

        return !identifiers.isEmpty();
    }

    /**
     * Restituisce le definizioni utilizzate
     * durante l'identificazione automatica.
     *
     * @return lista immutabile.
     */
    @NonNull
    public List<EcuIdentificationDefinition>
    getIdentificationDefinitions() {

        return identification;
    }

    /**
     * Indica se è stata definita una strategia
     * di identificazione automatica.
     *
     * @return true se presente almeno una voce.
     */
    public boolean hasIdentificationDefinitions() {

        return !identification.isEmpty();
    }

    /**
     * Restituisce tutti i dataset associati alla ECU.
     *
     * @return lista immutabile.
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
                ", identification=" +
                identification.size() +
                ", datasets=" +
                datasets.size() +
                '}';
    }
}