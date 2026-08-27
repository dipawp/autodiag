package com.dipasoftware.autodiag.diagnostic;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * ****************************************************************************
 *
 * Classe.....: ObdVehicleInformationParserTest
 *
 * Tipo.......: Android Instrumented Test
 *
 * ****************************************************************************
 */
@RunWith(AndroidJUnit4.class)
public class ObdVehicleInformationParserTest {

    /**
     * Verifica il parsing del VIN.
     */
    @Test
    public void parseVinResponse()
            throws Exception {

        ObdVehicleInformationParser parser =
                new ObdVehicleInformationParser();

        ObdVehicleInformationParser
                .VehicleInformationResponse result =
                parser.parseVin(
                        "49 02 01 54 45 53 54 56 49 4E 31 32 33 34 35 36 37 38 39 30\r>",
                        "0902"
                );

        assertEquals(
                "TESTVIN1234567890",
                result.getVin()
        );

        assertEquals(
                0x49,
                result.getService()
        );

        assertEquals(
                0x02,
                result.getPid()
        );
    }

    /**
     * Verifica il rifiuto di un service errato.
     */
    @Test(expected = IllegalArgumentException.class)
    public void invalidVinResponseIsRejected()
            throws Exception {

        ObdVehicleInformationParser parser =
                new ObdVehicleInformationParser();

        parser.parseVin(
                "41 0C 1A F8",
                "0902"
        );
    }

    /**
     * Verifica il rifiuto di una request non VIN.
     */
    @Test(expected = IllegalArgumentException.class)
    public void invalidVinRequestIsRejected()
            throws Exception {

        ObdVehicleInformationParser parser =
                new ObdVehicleInformationParser();

        parser.parseVin(
                "49 02 01 54 45 53 54 56 49 4E 31 32 33 34 35 36 37 38 39",
                "0904"
        );
    }
}