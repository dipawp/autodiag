package com.dipasoftware.autodiag.diagnostic;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Set;

public class PidSupportCheckerTest {

    @Test
    public void testBitmap0100() {

        PidSupportChecker checker =
                new PidSupportChecker();

        byte[] data = new byte[]{
                (byte) 0x98,
                (byte) 0x3B,
                (byte) 0x00,
                (byte) 0x11
        };

        Set<String> supported =
                checker.getSupportedPids(
                        0x00,
                        data
                );

        assertTrue(
                supported.contains("0101")
        );
    }
}