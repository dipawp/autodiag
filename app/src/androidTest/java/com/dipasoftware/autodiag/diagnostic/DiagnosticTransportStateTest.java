package com.dipasoftware.autodiag.diagnostic;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * ****************************************************************************
 *
 * Classe.....: DiagnosticTransportStateTest
 *
 * Tipo.......: Android Instrumented Test
 *
 * ****************************************************************************
 */
@RunWith(AndroidJUnit4.class)
public class DiagnosticTransportStateTest {

    /**
     * Verifica lo stato iniziale.
     */
    @Test
    public void initialStateIsIdle() {

        DiagnosticTransportState state =
                new DiagnosticTransportState();

        assertTrue(
                state.isIdle()
        );

        assertFalse(
                state.isWaitingResponse()
        );

        assertFalse(
                state.hasResponse()
        );

        assertNull(
                state.getTarget()
        );

        assertEquals(
                "",
                state.getRequest()
        );

        assertEquals(
                DiagnosticTransportState.Status.IDLE,
                state.getStatus()
        );
    }

    /**
     * Verifica apertura transazione.
     */
    @Test
    public void beginStartsWaitingState() {

        DiagnosticTransportState state =
                new DiagnosticTransportState();

        DiagnosticTargetDefinition target =
                new DiagnosticTargetDefinition(
                        "CAN",
                        "7E0",
                        "7E8",
                        "PHYSICAL",
                        11
                );

        state.begin(
                target,
                "22F190"
        );

        assertTrue(
                state.isWaitingResponse()
        );

        assertEquals(
                target,
                state.getTarget()
        );

        assertEquals(
                "22F190",
                state.getRequest()
        );
    }

    /**
     * Verifica la corrispondenza del target.
     */
    @Test
    public void matchingTargetIsAccepted() {

        DiagnosticTransportState state =
                new DiagnosticTransportState();

        DiagnosticTargetDefinition first =
                new DiagnosticTargetDefinition(
                        "CAN",
                        "7E0",
                        "7E8",
                        "PHYSICAL",
                        11
                );

        DiagnosticTargetDefinition second =
                new DiagnosticTargetDefinition(
                        "CAN",
                        "7E0",
                        "7E8",
                        "PHYSICAL",
                        11
                );

        state.begin(
                first,
                "22F190"
        );

        assertTrue(
                state.matchesTarget(
                        second
                )
        );
    }

    /**
     * Verifica che un target differente venga rifiutato.
     */
    @Test
    public void differentTargetIsRejected() {

        DiagnosticTransportState state =
                new DiagnosticTransportState();

        DiagnosticTargetDefinition first =
                new DiagnosticTargetDefinition(
                        "CAN",
                        "7E0",
                        "7E8",
                        "PHYSICAL",
                        11
                );

        DiagnosticTargetDefinition second =
                new DiagnosticTargetDefinition(
                        "CAN",
                        "7E1",
                        "7E9",
                        "PHYSICAL",
                        11
                );

        state.begin(
                first,
                "22F190"
        );

        assertFalse(
                state.matchesTarget(
                        second
                )
        );
    }

    /**
     * Verifica completamento della transazione.
     */
    @Test
    public void responseCanBeMarkedReceived() {

        DiagnosticTransportState state =
                new DiagnosticTransportState();

        DiagnosticTargetDefinition target =
                new DiagnosticTargetDefinition(
                        "CAN",
                        "7E0",
                        "7E8",
                        "PHYSICAL",
                        11
                );

        state.begin(
                target,
                "22F190"
        );

        state.markResponseReceived();

        assertTrue(
                state.hasResponse()
        );

        assertFalse(
                state.isWaitingResponse()
        );
    }

    /**
     * Verifica reset.
     */
    @Test
    public void resetReturnsToIdle() {

        DiagnosticTransportState state =
                new DiagnosticTransportState();

        DiagnosticTargetDefinition target =
                new DiagnosticTargetDefinition(
                        "CAN",
                        "7E0",
                        "7E8",
                        "PHYSICAL",
                        11
                );

        state.begin(
                target,
                "22F190"
        );

        state.reset();

        assertTrue(
                state.isIdle()
        );

        assertNull(
                state.getTarget()
        );

        assertEquals(
                "",
                state.getRequest()
        );
    }

    /**
     * Verifica il rifiuto di request vuota.
     */
    @Test(expected = IllegalArgumentException.class)
    public void emptyRequestIsRejected() {

        DiagnosticTransportState state =
                new DiagnosticTransportState();

        DiagnosticTargetDefinition target =
                new DiagnosticTargetDefinition(
                        "CAN",
                        "7E0",
                        "7E8",
                        "PHYSICAL",
                        11
                );

        state.begin(
                target,
                "   "
        );
    }
}