package com.dipasoftware.autodiag.diagnostic;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dipasoftware.autodiag.connection.Connection;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


/******************************************************************************
 *
 * Classe.....: Elm327Manager
 *
 * Tipo.......: Classe di servizio
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Gestisce la comunicazione con ELM327.
 *
 * Responsabilità:
 *
 * - comunicazione con Connection;
 * - inizializzazione ELM327;
 * - configurazione protocollo;
 * - identificazione ELM327;
 * - analisi degli errori ELM327;
 * - comunicazione OBD-II;
 * - caricamento PID dal JSON;
 * - parsing risposta OBD-II;
 * - valutazione formula PID.
 *
 ******************************************************************************/
public class Elm327Manager {

    /**
     * Connessione fisica verso ELM327.
     */
    @NonNull
    private final Connection connection;

    /**
     * Versione ELM327.
     */
    @Nullable
    private String elmVersion;

    /**
     * Indica se ELM327 è inizializzato.
     */
    private boolean initialized;

    /**
     * Repository PID JSON.
     *
     * Può essere null nel costruttore utilizzato
     * esclusivamente dai test di comunicazione.
     */
    @Nullable
    private final JsonPidRepository pidRepository;

    /**
     * Parser OBD-II.
     */
    @NonNull
    private final ObdResponseParser obdResponseParser;

    /**
     * Valutatore formule PID.
     */
    @NonNull
    private final PidFormulaEvaluator pidFormulaEvaluator;



    @NonNull
    private final PidSupportScanner pidSupportScanner;



    @NonNull
    private final DiagnosticLogger diagnosticLogger;



    /**
     * Esecutore di una singola richiesta diagnostica.
     *
     * Centralizza:
     *
     * - costruzione della richiesta;
     * - invio tramite Connection;
     * - ricezione della risposta;
     * - parsing diagnostico.
     *
     * La gestione degli errori specifici ELM327 rimane
     * responsabilità di Elm327Manager.
     */
    @NonNull
    private final DiagnosticPidExecutor diagnosticPidExecutor;



    /**
     * DiagnosticPidExecutor utilizzato dal percorso
     * catalog-driven.
     *
     * Viene creato lazy e condiviso tra i componenti
     * della discovery.
     */
    private DiagnosticPidExecutor catalogDiagnosticPidExecutor;


    /**************************************************************************
     *
     * COSTRUTTORE PRINCIPALE
     *
     **************************************************************************/

    /**
     * Costruttore principale.
     *
     * @param connection connessione ELM327.
     * @param context context Android.
     */
    public Elm327Manager(
            @NonNull Connection connection,
            @NonNull Context context) {

        this.connection = connection;

        this.initialized = false;

        this.pidRepository = new JsonPidRepository(context.getApplicationContext());

        this.obdResponseParser = new ObdResponseParser();

        this.pidFormulaEvaluator = new PidFormulaEvaluator();


        this.pidSupportScanner =
                new PidSupportScanner(
                        new ObdCommandExecutor() {

                            @Override
                            public String execute(
                                    @NonNull String command)
                                    throws Exception {

                                return sendCommand(command);
                            }
                        },
                        new PidSupportChecker()
                );

        this.diagnosticLogger = new DiagnosticLogger(context);


        this.diagnosticPidExecutor = new DiagnosticPidExecutor(this.connection);

    }


    /**************************************************************************
     *
     * COSTRUTTORE TEST
     *
     **************************************************************************/

    /**
     * Costruttore utilizzato dai test unitari
     * che non richiedono il repository JSON.
     *
     * @param connection connessione ELM327.
     */
    public Elm327Manager(
            @NonNull Connection connection) {

        this.connection = connection;

        this.initialized = false;

        this.pidRepository = null;

        this.obdResponseParser = new ObdResponseParser();

        this.pidFormulaEvaluator = new PidFormulaEvaluator();

        this.pidSupportScanner =
                new PidSupportScanner(new ObdCommandExecutor() {
                            @Override
                            public String execute(
                                    @NonNull String command)
                                    throws Exception {

                                return sendCommand(command);
                            }
                        },
                        new PidSupportChecker()
                );

        this.diagnosticLogger = null;
        this.diagnosticPidExecutor = new DiagnosticPidExecutor(this.connection);
    }

    /**************************************************************************
     *
     * INIZIALIZZAZIONE
     *
     **************************************************************************/

    /**
     * Inizializza ELM327.
     *
     * IMPORTANTE:
     *
     * Non viene forzato AT SP 6.
     *
     * Viene utilizzato:
     *
     * AT SP 0
     *
     * lasciando all'ELM327 il rilevamento automatico
     * del protocollo.
     *
     * @throws IOException errore comunicazione.
     */
    public void initialize() throws IOException {
        initialized = false;
        /*
         * ---------------------------------------------------------
         * RESET
         * ---------------------------------------------------------
         */
        String response = sendCommand("AT Z");

        if (isEmpty(response)) {

            throw new IOException("Nessuna risposta da AT Z.");
        }

        /*
         * Dopo AT Z l'ELM può impiegare un breve
         * intervallo per completare il reset.
         *
         * Il comando ATI viene utilizzato per
         * identificare nuovamente il dispositivo.
         */

        response = sendCommand("ATI");

        if (isEmpty(response)) {
            throw new IOException("Nessuna risposta da ATI.");
        }

        elmVersion = extractElmVersion(response);
        /*
         * ---------------------------------------------------------
         * ECHO OFF
         * ---------------------------------------------------------
         */
        response = sendCommand("AT E0");
        checkOkResponse("AT E0",response);
        /*
         * ---------------------------------------------------------
         * LINE FEED OFF
         * ---------------------------------------------------------
         */
        response = sendCommand("AT L0");
        checkOkResponse("AT L0",response);
        /*
         * ---------------------------------------------------------
         * SPACES ON
         *
         * Utile per ottenere risposte:
         *
         * 41 0C 1A F8
         *
         * invece di:
         *
         * 410C1AF8
         *
         * ---------------------------------------------------------
         */
        response = sendCommand("AT S1");
        checkOkResponse("AT S1",response);
        /*
         * ---------------------------------------------------------
         * HEADERS OFF
         *
         * Per il normale parsing OBD-II.
         * ---------------------------------------------------------
         */
        response = sendCommand("AT H0");
        checkOkResponse("AT H0",response);
        /*
         * ---------------------------------------------------------
         * AUTO PROTOCOL
         * ---------------------------------------------------------
         */
        response = sendCommand("AT SP 0");
        checkOkResponse("AT SP 0",response);
        /*
         * ---------------------------------------------------------
         * TIMEOUT STANDARD
         * ---------------------------------------------------------
         */
        response = sendCommand("AT ST 64");
        /*
         * AT ST 64 è opzionale.
         *
         * Alcuni ELM327/cloni non supportano
         * questo comando oppure utilizzano
         * una sintassi diversa.
         *
         * Se risponde OK lo utilizziamo.
         * Se risponde ? continuiamo comunque
         * l'inizializzazione.
         */
        if (!isOkResponse(response)) {
            String normalized =
                    response == null
                            ? ""
                            : response
                            .replace("\r", "")
                            .replace("\n", "")
                            .replace(">", "")
                            .trim()
                            .toUpperCase(Locale.US);

            if (!normalized.equals("?")) {
                throw new IOException("Risposta inattesa da AT ST 64: " + formatResponse(response));
            }
        }
        /*
         * L'ELM327 è configurato.
         *
         * ATTENZIONE:
         *
         * questo NON significa ancora che la ECU
         * risponda.
         *
         * Per questo il test reale viene effettuato
         * da runObdTest().
         */
        initialized = true;
    }
    /**************************************************************************
     *
     * CONNECTION TEST
     *
     **************************************************************************/

    /**
     * Esegue il test della connessione ELM327.
     *
     * Non forza il protocollo CAN.
     *
     * @return risultato test.
     *
     * @throws IOException errore comunicazione.
     */
    @NonNull
    public String runConnectionTest() throws IOException {
        initialized = false;
        StringBuilder result = new StringBuilder();
        result.append("=== TEST ELM327 ===\n\n");
        /*
         * ---------------------------------------------------------
         * CONNESSIONE
         * ---------------------------------------------------------
         */
        result.append("CONNESSIONE: ");

        if (connection.isConnected()) {
            result.append("OK\n\n");
        } else {
            result.append("NON CONNESSA\n\n");
            result.append("ERRORE: Connection non connessa.");
            return result.toString();
        }
        /*
         * ---------------------------------------------------------
         * ATI
         * ---------------------------------------------------------
         */
        result.append("Invio: ATI\n");

        String response = sendCommand("ATI");
        result.append("RX: " + formatResponse(response) + "\n\n");
        elmVersion = extractElmVersion(response);

        if (isEmpty(response)) {
            result.append("ERRORE: ELM327 non risponde ad ATI.\n");
            return result.toString();
        }
        /*
         * ---------------------------------------------------------
         * RESET
         * ---------------------------------------------------------
         */
        result.append("Invio: AT Z\n");
        response = sendCommand("AT Z");

        result.append("RX: " + formatResponse(response) + "\n\n");

        if (isEmpty(response)) {
            result.append("ERRORE: nessuna risposta da AT Z.\n");
            return result.toString();
        }
        /*
         * ---------------------------------------------------------
         * ECHO OFF
         * ---------------------------------------------------------
         */
        if (!executeAtCommand(result,"AT E0")) {
            return result.toString();
        }
        /*
         * ---------------------------------------------------------
         * LINE FEED OFF
         * ---------------------------------------------------------
         */
        if (!executeAtCommand(result,"AT L0")) {
            return result.toString();
        }
        /*
         * ---------------------------------------------------------
         * SPACES ON
         * ---------------------------------------------------------
         */
        if (!executeAtCommand(result,"AT S1")) {
            return result.toString();
        }
        /*
         * ---------------------------------------------------------
         * HEADERS OFF
         * ---------------------------------------------------------
         */
        if (!executeAtCommand(result,"AT H0")) {
            return result.toString();
        }
        /*
         * ---------------------------------------------------------
         * AUTO PROTOCOL
         * ---------------------------------------------------------
         */
        if (!executeAtCommand(result,"AT SP 0")) {
            return result.toString();
        }
        /*
         * ---------------------------------------------------------
         * TIMEOUT
         * ---------------------------------------------------------
         */
        if (!executeOptionalAtCommand(result,"AT ST 64")) {
            return result.toString();
        }
        /*
         * ---------------------------------------------------------
         * PROTOCOLLO RILEVATO
         * ---------------------------------------------------------
         */
        result.append("Invio: AT DP\n");
        response = sendCommand("AT DP");
        result.append("RX: " + formatResponse(response) + "\n\n");
        /*
         * ---------------------------------------------------------
         * NUMERO PROTOCOLLO
         * ---------------------------------------------------------
         */
        result.append("Invio: AT DPN\n");
        response = sendCommand("AT DPN");
        result.append("RX: " + formatResponse(response) + "\n\n");
        /*
         * ---------------------------------------------------------
         * RISULTATO
         * ---------------------------------------------------------
         */
        initialized = true;
        result.append("ELM327: ");

        if (elmVersion != null) {
            result.append(elmVersion);
        } else {
            result.append("versione non rilevata");
        }

        result.append("\n");
        result.append("STATO ELM327: INIZIALIZZATO\n");
        result.append("STATO ECU: NON ANCORA VERIFICATO\n\n");
        result.append("=== TEST COMPLETATO ===");
        saveDiagnosticLog(result);
        return result.toString();
    }
    /**************************************************************************
     *
     * AT COMMAND
     *
     **************************************************************************/

    /**
     * Esegue un comando AT e aggiunge il risultato
     * al buffer del test.
     *
     * @param result buffer.
     * @param command comando.
     *
     * @return true se OK.
     *
     * @throws IOException errore comunicazione.
     */
    private boolean executeAtCommand(@NonNull StringBuilder result,@NonNull String command) throws IOException {
        result.append("Invio: " + command + "\n");
        String response = sendCommand(command);
        result.append("RX: " + formatResponse(response) + "\n\n");

        if (!isOkResponse(response)) {
            result.append("ERRORE: risposta inattesa da " + command + ".\n");
            return false;
        }
        return true;
    }
    /**************************************************************************
     *
     * OBD TEST
     *
     **************************************************************************/

    /**
     * Esegue il test OBD-II.
     *
     * Il primo comando è 0100.
     *
     * Questo comando serve anche a verificare
     * se la ECU risponde realmente.
     *
     * @return risultato.
     *
     * @throws IOException errore comunicazione.
     */
    @NonNull
    public String runObdTest() throws IOException {
        StringBuilder result = new StringBuilder();
        result.append("=== TEST OBD-II ===\n\n");

        try {
            ensureInitialized();
        } catch (IOException exception) {
            result.append("ERRORE INIZIALIZZAZIONE ELM327:\n" + exception.getMessage() + "\n");
            return result.toString();
        }

        result.append("Invio: 0100\n");
        String response = sendCommand("0100");
        result.append("RX: " + formatResponse(response) + "\n\n");
        /*
         * Analisi errori ELM327.
         */
        ElmError error = detectElmError(response);
        if (error != ElmError.NONE) {
            appendElmError(result,error,response);
            return result.toString();
        }
        /*
         * Risposta vuota.
         */
        if (isEmpty(response)) {
            result.append("STATO ECU: NESSUNA RISPOSTA\n");
            return result.toString();
        }
        /*
         * Tentiamo il parsing reale.
         */
        try {
            ObdResponseParser.ObdResponse obdResponse = obdResponseParser.parse(response,"0100");
            result.append("SERVICE: " + formatHex(obdResponse.getService()) + "\n");
            result.append("PID: " + formatHex(obdResponse.getPid()) + "\n");
            result.append("DATA: " + obdResponse.getDataHex() + "\n\n");
            result.append("STATO ECU: RISPOSTA ECU VALIDA\n");
        } catch (IllegalArgumentException exception) {
            result.append("STATO ECU: RISPOSTA NON RICONOSCIUTA\n");
            result.append("MOTIVO: " + exception.getMessage() + "\n");
        }
        return result.toString();
    }
    /**
     * Esegue il test dei PID OBD-II standard.
     *
     * Prima viene effettuata la scansione delle
     * bitmap OBD-II per determinare quali PID
     * sono realmente supportati dalla ECU.
     *
     * Successivamente vengono interrogati
     * solamente i PID presenti nel dataset JSON
     * e dichiarati supportati dalla ECU.
     *
     * @return risultato del test.
     *
     * @throws IOException errore di comunicazione.
     */
    @NonNull
    public String runLiveDataTest() throws IOException {
        StringBuilder result = new StringBuilder();
        result.append("=== LIVE DATA TEST ===\n\n");
        try {
            ensureInitialized();
        } catch (IOException exception) {
            result.append("ERRORE INIZIALIZZAZIONE ELM327:\n" + exception.getMessage() + "\n");
            saveDiagnosticLog(result);
            return result.toString();
        }
        if (pidRepository == null) {
            result.append("ERRORE:\n" + "Repository PID non disponibile.");
            return result.toString();
        }
        List<PidDefinition> definitions;
        try {
            definitions = pidRepository.loadStandardPids();
        } catch (JSONException exception) {
            result.append("ERRORE CARICAMENTO PID JSON:\n" + exception.getMessage());
            return result.toString();
        }
        if (definitions.isEmpty()) {
            result.append("ERRORE:\n" + "Nessun PID presente nel dataset.");
            return result.toString();
        }
        result.append("PID CARICATI: " + definitions.size() + "\n\n");
        /*
         * =========================================================
         * SCANSIONE BITMAP
         * =========================================================
         */
        result.append("=== SCANSIONE PID SUPPORTATI ===\n\n");
        SupportedPidSet supportedPids;
        try {
            supportedPids = pidSupportScanner.scan();
        } catch (Exception exception) {
            result.append("ERRORE SCANSIONE PID SUPPORTATI:\n" + exception.getMessage() + "\n");
            saveDiagnosticLog(result);
            return result.toString();
        }
        result.append("TOTALE PID SUPPORTATI ECU: " + supportedPids.size() + "\n\n");
        /*
         * =========================================================
         * LIVE DATA
         * =========================================================
         */
        result.append("=== LIVE DATA ===\n\n");
        int tested = 0;
        int skipped = 0;
        /*
         * Eseguiamo solamente i PID presenti
         * nel dataset e supportati dalla ECU.
         */
        for (PidDefinition definition : definitions) {
            String pid = definition.getPid().trim().toUpperCase();
            /*
             * Il PID non è stato dichiarato
             * supportato dalla bitmap.
             */
            if (!supportedPids.contains(pid)) {
                result.append("SKIP: " + pid + " non supportato dalla ECU\n\n");
                skipped++;
                continue;
            }
            /*
             * PID supportato:
             * eseguiamo la richiesta reale.
             */
            appendPidResult(result, definition);
            tested++;
        }

        result.append("=== RIEPILOGO ===\n");
        result.append("PID TESTATI: " + tested + "\n");
        result.append("PID SALTATI: " + skipped + "\n\n");
        result.append("=== LIVE DATA COMPLETATO ===");
        saveDiagnosticLog(result);
        return result.toString();
    }
    /**
     * Invia un singolo parametro diagnostico e interpreta
     * la risposta ricevuta.
     *
     * La costruzione della request, la comunicazione e
     * il parsing vengono delegati a DiagnosticPidExecutor.
     *
     * Elm327Manager mantiene invece la responsabilità
     * di interpretare gli errori specifici del firmware ELM327
     * e di produrre il log diagnostico.
     *
     * @param result buffer del risultato.
     * @param definition definizione PID/DID.
     *
     * @return risultato del test.
     *
     * @throws IOException errore comunicazione.
     */
    @NonNull
    private PidTestResult appendPidResult(
            @NonNull StringBuilder result,
            @NonNull PidDefinition definition)
            throws IOException {

        /*
         * ---------------------------------------------------------
         * ESECUZIONE REQUEST
         * ---------------------------------------------------------
         */

        DiagnosticPidExecutor.DiagnosticPidExecution execution;

        try {

            execution =
                    diagnosticPidExecutor.execute(
                            definition
                    );

        } catch (IllegalArgumentException exception) {

            result.append(
                    "ERRORE REQUEST/PARSING: "
                            + exception.getMessage()
                            + "\n\n"
            );

            return PidTestResult.INVALID_RESPONSE;

        } catch (IOException exception) {

            result.append(
                    "ERRORE COMUNICAZIONE: "
                            + exception.getMessage()
                            + "\n\n"
            );

            return PidTestResult.NO_RESPONSE;
        }

        /*
         * ---------------------------------------------------------
         * REQUEST
         * ---------------------------------------------------------
         */

        result.append(
                "Invio: "
                        + execution.getRequest()
                        + "\n"
        );

        /*
         * ---------------------------------------------------------
         * RISPOSTA RAW
         * ---------------------------------------------------------
         */

        String response =
                execution.getRawResponse();

        result.append(
                "RX: "
                        + formatResponse(
                        response
                )
                        + "\n"
        );

        /*
         * ---------------------------------------------------------
         * ERRORI ELM327
         * ---------------------------------------------------------
         *
         * Questa parte rimane nel manager.
         *
         * È importante perché:
         *
         * CAN ERROR
         * BUS ERROR
         * NO DATA
         * SEARCHING
         * UNABLE TO CONNECT
         *
         * non sono errori del parser diagnostico.
         */

        ElmError elmError =
                detectElmError(
                        response
                );

        if (elmError != ElmError.NONE) {

            appendElmError(
                    result,
                    elmError,
                    response
            );

            result.append(
                    "\n"
            );

            if (elmError == ElmError.CAN_ERROR ||
                    elmError == ElmError.BUS_ERROR ||
                    elmError == ElmError.UNABLE_TO_CONNECT) {

                return PidTestResult.BUS_ERROR;
            }

            return PidTestResult.ELM_ERROR;
        }

        /*
         * ---------------------------------------------------------
         * RISPOSTA VUOTA
         * ---------------------------------------------------------
         */

        if (isEmpty(response)) {

            result.append(
                    "STATO: NESSUNA RISPOSTA\n\n"
            );

            return PidTestResult.NO_RESPONSE;
        }

        /*
         * ---------------------------------------------------------
         * PARSING
         * ---------------------------------------------------------
         *
         * DiagnosticPidExecutor ha conservato sia:
         *
         * - risposta raw;
         * - risultato parsato.
         *
         * Se il parser non è riuscito a interpretare la risposta,
         * il risultato può essere null.
         */

        DiagnosticResponseResult diagnosticResponse =
                execution.getParsedResponse();

        if (diagnosticResponse == null) {

            result.append(
                    "STATO: RISPOSTA DIAGNOSTICA "
                            + "NON RICONOSCIUTA\n\n"
            );

            return PidTestResult.INVALID_RESPONSE;
        }

        /*
         * ---------------------------------------------------------
         * RISPOSTA NEGATIVA ECU
         * ---------------------------------------------------------
         */

        if (diagnosticResponse.isNegative()) {

            result.append(
                    "STATO: RISPOSTA NEGATIVA ECU\n"
            );

            result.append(
                    String.format(
                            Locale.US,
                            "SERVICE: %02X\n",
                            diagnosticResponse.getService()
                    )
            );

            result.append(
                    String.format(
                            Locale.US,
                            "IDENTIFIER: %04X\n",
                            diagnosticResponse.getIdentifier()
                    )
            );

            result.append(
                    String.format(
                            Locale.US,
                            "NRC: %02X\n\n",
                            diagnosticResponse
                                    .getNegativeResponseCode()
                    )
            );

            return PidTestResult.INVALID_RESPONSE;
        }

        /*
         * ---------------------------------------------------------
         * DATA
         * ---------------------------------------------------------
         */

        byte[] data =
                diagnosticResponse.getData();

        /*
         * ---------------------------------------------------------
         * OFFSET DATI
         * ---------------------------------------------------------
         *
         * Il parser può restituire il payload già depurato
         * dall'header diagnostico.
         *
         * responseDataOffset permette comunque a un dataset
         * di dichiarare un offset aggiuntivo.
         */

        int responseDataOffset =
                definition.getResponseDataOffset();

        if (responseDataOffset > 0) {

            if (responseDataOffset >=
                    data.length) {

                result.append(
                        "ERRORE: responseDataOffset "
                                + "oltre i dati ricevuti.\n"
                );

                return PidTestResult.INVALID_RESPONSE;
            }

            byte[] adjustedData =
                    new byte[
                            data.length
                                    - responseDataOffset
                            ];

            System.arraycopy(
                    data,
                    responseDataOffset,
                    adjustedData,
                    0,
                    adjustedData.length
            );

            data =
                    adjustedData;
        }

        /*
         * ---------------------------------------------------------
         * VERIFICA BYTE
         * ---------------------------------------------------------
         */

        if (definition.getBytes() > 0 &&
                data.length <
                        definition.getBytes()) {

            result.append(
                    "ERRORE: dati insufficienti.\n"
            );

            result.append(
                    "BYTE ATTESI: "
                            + definition.getBytes()
                            + "\n"
            );

            result.append(
                    "BYTE RICEVUTI: "
                            + data.length
                            + "\n\n"
            );

            return PidTestResult.INVALID_RESPONSE;
        }

        /*
         * ---------------------------------------------------------
         * INFORMAZIONI RISPOSTA
         * ---------------------------------------------------------
         */

        result.append(
                "PROTOCOLLO: "
                        + diagnosticResponse
                        .getProtocolType()
                        + "\n"
        );

        result.append(
                String.format(
                        Locale.US,
                        "SERVICE: %02X\n",
                        diagnosticResponse
                                .getService()
                )
        );

        result.append(
                String.format(
                        Locale.US,
                        "IDENTIFIER: %04X\n",
                        diagnosticResponse
                                .getIdentifier()
                )
        );

        result.append(
                "DATA: "
                        + bytesToHex(
                        data
                )
                        + "\n"
        );

        /*
         * ---------------------------------------------------------
         * FORMULA
         * ---------------------------------------------------------
         */

        result.append(
                "FORMULA JSON: "
                        + definition.getFormula()
                        + "\n"
        );

        /*
         * ---------------------------------------------------------
         * DECODIFICA
         * ---------------------------------------------------------
         */

        try {

            double value =
                    pidFormulaEvaluator.evaluate(
                            definition,
                            data
                    );

            result.append(
                    definition.getNameKey()
                            + ": "
                            + formatValue(
                            value
                    )
            );

            if (!definition.getUnit()
                    .isEmpty()) {

                result.append(
                        " "
                                + definition.getUnit()
                );
            }

            result.append(
                    "\n\n"
            );

            return PidTestResult.OK;

        } catch (
                IllegalArgumentException exception) {

            result.append(
                    "ERRORE DECODIFICA: "
                            + exception.getMessage()
                            + "\n\n"
            );

            return PidTestResult.INVALID_RESPONSE;
        }
    }



    /**************************************************************************
     *
     * ELM ERROR
     *
     **************************************************************************/

    /**
     * Tipi di errore generati dall'ELM327.
     */
    private enum ElmError {

        NONE,

        CAN_ERROR,

        BUS_ERROR,

        UNABLE_TO_CONNECT,

        NO_DATA,

        SEARCHING,

        BUFFER_FULL,

        ERROR
    }


    /**
     * Risultato elaborazione PID.
     */
    private enum PidTestResult {

        OK,

        NO_RESPONSE,

        INVALID_RESPONSE,

        ELM_ERROR,

        BUS_ERROR
    }


    /**
     * Rileva errori ELM327.
     *
     * @param response risposta.
     *
     * @return tipo errore.
     */
    @NonNull
    private ElmError detectElmError(
            String response) {

        if (response == null) {

            return ElmError.NONE;
        }

        String normalized =
                response
                        .replace("\r", " ")
                        .replace("\n", " ")
                        .replace(">", " ")
                        .trim()
                        .toUpperCase(
                                Locale.US
                        );

        if (normalized.isEmpty()) {

            return ElmError.NONE;
        }

        /*
         * CAN ERROR
         *
         * È esattamente il caso che stai
         * ricevendo in macchina.
         */

        if (normalized.contains("CAN ERROR")) {

            return ElmError.CAN_ERROR;
        }

        /*
         * BUS ERROR
         */

        if (normalized.contains("BUS ERROR")) {

            return ElmError.BUS_ERROR;
        }

        /*
         * UNABLE TO CONNECT
         */

        if (normalized.contains(
                "UNABLE TO CONNECT")) {

            return ElmError.UNABLE_TO_CONNECT;
        }

        /*
         * NO DATA
         */

        if (normalized.contains("NO DATA")) {

            return ElmError.NO_DATA;
        }

        /*
         * SEARCHING
         */

        if (normalized.contains("SEARCHING")) {

            return ElmError.SEARCHING;
        }

        /*
         * BUFFER FULL
         */

        if (normalized.contains(
                "BUFFER FULL")) {

            return ElmError.BUFFER_FULL;
        }

        /*
         * ERROR generico.
         *
         * Evitiamo di considerare "CAN ERROR"
         * qui perché è già stato intercettato sopra.
         */

        if (normalized.equals("ERROR") ||
                normalized.contains(" ERROR ")) {

            return ElmError.ERROR;
        }

        return ElmError.NONE;
    }


    /**
     * Aggiunge al risultato la descrizione
     * dell'errore ELM327.
     *
     * @param result buffer.
     * @param error errore.
     * @param response risposta originale.
     */
    private void appendElmError(
            @NonNull StringBuilder result,
            @NonNull ElmError error,
            String response) {

        switch (error) {

            case CAN_ERROR:

                result.append(
                        "STATO: ERRORE BUS CAN\n"
                );

                result.append(
                        "ERRORE ELM327: CAN ERROR\n"
                );

                result.append(
                        "L'ELM327 non sta riuscendo "
                                + "a comunicare correttamente "
                                + "sul bus CAN.\n"
                );

                result.append(
                        "Il problema NON è la formula "
                                + "del PID e NON è il parser.\n"
                );

                result.append(
                        "Verificare protocollo, connessione "
                                + "CAN, alimentazione e compatibilità "
                                + "dell'adattatore.\n"
                );

                break;


            case BUS_ERROR:

                result.append(
                        "STATO: ERRORE BUS\n"
                );

                result.append(
                        "ERRORE ELM327: BUS ERROR\n"
                );

                break;


            case UNABLE_TO_CONNECT:

                result.append(
                        "STATO: ELM327 NON RIESCE "
                                + "A CONNETTERSI AL BUS\n"
                );

                break;


            case NO_DATA:

                result.append(
                        "STATO: NESSUN DATO DALLA ECU\n"
                );

                break;


            case SEARCHING:

                result.append(
                        "STATO: ELM327 STA CERCANDO "
                                + "IL PROTOCOLLO/ECU\n"
                );

                break;


            case BUFFER_FULL:

                result.append(
                        "STATO: BUFFER ELM327 PIENO\n"
                );

                break;


            case ERROR:

                result.append(
                        "STATO: ERRORE ELM327\n"
                );

                break;


            default:

                result.append(
                        "STATO: ERRORE ELM327\n"
                );

                break;
        }

        if (response != null) {

            result.append(
                    "RISPOSTA ELM327: "
                            + formatResponse(response)
                            + "\n"
            );
        }
    }


    /**************************************************************************
     *
     * SEND COMMAND
     *
     **************************************************************************/

    /**
     * Invia comando all'ELM327.
     *
     * Il CR viene aggiunto automaticamente.
     *
     * @param command comando.
     *
     * @return risposta.
     *
     * @throws IOException errore comunicazione.
     */
    @NonNull
    public String sendCommand(
            @NonNull String command)
            throws IOException {

        connection.send(
                command + "\r"
        );

        String response =
                connection.receive();

        if (response == null) {

            return "";
        }

        return response;
    }


    /**************************************************************************
     *
     * OK
     *
     **************************************************************************/

    /**
     * Verifica risposta OK.
     *
     * @param command comando.
     * @param response risposta.
     *
     * @throws IOException risposta non OK.
     */
    private void checkOkResponse(
            @NonNull String command,
            String response)
            throws IOException {

        if (!isOkResponse(response)) {

            throw new IOException(
                    "Risposta inattesa da "
                            + command
                            + ": "
                            + formatResponse(response)
            );
        }
    }


    /**
     * Verifica risposta OK.
     *
     * @param response risposta.
     *
     * @return true se OK.
     */
    private boolean isOkResponse(
            String response) {

        if (response == null) {

            return false;
        }

        return response
                .trim()
                .toUpperCase(
                        Locale.US
                )
                .contains("OK");
    }


    /**************************************************************************
     *
     * VERSIONE
     *
     **************************************************************************/

    /**
     * Estrae la versione ELM327.
     *
     * @param response risposta ATI.
     *
     * @return versione o null.
     */
    @Nullable
    private String extractElmVersion(String response) {
        if (isEmpty(response)) {
            return null;
        }
        String cleaned = response.replace("\r", "\n").replace(">", "").trim();

        if (cleaned.isEmpty()) {
            return null;
        }

        String[] lines = cleaned.split("\\n");

        for (String line : lines) {
            String current = line.trim();
            if (current.isEmpty()) {
                continue;
            }
            if (current.equalsIgnoreCase("ATI")) {
                continue;
            }
            if (current.toUpperCase(Locale.US).startsWith("ELM327")) {
                return current;
            }
        }
        return cleaned;
    }


    /**************************************************************************
     *
     * UTILITÀ
     *
     **************************************************************************/

    /**
     * Verifica risposta vuota.
     *
     * @param response risposta.
     *
     * @return true se vuota.
     */
    private boolean isEmpty(String response) {
        return response == null || response.trim().isEmpty();
    }


    /**
     * Formatta risposta ELM327.
     *
     * @param response risposta.
     *
     * @return risposta leggibile.
     */
    @NonNull
    private String formatResponse(String response) {
        if (isEmpty(response)) {
            return "(nessuna risposta)";
        }
        return response.replace("\r", "\\r").replace("\n", "\\n");
    }


    /**
     * Formatta byte HEX.
     *
     * @param value valore.
     *
     * @return HEX.
     */
    @NonNull
    private String formatHex(int value) {
        return String.format(Locale.US,"%02X",value & 0xFF);
    }


    /**
     * Formatta valore PID.
     *
     * @param value valore.
     *
     * @return valore formattato.
     */
    @NonNull
    private String formatValue(double value) {
        if (value == Math.rint(value)) {
            return String.format(Locale.US,"%.0f",value);
        }
        return String.format(Locale.US, "%.2f", value);
    }



    /**
     * Converte un array di byte in una stringa HEX
     * separata da spazi.
     *
     * @param data dati.
     *
     * @return rappresentazione HEX.
     */
    @NonNull
    private String bytesToHex(
            @NonNull byte[] data) {

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
                            Locale.US,
                            "%02X",
                            data[index] & 0xFF
                    )
            );
        }

        return result.toString();
    }


    /**************************************************************************
     *
     * GETTER
     *
     **************************************************************************/

    /**
     * Restituisce versione ELM327.
     *
     * @return versione.
     */
    @Nullable
    public String getElmVersion() {
        return elmVersion;
    }


    /**
     * Restituisce stato inizializzazione.
     *
     * @return true se inizializzato.
     */
    public boolean isInitialized() {
        return initialized;
    }


    /**
     * Assicura che l'ELM327 sia inizializzato.
     *
     * Se non è ancora inizializzato, esegue automaticamente
     * la procedura di inizializzazione.
     *
     * @throws IOException errore di comunicazione.
     */
    private void ensureInitialized() throws IOException {
        if (initialized) {
            return;
        }
        if (!connection.isConnected()) {
            throw new IOException("Connection non connessa.");
        }
        initialize();
    }


    /**
     * Salva il log diagnostico se il logger è disponibile.
     *
     * @param result contenuto del log.
     */
    private void saveDiagnosticLog(@NonNull StringBuilder result) {

        if (diagnosticLogger == null) {
            return;
        }
        try {
            diagnosticLogger.saveLog(result.toString());
        } catch (IOException exception) {
            result.append("\nERRORE SALVATAGGIO LOG: " + exception.getMessage() + "\n");
        }
    }



    private boolean executeOptionalAtCommand(@NonNull StringBuilder result, @NonNull String command) throws IOException {

        result.append("Invio: " + command + "\n");
        String response = sendCommand(command);
        result.append("RX: " + formatResponse(response) + "\n\n");

        if (isOkResponse(response)) {
            return true;
        }

        String normalized =
                response == null
                        ? ""
                        : response
                        .replace("\r", "")
                        .replace("\n", "")
                        .replace(">", "")
                        .trim()
                        .toUpperCase(Locale.US);

        if (normalized.equals("?")) {
            result.append("AVVISO: comando " + command + " non supportato " + "dal firmware ELM327.\n\n");
            return true;
        }

        result.append("ERRORE: risposta inattesa da " + command + ".\n");
        return false;
    }


    /**
     * Crea il transport diagnostico catalog-driven.
     *
     * Questo percorso utilizza:
     *
     * EcuDefinition
     *      ↓
     * DiagnosticTargetDefinition
     *      ↓
     * Elm327AdapterConfigurator
     *      ↓
     * Elm327ConfigurationExecutor
     *      ↓
     * Elm327CommandExecutor
     *      ↓
     * Elm327Manager
     *      ↓
     * Connection
     *
     * Il percorso legacy del DiagnosticPidExecutor interno
     * non viene modificato.
     *
     * @return transport diagnostico configurato per lavorare
     *         con target ECU espliciti.
     */
    @NonNull
    public DiagnosticTransport createDiagnosticTransport() {

        Elm327ManagerCommandSender commandSender =
                new Elm327ManagerCommandSender(
                        this
                );

        Elm327CommandExecutor commandExecutor =
                new Elm327CommandExecutor(
                        commandSender
                );

        Elm327ConfigurationExecutor configurationExecutor =
                new Elm327ConfigurationExecutor(
                        commandExecutor
                );

        Elm327AdapterConfigurator adapterConfigurator =
                new Elm327AdapterConfigurator();

        return new Elm327DiagnosticTransport(
                connection,
                adapterConfigurator,
                configurationExecutor
        );
    }


    /**
     * Restituisce il DiagnosticPidExecutor del percorso
     * catalog-driven.
     *
     * La stessa istanza viene riutilizzata da tutte le componenti
     * che appartengono alla stessa sessione diagnostica del manager.
     *
     * In questo modo:
     *
     * VehicleIdentifier
     * EcuIdentifier
     *
     * possono condividere lo stesso:
     *
     * DiagnosticTransport
     * DiagnosticTransportState
     * target configurato
     * stato configurazione ELM327
     *
     * @return executor catalog-driven condiviso.
     */
    @NonNull
    public synchronized DiagnosticPidExecutor
    createCatalogDiagnosticPidExecutor() {

        if (catalogDiagnosticPidExecutor == null) {

            Elm327ManagerCommandSender commandSender =
                    new Elm327ManagerCommandSender(
                            this
                    );

            Elm327CommandExecutor commandExecutor =
                    new Elm327CommandExecutor(
                            commandSender
                    );

            Elm327ConfigurationExecutor configurationExecutor =
                    new Elm327ConfigurationExecutor(
                            commandExecutor
                    );

            Elm327AdapterConfigurator adapterConfigurator =
                    new Elm327AdapterConfigurator();

            DiagnosticTransport transport =
                    new Elm327DiagnosticTransport(
                            connection,
                            adapterConfigurator,
                            configurationExecutor
                    );

            catalogDiagnosticPidExecutor =
                    new DiagnosticPidExecutor(
                            transport,
                            new ReadOnlyDiagnosticPolicy()
                    );
        }

        return catalogDiagnosticPidExecutor;
    }


    /**
     * Crea un EcuIdentifier utilizzando lo stesso
     * DiagnosticPidExecutor catalog-driven del manager.
     *
     * Non viene creato un nuovo executor.
     *
     * @return EcuIdentifier catalog-driven.
     */
    @NonNull
    public EcuIdentifier createCatalogEcuIdentifier() {

        return new EcuIdentifier(
                createCatalogDiagnosticPidExecutor()
        );
    }
}
