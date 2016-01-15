package com.liodevel.lioapp_1.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
    Context context;

    public MyTracksListAdapter(Context context, ArrayList<Track> tracks) {
        super(context, 0, tracks);
        this.context = context;
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
        TextView averageSpeed = (TextView) convertView.findViewById(R.id.text_track_average_speed_item);
        ImageView favorite = (ImageView) convertView.findViewById(R.id.favorite_icon_item);
        TextView speedBar = (TextView) convertView.findViewById(R.id.speed_bar_item);

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
            if (c.get(Calendar.DAY_OF_WEEK) == 1) {
                weekDay = context.getResources().getString(R.string.sunday);
            } else if (c.get(Calendar.DAY_OF_WEEK) == 2) {
                weekDay = context.getResources().getString(R.string.monday);
            } else if (c.get(Calendar.DAY_OF_WEEK) == 3) {
                weekDay = context.getResources().getString(R.string.tuesday);
            } else if (c.get(Calendar.DAY_OF_WEEK) == 4) {
                weekDay = context.getResources().getString(R.string.wednesday);
            } else if (c.get(Calendar.DAY_OF_WEEK) == 5) {
                weekDay = context.getResources().getString(R.string.thursday);
            } else if (c.get(Calendar.DAY_OF_WEEK) == 6) {
                weekDay = context.getResources().getString(R.string.friday);
            } else if (c.get(Calendar.DAY_OF_WEEK) == 7) {
                weekDay = context.getResources().getString(R.string.saturday);
            }
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

        double durationDouble = 0.0;

        // Duration
        if (track.getDateEnd() != null) {
            Long durationLong = track.getDateEnd().getTime() - track.getDate().getTime();
            // duracion en minutos;
            durationDouble = durationLong / 1000 / 60;
            duration.setText(Utils.minutesToHour(durationDouble));

        } else {
            duration.setText("");
        }

        // Velocidad media
        double averageSpeedFloat = 0.0f;
        averageSpeedFloat = (track.getDistance() / 1000.0f) / (durationDouble / 60.0);

        averageSpeed.setText(df.format(averageSpeedFloat) + " km/h");

        // Info
        if (track.getInfo() != null){
            info.setText(track.getInfo());
        } else {
            info.setText("");
        }

        // Favorito
        if (track.isFavorite()){
            favorite.setBackground(getContext().getResources().getDrawable(R.drawable.ic_action_action_favorite));
        } else {
            favorite.setBackground(getContext().getResources().getDrawable(R.drawable.ic_action_action_favorite_outline));
        }

        double speed = averageSpeedFloat;
        int vehicle = track.getVehicle();
        int colorTrack = ContextCompat.getColor(context, R.color.liodevel_chart_black);

        if (vehicle == 1 || vehicle == 2) {
            if (speed < 10) {
                colorTrack = ContextCompat.getColor(context, R.color.liodevel_chart_black);
            } else if (speed < 20) {
                colorTrack = ContextCompat.getColor(context, R.color.liodevel_chart_red);
            } else if (speed < 30) {
                colorTrack = ContextCompat.getColor(context, R.color.liodevel_chart_orange);
            } else if (speed < 40) {
                colorTrack = ContextCompat.getColor(context, R.color.liodevel_chart_yellow);
            } else if (speed < 50) {
                colorTrack = ContextCompat.getColor(context, R.color.liodevel_chart_green);
            } else if (speed < 70) {
                colorTrack = ContextCompat.getColor(context, R.color.liodevel_chart_dark_green);
            } else if (speed < 90) {
                colorTrack = ContextCompat.getColor(context, R.color.liodevel_chart_blue);
            } else if (speed < 120) {
                colorTrack = ContextCompat.getColor(context, R.color.liodevel_chart_cyan);
            } else {
                colorTrack = ContextCompat.getColor(context, R.color.liodevel_chart_magenta);
            }
        } else {
            if (speed < 10) {
                colorTrack = ContextCompat.getColor(context, R.color.liodevel_chart_black);
            } else if (speed < 20) {
                colorTrack = ContextCompat.getColor(context, R.color.liodevel_chart_red);
            } else if (speed < 30) {
                colorTrack = ContextCompat.getColor(context, R.color.liodevel_chart_orange);
            } else if (speed < 40) {
                colorTrack = ContextCompat.getColor(context, R.color.liodevel_chart_yellow);
            } else if (speed < 50) {
                colorTrack = ContextCompat.getColor(context, R.color.liodevel_chart_green);
            } else if (speed < 70) {
                colorTrack = ContextCompat.getColor(context, R.color.liodevel_chart_dark_green);
            } else if (speed < 90) {
                colorTrack = ContextCompat.getColor(context, R.color.liodevel_chart_blue);
            } else if (speed < 120) {
                colorTrack = ContextCompat.getColor(context, R.color.liodevel_chart_cyan);
            } else {
                colorTrack = ContextCompat.getColor(context, R.color.liodevel_chart_magenta);
            }

        }
        speedBar.setBackgroundColor(colorTrack);


        return convertView;
    }
}
