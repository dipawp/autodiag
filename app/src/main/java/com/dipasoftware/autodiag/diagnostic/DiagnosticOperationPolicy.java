package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

/**
 * ****************************************************************************
 *
 * Classe.....: DiagnosticOperationPolicy
 *
 * Tipo.......: Policy / Interface
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Definisce la politica di sicurezza applicata alle richieste
 * diagnostiche prima che vengano inviate alla Connection.
 *
 * La policy è separata dall'esecuzione vera e propria in modo da
 * permettere, in futuro, l'introduzione di modalità differenti
 * senza modificare DiagnosticPidExecutor.
 *
 * Nella versione corrente dell'applicazione viene utilizzata
 * esclusivamente ReadOnlyDiagnosticPolicy.
 *
 * ****************************************************************************
 */
public interface DiagnosticOperationPolicy {

    /**
     * Verifica se una richiesta diagnostica è consentita.
     *
     * @param definition definizione del parametro.
     * @param request richiesta HEX già costruita.
     *
     * @return true se la richiesta è consentita.
     */
    boolean isAllowed(
            @NonNull PidDefinition definition,
            @NonNull String request
    );

    /**
     * Verifica una richiesta e genera un errore
     * se la policy non la consente.
     *
     * @param definition definizione del parametro.
     * @param request richiesta HEX.
     *
     * @throws IllegalArgumentException richiesta non consentita.
     */
    default void validate(
            @NonNull PidDefinition definition,
            @NonNull String request) {

        if (!isAllowed(
                definition,
                request
        )) {

            throw new IllegalArgumentException(
                    "Operazione diagnostica non consentita "
                            + "dalla policy: "
                            + request
            );
        }
    }
}