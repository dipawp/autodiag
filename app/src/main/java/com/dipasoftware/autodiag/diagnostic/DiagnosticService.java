package com.dipasoftware.autodiag.diagnostic;

/******************************************************************************
 *
 * Classe.....: DiagnosticService
 *
 * Tipo.......: Enum
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Rappresenta i servizi diagnostici utilizzati dal motore
 * di AutoDiag.
 *
 * Ogni valore identifica un servizio del protocollo OBD-II
 * o UDS.
 *
 * La enum NON contiene logica e NON conosce i PID.
 *
 ******************************************************************************/
public enum DiagnosticService {

    /**
     * Mode 01 - Show Current Data
     */
    SHOW_CURRENT_DATA("01"),

    /**
     * Mode 03 - Read DTC
     */
    READ_DTC("03"),

    /**
     * Mode 04 - Clear DTC
     */
    CLEAR_DTC("04"),

    /**
     * Mode 07 - Pending DTC
     */
    READ_PENDING_DTC("07"),

    /**
     * Mode 09 - Vehicle Information
     */
    VEHICLE_INFORMATION("09"),

    /**
     * UDS Service 22 - Read Data By Identifier
     */
    READ_DATA_BY_IDENTIFIER("22"),

    /**
     * UDS Service 2E - Write Data By Identifier
     */
    WRITE_DATA_BY_IDENTIFIER("2E"),

    /**
     * UDS Service 19 - Read DTC Information
     */
    READ_DTC_INFORMATION("19");

    /**
     * Codice esadecimale del servizio.
     */
    private final String code;

    /**
     * Costruttore della enum.
     *
     * @param code codice del servizio diagnostico.
     */
    DiagnosticService(String code) {
        this.code = code;
    }

    /**
     * Restituisce il codice esadecimale del servizio.
     *
     * @return codice del servizio.
     */
    public String getCode() {
        return code;
    }

    /**
     * Restituisce il servizio diagnostico corrispondente
     * al codice esadecimale.
     *
     * @param code codice del servizio.
     *
     * @return servizio diagnostico.
     *
     * @throws IllegalArgumentException
     * se il codice non è supportato.
     */
    public static DiagnosticService fromCode(String code) {

        for (DiagnosticService service : values()) {

            if (service.code.equalsIgnoreCase(code)) {
                return service;
            }

        }

        throw new IllegalArgumentException(
                "Servizio diagnostico non supportato: " + code
        );
    }

}