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
 * La classe NON invia direttamente i comandi.
 *
 * Produce esclusivamente Elm327CommandPlan.
 *
 * Strategia:
 *
 * - bitrate 0    -> ATSP0
 *                   protocollo automatico, compatibilità legacy;
 *
 * - bitrate 500  -> ATTP6 per CAN 11 bit;
 *                   ATTP7 per CAN 29 bit.
 *
 * ATTP viene utilizzato per provare il protocollo senza renderlo
 * il protocollo predefinito dell'ELM327.
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
     * Stato configuratore.
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
     * Configura logicamente l'adapter.
     *
     * Non viene eseguita alcuna comunicazione.
     *
     * @param target target diagnostico.
     *
     * @throws IOException target non supportato.
     */
    @Override
    public void configure(
            @NonNull DiagnosticTargetDefinition target)
            throws IOException {

        if (!target.isCan()
                && !target.isAutomaticProtocol()) {

            throw new IOException(
                    "Protocollo non supportato dal "
                            + "configuratore ELM327: "
                            + target.getProtocol()
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
     * Ripristina lo stato del configuratore.
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
     * Costruisce il piano di configurazione.
     *
     * Regole:
     *
     * bitrate 0:
     *
     *     ATSP0
     *
     *     Il catalogo non specifica il bitrate.
     *
     * bitrate 500:
     *
     *     11 bit -> ATTP6
     *     29 bit -> ATTP7
     *
     * Altri bitrate non sono ancora supportati.
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



        if (target.isAutomaticProtocol()) {

            List<String> commands =
                    new ArrayList<>();

            /*
             * Non forziamo alcun protocollo.
             *
             * L'ELM327 rimane in AUTO e utilizza
             * il protocollo che ha già rilevato.
             *
             * Non impostiamo:
             *
             * ATTP
             * ATSH
             * ATCRA
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
         * SELEZIONE PROTOCOLLO
         * ---------------------------------------------------------
         *
         * Se il bitrate non è specificato manteniamo
         * il comportamento legacy ATSP0.
         *
         * Se invece il catalogo specifica 500 kbit/s,
         * utilizziamo ATTP per provare il protocollo
         * senza selezionarlo come default.
         */
        if (bitrate == 0) {

            commands.add(
                    "ATSP0"
            );

        } else if (bitrate == 500) {

            if (target.isStandardCanId()) {

                commands.add(
                        "ATTP6"
                );

            } else if (target.isExtendedCanId()) {

                commands.add(
                        "ATTP7"
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
         * OUTPUT
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
     * Esegue l'ultimo piano.
     *
     * Ogni comando deve restituire una risposta OK.
     *
     * @param executor esecutore comandi AT.
     *
     * @throws IOException errore configurazione.
     */
    public void executePlan(
            @NonNull Elm327CommandExecutor executor)
            throws IOException {

        if (lastPlan == null) {

            throw new IOException(
                    "Nessun piano di configurazione disponibile."
            );
        }

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
     * Costruisce ed esegue il piano.
     *
     * @param target target.
     * @param executor executor AT.
     *
     * @throws IOException errore configurazione.
     */
    public void configureAndExecute(
            @NonNull DiagnosticTargetDefinition target,
            @NonNull Elm327CommandExecutor executor)
            throws IOException {

        configure(
                target
        );

        executePlan(
                executor
        );
    }

    /**
     * Restituisce target configurato.
     *
     * @return target oppure null.
     */
    public DiagnosticTargetDefinition
    getConfiguredTarget() {

        return configuredTarget;
    }

    /**
     * Restituisce ultimo piano.
     *
     * @return piano oppure null.
     */
    public Elm327CommandPlan getLastPlan() {

        return lastPlan;
    }

    /**
     * Indica se configurato.
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
}