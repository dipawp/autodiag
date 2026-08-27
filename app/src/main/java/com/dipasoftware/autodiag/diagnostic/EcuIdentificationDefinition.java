package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

/**
 * ****************************************************************************
 *
 * Classe.....: EcuIdentificationDefinition
 *
 * Tipo.......: Model
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Definisce un singolo identificatore diagnostico che può essere
 * interrogato durante la fase di identificazione automatica della ECU.
 *
 * La definizione contiene le informazioni necessarie per sapere:
 *
 * - quale servizio utilizzare;
 * - quale DID interrogare;
 * - in quale campo di EcuIdentification salvare il risultato;
 * - quale decoder utilizzare;
 * - se l'identificatore è obbligatorio;
 * - eventuale offset iniziale;
 * - eventuale lunghezza attesa.
 *
 * La classe NON esegue alcuna comunicazione.
 *
 * ****************************************************************************
 */
public class EcuIdentificationDefinition {

    /**
     * Servizio diagnostico.
     *
     * Esempio:
     *
     * 22 = ReadDataByIdentifier.
     */
    @NonNull
    private final String service;

    /**
     * Data Identifier.
     *
     * Esempio:
     *
     * F190
     */
    @NonNull
    private final String did;

    /**
     * Campo logico di EcuIdentification da valorizzare.
     *
     * Esempi:
     *
     * vin
     * ecuPartNumber
     * ecuHardwareNumber
     * ecuSoftwareNumber
     * ecuSoftwareVersion
     * supplier
     * serialNumber
     * systemName
     */
    @NonNull
    private final String field;

    /**
     * Decoder da utilizzare sui dati restituiti.
     *
     * Esempi:
     *
     * STRING
     * ASCII
     * HEX
     * UINT
     * SIGNED
     */
    @NonNull
    private final String decoder;

    /**
     * Indica se il DID è necessario per considerare
     * completata l'identificazione.
     */
    private final boolean required;

    /**
     * Offset all'interno del payload del DID.
     */
    private final int byteOffset;

    /**
     * Numero di byte da leggere.
     *
     * 0 = lunghezza variabile/non specificata.
     */
    private final int byteLength;

    /**
     * Costruttore completo.
     *
     * @param service servizio diagnostico.
     * @param did DID.
     * @param field campo di destinazione.
     * @param decoder decoder.
     * @param required se obbligatorio.
     * @param byteOffset offset dati.
     * @param byteLength lunghezza dati.
     */
    public EcuIdentificationDefinition(
            @NonNull String service,
            @NonNull String did,
            @NonNull String field,
            @NonNull String decoder,
            boolean required,
            int byteOffset,
            int byteLength) {

        this.service =
                normalizeHex(
                        service,
                        "service"
                );

        this.did =
                normalizeDid(
                        did
                );

        this.field =
                normalizeField(
                        field
                );

        this.decoder =
                decoder.trim()
                        .toUpperCase();

        if (this.decoder.isEmpty()) {

            throw new IllegalArgumentException(
                    "Decoder identificazione vuoto."
            );
        }

        if (byteOffset < 0) {

            throw new IllegalArgumentException(
                    "byteOffset non valido: "
                            + byteOffset
            );
        }

        if (byteLength < 0) {

            throw new IllegalArgumentException(
                    "byteLength non valido: "
                            + byteLength
            );
        }

        this.required =
                required;

        this.byteOffset =
                byteOffset;

        this.byteLength =
                byteLength;
    }

    /**
     * Costruttore semplificato.
     *
     * Utilizza:
     *
     * service = 22
     * byteOffset = 0
     * byteLength = 0
     *
     * @param did DID.
     * @param field campo.
     * @param decoder decoder.
     * @param required obbligatorio.
     */
    public EcuIdentificationDefinition(
            @NonNull String did,
            @NonNull String field,
            @NonNull String decoder,
            boolean required) {

        this(
                "22",
                did,
                field,
                decoder,
                required,
                0,
                0
        );
    }

    /**
     * Restituisce il servizio diagnostico.
     *
     * @return service.
     */
    @NonNull
    public String getService() {

        return service;
    }

    /**
     * Restituisce il DID.
     *
     * @return DID.
     */
    @NonNull
    public String getDid() {

        return did;
    }

    /**
     * Restituisce il campo di destinazione.
     *
     * @return campo.
     */
    @NonNull
    public String getField() {

        return field;
    }

    /**
     * Restituisce il decoder.
     *
     * @return decoder.
     */
    @NonNull
    public String getDecoder() {

        return decoder;
    }

    /**
     * Indica se l'identificatore è obbligatorio.
     *
     * @return true se obbligatorio.
     */
    public boolean isRequired() {

        return required;
    }

    /**
     * Restituisce l'offset iniziale.
     *
     * @return offset.
     */
    public int getByteOffset() {

        return byteOffset;
    }

    /**
     * Restituisce la lunghezza attesa.
     *
     * @return lunghezza oppure 0.
     */
    public int getByteLength() {

        return byteLength;
    }

    /**
     * Verifica se è stata specificata una lunghezza.
     *
     * @return true se presente.
     */
    public boolean hasByteLength() {

        return byteLength > 0;
    }

    /**
     * Verifica se viene utilizzato il servizio standard
     * UDS ReadDataByIdentifier.
     *
     * @return true se service = 22.
     */
    public boolean isReadDataByIdentifier() {

        return "22".equals(
                service
        );
    }

    /**
     * Costruisce la richiesta diagnostica.
     *
     * Esempio:
     *
     * service = 22
     * did     = F190
     *
     * risultato:
     *
     * 22F190
     *
     * @return request.
     */
    @NonNull
    public String buildRequest() {

        return service + did;
    }

    /**
     * Normalizza un valore HEX.
     *
     * @param value valore.
     * @param name nome campo.
     *
     * @return valore normalizzato.
     */
    @NonNull
    private String normalizeHex(
            @NonNull String value,
            @NonNull String name) {

        String normalized =
                value.trim()
                        .toUpperCase();

        if (normalized.isEmpty()) {

            throw new IllegalArgumentException(
                    name
                            + " vuoto."
            );
        }

        if ((normalized.length() % 2) != 0) {

            throw new IllegalArgumentException(
                    name
                            + " con numero dispari "
                            + "di caratteri HEX: "
                            + normalized
            );
        }

        for (
                int index = 0;
                index < normalized.length();
                index++
        ) {

            char character =
                    normalized.charAt(
                            index
                    );

            if (!isHexCharacter(
                    character
            )) {

                throw new IllegalArgumentException(
                        name
                                + " contiene un carattere "
                                + "non HEX: "
                                + character
                );
            }
        }

        return normalized;
    }

    /**
     * Normalizza un DID.
     *
     * Il DID UDS deve essere composto da esattamente
     * due byte, quindi quattro caratteri HEX.
     *
     * @param value DID.
     *
     * @return DID normalizzato.
     */
    @NonNull
    private String normalizeDid(
            @NonNull String value) {

        String normalized =
                value.trim()
                        .replace(
                                " ",
                                ""
                        )
                        .toUpperCase();

        if (normalized.length() != 4) {

            throw new IllegalArgumentException(
                    "DID non valido: "
                            + normalized
                            + ". Attesi 4 caratteri HEX."
            );
        }

        for (
                int index = 0;
                index < normalized.length();
                index++
        ) {

            if (!isHexCharacter(
                    normalized.charAt(index)
            )) {

                throw new IllegalArgumentException(
                        "DID non HEX: "
                                + normalized
                );
            }
        }

        return normalized;
    }

    /**
     * Normalizza il nome del campo.
     *
     * @param value campo.
     *
     * @return campo normalizzato.
     */
    @NonNull
    private String normalizeField(
            @NonNull String value) {

        String normalized =
                value.trim();

        if (normalized.isEmpty()) {

            throw new IllegalArgumentException(
                    "Campo identificazione vuoto."
            );
        }

        return normalized;
    }

    /**
     * Verifica un carattere HEX.
     *
     * @param character carattere.
     *
     * @return true se HEX.
     */
    private boolean isHexCharacter(
            char character) {

        return (character >= '0'
                && character <= '9')
                ||
                (character >= 'A'
                        && character <= 'F');
    }

    /**
     * Rappresentazione testuale.
     *
     * @return descrizione.
     */
    @NonNull
    @Override
    public String toString() {

        return "EcuIdentificationDefinition{" +
                "service='" +
                service +
                '\'' +
                ", did='" +
                did +
                '\'' +
                ", field='" +
                field +
                '\'' +
                ", decoder='" +
                decoder +
                '\'' +
                ", required=" +
                required +
                ", byteOffset=" +
                byteOffset +
                ", byteLength=" +
                byteLength +
                '}';
    }
}