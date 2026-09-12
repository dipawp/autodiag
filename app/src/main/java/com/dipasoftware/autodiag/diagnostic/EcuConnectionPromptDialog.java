package com.dipasoftware.autodiag.diagnostic;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class EcuConnectionPromptDialog extends Dialog {

    public interface Listener {
        void onConnectEcu();
        void onConnectionCancelled();
    }

    @NonNull
    private final Listener listener;

    public EcuConnectionPromptDialog(@NonNull Context context,
                                     @NonNull Listener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(getContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(
                dp(24),
                dp(24),
                dp(24),
                dp(16)
        );

        TextView title = new TextView(getContext());
        title.setText("Diagnostica veicolo");
        title.setTextSize(20);
        title.setPadding(0, 0, 0, dp(16));

        root.addView(
                title,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        TextView message = new TextView(getContext());
        message.setText(
                "L'ELM327 è connesso.\n\n"
                        + "Vuoi avviare la ricerca e la connessione "
                        + "alla centralina del veicolo?"
        );
        message.setTextSize(16);

        root.addView(
                message,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        LinearLayout buttons = new LinearLayout(getContext());
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setGravity(android.view.Gravity.END);
        buttons.setPadding(0, dp(24), 0, 0);

        Button cancelButton = new Button(getContext());
        cancelButton.setText("Annulla");
        cancelButton.setOnClickListener(v -> {
            listener.onConnectionCancelled();
            dismiss();
        });

        Button connectButton = new Button(getContext());
        connectButton.setText("Connetti ECU");
        connectButton.setOnClickListener(v -> {
            listener.onConnectEcu();
            dismiss();
        });

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        buttons.addView(cancelButton, buttonParams);
        buttons.addView(connectButton, buttonParams);

        root.addView(
                buttons,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        setContentView(root);

        configureWindow();
    }

    private void configureWindow() {
        if (getWindow() == null) {
            return;
        }

        getWindow().setLayout(
                dp(360),
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private int dp(int value) {
        float density = getContext()
                .getResources()
                .getDisplayMetrics()
                .density;

        return Math.round(value * density);
    }
}