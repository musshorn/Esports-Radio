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
 * Created by snippet on 18/9/2014.
 */
public class GamesListAdapter extends ArrayAdapter<String>{
    private final Activity context;
    private final ArrayList<String> games;
    private final ArrayList<String> gamesLogos;
    ImageLoader imageLoader;
    //private final Integer[] imageId;
    public GamesListAdapter(Activity context,
                      ArrayList<String> games, ArrayList<String> gamesLogos) {
        super(context, R.layout.game_row);
        this.context = context;
        this.games = games;
        this.gamesLogos = gamesLogos;

        this.imageLoader = ImageLoader.getInstance();
    }


    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.game_row, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        if (games.size() > 0) {
            txtTitle.setText(games.get(position));
            imageLoader.displayImage(gamesLogos.get(position), imageView);
        }
        return rowView;
    }

    public void addGame(String gameName, String logoURL){
        this.gamesLogos.add(logoURL);
        this.games.add(gameName);
        this.notifyDataSetChanged();
    }
}