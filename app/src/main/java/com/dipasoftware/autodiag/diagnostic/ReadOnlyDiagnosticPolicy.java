package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * ****************************************************************************
 *
 * Classe.....: ReadOnlyDiagnosticPolicy
 *
 * Tipo.......: Policy
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Policy di sicurezza read-only dell'applicazione.
 *
 * CONSENTE esclusivamente servizi diagnostici classificati
 * come lettura.
 *
 * Tutto ciò che non appartiene esplicitamente alla whitelist
 * viene rifiutato.
 *
 * IMPORTANTE:
 *
 * Questa classe non tenta di individuare solamente i comandi
 * "noti per essere pericolosi".
 *
 * Utilizza una whitelist positiva:
 *
 *     servizio presente nella whitelist -> consentito
 *     servizio assente dalla whitelist  -> bloccato
 *
 * Questo approccio permette di mantenere l'applicazione
 * read-only anche quando in futuro verranno aggiunti nuovi
 * servizi diagnostici.
 *
 * ****************************************************************************
 */
public class ReadOnlyDiagnosticPolicy
        implements DiagnosticOperationPolicy {

    /**
     * Servizi OBD-II di sola lettura supportati dalla V1.
     *
     * 01 = current data
     * 02 = freeze frame
     * 03 = stored DTC
     * 06 = test results
     * 07 = pending DTC
     * 09 = vehicle information
     * 0A = permanent DTC
     */
    private static final Set<Integer> OBD_READ_SERVICES;

    /**
     * Servizi UDS considerati di sola lettura.
     *
     * 19 = ReadDTCInformation
     * 22 = ReadDataByIdentifier
     * 23 = ReadMemoryByAddress
     * 24 = ReadScalingDataByIdentifier
     * 2A = ReadDataByPeriodicIdentifier
     */
    private static final Set<Integer> UDS_READ_SERVICES;

    static {

        Set<Integer> obdServices =
                new HashSet<>();

        obdServices.add(0x01);
        obdServices.add(0x02);
        obdServices.add(0x03);
        obdServices.add(0x06);
        obdServices.add(0x07);
        obdServices.add(0x09);
        obdServices.add(0x0A);

        OBD_READ_SERVICES =
                Collections.unmodifiableSet(
                        obdServices
                );

        Set<Integer> udsServices =
                new HashSet<>();

        udsServices.add(0x19);
        udsServices.add(0x22);
        udsServices.add(0x23);
        udsServices.add(0x24);
        udsServices.add(0x2A);

        UDS_READ_SERVICES =
                Collections.unmodifiableSet(
                        udsServices
                );
    }

    /**
     * Verifica se una richiesta appartiene a un servizio
     * esplicitamente autorizzato.
     *
     * La verifica utilizza il primo byte della request,
     * cioè il service/mode diagnostico.
     *
     * @param definition definizione parametro.
     * @param request richiesta HEX.
     *
     * @return true se consentita.
     */
    @Override
    public boolean isAllowed(
            @NonNull PidDefinition definition,
            @NonNull String request) {

        String normalized =
                normalize(
                        request
                );

        if (normalized.length() < 2) {

            return false;
        }

        int service =
                parseByte(
                        normalized.substring(
                                0,
                                2
                        )
                );

        /*
         * Servizi OBD-II standard.
         */
        if (isObdReadService(
                service
        )) {

            return true;
        }

        /*
         * Servizi UDS di sola lettura.
         */
        if (isUdsReadService(
                service
        )) {

            return true;
        }

        /*
         * Tutto ciò che non è esplicitamente
         * nella whitelist viene bloccato.
         */
        return false;
    }

    /**
     * Verifica un servizio OBD-II di sola lettura.
     *
     * @param service service/mode.
     *
     * @return true se consentito.
     */
    public boolean isObdReadService(
            int service) {

        return OBD_READ_SERVICES.contains(
                service
        );
    }

    /**
     * Verifica un servizio UDS di sola lettura.
     *
     * @param service service UDS.
     *
     * @return true se consentito.
     */
    public boolean isUdsReadService(
            int service) {

        return UDS_READ_SERVICES.contains(
                service
        );
    }

    /**
     * Normalizza una request HEX.
     *
     * @param value request.
     *
     * @return stringa HEX normalizzata.
     */
    @NonNull
    private String normalize(
            @NonNull String value) {

        return value
                .replace(
                        " ",
                        ""
                )
                .replace(
                        "\r",
                        ""
                )
                .replace(
                        "\n",
                        ""
                )
                .replace(
                        ">",
                        ""
                )
                .trim()
                .toUpperCase(
                        Locale.US
                );
    }

    /**
     * Converte due caratteri HEX in un byte.
     *
     * @param value valore HEX.
     *
     * @return valore numerico.
     */
    private int parseByte(
            @NonNull String value) {

        try {

            return Integer.parseInt(
                    value,
                    16
            );

        } catch (NumberFormatException exception) {

            return -1;
        }
    }
}