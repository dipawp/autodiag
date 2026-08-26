package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
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
 * esclusivamente richieste diagnostiche di lettura.
 *
 * La classe NON identifica direttamente marca/modello/ECU.
 *
 * Produce invece EcuIdentification, che verrà successivamente
 * confrontato con il catalogo tramite EcuCatalogRepository.
 *
 * ****************************************************************************
 */
public class EcuIdentifier {

    /**
     * DID VIN.
     */
    private static final String DID_VIN =
            "F190";

    /**
     * DID ECU part number.
     */
    private static final String DID_ECU_PART_NUMBER =
            "F187";

    /**
     * DID ECU software number.
     */
    private static final String DID_ECU_SOFTWARE_NUMBER =
            "F188";

    /**
     * DID ECU software version.
     */
    private static final String DID_ECU_SOFTWARE_VERSION =
            "F189";

    /**
     * DID system supplier.
     */
    private static final String DID_SUPPLIER =
            "F18A";

    /**
     * DID ECU serial number.
     */
    private static final String DID_SERIAL_NUMBER =
            "F18C";

    /**
     * DID ECU hardware number.
     */
    private static final String DID_ECU_HARDWARE_NUMBER =
            "F191";

    /**
     * DID supplier hardware number.
     */
    private static final String DID_SUPPLIER_HARDWARE =
            "F192";

    /**
     * DID supplier software number.
     */
    private static final String DID_SUPPLIER_SOFTWARE =
            "F194";

    /**
     * DID supplier software version.
     */
    private static final String DID_SUPPLIER_SOFTWARE_VERSION =
            "F195";

    /**
     * DID system name / engine type.
     */
    private static final String DID_SYSTEM_NAME =
            "F197";

    /**
     * Esecutore delle richieste diagnostiche.
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
     * Esegue la procedura di identificazione ECU.
     *
     * Tutte le richieste utilizzate dalla procedura passano
     * attraverso DiagnosticPidExecutor e quindi attraverso
     * ReadOnlyDiagnosticPolicy.
     *
     * Una singola DID non disponibile NON interrompe
     * l'intera identificazione.
     *
     * @return informazioni identificate.
     */
    @NonNull
    public EcuIdentification identify() {

        String vin =
                readTextDid(
                        DID_VIN
                );

        String ecuPartNumber =
                readTextDid(
                        DID_ECU_PART_NUMBER
                );

        String ecuSoftwareNumber =
                readTextDid(
                        DID_ECU_SOFTWARE_NUMBER
                );

        String ecuSoftwareVersion =
                readTextDid(
                        DID_ECU_SOFTWARE_VERSION
                );

        String supplier =
                readTextDid(
                        DID_SUPPLIER
                );

        String serialNumber =
                readTextDid(
                        DID_SERIAL_NUMBER
                );

        String ecuHardwareNumber =
                readTextDid(
                        DID_ECU_HARDWARE_NUMBER
                );

        String supplierHardwareNumber =
                readTextDid(
                        DID_SUPPLIER_HARDWARE
                );

        String supplierSoftwareNumber =
                readTextDid(
                        DID_SUPPLIER_SOFTWARE
                );

        String supplierSoftwareVersion =
                readTextDid(
                        DID_SUPPLIER_SOFTWARE_VERSION
                );

        String systemName =
                readTextDid(
                        DID_SYSTEM_NAME
                );

        Map<String, String> additional =
                new LinkedHashMap<>();

        /*
         * Per ora i DID conosciuti vengono mappati
         * nei campi principali di EcuIdentification.
         *
         * La mappa aggiuntiva rimane disponibile per
         * future estensioni senza modificare il modello.
         */

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
                additional
        );
    }

    /**
     * Legge un DID UDS e lo interpreta come stringa.
     *
     * Viene utilizzata la richiesta:
     *
     * 22 + DID
     *
     * Esempio:
     *
     * 22F190
     *
     * @param did DID.
     *
     * @return stringa decodificata oppure stringa vuota
     *         se il DID non è disponibile.
     */
    @NonNull
    private String readTextDid(
            @NonNull String did) {

        PidDefinition definition =
                createDidDefinition(
                        did
                );

        try {

            DiagnosticPidExecutor.DiagnosticPidExecution execution =
                    executor.execute(
                            definition
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

            return decodeText(
                    result.getData()
            );

        } catch (
                Exception exception) {

            /*
             * Un DID non disponibile non deve impedire
             * l'identificazione tramite gli altri DID.
             */
            return "";
        }
    }

    /**
     * Crea la definizione diagnostica per un DID UDS.
     *
     * La request viene dichiarata esplicitamente per evitare
     * qualsiasi ambiguità nel percorso diagnostico.
     *
     * @param did DID.
     *
     * @return definizione.
     */
    @NonNull
    private PidDefinition createDidDefinition(
            @NonNull String did) {

        String normalizedDid =
                did.trim()
                        .toUpperCase();

        return new PidDefinition(
                normalizedDid,
                "ecu_identification_" + normalizedDid,
                "ecu_identification_" + normalizedDid + "_description",
                "",
                "RAW",
                "",
                0,
                "22",
                "STRING",
                "STANDARD",
                false,
                "BIG_ENDIAN",
                0,
                0,
                0,
                "22" + normalizedDid,
                "62",
                0
        );
    }

    /**
     * Decodifica dati testuali restituendo il contenuto ASCII/UTF-8
     * senza padding finale.
     *
     * Alcune ECU restituiscono campi identificativi riempiti
     * con spazi o byte NUL.
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
}