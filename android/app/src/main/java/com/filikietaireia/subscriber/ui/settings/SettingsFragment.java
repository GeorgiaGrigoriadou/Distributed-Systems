package com.filikietaireia.subscriber.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.filikietaireia.subscriber.R;
import com.filikietaireia.subscriber.common.CommonClass;

public class SettingsFragment extends Fragment implements View.OnClickListener {

    private SettingsViewModel notificationsViewModel;
    private Button buttonApplyChanges;
    private View root;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel = ViewModelProviders.of(this).get(SettingsViewModel.class);
        root = inflater.inflate(R.layout.fragment_settings, container, false);

        buttonApplyChanges = root.findViewById(R.id.buttonApplyChanges);
        buttonApplyChanges.setOnClickListener(this);

        String ip = CommonClass.subscriber.getDefaultBrokerInfo().ip;
        int port = CommonClass.subscriber.getDefaultBrokerInfo().port;

        ((EditText)root.findViewById(R.id.brokerIP)).setText(ip);
        ((EditText)root.findViewById(R.id.brokerPort)).setText(String.valueOf(port));

        return root;
    }

    @Override
    public void onClick(View v) {
        if (v == buttonApplyChanges) {
            try {
                CommonClass.subscriber.getDefaultBrokerInfo().ip = ((EditText) root.findViewById(R.id.brokerIP)).getText().toString();
                CommonClass.subscriber.getDefaultBrokerInfo().port = Integer.parseInt(((EditText) root.findViewById(R.id.brokerPort)).getText().toString());
                Toast.makeText(this.getContext(), "Changes saved!", Toast.LENGTH_LONG).show();
            } catch (Exception ex) {
                Toast.makeText(this.getContext(), "Error! " + ex.toString(), Toast.LENGTH_LONG).show();
            }

        }
    }
}
