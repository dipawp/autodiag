package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * ****************************************************************************
 *
 * Classe.....: DiagnosticRealConnectionCheckTest
 *
 * Tipo.......: Android Instrumented Test
 *
 * ****************************************************************************
 */
@RunWith(AndroidJUnit4.class)
public class DiagnosticRealConnectionCheckTest {

    /**
     * Verifica ATI + ATDP.
     */
    @Test
    public void checkReadsAdapterIdentification()
            throws Exception {

        FakeSender sender =
                new FakeSender();

        DiagnosticRealConnectionCheck check =
                new DiagnosticRealConnectionCheck(
                        sender
                );

        DiagnosticRealConnectionCheck.Result result =
                check.check();

        assertTrue(
                result.isValid()
        );

        assertTrue(
                result.looksLikeElm327()
        );

        assertTrue(
                result.reportsCanProtocol()
        );

        assertTrue(
                result.reportsIso15765()
        );

        assertTrue(
                result.isCanReady()
        );

        assertEquals(
                3,
                sender.commands.size()
        );

        assertEquals(
                "ATI",
                sender.commands.get(0)
        );

        assertEquals(
                "ATDP",
                sender.commands.get(1)
        );

        assertEquals(
                "ATDPN",
                sender.commands.get(2)
        );

        assertEquals(
                "6",
                result.getProtocolNumber()
        );
    }

    /**
     * Verifica che una risposta ATI vuota venga rifiutata.
     */
    @Test(expected = IOException.class)
    public void emptyIdentificationIsRejected()
            throws Exception {

        FakeSender sender =
                new FakeSender();

        sender.identificationResponse =
                "";

        DiagnosticRealConnectionCheck check =
                new DiagnosticRealConnectionCheck(
                        sender
                );

        check.check();
    }

    /**
     * Verifica che una risposta ATDP vuota venga rifiutata.
     */
    @Test(expected = IOException.class)
    public void emptyProtocolIsRejected()
            throws Exception {

        FakeSender sender =
                new FakeSender();

        sender.protocolResponse =
                "";

        DiagnosticRealConnectionCheck check =
                new DiagnosticRealConnectionCheck(
                        sender
                );

        check.check();
    }

    /**
     * Sender fittizio.
     */
    private static class FakeSender
            implements Elm327CommandExecutor.CommandSender {

        @NonNull
        private final List<String> commands =
                new ArrayList<>();

        @NonNull
        private String identificationResponse =
                "ELM327 v1.5\r>";

        @NonNull
        private String protocolResponse =
                "ISO 15765-4 (CAN 11/500)\r>";

        @NonNull
        private String protocolNumberResponse =
                "6\r>";

        @Override
        @NonNull
        public String sendCommand(
                @NonNull String command)
                throws IOException {

            commands.add(
                    command
            );

            if ("ATI".equalsIgnoreCase(
                    command
            )) {

                return identificationResponse;
            }

            if ("ATDP".equalsIgnoreCase(
                    command
            )) {

                return protocolResponse;
            }

            if ("ATDPN".equalsIgnoreCase(
                    command
            )) {

                return protocolNumberResponse;
            }

            return "";
        }
    }



    /**
     * Una risposta ATI non ELM327 non deve essere considerata
     * un'identificazione ELM327 valida.
     */
    @Test
    public void nonElmIdentificationIsDetected()
            throws Exception {

        FakeSender sender =
                new FakeSender();

        sender.identificationResponse =
                "OBD ADAPTER XYZ\r>";

        DiagnosticRealConnectionCheck check =
                new DiagnosticRealConnectionCheck(
                        sender
                );

        DiagnosticRealConnectionCheck.Result result =
                check.check();

        assertTrue(
                result.isValid()
        );

        assertTrue(
                !result.looksLikeElm327()
        );

        assertTrue(
                !result.isCanReady()
        );
    }

    /**
     * Un protocollo non CAN non deve risultare CAN-ready.
     */
    @Test
    public void nonCanProtocolIsDetected()
            throws Exception {

        FakeSender sender =
                new FakeSender();

        sender.protocolResponse =
                "ISO 9141-2\r>";

        DiagnosticRealConnectionCheck check =
                new DiagnosticRealConnectionCheck(
                        sender
                );

        DiagnosticRealConnectionCheck.Result result =
                check.check();

        assertTrue(
                result.isValid()
        );

        assertTrue(
                result.looksLikeElm327()
        );

        assertTrue(
                !result.reportsCanProtocol()
        );

        assertTrue(
                !result.reportsIso15765()
        );

        assertTrue(
                !result.isCanReady()
        );
    }
}