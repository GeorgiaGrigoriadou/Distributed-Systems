package com.filikietaireia.subscriber.ui.downloads;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.filikietaireia.subscriber.R;

public class DownloadsFragment extends Fragment implements View.OnClickListener {

    private DownloadsViewModel dashboardViewModel;
    private Button play;
    private Button stop;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(DownloadsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_downloads, container, false);
//        final TextView textView = root.findViewById(R.id.text_dashboard);
//        dashboardViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });

        play = root.findViewById(R.id.buttonOfflinePlay);
        play.setOnClickListener(this);

        stop = root.findViewById(R.id.buttonOfflineStop);
        stop.setOnClickListener(this);

        return root;
    }

    @Override
    public void onClick(View v) {
        if (v == play) {
            Toast.makeText(this.getContext(), "Offline Play!", Toast.LENGTH_LONG).show();
        }

        if (v == stop) {
            Toast.makeText(this.getContext(), "Offline Stop!", Toast.LENGTH_LONG).show();
        }
    }
}
