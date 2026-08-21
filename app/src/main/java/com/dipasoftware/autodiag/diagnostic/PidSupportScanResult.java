package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * ****************************************************************************
 *
 * Classe.....: PidSupportScanResult
 *
 * Tipo.......: Model
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Contiene il risultato dell'interrogazione di una singola
 * bitmap di supporto PID.
 *
 * Esempio:
 *
 *     richiesta 0100
 *
 *     risposta:
 *
 *     41 00 98 3B 00 11
 *
 * Il risultato conterrà:
 *
 *     status = SUPPORTED
 *     basePid = 00
 *     bitfield = 98 3B 00 11
 *
 * Per una risposta:
 *
 *     7F 01 12
 *
 * il risultato conterrà:
 *
 *     status = NEGATIVE_RESPONSE
 *     bitfield = null
 *
 * ****************************************************************************
 */
public class PidSupportScanResult {

    /**
     * Esito della richiesta della bitmap.
     */
    @NonNull
    private final PidSupportScanStatus status;

    /**
     * PID base della bitmap richiesta.
     *
     * Esempi:
     *
     * 00
     * 20
     * 40
     */
    private final int basePid;

    /**
     * Quattro byte della bitmap ricevuta.
     *
     * Null quando la risposta non contiene una bitmap valida.
     */
    @Nullable
    private final byte[] bitfield;

    /**
     * Risposta originale ricevuta dall'ELM327.
     *
     * Utile per logging e diagnostica.
     */
    @NonNull
    private final String rawResponse;

    /**
     * Costruisce il risultato della scansione.
     *
     * @param status stato della risposta.
     * @param basePid PID base interrogato.
     * @param bitfield bitmap ricevuta oppure null.
     * @param rawResponse risposta originale ELM327.
     */
    public PidSupportScanResult(
            @NonNull PidSupportScanStatus status,
            int basePid,
            @Nullable byte[] bitfield,
            @NonNull String rawResponse) {

        this.status = status;
        this.basePid = basePid;
        this.bitfield = bitfield;
        this.rawResponse = rawResponse;
    }

    /**
     * Restituisce lo stato della richiesta.
     *
     * @return stato scansione.
     */
    @NonNull
    public PidSupportScanStatus getStatus() {
        return status;
    }

    /**
     * Restituisce il PID base interrogato.
     *
     * @return PID base.
     */
    public int getBasePid() {
        return basePid;
    }

    /**
     * Restituisce la bitmap ricevuta.
     *
     * @return bitmap oppure null.
     */
    @Nullable
    public byte[] getBitfield() {
        return bitfield;
    }

    /**
     * Restituisce la risposta originale ELM327.
     *
     * @return risposta raw.
     */
    @NonNull
    public String getRawResponse() {
        return rawResponse;
    }

    /**
     * Verifica se la risposta contiene una bitmap valida.
     *
     * @return true se la risposta è positiva.
     */
    public boolean isSupported() {
        return status ==
                PidSupportScanStatus.SUPPORTED;
    }

    /**
     * Verifica se la ECU ha risposto negativamente.
     *
     * @return true se risposta negativa.
     */
    public boolean isNegativeResponse() {
        return status ==
                PidSupportScanStatus.NEGATIVE_RESPONSE;
    }
}