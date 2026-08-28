package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

import java.io.IOException;

/**
 * ****************************************************************************
 *
 * Classe.....: DiagnosticTransport
 *
 * Tipo.......: Interface
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Astrae il trasporto utilizzato per inviare una richiesta diagnostica
 * verso una specifica ECU.
 *
 * Il transport layer conosce:
 *
 * - target diagnostico;
 * - richiesta diagnostica;
 * - ricezione della risposta.
 *
 * Non decide:
 *
 * - se un servizio diagnostico è autorizzato;
 * - come interpretare la risposta;
 * - quale PID/DID utilizzare.
 *
 * La policy di sicurezza rimane a monte.
 *
 * ****************************************************************************
 */
public interface DiagnosticTransport {

    /**
     * Invia una richiesta diagnostica verso il target indicato.
     *
     * IMPORTANTE:
     *
     * La richiesta deve essere già stata autorizzata dalla
     * DiagnosticOperationPolicy prima di arrivare qui.
     *
     * @param target target diagnostico.
     * @param request richiesta diagnostica senza CR finale.
     *
     * @throws IOException errore di comunicazione.
     */
    void send(
            @NonNull DiagnosticTargetDefinition target,
            @NonNull String request)
            throws IOException;

    /**
     * Riceve la risposta relativa all'ultima richiesta.
     *
     * @param target target diagnostico.
     *
     * @return risposta raw.
     *
     * @throws IOException errore di comunicazione.
     */
    @NonNull
    String receive(
            @NonNull DiagnosticTargetDefinition target)
            throws IOException;
}