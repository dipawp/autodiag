package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * ****************************************************************************
 *
 * Classe.....: EcuIdentifier
 *
 * Tipo.......: Service
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Raccoglie informazioni identificative dalla ECU utilizzando
 * esclusivamente le definizioni presenti nel catalogo ECU.
 *
 * La classe NON contiene DID hardcoded.
 *
 * La strategia di identificazione viene definita da:
 *
 * EcuDefinition
 *      |
 *      +-- identification[]
 *              |
 *              +-- service
 *              +-- did
 *              +-- field
 *              +-- decoder
 *              +-- required
 *              +-- byteOffset
 *              +-- byteLength
 *
 * Le richieste vengono eseguite tramite DiagnosticPidExecutor,
 * che applica automaticamente la ReadOnlyDiagnosticPolicy.
 *
 * La classe NON identifica direttamente marca/modello/ECU.
 *
 * Produce invece EcuIdentification, che verrà successivamente
 * confrontato con il catalogo tramite EcuCatalogMatcher.
 *
 * ****************************************************************************
 */
public class EcuIdentifier {

    /**
     * Esecutore delle richieste diagnostiche.
     *
     * DiagnosticPidExecutor garantisce che la richiesta
     * passi attraverso ReadOnlyDiagnosticPolicy.
     */
    @NonNull
    private final DiagnosticPidExecutor executor;

    /**
     * Costruttore.
     *
     * @param executor esecutore diagnostico.
     */
    public EcuIdentifier(
            @NonNull DiagnosticPidExecutor executor) {

        this.executor =
                executor;
    }

    /**
     * Esegue l'identificazione utilizzando esclusivamente
     * le definizioni presenti nel catalogo ECU.
     *
     * Nessun DID è definito direttamente nel codice.
     *
     * Una singola lettura non disponibile non interrompe
     * l'intero processo.
     *
     * @param ecuDefinition definizione ECU del catalogo.
     *
     * @return informazioni identificate.
     */
    @NonNull
    public EcuIdentification identify(
            @NonNull EcuDefinition ecuDefinition) {

        String vin =
                "";

        String ecuPartNumber =
                "";

        String ecuHardwareNumber =
                "";

        String ecuSoftwareNumber =
                "";

        String ecuSoftwareVersion =
                "";

        String supplier =
                "";

        String serialNumber =
                "";

        String supplierHardwareNumber =
                "";

        String supplierSoftwareNumber =
                "";

        String supplierSoftwareVersion =
                "";

        String systemName =
                "";

        Map<String, String> additionalIdentifiers =
                new LinkedHashMap<>();

        List<EcuIdentificationDefinition> definitions =
                ecuDefinition.getIdentificationDefinitions();

        for (
                EcuIdentificationDefinition definition :
                definitions
        ) {

            String value =
                    readIdentificationDefinition(
                            definition
                    );

            if (value.isEmpty()) {

                /*
                 * Il DID può non essere supportato dalla ECU.
                 *
                 * Non interrompiamo l'identificazione.
                 */
                continue;
            }

            String field =
                    definition.getField()
                            .trim();

            /*
             * ---------------------------------------------------------
             * CAMPI STANDARD DI EcuIdentification
             * ---------------------------------------------------------
             */

            switch (
                    field.toLowerCase(
                            Locale.US
                    )
            ) {

                case "vin":

                    vin =
                            value;

                    break;

                case "ecupartnumber":

                    ecuPartNumber =
                            value;

                    break;

                case "ecuhardwarenumber":

                    ecuHardwareNumber =
                            value;

                    break;

                case "ecusoftwarenumber":

                    ecuSoftwareNumber =
                            value;

                    break;

                case "ecusoftwareversion":

                    ecuSoftwareVersion =
                            value;

                    break;

                case "supplier":

                    supplier =
                            value;

                    break;

                case "serialnumber":

                    serialNumber =
                            value;

                    break;

                case "supplierhardwarenumber":

                    supplierHardwareNumber =
                            value;

                    break;

                case "suppliersoftwarenumber":

                    supplierSoftwareNumber =
                            value;

                    break;

                case "suppliersoftwareversion":

                    supplierSoftwareVersion =
                            value;

                    break;

                case "systemname":

                    systemName =
                            value;

                    break;

                default:

                    /*
                     * Campo specifico OEM/non ancora conosciuto
                     * dal modello principale.
                     *
                     * Viene mantenuto senza perdita di informazione.
                     */
                    additionalIdentifiers.put(
                            definition.getDid(),
                            value
                    );

                    break;
            }
        }

        return new EcuIdentification(
                vin,
                ecuPartNumber,
                ecuHardwareNumber,
                ecuSoftwareNumber,
                ecuSoftwareVersion,
                supplier,
                serialNumber,
                supplierHardwareNumber,
                supplierSoftwareNumber,
                supplierSoftwareVersion,
                systemName,
                additionalIdentifiers
        );
    }

    /**
     * Versione semplificata mantenuta per compatibilità
     * con eventuale codice precedente.
     *
     * Non esistendo più DID hardcoded, senza una EcuDefinition
     * non è possibile sapere quali identificatori debbano essere
     * interrogati.
     *
     * Questo metodo restituisce quindi un'identificazione vuota.
     *
     * Il percorso corretto per la nuova architettura è:
     *
     * identify(ecuDefinition)
     *
     * @return identificazione vuota.
     */
    @NonNull
    public EcuIdentification identify() {

        return new EcuIdentification(
                "",
                "",
                "",
                ""
        );
    }

    /**
     * Legge una singola definizione di identificazione.
     *
     * @param definition definizione catalogata.
     *
     * @return valore decodificato oppure stringa vuota.
     */
    @NonNull
    private String readIdentificationDefinition(
            @NonNull EcuIdentificationDefinition definition) {

        /*
         * ---------------------------------------------------------
         * SERVICE
         * ---------------------------------------------------------
         *
         * Attualmente DiagnosticResponseParser supporta
         * il servizio UDS 0x22 per questa fase.
         *
         * Altri servizi potranno essere aggiunti in futuro
         * quando verranno implementati i relativi parser.
         */

        if (!definition.isReadDataByIdentifier()) {

            return "";
        }

        PidDefinition pidDefinition;

        try {

            pidDefinition =
                    createPidDefinition(
                            definition
                    );

        } catch (IllegalArgumentException exception) {

            return "";
        }

        try {

            DiagnosticPidExecutor.DiagnosticPidExecution execution =
                    executor.execute(
                            pidDefinition
                    );

            if (!execution.hasParsedResponse()) {

                return "";
            }

            DiagnosticResponseResult result =
                    execution.getParsedResponse();

            if (result == null ||
                    result.isNegative()) {

                return "";
            }

            byte[] data =
                    result.getData();

            return decodeIdentificationValue(
                    definition,
                    data
            );

        } catch (Exception exception) {

            /*
             * Una singola identificazione non disponibile
             * non deve interrompere la discovery.
             */
            return "";
        }
    }

    /**
     * Costruisce una PidDefinition temporanea utilizzabile
     * dal normale percorso diagnostico.
     *
     * La richiesta esplicita viene costruita come:
     *
     * service + DID
     *
     * Esempio:
     *
     * 22 + F190 = 22F190
     *
     * @param definition definizione identificazione.
     *
     * @return definizione diagnostica temporanea.
     */
    @NonNull
    private PidDefinition createPidDefinition(
            @NonNull EcuIdentificationDefinition definition) {

        String service =
                definition.getService()
                        .trim()
                        .toUpperCase(
                                Locale.US
                        );

        String did =
                definition.getDid()
                        .trim()
                        .toUpperCase(
                                Locale.US
                        );

        return new PidDefinition(
                did,
                "ecu_identification_" + did,
                "ecu_identification_"
                        + did
                        + "_description",
                "",
                "RAW",
                "",
                definition.hasByteLength()
                        ? definition.getByteLength()
                        : 0,
                service,
                "STRING",
                "STANDARD"
        );
    }

    /**
     * Decodifica il payload ricevuto dalla ECU.
     *
     * Decoder supportati in questa fase:
     *
     * STRING
     * ASCII
     * HEX
     *
     * Per decoder non ancora supportati viene restituita
     * una stringa vuota.
     *
     * @param definition definizione identificatore.
     * @param data dati ricevuti.
     *
     * @return valore decodificato.
     */
    @NonNull
    private String decodeIdentificationValue(
            @NonNull EcuIdentificationDefinition definition,
            @NonNull byte[] data) {

        byte[] adjustedData =
                applyDataRange(
                        definition,
                        data
                );

        if (adjustedData.length == 0) {

            return "";
        }

        String decoder =
                definition.getDecoder()
                        .trim()
                        .toUpperCase(
                                Locale.US
                        );

        switch (decoder) {

            case "STRING":
            case "ASCII":

                return decodeText(
                        adjustedData
                );

            case "HEX":

                return bytesToHex(
                        adjustedData
                );

            case "RAW":

                return decodeText(
                        adjustedData
                );

            default:

                /*
                 * Decoder non ancora implementato.
                 */
                return "";
        }
    }

    /**
     * Applica offset e lunghezza definiti nel catalogo.
     *
     * @param definition definizione.
     * @param data payload.
     *
     * @return porzione dati.
     */
    @NonNull
    private byte[] applyDataRange(
            @NonNull EcuIdentificationDefinition definition,
            @NonNull byte[] data) {

        int offset =
                definition.getByteOffset();

        if (offset < 0 ||
                offset >= data.length) {

            if (data.length == 0 &&
                    offset == 0) {

                return new byte[0];
            }

            return new byte[0];
        }

        int availableLength =
                data.length - offset;

        int requestedLength =
                definition.getByteLength();

        int length;

        if (requestedLength > 0) {

            length =
                    Math.min(
                            requestedLength,
                            availableLength
                    );

        } else {

            length =
                    availableLength;
        }

        if (length <= 0) {

            return new byte[0];
        }

        byte[] result =
                new byte[length];

        System.arraycopy(
                data,
                offset,
                result,
                0,
                length
        );

        return result;
    }

    /**
     * Decodifica dati testuali.
     *
     * Rimuove byte NUL e spazi di padding.
     *
     * @param data dati.
     *
     * @return testo pulito.
     */
    @NonNull
    private String decodeText(
            @NonNull byte[] data) {

        if (data.length == 0) {

            return "";
        }

        String value =
                new String(
                        data,
                        StandardCharsets.UTF_8
                );

        return value
                .replace(
                        "\u0000",
                        ""
                )
                .trim();
    }

    /**
     * Converte i dati in HEX.
     *
     * @param data dati.
     *
     * @return stringa HEX.
     */
    @NonNull
    private String bytesToHex(
            @NonNull byte[] data) {

        if (data.length == 0) {

            return "";
        }

        StringBuilder result =
                new StringBuilder();

        for (
                int index = 0;
                index < data.length;
                index++
        ) {

            if (index > 0) {

                result.append(
                        " "
                );
            }

            result.append(
                    String.format(
                            Locale.US,
                            "%02X",
                            data[index] & 0xFF
                    )
            );
        }

        return result.toString();
    }
}