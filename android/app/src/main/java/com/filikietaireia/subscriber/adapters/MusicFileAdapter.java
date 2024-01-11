package com.filikietaireia.subscriber.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.filikietaireia.subscriber.R;

import java.util.List;

import data.MusicFile;

public class MusicFileAdapter extends RecyclerView.Adapter<MusicFileAdapter.ViewHolder>{
    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView textArtistName;
        public TextView textSongName;
        public LinearLayout layout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.textArtistName = itemView.findViewById(R.id.textArtistName);
            this.textSongName = itemView.findViewById(R.id.textSongName);
            this.layout = itemView.findViewById(R.id.itemLayout);
        }
    }

    private List<MusicFile> songs;
    private int userChoice = 0;

    public MusicFileAdapter(List<MusicFile> songs) {
        this.songs = songs;
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public MusicFile getSelectedSong() {
        try {
            MusicFile f = songs.get(userChoice);
            return f;
        } catch (Exception ex) {
            return null;
        }
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.musicfile_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final MusicFile mf  = songs.get(position);
        holder.textSongName.setText(mf.trackName.replace(".mp3", ""));
        holder.textArtistName.setText(mf.artistName);
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userChoice = position;
                notifyDataSetChanged();
            }
        });
        if (position == userChoice) {
            holder.layout.setBackgroundColor(Color.rgb(125,200,150));
        } else {
            holder.layout.setBackgroundColor(Color.WHITE);
        }
    }
}
