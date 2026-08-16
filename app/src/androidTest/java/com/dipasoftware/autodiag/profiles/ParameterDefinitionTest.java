package com.dipasoftware.autodiag.profiles;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import com.dipasoftware.autodiag.core.models.ParameterDefinition;

/**
 * ****************************************************************************
 * Test della classe ParameterDefinition.
 *****************************************************************************/
public class ParameterDefinitionTest {

    /**
     * Verifica che due parametri con lo stesso ID
     * siano considerati uguali.
     */
    @Test
    public void testEqualsById() {

        ParameterDefinition p1 = new ParameterDefinition();
        p1.setId("ENGINE_RPM");

        ParameterDefinition p2 = new ParameterDefinition();
        p2.setId("ENGINE_RPM");

        assertEquals(p1, p2);

    }

}