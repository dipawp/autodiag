package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

import java.util.Locale;

/**
 * ****************************************************************************
 *
 * Classe.....: DiagnosticDataset
 *
 * Tipo.......: Model
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Rappresenta un dataset diagnostico associato a una ECU.
 *
 * Il dataset identifica:
 *
 * - il tipo di dati;
 * - il percorso del file JSON;
 * - il protocollo o servizio utilizzato;
 * - una descrizione leggibile del dataset.
 *
 * Tipi previsti:
 *
 * STANDARD_OBD
 * OEM_PID
 * UDS_DID
 *
 * In futuro potranno essere aggiunti altri tipi senza
 * modificare EcuDefinition.
 *
 * ****************************************************************************
 */
public class DiagnosticDataset {

    /**
     * Tipo del dataset.
     *
     * Esempi:
     *
     * STANDARD_OBD
     * OEM_PID
     * UDS_DID
     */
    @NonNull
    private final String type;

    /**
     * Percorso del file JSON negli assets.
     */
    @NonNull
    private final String filePath;

    /**
     * Protocollo o servizio associato al dataset.
     *
     * Esempi:
     *
     * OBD-II
     * CAN
     * UDS
     */
    @NonNull
    private final String protocol;

    /**
     * Descrizione leggibile del dataset.
     */
    @NonNull
    private final String description;

    /**
     * Costruttore.
     *
     * @param type tipo dataset.
     * @param filePath percorso JSON.
     * @param protocol protocollo.
     * @param description descrizione.
     */
    public DiagnosticDataset(
            @NonNull String type,
            @NonNull String filePath,
            @NonNull String protocol,
            @NonNull String description) {

        this.type =
                type.trim()
                        .toUpperCase(Locale.US);

        this.filePath =
                filePath.trim();

        this.protocol =
                protocol.trim();

        this.description =
                description.trim();
    }

    /**
     * Restituisce il tipo del dataset.
     *
     * @return tipo.
     */
    @NonNull
    public String getType() {

        return type;
    }

    /**
     * Restituisce il percorso del file JSON.
     *
     * @return percorso.
     */
    @NonNull
    public String getFilePath() {

        return filePath;
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
     * Restituisce la descrizione.
     *
     * @return descrizione.
     */
    @NonNull
    public String getDescription() {

        return description;
    }

    /**
     * Indica se il dataset contiene PID OBD-II standard.
     *
     * @return true se STANDARD_OBD.
     */
    public boolean isStandardObd() {

        return "STANDARD_OBD".equals(
                type
        );
    }

    /**
     * Indica se il dataset contiene PID proprietari.
     *
     * @return true se OEM_PID.
     */
    public boolean isOemPid() {

        return "OEM_PID".equals(
                type
        );
    }

    /**
     * Indica se il dataset contiene DID UDS.
     *
     * @return true se UDS_DID.
     */
    public boolean isUdsDid() {

        return "UDS_DID".equals(
                type
        );
    }

    /**
     * Restituisce la rappresentazione testuale.
     *
     * @return descrizione dataset.
     */
    @NonNull
    @Override
    public String toString() {

        return "DiagnosticDataset{" +
                "type='" +
                type +
                '\'' +
                ", filePath='" +
                filePath +
                '\'' +
                ", protocol='" +
                protocol +
                '\'' +
                ", description='" +
                description +
                '\'' +
                '}';
    }
}