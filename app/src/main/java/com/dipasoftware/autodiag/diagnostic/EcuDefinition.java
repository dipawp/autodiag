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
 *     istruzioni catalogate su quali identificatori leggere;
 *
 * - target:
 *     informazioni di indirizzamento diagnostico della ECU.
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
     * automatico della ECU.
     */
    @NonNull
    private final EcuDefinitionIdentifier identifiers;

    /**
     * Definizioni utilizzabili durante la fase
     * di identificazione automatica.
     */
    @NonNull
    private final List<EcuIdentificationDefinition> identification;

    /**
     * Target diagnostico della ECU.
     *
     * Contiene le informazioni necessarie per l'indirizzamento
     * della comunicazione.
     */
    @NonNull
    private final DiagnosticTargetDefinition target;

    /**
     * Dataset diagnostici associati all'ECU.
     */
    @NonNull
    private final List<DiagnosticDataset> datasets;

    /**
     * Costruttore compatibile con la versione originale.
     *
     * Utilizza:
     *
     * - identifiers vuoti;
     * - identification vuota;
     * - target vuoto;
     * - dataset vuoto.
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
                createDefaultTarget(
                        protocol
                ),
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
                createDefaultTarget(
                        protocol
                ),
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
                createDefaultTarget(
                        protocol
                ),
                datasets
        );
    }

    /**
     * Costruttore completo senza target esplicito.
     *
     * Il target predefinito viene costruito automaticamente
     * in base al protocollo.
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

        this(
                brand,
                model,
                engine,
                ecu,
                protocol,
                pidFile,
                identifiers,
                identification,
                createDefaultTarget(
                        protocol
                ),
                datasets
        );
    }

    /**
     * Costruttore completo con target esplicito.
     *
     * @param brand marca.
     * @param model modello.
     * @param engine motore.
     * @param ecu centralina.
     * @param protocol protocollo.
     * @param pidFile dataset principale.
     * @param identifiers identificativi ECU.
     * @param identification strategia identificazione.
     * @param target target diagnostico.
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
            @NonNull DiagnosticTargetDefinition target,
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

        this.target =
                target;

        this.datasets =
                Collections.unmodifiableList(
                        datasets
                );
    }

    /**
     * Crea un target predefinito compatibile con il protocollo.
     *
     * IMPORTANTE:
     *
     * Il target predefinito NON rappresenta necessariamente
     * l'indirizzamento reale della ECU.
     *
     * Serve esclusivamente per mantenere compatibilità
     * con le definizioni precedenti che non avevano un target.
     *
     * Per un ECU reale il catalogo dovrà fornire il target esplicito.
     *
     * @param protocol protocollo.
     *
     * @return target predefinito.
     */
    @NonNull
    private static DiagnosticTargetDefinition
    createDefaultTarget(
            @NonNull String protocol) {

        String normalized =
                protocol.trim()
                        .toUpperCase();

        /*
         * Target convenzionale usato esclusivamente
         * come valore di compatibilità.
         *
         * Non deve essere utilizzato per una ECU reale
         * finché il catalogo non specifica il target.
         */
        if ("CAN".equals(normalized)
                ||
                "ISO_15765_4_CAN".equals(normalized)
                ||
                "UDS".equals(normalized)) {

            return new DiagnosticTargetDefinition(
                    normalized,
                    "7E0",
                    "7E8",
                    "PHYSICAL",
                    11
            );
        }

        /*
         * Per protocolli non-CAN manteniamo comunque
         * un oggetto non nullo.
         *
         * Questi valori sono solo placeholder compatibili
         * con il modello e NON vengono utilizzati dal transport
         * finché il relativo protocollo non sarà implementato.
         */
        return new DiagnosticTargetDefinition(
                normalized.isEmpty()
                        ? "UNKNOWN"
                        : normalized,
                "0",
                "0",
                "PHYSICAL",
                11
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
     * Restituisce il target diagnostico.
     *
     * @return target.
     */
    @NonNull
    public DiagnosticTargetDefinition getTarget() {

        return target;
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
                ", target=" +
                target +
                ", datasets=" +
                datasets.size() +
                '}';
    }
}