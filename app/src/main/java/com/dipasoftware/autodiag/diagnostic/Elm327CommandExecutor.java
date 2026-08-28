package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

import java.io.IOException;

/**
 * ****************************************************************************
 *
 * Classe.....: Elm327CommandExecutor
 *
 * Tipo.......: Service
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Esegue comandi di configurazione destinati ESCLUSIVAMENTE
 * all'adapter ELM327.
 *
 * NON esegue richieste diagnostiche verso la ECU.
 *
 * NON sostituisce DiagnosticOperationPolicy.
 *
 * Responsabilità:
 *
 * - inviare un comando AT al manager ELM327;
 * - verificare la risposta;
 * - interrompere la configurazione in caso di errore.
 *
 * ****************************************************************************
 */
public class Elm327CommandExecutor {

    /**
     * Interfaccia minima utilizzata per comunicare con l'ELM327.
     */
    public interface CommandSender {

        /**
         * Invia un comando all'adapter.
         *
         * @param command comando AT.
         *
         * @return risposta adapter.
         *
         * @throws IOException errore comunicazione.
         */
        @NonNull
        String sendCommand(
                @NonNull String command)
                throws IOException;
    }

    /**
     * Sender utilizzato.
     */
    @NonNull
    private final CommandSender commandSender;

    /**
     * Costruttore.
     *
     * @param commandSender sender.
     */
    public Elm327CommandExecutor(
            @NonNull CommandSender commandSender) {

        this.commandSender =
                commandSender;
    }

    /**
     * Esegue un singolo comando AT.
     *
     * @param command comando.
     *
     * @return risposta.
     *
     * @throws IOException errore o risposta non valida.
     */
    @NonNull
    public String execute(
            @NonNull String command)
            throws IOException {

        String normalized =
                command.trim();

        if (normalized.isEmpty()) {

            throw new IOException(
                    "Comando ELM327 vuoto."
            );
        }

        if (!isAtCommand(
                normalized
        )) {

            throw new IOException(
                    "Sono consentiti esclusivamente "
                            + "comandi AT ELM327."
            );
        }

        String response =
                commandSender.sendCommand(
                        normalized
                );

        if (response == null) {

            throw new IOException(
                    "Nessuna risposta dal ELM327 "
                            + "per il comando "
                            + normalized
            );
        }

        return response;
    }

    /**
     * Esegue un comando AT e richiede risposta OK.
     *
     * @param command comando.
     *
     * @return risposta.
     *
     * @throws IOException risposta diversa da OK.
     */
    @NonNull
    public String executeExpectOk(
            @NonNull String command)
            throws IOException {

        String response =
                execute(
                        command
                );

        if (!isOkResponse(
                response
        )) {

            throw new IOException(
                    "Risposta inattesa da "
                            + command
                            + ": "
                            + response
            );
        }

        return response;
    }

    /**
     * Verifica che il comando sia un comando AT.
     *
     * @param command comando.
     *
     * @return true se AT.
     */
    private boolean isAtCommand(
            @NonNull String command) {

        return command
                .toUpperCase()
                .startsWith("AT");
    }

    /**
     * Verifica una risposta OK.
     *
     * @param response risposta.
     *
     * @return true se contiene OK.
     */
    private boolean isOkResponse(
            @NonNull String response) {

        return response
                .trim()
                .toUpperCase()
                .contains("OK");
    }
}