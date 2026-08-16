package com.dipasoftware.autodiag;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import com.dipasoftware.autodiag.core.parameters.ParameterCategory;

/******************************************************************************
 *
 * Classe.....: ParameterCategoryTest
 *
 * Descrizione:
 * Test della enum ParameterCategory.
 *
 ******************************************************************************/
public class ParameterCategoryTest {

    /**
     * Verifica che il valore DPF sia presente
     * e abbia il nome corretto.
     */
    @Test
    public void dpfCategory_ShouldHaveCorrectName() {

        assertEquals(
                "DPF",
                ParameterCategory.DPF.name()
        );
    }

}