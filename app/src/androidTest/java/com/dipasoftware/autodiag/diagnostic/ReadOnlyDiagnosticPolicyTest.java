package com.dipasoftware.autodiag.diagnostic;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * ****************************************************************************
 *
 * Classe.....: ReadOnlyDiagnosticPolicyTest
 *
 * Tipo.......: Android Instrumented Test
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Verifica la whitelist delle operazioni diagnostiche
 * consentite dalla V1 read-only.
 *
 * ****************************************************************************
 */
public class ReadOnlyDiagnosticPolicyTest {

    /**
     * Definizione standard di test.
     */
    private PidDefinition createDefinition() {

        return new PidDefinition(
                "010C",
                "pid_engine_rpm",
                "pid_engine_rpm_description",
                "rpm",
                "FORMULA",
                "((A*256)+B)/4",
                2,
                "01",
                "RPM"
        );
    }

    /**
     * Mode 01 deve essere consentito.
     */
    @Test
    public void mode01IsAllowed() {

        ReadOnlyDiagnosticPolicy policy =
                new ReadOnlyDiagnosticPolicy();

        assertTrue(
                policy.isAllowed(
                        createDefinition(),
                        "010C"
                )
        );
    }

    /**
     * Mode 03 deve essere consentito.
     */
    @Test
    public void mode03IsAllowed() {

        ReadOnlyDiagnosticPolicy policy =
                new ReadOnlyDiagnosticPolicy();

        assertTrue(
                policy.isAllowed(
                        createDefinition(),
                        "03"
                )
        );
    }

    /**
     * Mode 09 deve essere consentito.
     */
    @Test
    public void mode09IsAllowed() {

        ReadOnlyDiagnosticPolicy policy =
                new ReadOnlyDiagnosticPolicy();

        assertTrue(
                policy.isAllowed(
                        createDefinition(),
                        "0902"
                )
        );
    }

    /**
     * UDS 0x19 deve essere consentito.
     */
    @Test
    public void uds19IsAllowed() {

        ReadOnlyDiagnosticPolicy policy =
                new ReadOnlyDiagnosticPolicy();

        assertTrue(
                policy.isAllowed(
                        createDefinition(),
                        "1902"
                )
        );
    }

    /**
     * UDS 0x22 deve essere consentito.
     */
    @Test
    public void uds22IsAllowed() {

        ReadOnlyDiagnosticPolicy policy =
                new ReadOnlyDiagnosticPolicy();

        assertTrue(
                policy.isAllowed(
                        createDefinition(),
                        "22F190"
                )
        );
    }

    /**
     * UDS 0x23 deve essere consentito.
     */
    @Test
    public void uds23IsAllowed() {

        ReadOnlyDiagnosticPolicy policy =
                new ReadOnlyDiagnosticPolicy();

        assertTrue(
                policy.isAllowed(
                        createDefinition(),
                        "23000000"
                )
        );
    }

    /**
     * UDS 0x2A deve essere consentito.
     */
    @Test
    public void uds2aIsAllowed() {

        ReadOnlyDiagnosticPolicy policy =
                new ReadOnlyDiagnosticPolicy();

        assertTrue(
                policy.isAllowed(
                        createDefinition(),
                        "2A01"
                )
        );
    }

    /**
     * WriteDataByIdentifier 0x2E deve essere bloccato.
     */
    @Test
    public void uds2eIsBlocked() {

        ReadOnlyDiagnosticPolicy policy =
                new ReadOnlyDiagnosticPolicy();

        assertFalse(
                policy.isAllowed(
                        createDefinition(),
                        "2EF190"
                )
        );
    }

    /**
     * RoutineControl 0x31 deve essere bloccato.
     */
    @Test
    public void uds31IsBlocked() {

        ReadOnlyDiagnosticPolicy policy =
                new ReadOnlyDiagnosticPolicy();

        assertFalse(
                policy.isAllowed(
                        createDefinition(),
                        "310100"
                )
        );
    }

    /**
     * ECUReset 0x11 deve essere bloccato.
     */
    @Test
    public void uds11IsBlocked() {

        ReadOnlyDiagnosticPolicy policy =
                new ReadOnlyDiagnosticPolicy();

        assertFalse(
                policy.isAllowed(
                        createDefinition(),
                        "1101"
                )
        );
    }

    /**
     * SecurityAccess 0x27 deve essere bloccato.
     */
    @Test
    public void uds27IsBlocked() {

        ReadOnlyDiagnosticPolicy policy =
                new ReadOnlyDiagnosticPolicy();

        assertFalse(
                policy.isAllowed(
                        createDefinition(),
                        "2701"
                )
        );

    }

    /**
     * Servizio sconosciuto deve essere bloccato.
     */
    @Test
    public void unknownServiceIsBlocked() {

        ReadOnlyDiagnosticPolicy policy =
                new ReadOnlyDiagnosticPolicy();

        assertFalse(
                policy.isAllowed(
                        createDefinition(),
                        "AA1234"
                )
        );
    }
}