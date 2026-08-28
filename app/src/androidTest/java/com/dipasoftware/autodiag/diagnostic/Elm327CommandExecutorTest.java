package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * ****************************************************************************
 *
 * Classe.....: Elm327CommandExecutorTest
 *
 * Tipo.......: Android Instrumented Test
 *
 * ****************************************************************************
 */
@RunWith(AndroidJUnit4.class)
public class Elm327CommandExecutorTest {

    /**
     * Verifica esecuzione comando AT.
     */
    @Test
    public void atCommandIsExecuted()
            throws Exception {

        FakeSender sender =
                new FakeSender(
                        "OK\r>"
                );

        Elm327CommandExecutor executor =
                new Elm327CommandExecutor(
                        sender
                );

        String response =
                executor.execute(
                        "ATSH 7E0"
                );

        assertEquals(
                "OK\r>",
                response
        );

        assertEquals(
                "ATSH 7E0",
                sender.getLastCommand()
        );
    }

    /**
     * Verifica comando con risposta OK.
     */
    @Test
    public void executeExpectOkWorks()
            throws Exception {

        FakeSender sender =
                new FakeSender(
                        "OK\r>"
                );

        Elm327CommandExecutor executor =
                new Elm327CommandExecutor(
                        sender
                );

        String response =
                executor.executeExpectOk(
                        "ATE0"
                );

        assertEquals(
                "OK\r>",
                response
        );
    }

    /**
     * Un comando diagnostico raw non deve essere accettato
     * dal CommandExecutor dell'adapter.
     */
    @Test(expected = IOException.class)
    public void diagnosticCommandIsRejected()
            throws Exception {

        FakeSender sender =
                new FakeSender(
                        "41 0C 1A F8\r>"
                );

        Elm327CommandExecutor executor =
                new Elm327CommandExecutor(
                        sender
                );

        executor.execute(
                "010C"
        );
    }

    /**
     * Risposta non OK.
     */
    @Test(expected = IOException.class)
    public void unexpectedResponseIsRejected()
            throws Exception {

        FakeSender sender =
                new FakeSender(
                        "?\r>"
                );

        Elm327CommandExecutor executor =
                new Elm327CommandExecutor(
                        sender
                );

        executor.executeExpectOk(
                "ATSH 7E0"
        );
    }

    /**
     * Comando vuoto.
     */
    @Test(expected = IOException.class)
    public void emptyCommandIsRejected()
            throws Exception {

        FakeSender sender =
                new FakeSender(
                        "OK\r>"
                );

        Elm327CommandExecutor executor =
                new Elm327CommandExecutor(
                        sender
                );

        executor.execute(
                "   "
        );
    }

    /**
     * Sender fittizio.
     */
    private static class FakeSender
            implements Elm327CommandExecutor.CommandSender {

        @NonNull
        private final String response;

        @NonNull
        private String lastCommand =
                "";

        FakeSender(
                @NonNull String response) {

            this.response =
                    response;
        }

        @Override
        @NonNull
        public String sendCommand(
                @NonNull String command)
                throws IOException {

            lastCommand =
                    command;

            return response;
        }

        @NonNull
        String getLastCommand() {

            return lastCommand;
        }
    }
}