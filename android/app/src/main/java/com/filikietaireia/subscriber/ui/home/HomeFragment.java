package com.filikietaireia.subscriber.ui.home;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.filikietaireia.subscriber.R;
import com.filikietaireia.subscriber.adapters.MusicFileAdapter;
import com.filikietaireia.subscriber.common.CommonClass;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import data.MusicFile;
import skeleton.Subscriber;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private HomeViewModel homeViewModel;
    private Button signin;
    private Button play;
    private Button stop;
    private Button download;
    private Spinner spinner;
    private Button search;
    private RecyclerView recyclerView;
    private MediaPlayer mediaPlayer = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
//        final TextView textView = root.findViewById(R.id.text_home);
//        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });



        signin = root.findViewById(R.id.buttonSignin);
        signin.setOnClickListener(this);

        recyclerView = root.findViewById(R.id.recyclerViewArtistSongs);

        search = root.findViewById(R.id.buttonSearch);
        search.setOnClickListener(this);

        play = root.findViewById(R.id.buttonPlay);
        play.setOnClickListener(this);

        stop = root.findViewById(R.id.buttonStop);
        stop.setOnClickListener(this);

        download = root.findViewById(R.id.buttonDownload);
        download.setOnClickListener(this);

        spinner = root.findViewById(R.id.spinnerArtists);

        return root;
    }

    private class AsyncTaskGetSongList extends AsyncTask<String, Void, IOException> {
        @Override
        protected IOException doInBackground(String... strings) {
            try {
                List<IOException> errorList = CommonClass.subscriber.getSongListFromNetwork();// register
                if (errorList.isEmpty()) {
                    return null;
                } else {
                    throw errorList.get(0);
                }
            } catch (IOException e) {
                return e;
            }
        }
        @Override
        protected void onPostExecute(IOException ex) {
            super.onPostExecute(ex);

            if (ex == null) {
                int songs = CommonClass.subscriber.getInfoSongList().size();
                Toast.makeText(HomeFragment.this.getContext(), "Songlist updated, songs: " + songs, Toast.LENGTH_LONG).show();
                updateArtists();
            } else {
                Toast.makeText(HomeFragment.this.getContext(), "Songlist update failed:" + ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private class AsyncTaskGetBrokerList extends AsyncTask<String, Void, IOException> {
        @Override
        protected IOException doInBackground(String... strings) {
            try {
                CommonClass.subscriber.getBrokerListFromNetwork(); // register


                return null;
            } catch (IOException e) {
                return e;
            }
        }
        @Override
        protected void onPostExecute(IOException ex) {
            super.onPostExecute(ex);

            if (ex == null) {
                int brokers = CommonClass.subscriber.brokers.size();
                Toast.makeText(HomeFragment.this.getContext(), "Sign in successful, brokers: " + brokers, Toast.LENGTH_LONG).show();

                AsyncTaskGetSongList a = new AsyncTaskGetSongList();
                a.execute();
            } else {
                Toast.makeText(HomeFragment.this.getContext(), "Sign in failed:" + ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private class AsyncTaskGetSong extends AsyncTask<MusicFile, Void, IOException> {
        private String filename = null;

        @Override
        protected IOException doInBackground(MusicFile... musicfile) {
            try {
                filename =CommonClass.subscriber.downloadSong(getContext(), musicfile[0]); // register
                return null;
            } catch (IOException e) {
                return e;
            }
        }
        @Override
        protected void onPostExecute(IOException ex) {
            super.onPostExecute(ex);

            if (ex == null) {
                Toast.makeText(HomeFragment.this.getContext(), "Song downloaded!", Toast.LENGTH_SHORT).show();

                try {
                    FileInputStream fis = getContext().openFileInput(filename);
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setLooping(true);
                    mediaPlayer.setDataSource(fis.getFD());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(HomeFragment.this.getContext(), "File not found!", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(HomeFragment.this.getContext(), "I/O exception !", Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(HomeFragment.this.getContext(), "Sign in failed:" + ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void updateArtists() {
        String [] artists = CommonClass.subscriber.getArtistArray();
        ArrayAdapter aa = new ArrayAdapter(this.getContext(),android.R.layout.simple_spinner_item, artists);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        aa.notifyDataSetChanged();
        spinner.setAdapter(aa);

        search.setEnabled(true);
    }
    @Override
    public void onClick(View v) {
        if (v == signin) {
            Toast.makeText(HomeFragment.this.getContext(), "Trying to sign in", Toast.LENGTH_LONG).show();
            AsyncTaskGetBrokerList task = new AsyncTaskGetBrokerList();
            task.execute();
        }

        if (v == play) {
//            Toast.makeText(this.getContext(), "Play!", Toast.LENGTH_SHORT).show();

            MusicFile f = ((MusicFileAdapter)(recyclerView.getAdapter())).getSelectedSong();

            if (f == null) {
                Toast.makeText(this.getContext(), "Please select a song!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this.getContext(), "You have selected: " + f.trackName, Toast.LENGTH_SHORT).show();
                AsyncTaskGetSong task = new AsyncTaskGetSong();
                task.execute(f);
            }
        }

        if (v == stop) {
            Toast.makeText(this.getContext(), "Stop!", Toast.LENGTH_SHORT).show();

            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer = null;
            }
        }

        if (v == download) {
            Toast.makeText(this.getContext(), "Download!", Toast.LENGTH_SHORT).show();
        }

        if (v == search) {
            Toast.makeText(this.getContext(), "Search!", Toast.LENGTH_SHORT).show();
            updateSongs();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer = null;
        }
    }

    private void updateSongs() {
        List<MusicFile> infoSongList = CommonClass.subscriber.getInfoSongList();

        List<MusicFile> results = new ArrayList<MusicFile>();

        for (MusicFile f : infoSongList) {
            if (f.artistName.equals(spinner.getSelectedItem().toString())) {
                results.add(f);
            }
        }

        MusicFileAdapter adapter = new MusicFileAdapter(results);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerView.setAdapter(adapter);


    }
}
