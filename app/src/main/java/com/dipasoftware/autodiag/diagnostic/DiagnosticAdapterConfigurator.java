package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

import java.io.IOException;

/**
 * ****************************************************************************
 *
 * Classe.....: DiagnosticAdapterConfigurator
 *
 * Tipo.......: Interface
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Astrae la configurazione dell'adapter diagnostico necessaria
 * per comunicare con uno specifico target ECU.
 *
 * Questa interfaccia NON definisce servizi diagnostici.
 *
 * NON decide se una richiesta diagnostica è autorizzata.
 *
 * NON invia direttamente richieste UDS/OBD alla ECU.
 *
 * Il suo compito è esclusivamente configurare il mezzo di comunicazione
 * in funzione del DiagnosticTargetDefinition.
 *
 * ****************************************************************************
 */
public interface DiagnosticAdapterConfigurator {

    /**
     * Configura l'adapter per il target specificato.
     *
     * @param target target diagnostico.
     *
     * @throws IOException errore di comunicazione con l'adapter.
     */
    void configure(
            @NonNull DiagnosticTargetDefinition target)
            throws IOException;

    /**
     * Ripristina la configurazione precedente/default dell'adapter.
     *
     * Questo metodo viene utilizzato quando una sessione diagnostica
     * termina o quando si deve cambiare target.
     *
     * @throws IOException errore di comunicazione.
     */
    void reset()
            throws IOException;
}