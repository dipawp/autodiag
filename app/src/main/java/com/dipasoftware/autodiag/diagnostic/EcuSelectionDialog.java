package com.dipasoftware.autodiag.diagnostic;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * ****************************************************************************
 *
 * Classe.....: EcuSelectionDialog
 *
 * Tipo.......: Dialog
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Dialog dedicato alla presentazione del risultato della discovery ECU
 * e alla selezione/conferma della centralina diagnostica.
 *
 * La classe NON è un Activity e NON è un Fragment.
 *
 * Gestisce esclusivamente la presentazione UI del risultato prodotto da:
 *
 *     DiagnosticDiscoveryCoordinator
 *              |
 *              v
 *       EcuSelectionPolicy
 *
 * La logica di discovery e di selezione automatica rimane fuori dal Dialog.
 *
 * ****************************************************************************
 */
public class EcuSelectionDialog extends Dialog {

    /**
     * Listener del risultato della selezione.
     */
    public interface Listener {

        /**
         * Chiamato quando l'utente conferma/seleziona una ECU.
         *
         * @param observation observation selezionata.
         */
        void onEcuSelected(
                @NonNull EcuDiscoveryObservation observation
        );

        /**
         * Chiamato quando l'utente annulla o chiude
         * la selezione ECU.
         */
        void onSelectionCancelled();
    }

    /**
     * Risultato della discovery.
     */
    @NonNull
    private final DiagnosticDiscoveryCoordinator.Result coordinatorResult;

    /**
     * Listener UI.
     */
    @NonNull
    private final Listener listener;

    /**
     * Gruppo radio utilizzato per la selezione
     * quando sono presenti più ECU.
     */
    @Nullable
    private RadioGroup radioGroup;

    /**
     * Costruttore.
     *
     * @param context context Activity.
     * @param coordinatorResult risultato del coordinator.
     * @param listener listener della selezione.
     */
    public EcuSelectionDialog(
            @NonNull Context context,
            @NonNull DiagnosticDiscoveryCoordinator.Result coordinatorResult,
            @NonNull Listener listener) {

        super(context);

        this.coordinatorResult =
                coordinatorResult;

        this.listener =
                listener;
    }

    /**
     * Crea il contenuto del Dialog.
     *
     * @param savedInstanceState stato precedente.
     */
    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        buildDialog();
    }

    /**
     * Costruisce il contenuto del Dialog.
     */
    private void buildDialog() {

        LinearLayout root =
                new LinearLayout(
                        getContext()
                );

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        int padding =
                dp(24);

        root.setPadding(
                padding,
                padding,
                padding,
                padding
        );

        TextView title =
                new TextView(
                        getContext()
                );

        title.setText(
                getTitleText()
        );

        title.setTextSize(
                20
        );

        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        root.addView(
                title,
                createLayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        TextView message =
                new TextView(
                        getContext()
                );

        message.setText(
                getMessageText()
        );

        message.setTextSize(
                16
        );

        message.setPadding(
                0,
                dp(12),
                0,
                dp(12)
        );

        root.addView(
                message,
                createLayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        EcuSelectionPolicy.Status status =
                coordinatorResult
                        .getSelectionResult()
                        .getStatus();

        if (status ==
                EcuSelectionPolicy.Status.MULTIPLE_ECUS) {

            addMultipleEcuSelection(
                    root
            );
        }

        LinearLayout buttons =
                new LinearLayout(
                        getContext()
                );

        buttons.setOrientation(
                LinearLayout.HORIZONTAL
        );

        buttons.setGravity(
                Gravity.END
        );

        buttons.setPadding(
                0,
                dp(12),
                0,
                0
        );

        Button cancelButton =
                new Button(
                        getContext()
                );

        cancelButton.setText(
                "Annulla"
        );

        cancelButton.setOnClickListener(
                view -> {
                    listener.onSelectionCancelled();
                    dismiss();
                }
        );

        buttons.addView(
                cancelButton,
                createButtonLayoutParams()
        );

        if (status ==
                EcuSelectionPolicy.Status.NONE) {

            Button closeButton =
                    new Button(
                            getContext()
                    );

            closeButton.setText(
                    "Chiudi"
            );

            closeButton.setOnClickListener(
                    view -> {
                        listener.onSelectionCancelled();
                        dismiss();
                    }
            );

            buttons.addView(
                    closeButton,
                    createButtonLayoutParams()
            );

        } else {

            Button confirmButton =
                    new Button(
                            getContext()
                    );

            confirmButton.setText(
                    getConfirmButtonText()
            );

            confirmButton.setOnClickListener(
                    view -> confirmSelection()
            );

            buttons.addView(
                    confirmButton,
                    createButtonLayoutParams()
            );
        }

        root.addView(
                buttons,
                createLayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        setContentView(
                root
        );

        configureWindow();
    }

    /**
     * Aggiunge la lista delle ECU quando sono presenti
     * più osservazioni.
     *
     * @param root contenitore principale.
     */
    private void addMultipleEcuSelection(
            @NonNull LinearLayout root) {

        ScrollView scrollView =
                new ScrollView(
                        getContext()
                );

        radioGroup =
                new RadioGroup(
                        getContext()
                );

        radioGroup.setOrientation(
                RadioGroup.VERTICAL
        );

        List<EcuDiscoveryObservation> observations =
                coordinatorResult
                        .getDiscoveryResult()
                        .getEcuObservations();

        for (
                int index = 0;
                index < observations.size();
                index++
        ) {

            EcuDiscoveryObservation observation =
                    observations.get(index);

            RadioButton radioButton =
                    new RadioButton(
                            getContext()
                    );

            radioButton.setText(
                    buildObservationText(
                            observation
                    )
            );

            radioButton.setTextSize(
                    16
            );

            radioButton.setTag(
                    observation
            );

            radioGroup.addView(
                    radioButton,
                    createLayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    )
            );

            if (index == 0) {
                radioButton.setChecked(
                        true
                );
            }
        }

        scrollView.addView(
                radioGroup,
                new ScrollView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        root.addView(
                scrollView,
                createLayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(220)
                )
        );
    }

    /**
     * Conferma la selezione corrente.
     */
    private void confirmSelection() {

        EcuSelectionPolicy.Status status =
                coordinatorResult
                        .getSelectionResult()
                        .getStatus();

        if (status ==
                EcuSelectionPolicy.Status.AUTO_SELECTED
                ||
                status ==
                        EcuSelectionPolicy.Status.CONFIRMATION_REQUIRED) {

            EcuDiscoveryObservation observation =
                    coordinatorResult
                            .getSelectionResult()
                            .getObservation();

            if (observation == null) {
                listener.onSelectionCancelled();
                dismiss();
                return;
            }

            listener.onEcuSelected(
                    observation
            );

            dismiss();
            return;
        }

        if (status ==
                EcuSelectionPolicy.Status.MULTIPLE_ECUS) {

            EcuDiscoveryObservation observation =
                    getSelectedObservation();

            if (observation == null) {
                return;
            }

            listener.onEcuSelected(
                    observation
            );

            dismiss();
        }
    }

    /**
     * Restituisce l'observation selezionata nella lista.
     *
     * @return observation oppure null.
     */
    @Nullable
    private EcuDiscoveryObservation getSelectedObservation() {

        if (radioGroup == null) {
            return null;
        }

        int checkedId =
                radioGroup.getCheckedRadioButtonId();

        if (checkedId == -1) {
            return null;
        }

        View selected =
                radioGroup.findViewById(
                        checkedId
                );

        if (!(selected instanceof RadioButton)) {
            return null;
        }

        Object tag =
                selected.getTag();

        if (!(tag instanceof EcuDiscoveryObservation)) {
            return null;
        }

        return (EcuDiscoveryObservation) tag;
    }

    /**
     * Restituisce il titolo in base allo stato.
     *
     * @return titolo.
     */
    @NonNull
    private String getTitleText() {

        EcuSelectionPolicy.Status status =
                coordinatorResult
                        .getSelectionResult()
                        .getStatus();

        switch (status) {

            case AUTO_SELECTED:
                return "ECU identificata";

            case CONFIRMATION_REQUIRED:
                return "Conferma ECU";

            case MULTIPLE_ECUS:
                return "Seleziona ECU";

            case NONE:
            default:
                return "ECU non identificata";
        }
    }

    /**
     * Restituisce il messaggio principale.
     *
     * @return messaggio.
     */
    @NonNull
    private String getMessageText() {

        EcuSelectionPolicy.Status status =
                coordinatorResult
                        .getSelectionResult()
                        .getStatus();

        switch (status) {

            case AUTO_SELECTED: {
                EcuDiscoveryObservation observation =
                        coordinatorResult
                                .getSelectionResult()
                                .getObservation();

                if (observation == null) {
                    return "Nessuna ECU disponibile.";
                }

                return "L'ECU è stata identificata automaticamente.\n\n"
                        + buildObservationText(
                        observation
                );
            }

            case CONFIRMATION_REQUIRED: {
                EcuDiscoveryObservation observation =
                        coordinatorResult
                                .getSelectionResult()
                                .getObservation();

                if (observation == null) {
                    return "È stata trovata una ECU, "
                            + "ma non è disponibile per la conferma.";
                }

                return "L'identificazione non è sufficientemente "
                        + "sicura per la selezione automatica.\n\n"
                        + buildObservationText(
                        observation
                );
            }

            case MULTIPLE_ECUS:
                return "Sono state trovate più ECU. "
                        + "Seleziona la centralina da utilizzare.";

            case NONE:
            default:
                return "Non è stato possibile identificare "
                        + "una ECU diagnostica.";
        }
    }

    /**
     * Restituisce il testo del pulsante di conferma.
     *
     * @return testo.
     */
    @NonNull
    private String getConfirmButtonText() {

        EcuSelectionPolicy.Status status =
                coordinatorResult
                        .getSelectionResult()
                        .getStatus();

        switch (status) {

            case AUTO_SELECTED:
                return "Continua";

            case CONFIRMATION_REQUIRED:
                return "Conferma";

            case MULTIPLE_ECUS:
                return "Seleziona";

            case NONE:
            default:
                return "Chiudi";
        }
    }

    /**
     * Costruisce la descrizione di una observation.
     *
     * @param observation observation ECU.
     *
     * @return descrizione.
     */
    @NonNull
    private String buildObservationText(
            @NonNull EcuDiscoveryObservation observation) {

        EcuDefinition candidate =
                observation.getCandidate();

        EcuIdentification identification =
                observation.getIdentification();

        StringBuilder builder =
                new StringBuilder();

        builder.append(
                candidate.getEcu()
        );

        if (!candidate.getBrand().isEmpty()) {
            builder.append(
                    "\n"
            );
            builder.append(
                    candidate.getBrand()
            );
        }

        if (!candidate.getModel().isEmpty()) {
            builder.append(
                    " "
            );
            builder.append(
                    candidate.getModel()
            );
        }

        if (!candidate.getEngine().isEmpty()) {
            builder.append(
                    "\nMotore: "
            );
            builder.append(
                    candidate.getEngine()
            );
        }

        if (!candidate.getProtocol().isEmpty()) {
            builder.append(
                    "\nProtocollo: "
            );
            builder.append(
                    candidate.getProtocol()
            );
        }

        if (identification != null) {

            if (!identification
                    .getEcuHardwareNumber()
                    .isEmpty()) {

                builder.append(
                        "\nHW: "
                );

                builder.append(
                        identification
                                .getEcuHardwareNumber()
                );
            }

            if (!identification
                    .getEcuSoftwareNumber()
                    .isEmpty()) {

                builder.append(
                        "\nSW: "
                );

                builder.append(
                        identification
                                .getEcuSoftwareNumber()
                );
            }

            if (!identification
                    .getEcuPartNumber()
                    .isEmpty()) {

                builder.append(
                        "\nPart: "
                );

                builder.append(
                        identification
                                .getEcuPartNumber()
                );
            }

            if (!identification
                    .getSupplier()
                    .isEmpty()) {

                builder.append(
                        "\nFornitore: "
                );

                builder.append(
                        identification
                                .getSupplier()
                );
            }
        }

        return builder.toString();
    }

    /**
     * Configura le dimensioni della finestra.
     */
    private void configureWindow() {

        if (getWindow() == null) {
            return;
        }

        getWindow()
                .setLayout(
                        dp(340),
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
    }

    /**
     * Crea LayoutParams standard.
     *
     * @param width larghezza.
     * @param height altezza.
     *
     * @return parametri.
     */
    @NonNull
    private LinearLayout.LayoutParams createLayoutParams(
            int width,
            int height) {

        return new LinearLayout.LayoutParams(
                width,
                height
        );
    }

    /**
     * Crea LayoutParams per i pulsanti.
     *
     * @return parametri.
     */
    @NonNull
    private LinearLayout.LayoutParams createButtonLayoutParams() {

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(
                dp(8),
                0,
                0,
                0
        );

        return params;
    }

    /**
     * Converte dp in pixel.
     *
     * @param value valore dp.
     *
     * @return pixel.
     */
    private int dp(
            int value) {

        float density =
                getContext()
                        .getResources()
                        .getDisplayMetrics()
                        .density;

        return Math.round(
                value * density
        );
    }
}