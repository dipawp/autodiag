package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * ****************************************************************************
 *
 * Classe.....: EcuIdentifierMultiTargetTest
 *
 * Tipo.......: Android Instrumented Test
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Verifica il percorso completo di identificazione ECU quando due ECU
 * utilizzano target diagnostici differenti.
 *
 * ECU A:
 *
 *      request  = 7E0
 *      response = 7E8
 *
 * ECU B:
 *
 *      request  = 7E1
 *      response = 7E9
 *
 * Entrambe utilizzano la stessa identificazione:
 *
 *      22 F1 90
 *
 * ma il target viene preso dalla rispettiva EcuDefinition.
 *
 * ****************************************************************************
 */
@RunWith(AndroidJUnit4.class)
public class EcuIdentifierMultiTargetTest {

    @Test
    public void differentEcuDefinitionsUseTheirOwnTargets() {

        TargetAwareTransport transport =
                new TargetAwareTransport();

        DiagnosticPidExecutor executor =
                new DiagnosticPidExecutor(
                        transport
                );

        EcuIdentifier identifier =
                new EcuIdentifier(
                        executor
                );

        EcuDefinition ecuA =
                createEcu(
                        "ECU_A",
                        "7E0",
                        "7E8"
                );

        EcuDefinition ecuB =
                createEcu(
                        "ECU_B",
                        "7E1",
                        "7E9"
                );

        EcuIdentification identificationA =
                identifier.identify(
                        ecuA
                );

        assertEquals(
                "VIN_ECU_A",
                identificationA.getVin()
        );

        assertEquals(
                "7E0",
                transport.getLastRequestId()
        );

        assertEquals(
                "7E8",
                transport.getLastResponseId()
        );

        EcuIdentification identificationB =
                identifier.identify(
                        ecuB
                );

        assertEquals(
                "VIN_ECU_B",
                identificationB.getVin()
        );

        assertEquals(
                "7E1",
                transport.getLastRequestId()
        );

        assertEquals(
                "7E9",
                transport.getLastResponseId()
        );
    }

    @Test
    public void failedFirstEcuDoesNotAffectSecondEcu() {

        TargetAwareTransport transport =
                new TargetAwareTransport();

        transport.setFailRequestId(
                "7E0"
        );

        DiagnosticPidExecutor executor =
                new DiagnosticPidExecutor(
                        transport
                );

        EcuIdentifier identifier =
                new EcuIdentifier(
                        executor
                );

        EcuDefinition ecuA =
                createEcu(
                        "ECU_A",
                        "7E0",
                        "7E8"
                );

        EcuDefinition ecuB =
                createEcu(
                        "ECU_B",
                        "7E1",
                        "7E9"
                );

        EcuIdentification identificationA =
                identifier.identify(
                        ecuA
                );

        assertEquals(
                "",
                identificationA.getVin()
        );

        EcuIdentification identificationB =
                identifier.identify(
                        ecuB
                );

        assertEquals(
                "VIN_ECU_B",
                identificationB.getVin()
        );

        assertEquals(
                "7E1",
                transport.getLastRequestId()
        );

        assertEquals(
                "7E9",
                transport.getLastResponseId()
        );
    }

    @NonNull
    private EcuDefinition createEcu(
            @NonNull String ecuName,
            @NonNull String requestId,
            @NonNull String responseId) {

        DiagnosticTargetDefinition target =
                new DiagnosticTargetDefinition(
                        "CAN",
                        requestId,
                        responseId,
                        "PHYSICAL",
                        11,
                        500
                );

        EcuIdentificationDefinition vinDefinition =
                new EcuIdentificationDefinition(
                        "22",
                        "F190",
                        "vin",
                        "STRING",
                        false,
                        0,
                        9
                );

        return new EcuDefinition(
                "TEST",
                "MULTI_ECU",
                "TEST_ENGINE",
                ecuName,
                "CAN",
                "",
                new EcuDefinitionIdentifier(),
                Collections.singletonList(
                        vinDefinition
                ),
                target,
                Collections.emptyList()
        );
    }

    /**
     * Transport fittizio sensibile al target.
     *
     * La risposta non viene scelta in base alla posizione della chiamata,
     * ma esclusivamente al CAN request ID.
     */
    private static class TargetAwareTransport
            implements DiagnosticTransport {

        @NonNull
        private String lastRequestId =
                "";

        @NonNull
        private String lastResponseId =
                "";

        @NonNull
        private String failRequestId =
                "";

        public void setFailRequestId(
                @NonNull String requestId) {

            failRequestId =
                    requestId;
        }

        @Override
        public void send(
                @NonNull DiagnosticTargetDefinition target,
                @NonNull String request)
                throws IOException {

            lastRequestId =
                    target.getRequestId();

            lastResponseId =
                    target.getResponseId();

            if (target.getRequestId().equals(
                    failRequestId
            )) {
                throw new IOException(
                        "ECU non raggiungibile: "
                                + target.getRequestId()
                );
            }
        }

        @NonNull
        @Override
        public String receive(
                @NonNull DiagnosticTargetDefinition target)
                throws IOException {

            lastRequestId =
                    target.getRequestId();

            lastResponseId =
                    target.getResponseId();

            if (target.getRequestId().equals(
                    failRequestId
            )) {
                throw new IOException(
                        "ECU non raggiungibile: "
                                + target.getRequestId()
                );
            }

            if ("7E0".equals(
                    target.getRequestId()
            )) {
                return createVinResponse(
                        "VIN_ECU_A"
                );
            }

            if ("7E1".equals(
                    target.getRequestId()
            )) {
                return createVinResponse(
                        "VIN_ECU_B"
                );
            }

            return "";
        }

        @NonNull
        public String getLastRequestId() {
            return lastRequestId;
        }

        @NonNull
        public String getLastResponseId() {
            return lastResponseId;
        }

        @NonNull
        private String createVinResponse(
                @NonNull String vin) {

            StringBuilder response =
                    new StringBuilder(
                            "62F190"
                    );

            byte[] bytes =
                    vin.getBytes(
                            java.nio.charset.StandardCharsets
                                    .UTF_8
                    );

            for (byte value : bytes) {
                response.append(
                        String.format(
                                java.util.Locale.US,
                                "%02X",
                                value & 0xFF
                        )
                );
            }

            return response.toString();
        }
    }
}