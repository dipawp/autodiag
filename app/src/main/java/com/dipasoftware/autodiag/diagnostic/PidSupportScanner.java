package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

import java.util.Locale;

/**
 * ****************************************************************************
 *
 * Classe.....: PidSupportScanner
 *
 * Tipo.......: Service
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Esegue la scansione delle bitmap di supporto PID OBD-II
 * della Mode 01.
 *
 * Le richieste vengono effettuate a blocchi di 32 PID:
 *
 *     0100 -> PID 01-20
 *     0120 -> PID 21-40
 *     0140 -> PID 41-60
 *     0160 -> PID 61-80
 *     ...
 *
 * La classe non conosce:
 *
 * - formule;
 * - descrizioni;
 * - JSON;
 * - PidDefinition.
 *
 * Determina esclusivamente quali PID sono dichiarati
 * supportati dalla ECU.
 *
 * ****************************************************************************
 */
public class PidSupportScanner {

    /**
     * Primo blocco bitmap.
     */
    private static final int FIRST_BASE_PID = 0x00;

    /**
     * Ultimo blocco bitmap standard.
     *
     * 0xE0 copre PID E1-FF.
     */
    private static final int LAST_BASE_PID = 0xE0;

    /**
     * Numero di byte della bitmap.
     */
    private static final int BITFIELD_LENGTH = 4;

    /**
     * Esecutore dei comandi OBD-II.
     */
    @NonNull
    private final ObdCommandExecutor commandExecutor;

    /**
     * Decoder della bitmap.
     */
    @NonNull
    private final PidSupportChecker supportChecker;

    /**
     * Costruttore.
     *
     * @param commandExecutor esecutore comandi OBD-II.
     * @param supportChecker decoder bitmap.
     */
    public PidSupportScanner(
            @NonNull ObdCommandExecutor commandExecutor,
            @NonNull PidSupportChecker supportChecker) {

        this.commandExecutor =
                commandExecutor;

        this.supportChecker =
                supportChecker;
    }

    /**
     * Esegue la scansione completa delle bitmap
     * Mode 01.
     *
     * La scansione parte da 0100 e procede:
     *
     * 0100
     * 0120
     * 0140
     * 0160
     * ...
     *
     * Se una ECU restituisce una risposta negativa
     * per un blocco, la scansione viene terminata.
     *
     * @return insieme dei PID supportati.
     *
     * @throws Exception errore di comunicazione.
     */
    @NonNull
    public SupportedPidSet scan()
            throws Exception {

        SupportedPidSet supportedPids =
                new SupportedPidSet();

        for (int basePid = FIRST_BASE_PID;
             basePid <= LAST_BASE_PID;
             basePid += 0x20) {

            PidSupportScanResult result =
                    scanBlock(basePid);

            /*
             * ---------------------------------------------------------
             * RISPOSTA POSITIVA
             * ---------------------------------------------------------
             */

            if (result.isSupported()) {

                byte[] bitfield =
                        result.getBitfield();

                if (bitfield == null ||
                        bitfield.length < BITFIELD_LENGTH) {

                    /*
                     * Risposta formalmente positiva ma
                     * bitmap incompleta.
                     *
                     * Non tentiamo di interpretarla.
                     */
                    break;
                }

                for (String pid :
                        supportChecker.getSupportedPids(
                                basePid,
                                bitfield
                        )) {

                    supportedPids.add(pid);
                }

                continue;
            }

            /*
             * ---------------------------------------------------------
             * RISPOSTA NEGATIVA
             * ---------------------------------------------------------
             */

            if (result.isNegativeResponse()) {

                break;
            }

            /*
             * ---------------------------------------------------------
             * RISPOSTA NON VALIDA
             * ---------------------------------------------------------
             */

            break;
        }

        return supportedPids;
    }

    /**
     * Interroga una singola bitmap.
     *
     * Esempio:
     *
     * basePid = 0x00
     *
     * comando = 0100
     *
     * @param basePid PID base numerico.
     *
     * @return risultato della scansione.
     *
     * @throws Exception errore di comunicazione.
     */
    @NonNull
    public PidSupportScanResult scanBlock(
            int basePid)
            throws Exception {

        validateBasePid(basePid);

        String command =
                buildCommand(basePid);

        String response =
                commandExecutor.execute(command);

        return parseResponse(
                basePid,
                response
        );
    }

    /**
     * Verifica che il PID base rappresenti
     * effettivamente un blocco bitmap.
     *
     * I blocchi validi sono:
     *
     * 00
     * 20
     * 40
     * 60
     * 80
     * A0
     * C0
     * E0
     *
     * @param basePid PID base.
     */
    private void validateBasePid(
            int basePid) {

        if (basePid < FIRST_BASE_PID ||
                basePid > LAST_BASE_PID ||
                (basePid % 0x20) != 0) {

            throw new IllegalArgumentException(
                    String.format(
                            Locale.US,
                            "PID base bitmap non valido: %02X",
                            basePid
                    )
            );
        }
    }

    /**
     * Costruisce il comando OBD-II.
     *
     * @param basePid PID base.
     *
     * @return comando, ad esempio 0100.
     */
    @NonNull
    private String buildCommand(
            int basePid) {

        return String.format(
                Locale.US,
                "01%02X",
                basePid
        );
    }

    /**
     * Analizza la risposta della ECU.
     *
     * Risposta positiva:
     *
     *     41 XX AA BB CC DD
     *
     * Risposta negativa:
     *
     *     7F 01 XX
     *
     * @param basePid PID richiesto.
     * @param response risposta ECU.
     *
     * @return risultato interpretato.
     */
    @NonNull
    private PidSupportScanResult parseResponse(
            int basePid,
            String response) {

        if (response == null ||
                response.trim().isEmpty()) {

            return new PidSupportScanResult(
                    PidSupportScanStatus.INVALID_RESPONSE,
                    basePid,
                    null,
                    response
            );
        }

        String normalized =
                normalizeResponse(response);

        /*
         * ---------------------------------------------------------
         * RISPOSTA NEGATIVA OBD-II
         * ---------------------------------------------------------
         */

        String[] tokens =
                normalized.split("\\s+");

        if (tokens.length >= 3 &&
                "7F".equals(tokens[0])) {

            return new PidSupportScanResult(
                    PidSupportScanStatus.NEGATIVE_RESPONSE,
                    basePid,
                    null,
                    response
            );
        }

        /*
         * ---------------------------------------------------------
         * RISPOSTA POSITIVA
         * ---------------------------------------------------------
         */

        /*
         * Deve contenere:
         *
         * 41 XX AA BB CC DD
         *
         * cioè almeno 6 byte.
         */
        if (tokens.length < 6) {

            return new PidSupportScanResult(
                    PidSupportScanStatus.INVALID_RESPONSE,
                    basePid,
                    null,
                    response
            );
        }

        /*
         * Service 01 positivo = 41.
         */
        if (!"41".equals(tokens[0])) {

            return new PidSupportScanResult(
                    PidSupportScanStatus.INVALID_RESPONSE,
                    basePid,
                    null,
                    response
            );
        }

        /*
         * ---------------------------------------------------------
         * VERIFICA PID
         * ---------------------------------------------------------
         */

        int responsePid;

        try {

            responsePid =
                    Integer.parseInt(
                            tokens[1],
                            16
                    );

        } catch (NumberFormatException exception) {

            return new PidSupportScanResult(
                    PidSupportScanStatus.INVALID_RESPONSE,
                    basePid,
                    null,
                    response
            );
        }

        if (responsePid != basePid) {

            return new PidSupportScanResult(
                    PidSupportScanStatus.INVALID_RESPONSE,
                    basePid,
                    null,
                    response
            );
        }

        /*
         * ---------------------------------------------------------
         * LETTURA BITMAP
         * ---------------------------------------------------------
         */

        byte[] bitfield =
                new byte[BITFIELD_LENGTH];

        for (int index = 0;
             index < BITFIELD_LENGTH;
             index++) {

            try {

                int value =
                        Integer.parseInt(
                                tokens[index + 2],
                                16
                        );

                if (value < 0 ||
                        value > 0xFF) {

                    return new PidSupportScanResult(
                            PidSupportScanStatus.INVALID_RESPONSE,
                            basePid,
                            null,
                            response
                    );
                }

                bitfield[index] =
                        (byte) value;

            } catch (NumberFormatException exception) {

                return new PidSupportScanResult(
                        PidSupportScanStatus.INVALID_RESPONSE,
                        basePid,
                        null,
                        response
                );
            }
        }

        return new PidSupportScanResult(
                PidSupportScanStatus.SUPPORTED,
                basePid,
                bitfield,
                response
        );
    }

    /**
     * Normalizza la risposta ELM327.
     *
     * @param response risposta originale.
     *
     * @return risposta normalizzata.
     */
    @NonNull
    private String normalizeResponse(
            @NonNull String response) {

        return response
                .replace(
                        ">",
                        " "
                )
                .replace(
                        "\r",
                        " "
                )
                .replace(
                        "\n",
                        " "
                )
                .trim()
                .toUpperCase(Locale.US)
                .replaceAll(
                        "\\s+",
                        " "
                );
    }
}