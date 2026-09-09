package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * ****************************************************************************
 *
 * Classe.....: DiagnosticPidExecutorTargetTest
 *
 * Tipo.......: Android Instrumented Test
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Verifica che DiagnosticPidExecutor mantenga il target diagnostico esplicito
 * durante una richiesta raw read-only.
 *
 * Il test simula due ECU differenti:
 *
 *      ECU A -> 7E0 / 7E8
 *      ECU B -> 7E1 / 7E9
 *
 * La request diagnostica è la stessa:
 *
 *      22 F1 90
 *
 * ma il target deve rimanere distinto.
 *
 * Questo impedisce che la diagnostica multi-ECU ricada accidentalmente
 * sul target legacy 7E0 / 7E8.
 *
 * ****************************************************************************
 */
@RunWith(AndroidJUnit4.class)
public class DiagnosticPidExecutorTargetTest {

    /**
     * Verifica che send() e receive() ricevano esattamente lo stesso target
     * specificato dal chiamante.
     */
    @Test
    public void executeRawPreservesExplicitTarget() throws Exception {

        RecordingTransport transport =
                new RecordingTransport();

        DiagnosticPidExecutor executor =
                new DiagnosticPidExecutor(
                        transport
                );

        DiagnosticTargetDefinition ecuA =
                new DiagnosticTargetDefinition(
                        "CAN",
                        "7E0",
                        "7E8",
                        "PHYSICAL",
                        11,
                        500
                );

        DiagnosticTargetDefinition ecuB =
                new DiagnosticTargetDefinition(
                        "CAN",
                        "7E1",
                        "7E9",
                        "PHYSICAL",
                        11,
                        500
                );

        DiagnosticPidExecutor.DiagnosticPidExecution resultA =
                executor.executeRaw(
                        ecuA,
                        "22 F1 90"
                );

        assertEquals(
                "22F190",
                resultA.getRequest()
        );

        assertEquals(
                "62 F1 90 ECU_A",
                resultA.getRawResponse()
        );

        assertSame(
                ecuA,
                transport.getLastSendTarget()
        );

        assertSame(
                ecuA,
                transport.getLastReceiveTarget()
        );

        DiagnosticPidExecutor.DiagnosticPidExecution resultB =
                executor.executeRaw(
                        ecuB,
                        "22 F1 90"
                );

        assertEquals(
                "22F190",
                resultB.getRequest()
        );

        assertEquals(
                "62 F1 90 ECU_B",
                resultB.getRawResponse()
        );

        assertSame(
                ecuB,
                transport.getLastSendTarget()
        );

        assertSame(
                ecuB,
                transport.getLastReceiveTarget()
        );
    }

    /**
     * Verifica esplicitamente che due ECU diverse non vengano confuse.
     */
    @Test
    public void differentTargetsRemainDistinct() throws Exception {

        RecordingTransport transport =
                new RecordingTransport();

        DiagnosticPidExecutor executor =
                new DiagnosticPidExecutor(
                        transport
                );

        DiagnosticTargetDefinition ecuA =
                new DiagnosticTargetDefinition(
                        "CAN",
                        "7E0",
                        "7E8",
                        "PHYSICAL",
                        11,
                        500
                );

        DiagnosticTargetDefinition ecuB =
                new DiagnosticTargetDefinition(
                        "CAN",
                        "7E1",
                        "7E9",
                        "PHYSICAL",
                        11,
                        500
                );

        executor.executeRaw(
                ecuA,
                "22F190"
        );

        DiagnosticTargetDefinition firstTarget =
                transport.getLastReceiveTarget();

        executor.executeRaw(
                ecuB,
                "22F190"
        );

        DiagnosticTargetDefinition secondTarget =
                transport.getLastReceiveTarget();

        assertSame(
                ecuA,
                firstTarget
        );

        assertSame(
                ecuB,
                secondTarget
        );

        assertEquals(
                "7E0",
                firstTarget.getRequestId()
        );

        assertEquals(
                "7E8",
                firstTarget.getResponseId()
        );

        assertEquals(
                "7E1",
                secondTarget.getRequestId()
        );

        assertEquals(
                "7E9",
                secondTarget.getResponseId()
        );
    }

    /**
     * Transport diagnostico fittizio.
     *
     * Non comunica con una Connection reale.
     *
     * La risposta dipende esclusivamente dal target ricevuto.
     */
    private static class RecordingTransport
            implements DiagnosticTransport {

        @NonNull
        private DiagnosticTargetDefinition lastSendTarget =
                createInitialTarget();

        @NonNull
        private DiagnosticTargetDefinition lastReceiveTarget =
                createInitialTarget();

        @NonNull
        private String lastRequest =
                "";

        @Override
        public void send(
                @NonNull DiagnosticTargetDefinition target,
                @NonNull String request)
                throws IOException {

            lastSendTarget =
                    target;

            lastRequest =
                    request;
        }

        @NonNull
        @Override
        public String receive(
                @NonNull DiagnosticTargetDefinition target)
                throws IOException {

            lastReceiveTarget =
                    target;

            if ("7E0".equals(
                    target.getRequestId()
            )) {
                return "62 F1 90 ECU_A";
            }

            if ("7E1".equals(
                    target.getRequestId()
            )) {
                return "62 F1 90 ECU_B";
            }

            return "";
        }

        @NonNull
        public DiagnosticTargetDefinition getLastSendTarget() {
            return lastSendTarget;
        }

        @NonNull
        public DiagnosticTargetDefinition getLastReceiveTarget() {
            return lastReceiveTarget;
        }

        @NonNull
        public String getLastRequest() {
            return lastRequest;
        }

        @NonNull
        private static DiagnosticTargetDefinition createInitialTarget() {

            return new DiagnosticTargetDefinition(
                    "CAN",
                    "7E0",
                    "7E8",
                    "PHYSICAL",
                    11,
                    500
            );
        }
    }
}