package com.liodevel.lioapp_1.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.liodevel.lioapp_1.Objects.Track;
import com.liodevel.lioapp_1.R;
import com.liodevel.lioapp_1.Utils.Utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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
        Track track = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_tracks_list, parent, false);
        }
        TextView date = (TextView) convertView.findViewById(R.id.text_track_date_item);
        TextView distance = (TextView) convertView.findViewById(R.id.text_track_distance_item);
        TextView duration = (TextView) convertView.findViewById(R.id.text_track_duration_item);
        TextView info = (TextView) convertView.findViewById(R.id.text_track_info_item);

        if (track.isChecked()){
            convertView.setBackground(getContext().getResources().getDrawable(R.drawable.item_selected));
        } else {
            convertView.setBackground(getContext().getResources().getDrawable(R.drawable.item));
        }

        // Fecha Inicio
        Date currentDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(track.getDate());

        if (currentDate.getTime() - track.getDate().getTime() < TimeUnit.MILLISECONDS.convert(6, TimeUnit.DAYS)){
            String weekDay = "";
            if (c.get(Calendar.DAY_OF_WEEK) == 1){weekDay = Utils.SATURDAY;}
            else if (c.get(Calendar.DAY_OF_WEEK) == 2){weekDay = Utils.MONDAY;}
            else if (c.get(Calendar.DAY_OF_WEEK) == 3){weekDay = Utils.TUESDAY;}
            else if (c.get(Calendar.DAY_OF_WEEK) == 4){weekDay = Utils.WEDNESDAY;}
            else if (c.get(Calendar.DAY_OF_WEEK) == 5){weekDay = Utils.THURSDAY;}
            else if (c.get(Calendar.DAY_OF_WEEK) == 6){weekDay = Utils.FRIDAY;}
            else if (c.get(Calendar.DAY_OF_WEEK) == 7){weekDay = Utils.SATURDAY;}

            date.setText(new SimpleDateFormat("HH:mm").format(track.getDate()) + "   " + weekDay);

        } else {
            date.setText(new SimpleDateFormat("HH:mm").format(track.getDate()) + "   " + new SimpleDateFormat("MM-dd-yyyy").format(track.getDate()));
        }

        // Distancia
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        if (track.getDistance() < 1000) {
            distance.setText(df.format(track.getDistance()) + " m");
        } else {
            distance.setText(df.format((track.getDistance() / 1000)) + " km");
        }

        // Duration
        if (track.getDateEnd() != null) {
            Long durationLong = track.getDateEnd().getTime() - track.getDate().getTime();
            double durationDouble;
            // duracion en minutos;
            durationDouble = durationLong / 1000 / 60;

            if (durationDouble < 60) {
                duration.setText(df.format(durationDouble) + " Min");
            } else {
                double hours = durationDouble / 60;
                duration.setText(df.format(hours) + " Hours");
            }
        } else {
            duration.setText("");
        }

        // Info
        if (track.getInfo() != null){
            info.setText(track.getInfo());
        } else {
            info.setText("");
        }
        return convertView;
    }
}
