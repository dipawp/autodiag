package com.dipasoftware.autodiag.dashboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.dipasoftware.autodiag.R;
import com.dipasoftware.autodiag.bluetooth.BluetoothDeviceInfo;
import com.dipasoftware.autodiag.bluetooth.BluetoothManager;
import com.dipasoftware.autodiag.bluetooth.BluetoothPermissionManager;
import com.dipasoftware.autodiag.connection.BluetoothConnection;
import com.dipasoftware.autodiag.connection.ConnectionManager;
import com.dipasoftware.autodiag.databinding.ActivityMainBinding;
import com.dipasoftware.autodiag.diagnostic.DiagnosticRealConnectionCheck;
import com.dipasoftware.autodiag.diagnostic.Elm327Manager;
import com.dipasoftware.autodiag.settings.BluetoothSettingsFragment;
import com.dipasoftware.autodiag.settings.SettingsFragment;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/******************************************************************************
 *
 * Classe.....: MainActivity
 *
 * Tipo.......: Activity
 *
 * Package....: com.dipasoftware.autodiag.dashboard
 *
 * Descrizione:
 *
 * Punto di ingresso dell'applicazione.
 *
 * Gestisce:
 *
 * - Dashboard;
 * - BottomNavigation;
 * - Settings;
 * - permessi Bluetooth;
 * - riconnessione automatica dell'ELM327.
 *
 ******************************************************************************/
public class MainActivity extends AppCompatActivity {

    /**
     * ViewBinding.
     */
    private ActivityMainBinding binding;

    /**
     * Adapter dashboard.
     */
    private DashboardPagerAdapter pagerAdapter;

    /**
     * Gestore permessi Bluetooth.
     */
    private BluetoothPermissionManager bluetoothPermissionManager;

    /**
     * BluetoothManager.
     */
    private BluetoothManager bluetoothManager;

    /**
     * ConnectionManager.
     */
    private ConnectionManager connectionManager;

    /**
     * Executor per la connessione automatica.
     */
    private final ExecutorService connectionExecutor =
            Executors.newSingleThreadExecutor();

    /**
     * Indica se è già stata avviata
     * la procedura di connessione automatica.
     */
    private boolean autoConnectionStarted;

    /**
     * Crea l'Activity.
     *
     * @param savedInstanceState stato precedente.
     */
    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        binding =
                ActivityMainBinding.inflate(
                        getLayoutInflater()
                );

        setContentView(
                binding.getRoot()
        );

        bluetoothPermissionManager =
                new BluetoothPermissionManager();

        bluetoothManager =
                new BluetoothManager(
                        this
                );

        connectionManager =
                ConnectionManager.getInstance();

        pagerAdapter =
                new DashboardPagerAdapter(this);

        binding.dashboardPager.setAdapter(
                pagerAdapter
        );

        binding.dashboardPager.setUserInputEnabled(
                false
        );

        configureBottomNavigation();

        configureToolbar();

        binding.bottomNavigation.setSelectedItemId(
                R.id.navigation_home
        );

        requestBluetoothPermissions();
    }

    /**
     * Configura la toolbar.
     */
    private void configureToolbar() {

        binding.mainToolbar.setOnMenuItemClickListener(
                item -> {

                    if (item.getItemId()
                            == R.id.action_settings) {

                        openSettings();

                        return true;
                    }

                    return false;
                }
        );
    }

    /**
     * Apre Settings.
     */
    private void openSettings() {

        getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        android.R.id.content,
                        new SettingsFragment()
                )
                .addToBackStack(null)
                .commit();
    }

    /**
     * Configura la navigazione.
     */
    private void configureBottomNavigation() {

        binding.bottomNavigation.setOnItemSelectedListener(
                item -> {

                    int itemId =
                            item.getItemId();

                    if (itemId ==
                            R.id.navigation_home) {

                        binding.dashboardPager.setCurrentItem(
                                0,
                                false
                        );

                        return true;
                    }

                    if (itemId ==
                            R.id.navigation_live) {

                        binding.dashboardPager.setCurrentItem(
                                1,
                                false
                        );

                        return true;
                    }

                    if (itemId ==
                            R.id.navigation_dpf) {

                        binding.dashboardPager.setCurrentItem(
                                2,
                                false
                        );

                        return true;
                    }

                    if (itemId ==
                            R.id.navigation_dtc) {

                        binding.dashboardPager.setCurrentItem(
                                3,
                                false
                        );

                        return true;
                    }

                    if (itemId ==
                            R.id.navigation_history) {

                        binding.dashboardPager.setCurrentItem(
                                4,
                                false
                        );

                        return true;
                    }

                    return false;
                }
        );

        binding.dashboardPager.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback() {

                    @Override
                    public void onPageSelected(
                            int position) {

                        super.onPageSelected(
                                position
                        );

                        switch (position) {

                            case 0:

                                binding.bottomNavigation
                                        .setSelectedItemId(
                                                R.id.navigation_home
                                        );

                                break;

                            case 1:

                                binding.bottomNavigation
                                        .setSelectedItemId(
                                                R.id.navigation_live
                                        );

                                break;

                            case 2:

                                binding.bottomNavigation
                                        .setSelectedItemId(
                                                R.id.navigation_dpf
                                        );

                                break;

                            case 3:

                                binding.bottomNavigation
                                        .setSelectedItemId(
                                                R.id.navigation_dtc
                                        );

                                break;

                            case 4:

                                binding.bottomNavigation
                                        .setSelectedItemId(
                                                R.id.navigation_history
                                        );

                                break;
                        }
                    }
                }
        );
    }

    /**
     * Controlla i permessi Bluetooth.
     */
    private void requestBluetoothPermissions() {

        if (bluetoothPermissionManager
                .hasRequiredPermissions(this)) {

            /*
             * Permessi già disponibili.
             */
            startAutomaticConnection();

            return;
        }

        bluetoothPermissionManager
                .requestPermissions(this);
    }

    /**
     * Riceve il risultato dei permessi.
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        bluetoothPermissionManager
                .handlePermissionResult(
                        requestCode,
                        grantResults
                );

        /*
         * Dopo la gestione della richiesta,
         * tentiamo la connessione automatica.
         */
        if (bluetoothPermissionManager
                .hasRequiredPermissions(this)) {

            startAutomaticConnection();
        }
    }

    /**
     * Avvia la procedura di connessione automatica.
     */
    private void startAutomaticConnection() {

        if (autoConnectionStarted) {
            return;
        }

        autoConnectionStarted = true;

        if (connectionManager.isConnected()) {
            return;
        }

        String savedAddress =
                getSavedDeviceAddress();

        /*
         * Nessun dispositivo salvato.
         */
        if (savedAddress == null ||
                savedAddress.trim().isEmpty()) {

            showBluetoothSelectionRequired();

            return;
        }

        connectionExecutor.execute(() -> {

            try {

                List<BluetoothDeviceInfo> devices =
                        bluetoothManager.getPairedDevices();

                BluetoothDeviceInfo savedDevice =
                        findDeviceByAddress(
                                devices,
                                savedAddress
                        );

                /*
                 * Il dispositivo non è più associato.
                 */
                if (savedDevice == null) {

                    runOnUiThread(
                            this::showBluetoothSelectionRequired
                    );

                    return;
                }

                /*
                 * Crea la connessione.
                 */
                BluetoothConnection connection =
                        bluetoothManager.createConnection(
                                savedDevice
                        );

                connection.connect();

                /*
                 * Registra la connessione globale.
                 */
                connectionManager.setConnection(
                        connection
                );

                runElm327ConnectionCheck(
                        connection
                );

                runOnUiThread(() -> {

                    Toast.makeText(
                            this,
                            "Connesso automaticamente a "
                                    + savedDevice.getDisplayName(),
                            Toast.LENGTH_SHORT
                    ).show();
                });

            } catch (IOException exception) {

                runOnUiThread(() -> {

                    Toast.makeText(
                            this,
                            "Connessione automatica fallita",
                            Toast.LENGTH_SHORT
                    ).show();

                    showBluetoothSelectionRequired();
                });

            } catch (SecurityException exception) {

                runOnUiThread(
                        this::showBluetoothSelectionRequired
                );

            } catch (RuntimeException exception) {

                runOnUiThread(
                        this::showBluetoothSelectionRequired
                );
            }
        });
    }

    /**
     * Cerca un dispositivo tramite indirizzo MAC.
     *
     * @param devices lista dispositivi.
     * @param address indirizzo cercato.
     *
     * @return dispositivo oppure null.
     */
    @Nullable
    private BluetoothDeviceInfo findDeviceByAddress(
            @NonNull List<BluetoothDeviceInfo> devices,
            @NonNull String address) {

        for (BluetoothDeviceInfo device : devices) {

            if (address.equalsIgnoreCase(
                    device.getAddress()
            )) {

                return device;
            }
        }

        return null;
    }

    /**
     * Restituisce il MAC salvato.
     *
     * @return MAC oppure null.
     */
    @Nullable
    private String getSavedDeviceAddress() {

        SharedPreferences preferences =
                getSharedPreferences(
                        BluetoothSettingsFragment.PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        return preferences.getString(
                BluetoothSettingsFragment.PREF_LAST_DEVICE_ADDRESS,
                null
        );
    }

    /**
     * Informa l'utente che deve selezionare
     * un dispositivo Bluetooth.
     */
    private void showBluetoothSelectionRequired() {

        Toast.makeText(
                this,
                "Nessun ELM327 configurato. Seleziona un dispositivo Bluetooth.",
                Toast.LENGTH_LONG
        ).show();

        openSettings();
    }

    /**
     * Restituisce il binding.
     *
     * @return binding.
     */
    public ActivityMainBinding getBinding() {

        return binding;
    }

    /**
     * Restituisce il PagerAdapter.
     *
     * @return adapter.
     */
    public DashboardPagerAdapter getPagerAdapter() {

        return pagerAdapter;
    }

    /**
     * Distrugge l'Activity.
     */
    @Override
    protected void onDestroy() {

        connectionExecutor.shutdownNow();

        /*
         * NON chiudiamo qui ConnectionManager.
         *
         * La connessione deve essere gestita dal
         * ConnectionManager e dalle successive schermate.
         */

        super.onDestroy();

        binding = null;
    }


    /**
     * Esegue il controllo non distruttivo dell'ELM327 dopo
     * una connessione Bluetooth riuscita.
     *
     * Vengono utilizzati esclusivamente:
     *
     * ATI
     * ATDP
     *
     * Non viene eseguita alcuna richiesta diagnostica alla ECU.
     */
    private void runElm327ConnectionCheck(
            @NonNull BluetoothConnection connection) {

        connectionExecutor.execute(() -> {

            try {

                Elm327Manager manager =
                        new Elm327Manager(
                                connection
                        );

                DiagnosticRealConnectionCheck.Result result =
                        manager.checkRealConnection();

                String identification =
                        result.getIdentificationResponse();

                String protocol =
                        result.getProtocolResponse();

                runOnUiThread(() -> {
                    String message;
                    if (result.isCanReady()) {
                        message = "ELM327 pronto\n" + identification + "\n" + protocol;
                    } else {
                        message = "ELM327 collegato\n" + identification + "\n" + protocol;
                    }
                    Toast.makeText(this,message,Toast.LENGTH_LONG).show();
                });

            } catch (
                    IOException exception) {

                runOnUiThread(() ->
                        Toast.makeText(
                                this,
                                "Check ELM327 fallito: "
                                        + exception.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
            } catch (
                    RuntimeException exception) {

                runOnUiThread(() ->
                        Toast.makeText(
                                this,
                                "Errore durante il check ELM327",
                                Toast.LENGTH_LONG
                        ).show()
                );
            }
        });
    }
}
