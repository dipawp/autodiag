package com.dipasoftware.autodiag;

import org.junit.Test;

import static org.junit.Assert.*;

import com.dipasoftware.autodiag.diagnostic.DiagnosticService;

/******************************************************************************
 *
 * Classe.....: DiagnosticServiceTest
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Test unitari della classe DiagnosticService.
 *
 * Verifica:
 * - conversione codice -> enum;
 * - conversione enum -> codice;
 * - gestione codici non validi.
 *
 ******************************************************************************/
public class DiagnosticServiceTest {

    /**
     * Verifica che il codice "22"
     * venga convertito correttamente.
     */
    @Test
    public void fromCode_ShouldReturnReadDataByIdentifier() {

        DiagnosticService service =
                DiagnosticService.fromCode("22");

        assertEquals(
                DiagnosticService.READ_DATA_BY_IDENTIFIER,
                service
        );
    }

    /**
     * Verifica il metodo getCode().
     */
    @Test
    public void getCode_ShouldReturn22() {

        assertEquals(
                "22",
                DiagnosticService.READ_DATA_BY_IDENTIFIER.getCode()
        );
    }

    /**
     * Verifica che un codice sconosciuto
     * generi una IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void fromCode_ShouldThrowException() {

        DiagnosticService.fromCode("99");

    }

}