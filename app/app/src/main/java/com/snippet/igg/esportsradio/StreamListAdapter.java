package com.snippet.igg.esportsradio;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * Created by snippet on 19/9/2014.
 */
public class StreamListAdapter extends ArrayAdapter<String> {
    private final Activity context;
    private final ArrayList<String> streamers;
    private final ArrayList<String> streamerLogos;
    private final ArrayList<String> statuss;
    private final ArrayList<Integer> viewers;

    ImageLoader imageLoader = ImageLoader.getInstance();

    //private final Integer[] imageId;
    public StreamListAdapter(Activity context,
                            ArrayList<String> streamers,
                            ArrayList<String> gamesLogos,
                            ArrayList<String> statuss,
                            ArrayList<Integer> viewers) {
        super(context, R.layout.game_row);
        this.context = context;
        this.streamers = streamers;
        this.streamerLogos = gamesLogos;
        this.statuss = statuss;
        this.imageLoader = ImageLoader.getInstance();
        this.viewers = viewers;
    }


    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.streamer_row, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.streamerName);
        TextView txtStatus = (TextView) rowView.findViewById(R.id.streamerStatus);
        TextView txtViewers = (TextView) rowView.findViewById(R.id.streamerViewers);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        if (streamers.size() > 0) {
            txtTitle.setText(streamers.get(position));
            txtViewers.setText("Viewers: " + viewers.get(position).toString());
            imageLoader.displayImage(streamerLogos.get(position), imageView);
            txtStatus.setText(statuss.get(position));
        }
        return rowView;
    }
}