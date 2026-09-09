package com.dipasoftware.autodiag.diagnostic;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * ****************************************************************************
 *
 * Classe.....: DiagnosticDiscoveryMultiEcuTest
 *
 * Tipo.......: Android Instrumented Test
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Verifica la discovery multi-ECU senza VIN utilizzando due ECU candidate
 * con target diagnostici differenti.
 *
 * ECU A:
 *
 *      7E0 -> 7E8
 *
 * ECU B:
 *
 *      7E1 -> 7E9
 *
 * Entrambe utilizzano:
 *
 *      22 F1 90
 *
 * per la lettura del VIN ECU.
 *
 * Il test verifica inoltre che il fallimento della prima ECU non impedisca
 * la discovery della seconda.
 *
 * Il metodo di discovery viene invocato tramite reflection esclusivamente
 * per evitare di modificare il production code introducendo una injection
 * del catalogo solo a fini di test.
 *
 * ****************************************************************************
 */
@RunWith(AndroidJUnit4.class)
public class DiagnosticDiscoveryMultiEcuTest {

    @Test
    public void discoveryKeepsIndependentObservationsForDifferentTargets()
            throws Exception {

        TargetAwareTransport transport =
                new TargetAwareTransport();

        DiagnosticPidExecutor executor =
                new DiagnosticPidExecutor(
                        transport
                );

        EcuIdentifier ecuIdentifier =
                new EcuIdentifier(
                        executor
                );

        Context context =
                ApplicationProvider
                        .getApplicationContext();

        DiagnosticDiscoveryService service =
                new DiagnosticDiscoveryService(
                        context,
                        executor,
                        ecuIdentifier
                );

        EcuDefinition ecuA =
                createEcu(
                        "ECU_A",
                        "7E0",
                        "7E8",
                        "VIN_ECU_A"
                );

        EcuDefinition ecuB =
                createEcu(
                        "ECU_B",
                        "7E1",
                        "7E9",
                        "VIN_ECU_B"
                );

        VehicleIdentification vehicleIdentification =
                new VehicleIdentification(
                        ""
                );

        DiagnosticDiscoveryResult result =
                invokeDiscoveryWithoutVin(
                        service,
                        vehicleIdentification,
                        Arrays.asList(
                                ecuA,
                                ecuB
                        )
                );

        assertNotNull(
                result
        );

        assertEquals(
                2,
                result.getEcuObservations().size()
        );

        EcuDiscoveryObservation observationA =
                result.getEcuObservations().get(0);

        EcuDiscoveryObservation observationB =
                result.getEcuObservations().get(1);

        assertSame(
                ecuA,
                observationA.getCandidate()
        );

        assertSame(
                ecuB,
                observationB.getCandidate()
        );

        assertEquals(
                "VIN_ECU_A",
                observationA
                        .getIdentification()
                        .getVin()
        );

        assertEquals(
                "VIN_ECU_B",
                observationB
                        .getIdentification()
                        .getVin()
        );

        assertEquals(
                "7E0",
                observationA
                        .getCandidate()
                        .getTarget()
                        .getRequestId()
        );

        assertEquals(
                "7E8",
                observationA
                        .getCandidate()
                        .getTarget()
                        .getResponseId()
        );

        assertEquals(
                "7E1",
                observationB
                        .getCandidate()
                        .getTarget()
                        .getRequestId()
        );

        assertEquals(
                "7E9",
                observationB
                        .getCandidate()
                        .getTarget()
                        .getResponseId()
        );

        assertEquals(
                "VIN_ECU_A",
                result
                        .getEcuIdentification()
                        .getVin()
        );

        assertTrue(
                result.hasEcuMatchResult()
        );
    }

    @Test
    public void failedFirstEcuDoesNotStopDiscoveryOfSecondEcu()
            throws Exception {

        TargetAwareTransport transport =
                new TargetAwareTransport();

        transport.setFailRequestId(
                "7E0"
        );

        DiagnosticPidExecutor executor =
                new DiagnosticPidExecutor(
                        transport
                );

        EcuIdentifier ecuIdentifier =
                new EcuIdentifier(
                        executor
                );

        Context context =
                ApplicationProvider
                        .getApplicationContext();

        DiagnosticDiscoveryService service =
                new DiagnosticDiscoveryService(
                        context,
                        executor,
                        ecuIdentifier
                );

        EcuDefinition ecuA =
                createEcu(
                        "ECU_A",
                        "7E0",
                        "7E8",
                        "VIN_ECU_A"
                );

        EcuDefinition ecuB =
                createEcu(
                        "ECU_B",
                        "7E1",
                        "7E9",
                        "VIN_ECU_B"
                );

        DiagnosticDiscoveryResult result =
                invokeDiscoveryWithoutVin(
                        service,
                        new VehicleIdentification(""),
                        Arrays.asList(
                                ecuA,
                                ecuB
                        )
                );

        assertNotNull(
                result
        );

        assertEquals(
                1,
                result.getEcuObservations().size()
        );

        EcuDiscoveryObservation observation =
                result.getEcuObservations().get(0);

        assertSame(
                ecuB,
                observation.getCandidate()
        );

        assertEquals(
                "VIN_ECU_B",
                observation
                        .getIdentification()
                        .getVin()
        );

        assertEquals(
                "7E1",
                observation
                        .getCandidate()
                        .getTarget()
                        .getRequestId()
        );

        assertEquals(
                "7E9",
                observation
                        .getCandidate()
                        .getTarget()
                        .getResponseId()
        );

        assertEquals(
                "VIN_ECU_B",
                result
                        .getEcuIdentification()
                        .getVin()
        );
    }

    @NonNull
    private DiagnosticDiscoveryResult invokeDiscoveryWithoutVin(
            @NonNull DiagnosticDiscoveryService service,
            @NonNull VehicleIdentification vehicleIdentification,
            @NonNull List<EcuDefinition> candidates)
            throws Exception {

        Method method =
                DiagnosticDiscoveryService.class
                        .getDeclaredMethod(
                                "discoverEcuWithoutVin",
                                VehicleIdentification.class,
                                List.class
                        );

        method.setAccessible(
                true
        );

        try {

            Object value =
                    method.invoke(
                            service,
                            vehicleIdentification,
                            candidates
                    );

            return (DiagnosticDiscoveryResult) value;

        } catch (
                InvocationTargetException exception) {

            Throwable cause =
                    exception.getCause();

            if (cause instanceof Exception) {
                throw (Exception) cause;
            }

            if (cause instanceof Error) {
                throw (Error) cause;
            }

            throw exception;
        }
    }

    @NonNull
    private EcuDefinition createEcu(
            @NonNull String ecuName,
            @NonNull String requestId,
            @NonNull String responseId,
            @NonNull String expectedVin) {

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
                        0
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
     * Transport fittizio che associa la risposta al target ECU.
     */
    private static class TargetAwareTransport
            implements DiagnosticTransport {

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