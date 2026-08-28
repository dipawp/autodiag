package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ****************************************************************************
 *
 * Classe.....: Elm327AdapterConfigurator
 *
 * Tipo.......: Adapter
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Traduce un DiagnosticTargetDefinition in un piano di configurazione
 * ELM327.
 *
 * La classe NON invia i comandi.
 *
 * Produce esclusivamente Elm327CommandPlan.
 *
 * Regole:
 *
 * - bitrate 0    = non specificato -> ATSP0
 * - 500 kbit/s
 *      11 bit     -> ATSP6
 *      29 bit     -> ATSP7
 *
 * ****************************************************************************
 */
public class Elm327AdapterConfigurator
        implements DiagnosticAdapterConfigurator {

    /**
     * Target configurato.
     */
    private DiagnosticTargetDefinition configuredTarget;

    /**
     * Ultimo piano generato.
     */
    private Elm327CommandPlan lastPlan;

    /**
     * Stato del configuratore.
     */
    private boolean configured;

    /**
     * Costruttore.
     */
    public Elm327AdapterConfigurator() {

        configuredTarget =
                null;

        lastPlan =
                null;

        configured =
                false;
    }

    /**
     * Registra il target e genera il piano.
     *
     * Nessuna comunicazione fisica viene effettuata.
     *
     * @param target target diagnostico.
     *
     * @throws IOException target non supportato.
     */
    @Override
    public void configure(
            @NonNull DiagnosticTargetDefinition target)
            throws IOException {

        if (!target.isCan()) {

            throw new IOException(
                    "Il target non è compatibile "
                            + "con ELM327 CAN."
            );
        }

        lastPlan =
                buildCommandPlan(
                        target
                );

        configuredTarget =
                target;

        configured =
                true;
    }

    /**
     * Ripristina la configurazione logica.
     *
     * @throws IOException non utilizzata.
     */
    @Override
    public void reset()
            throws IOException {

        configuredTarget =
                null;

        lastPlan =
                null;

        configured =
                false;
    }

    /**
     * Costruisce il piano ELM327.
     *
     * Regole:
     *
     * bitrate = 0:
     *
     *     ATSP0
     *
     *     Il catalogo non specifica il bitrate, quindi
     *     non facciamo assunzioni.
     *
     * bitrate = 500:
     *
     *     11 bit -> ATSP6
     *     29 bit -> ATSP7
     *
     * Altri bitrate non sono ancora mappati.
     *
     * @param target target diagnostico.
     *
     * @return piano.
     *
     * @throws IOException configurazione non supportata.
     */
    @NonNull
    public Elm327CommandPlan buildCommandPlan(
            @NonNull DiagnosticTargetDefinition target)
            throws IOException {

        if (!target.isCan()) {

            throw new IOException(
                    "Protocollo non CAN non supportato "
                            + "dal configuratore ELM327."
            );
        }

        int bitrate =
                target.getCanBitrateKbps();

        List<String> commands =
                new ArrayList<>();

        /*
         * ---------------------------------------------------------
         * SELEZIONE PROTOCOLLO ELM327
         * ---------------------------------------------------------
         *
         * 0 = bitrate non specificato
         *     -> lasciamo all'ELM327 il protocollo automatico.
         */
        if (bitrate == 0) {

            commands.add(
                    "ATSP0"
            );

        } else if (bitrate == 500) {

            /*
             * 6 = ISO 15765-4 CAN 11 bit / 500 kbaud
             */
            if (target.isStandardCanId()) {

                commands.add(
                        "ATSP6"
                );

                /*
                 * 7 = ISO 15765-4 CAN 29 bit / 500 kbaud
                 */
            } else if (target.isExtendedCanId()) {

                commands.add(
                        "ATSP7"
                );

            } else {

                throw new IOException(
                        "Dimensione CAN ID non supportata: "
                                + target.getCanIdBits()
                );
            }

        } else {

            throw new IOException(
                    "Bitrate CAN non supportato dal "
                            + "configuratore ELM327: "
                            + bitrate
                            + " kbit/s."
            );
        }

        /*
         * ---------------------------------------------------------
         * HEADER DI TRASMISSIONE
         * ---------------------------------------------------------
         */
        commands.add(
                "ATSH "
                        + target.getRequestId()
        );

        /*
         * ---------------------------------------------------------
         * FILTRO DI RICEZIONE
         * ---------------------------------------------------------
         */
        commands.add(
                "ATCRA "
                        + target.getResponseId()
        );

        /*
         * ---------------------------------------------------------
         * OUTPUT ELM327
         * ---------------------------------------------------------
         */
        commands.add(
                "ATE0"
        );

        commands.add(
                "ATH0"
        );

        return new Elm327CommandPlan(
                target,
                commands
        );
    }

    /**
     * Restituisce il target configurato.
     *
     * @return target oppure null.
     */
    public DiagnosticTargetDefinition
    getConfiguredTarget() {

        return configuredTarget;
    }

    /**
     * Restituisce l'ultimo piano.
     *
     * @return piano oppure null.
     */
    public Elm327CommandPlan getLastPlan() {

        return lastPlan;
    }

    /**
     * Indica se il configuratore è attivo.
     *
     * @return true se configurato.
     */
    public boolean isConfigured() {

        return configured;
    }

    /**
     * Restituisce i comandi pianificati.
     *
     * @return lista immutabile.
     */
    @NonNull
    public List<String> getPlannedCommands() {

        if (lastPlan == null) {

            return Collections.emptyList();
        }

        return lastPlan.getCommands();
    }


    /**
     * Esegue il piano di configurazione sull'ELM327.
     *
     * Ogni comando viene eseguito in ordine.
     *
     * Se un comando restituisce una risposta diversa da OK,
     * la configurazione viene interrotta immediatamente.
     *
     * @param executor esecutore dei comandi AT.
     * @throws IOException errore di comunicazione o configurazione.
     */
    public void executePlan(
            @NonNull Elm327CommandExecutor executor)
            throws IOException {

        if (lastPlan == null) {

            throw new IOException(
                    "Nessun piano di configurazione disponibile."
            );
        }

        configured =
                false;

        for (
                String command :
                lastPlan.getCommands()
        ) {

            executor.executeExpectOk(
                    command
            );
        }

        configured =
                true;
    }

    /**
     * Costruisce ed esegue il piano per il target indicato.
     *
     * @param target target diagnostico.
     * @param executor esecutore AT.
     *
     * @throws IOException errore di configurazione.
     */
    public void configureAndExecute(
            @NonNull DiagnosticTargetDefinition target,
            @NonNull Elm327CommandExecutor executor)
            throws IOException {

        /*
         * Costruzione del piano.
         */
        configure(
                target
        );

        /*
         * Esecuzione del piano.
         */
        executePlan(
                executor
        );
    }
}