package com.liodevel.lioapp_1.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.liodevel.lioapp_1.Objects.Track;
import com.liodevel.lioapp_1.R;

import java.util.ArrayList;

/**
 * Created by emilio on 22/12/2015.
 */
public class MyTracksListAdapter extends ArrayAdapter<Track> {

    ArrayList<Track> tracks;

    public MyTracksListAdapter(Context context, ArrayList<Track> tracks) {
        super(context, 0, tracks);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Track track = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_tracks_list, parent, false);
        }
        // Lookup view for data population
        TextView date = (TextView) convertView.findViewById(R.id.text_track_date_item);
        // Populate the data into the template view using the data object
        date.setText(track.getDate().toString());
        // Return the completed view to render on screen
        return convertView;
    }
}
