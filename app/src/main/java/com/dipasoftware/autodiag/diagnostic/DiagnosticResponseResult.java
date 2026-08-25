package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

/**
 * ****************************************************************************
 *
 * Classe.....: DiagnosticResponseResult
 *
 * Tipo.......: Model
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Rappresenta un risultato diagnostico normalizzato,
 * indipendentemente dal parser specifico utilizzato.
 *
 * Permette al livello superiore di trattare allo stesso modo:
 *
 * - risposta OBD-II;
 * - risposta UDS;
 * - futuri parser diagnostici.
 *
 * ****************************************************************************
 */
public class DiagnosticResponseResult {

    /**
     * Tipo di protocollo/parser utilizzato.
     */
    @NonNull
    private final String protocolType;

    /**
     * Service ricevuto.
     */
    private final int service;

    /**
     * Identificatore ricevuto.
     *
     * Per OBD-II rappresenta il PID.
     * Per UDS rappresenta il DID.
     */
    private final int identifier;

    /**
     * Dati diagnostici.
     */
    @NonNull
    private final byte[] data;

    /**
     * Indica se la risposta è positiva.
     */
    private final boolean positive;

    /**
     * Negative Response Code.
     *
     * Vale -1 se non disponibile.
     */
    private final int negativeResponseCode;

    /**
     * Costruttore.
     *
     * @param protocolType tipo protocollo.
     * @param service service.
     * @param identifier identificatore.
     * @param data dati.
     * @param positive risposta positiva.
     * @param negativeResponseCode NRC.
     */
    public DiagnosticResponseResult(
            @NonNull String protocolType,
            int service,
            int identifier,
            @NonNull byte[] data,
            boolean positive,
            int negativeResponseCode) {

        this.protocolType =
                protocolType;

        this.service =
                service;

        this.identifier =
                identifier;

        this.data =
                data.clone();

        this.positive =
                positive;

        this.negativeResponseCode =
                negativeResponseCode;
    }

    /**
     * Crea un risultato dal parser OBD-II.
     *
     * @param response risposta OBD.
     *
     * @return risultato normalizzato.
     */
    @NonNull
    public static DiagnosticResponseResult fromObd(
            @NonNull ObdResponseParser.ObdResponse response) {

        return new DiagnosticResponseResult(
                "OBD",
                response.getService(),
                response.getPid(),
                response.getData(),
                true,
                -1
        );
    }

    /**
     * Crea un risultato dal parser UDS.
     *
     * @param response risposta UDS.
     *
     * @return risultato normalizzato.
     */
    @NonNull
    public static DiagnosticResponseResult fromUds(
            @NonNull UdsResponseParser.UdsResponse response) {

        return new DiagnosticResponseResult(
                "UDS",
                response.getService(),
                response.getDid(),
                response.getData(),
                response.isPositive(),
                response.getNegativeResponseCode()
        );
    }

    /**
     * Restituisce il tipo di protocollo.
     *
     * @return OBD oppure UDS.
     */
    @NonNull
    public String getProtocolType() {

        return protocolType;
    }

    /**
     * Restituisce il service.
     *
     * @return service.
     */
    public int getService() {

        return service;
    }

    /**
     * Restituisce l'identificatore.
     *
     * @return PID/DID.
     */
    public int getIdentifier() {

        return identifier;
    }

    /**
     * Restituisce una copia dei dati.
     *
     * @return dati.
     */
    @NonNull
    public byte[] getData() {

        return data.clone();
    }

    /**
     * Indica se la risposta è positiva.
     *
     * @return true se positiva.
     */
    public boolean isPositive() {

        return positive;
    }

    /**
     * Indica se la risposta è negativa.
     *
     * @return true se negativa.
     */
    public boolean isNegative() {

        return !positive;
    }

    /**
     * Restituisce il Negative Response Code.
     *
     * @return NRC oppure -1.
     */
    public int getNegativeResponseCode() {

        return negativeResponseCode;
    }

    /**
     * Restituisce i dati in formato HEX.
     *
     * @return dati HEX.
     */
    @NonNull
    public String getDataHex() {

        if (data.length == 0) {

            return "";
        }

        StringBuilder result =
                new StringBuilder();

        for (int index = 0;
             index < data.length;
             index++) {

            if (index > 0) {

                result.append(" ");
            }

            result.append(
                    String.format(
                            java.util.Locale.US,
                            "%02X",
                            data[index] & 0xFF
                    )
            );
        }

        return result.toString();
    }
}