package com.dipasoftware.autodiag.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dipasoftware.autodiag.connection.Connection;
import com.dipasoftware.autodiag.connection.ConnectionManager;
import com.dipasoftware.autodiag.databinding.FragmentHomeBinding;
import com.dipasoftware.autodiag.diagnostic.Elm327Manager;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import com.dipasoftware.autodiag.diagnostic.ObdResponseParserTest;
import com.dipasoftware.autodiag.diagnostic.PidFormulaEvaluatorTest;
import com.dipasoftware.autodiag.diagnostic.PidRepositoryTest;

/******************************************************************************
 *
 * Classe.....: HomeFragment
 *
 * Tipo.......: Fragment
 *
 * Package....: com.dipasoftware.autodiag.home
 *
 * Descrizione:
 *
 * Rappresenta la schermata Home dell'applicazione AutoDiag.
 *
 * Gestisce:
 *
 * - visualizzazione dello stato della connessione;
 * - test ELM327;
 * - test OBD-II;
 * - console diagnostica.
 *
 * La connessione Bluetooth fisica viene gestita
 * dal ConnectionManager.
 *
 ******************************************************************************/
public class HomeFragment extends Fragment {

    /**
     * ViewBinding.
     */
    private FragmentHomeBinding binding;

    /**
     * Executor per le comunicazioni Bluetooth/OBD.
     */
    private final ExecutorService obdExecutor =
            Executors.newSingleThreadExecutor();

    /**
     * Listener dello stato della connessione.
     *
     * Riceve automaticamente le variazioni
     * dello stato dal ConnectionManager.
     */
    private final ConnectionManager.ConnectionStateListener
            connectionStateListener =
            connected -> {

                /*
                 * Il listener può essere chiamato
                 * da un thread diverso dal thread UI.
                 *
                 * Per questo motivo riportiamo
                 * l'aggiornamento sul thread principale.
                 */
                if (!isAdded()) {
                    return;
                }

                requireActivity().runOnUiThread(() -> {

                    /*
                     * Il Fragment potrebbe essere stato
                     * rimosso nel frattempo.
                     */
                    if (!isAdded() ||
                            binding == null) {

                        return;
                    }

                    /*
                     * Aggiorna lo stato visualizzato.
                     */
                    if (connected) {

                        binding.txtConnectionStatus.setText(
                                "● Connesso"
                        );

                    } else {

                        binding.txtConnectionStatus.setText(
                                "● Disconnesso"
                        );
                    }

                    Log.d(
                            "AutoDiag",
                            "Stato connessione: "
                                    + connected
                    );
                });
            };

    /**
     * Costruttore.
     */
    public HomeFragment() {
    }

    /**
     * Crea la View.
     */
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        binding =
                FragmentHomeBinding.inflate(
                        inflater,
                        container,
                        false
                );

        configureConnectButton();

        configureTestObdButton();

        /*
         * Aggiorna immediatamente lo stato attuale.
         *
         * Questo copre il caso in cui la connessione
         * sia già stata stabilita prima della registrazione
         * del listener.
         */
        updateConnectionStatus();



        /*=============TEMPORANEO===============*/
        runPidDatabaseTest();
        runPidFormulaEvaluatorTestTest();
        runObdResponseParserTest();
        /*======================================*/




        return binding.getRoot();
    }

    /**
     * Registra il listener quando la Home diventa visibile.
     *
     * ConnectionManager richiama immediatamente il listener
     * con lo stato corrente della connessione.
     */
    @Override
    public void onStart() {

        super.onStart();

        ConnectionManager.getInstance()
                .addConnectionStateListener(
                        connectionStateListener
                );
    }

    /**
     * Rimuove il listener quando la Home non è più visibile.
     *
     * In questo modo evitiamo memory leak e callback
     * verso un Fragment non più attivo.
     */
    @Override
    public void onStop() {

        ConnectionManager.getInstance()
                .removeConnectionStateListener(
                        connectionStateListener
                );

        super.onStop();
    }

    /**
     * Aggiorna lo stato della connessione quando
     * la Home torna visibile.
     *
     * Rimane come controllo di sicurezza.
     */
    @Override
    public void onResume() {

        super.onResume();

        updateConnectionStatus();
    }

    /**
     * Aggiorna il TextView dello stato connessione.
     *
     * Legge lo stato direttamente dal ConnectionManager,
     * che rappresenta la connessione globale dell'applicazione.
     */
    private void updateConnectionStatus() {

        if (binding == null) {
            return;
        }

        ConnectionManager connectionManager =
                ConnectionManager.getInstance();

        if (connectionManager.isConnected()) {

            binding.txtConnectionStatus.setText(
                    "● Connesso"
            );

        } else {

            binding.txtConnectionStatus.setText(
                    "● Disconnesso"
            );
        }
    }

    /**
     * Configura il pulsante CONNETTI.
     */
    private void configureConnectButton() {

        binding.btnConnect.setOnClickListener(
                view -> {

                    /*
                     * La connessione Bluetooth viene
                     * attualmente gestita dal
                     * BluetoothSettingsFragment.
                     *
                     * L'autoconnessione viene gestita
                     * all'avvio dell'applicazione.
                     */

                    updateConnectionStatus();
                }
        );
    }

    /**
     * Configura il pulsante TEST OBD.
     */
    private void configureTestObdButton() {

        binding.btnTestObd.setOnClickListener(
                view -> runElm327Test()
        );
    }

    /**
     * Esegue il test ELM327.
     */
    private void runElm327Test() {

        ConnectionManager connectionManager =
                ConnectionManager.getInstance();

        Connection connection =
                connectionManager.getConnection();

        /*
         * Nessuna connessione registrata.
         */
        if (connection == null) {

            updateConnectionStatus();

            /*binding.txtObdTestResult.setVisibility(
                    View.VISIBLE
            );

            binding.txtObdTestResult.setText(
                    "TEST ELM327\n\n"
                            + "ERRORE:\n"
                            + "Nessuna connessione ELM327."
            );*/



            binding.txtDiagnosticConsole.setVisibility(
                    View.VISIBLE
            );

            binding.txtDiagnosticConsole.setText(
                    "TEST ELM327\n\n"
                            + "ERRORE:\n"
                            + "Nessuna connessione ELM327."
            );




            return;
        }

        /*
         * La connessione esiste ma non è più attiva.
         */
        if (!connection.isConnected()) {

            updateConnectionStatus();

            /*binding.txtObdTestResult.setVisibility(
                    View.VISIBLE
            );

            binding.txtObdTestResult.setText(
                    "TEST ELM327\n\n"
                            + "ERRORE:\n"
                            + "La connessione non è attiva."
            );*/










            binding.txtDiagnosticConsole.setVisibility(
                    View.VISIBLE
            );

            binding.txtDiagnosticConsole.setText(
                    "TEST ELM327\n\n"
                            + "ERRORE:\n"
                            + "La connessione non è attiva."
            );

            return;
        }

        /*
         * La connessione è attiva.
         */
        updateConnectionStatus();

        /*
         * Visualizza immediatamente lo stato.
         */
        /*binding.txtObdTestResult.setVisibility(
                View.VISIBLE
        );

        binding.txtObdTestResult.setText(
                "TEST ELM327\n\n"
                        + "Connessione: OK\n"
                        + "Invio comandi AT..."
        );*/



        binding.txtDiagnosticConsole.setVisibility(
                View.VISIBLE
        );

        binding.txtDiagnosticConsole.setText(
                "TEST ELM327\n\n"
                        + "Connessione: OK\n"
                        + "Invio comandi AT..."
        );






        /*
         * Disabilita temporaneamente il pulsante
         * per evitare test simultanei.
         */
        binding.btnTestObd.setEnabled(false);

        obdExecutor.execute(() -> {

            try {

                Elm327Manager elm327Manager =
                        new Elm327Manager(
                                connection,requireContext()
                        );

                /*
                 * Test ELM327.
                 */
                String result =
                        elm327Manager.runConnectionTest();

                showTestResult(
                        result
                );

                /*
                 * Test OBD-II.
                 */
                String resultCommand =
                        elm327Manager.runObdTest();

                showTestResult(
                        resultCommand
                );


                String liveDataResult =
                        elm327Manager.runLiveDataTest();

                showTestResult(liveDataResult);







            } catch (IOException exception) {

                showTestResult(
                        "=== TEST ELM327 ===\n\n"
                                + "ERRORE DI COMUNICAZIONE\n\n"
                                + exception.getMessage()
                );

            } catch (RuntimeException exception) {

                showTestResult(
                        "=== TEST ELM327 ===\n\n"
                                + "ERRORE\n\n"
                                + exception.getMessage()
                );
            }
        });
    }

    /**
     * Visualizza il risultato del test sul thread UI.
     *
     * @param result risultato.
     */
    private void showTestResult(
            @NonNull String result) {

        if (!isAdded()) {
            return;
        }

        requireActivity().runOnUiThread(() -> {

            if (!isAdded() ||
                    binding == null) {

                return;
            }

            /*binding.txtObdTestResult.setVisibility(
                    View.VISIBLE
            );

            binding.txtObdTestResult.setText(
                    result
            );*/

            binding.txtDiagnosticConsole.setVisibility(
                    View.VISIBLE
            );

            binding.txtDiagnosticConsole.setText(
                    result
            );

            binding.btnTestObd.setEnabled(
                    true
            );

            /*
             * Aggiorna nuovamente lo stato della
             * connessione dopo il test.
             */
            updateConnectionStatus();

            Log.d(
                    "AutoDiag",
                    "Risultato: " + result
            );
        });
    }

    /**
     * Distrugge la View.
     */
    @Override
    public void onDestroyView() {

        super.onDestroyView();

        binding = null;
    }

    /**
     * Chiude l'executor.
     *
     * NON chiudiamo la BluetoothConnection qui.
     *
     * La connessione è gestita dal ConnectionManager
     * e deve rimanere disponibile anche quando
     * questo Fragment viene distrutto.
     */
    @Override
    public void onDestroy() {

        super.onDestroy();

        obdExecutor.shutdownNow();
    }






    /**
     * Esegue il test del database PID.
     */
    private void runPidDatabaseTest() {

        PidRepositoryTest test =
                new PidRepositoryTest(
                        requireContext()
                );

        String result =
                test.runTest();

        binding.txtDiagnosticConsole.setVisibility(
                View.VISIBLE
        );

        binding.txtDiagnosticConsole.setText(
                result
        );

        Log.d(
                "PidTest",
                result
        );
    }



    /**
     * Esegue il test del database PID Formula.
     */
    private void runPidFormulaEvaluatorTestTest() {

        PidFormulaEvaluatorTest test =
                new PidFormulaEvaluatorTest(
                        requireContext()
                );

        String result =
                test.runTest();

        binding.txtDiagnosticConsole.setVisibility(
                View.VISIBLE
        );

        binding.txtDiagnosticConsole.setText(
                result
        );

        Log.d(
                "FormulaTest",
                result
        );
    }


    /**
     * Esegue il test del database PID Formula.
     */
    private void runObdResponseParserTest() {

        ObdResponseParserTest test =
                new ObdResponseParserTest();

        String result =
                test.runTest();

        binding.txtDiagnosticConsole.setVisibility(
                View.VISIBLE
        );

        binding.txtDiagnosticConsole.setText(
                result
        );

        Log.d(
                "ResponseTest",
                result
        );
    }
}
