package com.liodevel.lioapp_1.Activities;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.liodevel.lioapp_1.Objects.Track;
import com.liodevel.lioapp_1.Objects.TrackPoint;
import com.liodevel.lioapp_1.R;
import com.liodevel.lioapp_1.Utils.Server;
import com.liodevel.lioapp_1.Utils.Utils;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;


import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TrackActivity extends AppCompatActivity {

    private Menu actionBarMenu;
    private static Context context;
    private String trackObjectId = "";
    private Track currentTrack = new Track();
    private static ProgressDialog progress;
    ParseObject trackObject = null;
    long trackPointsCount = 0;

    private MapView mMap = null;
    private String mapStyle = Style.MAPBOX_STREETS;


    //private Toolbar myToolbar;
    private TextView durationInfo;
    private TextView distanceInfo;
    private TextView info;
    private TextView date;
    private TextView averageSpeed;
    private EditText editInfo;

    ArrayList<TrackPoint> trackPoints = new ArrayList<>();

    private ImageView vehicleIcon;
    private LinearLayout leyenda1, leyenda2, leyenda3, leyenda4, leyenda5, leyendaColores;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        context = this;
        changeNotificationBar();

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.liodevel_white)));
        getSupportActionBar().setElevation(0);

        ArrayList<TrackPoint> trackPoints = new ArrayList<>();

        durationInfo = (TextView) findViewById(R.id.text_track_duration_track_info);
        distanceInfo = (TextView) findViewById(R.id.text_track_distance_track_info);
        info = (TextView) findViewById(R.id.text_track_info);
        editInfo = (EditText) findViewById(R.id.edit_info);
        date = (TextView) findViewById(R.id.text_track_date);
        averageSpeed = (TextView) findViewById(R.id.text_track_average_speed_track_info);
        getSupportActionBar().setTitle("");

        leyenda1 = (LinearLayout) findViewById(R.id.track_leyenda_1);
        leyenda2 = (LinearLayout) findViewById(R.id.track_leyenda_2);
        leyenda3 = (LinearLayout) findViewById(R.id.track_leyenda_3);
        leyenda4 = (LinearLayout) findViewById(R.id.track_leyenda_4);
        leyenda5 = (LinearLayout) findViewById(R.id.track_leyenda_5);
        leyendaColores = (LinearLayout) findViewById(R.id.track_leyenda_colores);
        vehicleIcon = (ImageView) findViewById(R.id.track_vehicle_icon);

        mMap = (MapView) findViewById(R.id.mapTrack);
        mMap.setAccessToken(getString(R.string.com_mapbox_mapboxsdk_accessToken));
        mMap.setStyle(Style.MAPBOX_STREETS);
        mMap.onCreate(savedInstanceState);

        //progressDialog.show(this, "Track", "Downloading track", true);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            trackObjectId = "";
        } else {
            trackObjectId = extras.getString("objectId");
            Utils.logInfo("ObjectId Track: " + trackObjectId);
        }

        if (mMap == null) {
            Utils.showMessage(getApplicationContext(), getResources().getString(R.string.unable_to_create_map));
        }

        getTrackByObjectId(trackObjectId);
        updateTrackInfo();
        toggleVehicle(currentTrack.getVehicle());
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateTrack();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        actionBarMenu = menu;
        getMenuInflater().inflate(R.menu.menu_actionbar_track, menu);

        // Favorito
        if (currentTrack != null && currentTrack.isFavorite()){
            actionBarMenu.findItem(R.id.track_action_favorite).setIcon(R.drawable.ic_action_action_favorite);;
        } else {
            actionBarMenu.findItem(R.id.track_action_favorite).setIcon(R.drawable.ic_action_action_favorite_outline);;
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.map_action_delete_track:
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder
                        .setMessage(getResources().getString(R.string.delete_track))
                        .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                Server.deleteTrackByObjectId(trackObjectId);
                                finish();
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                        .show();
                return true;

            // TIPO MAPA
            case R.id.track_action_type_map:
                toggleMapType();
                return true;

            // FAVORITO
            case R.id.track_action_favorite:
                updateTrackFavorite();
                return true;

            // EXPORTAR
            case R.id.track_action_export_kml:
                try {
                    shareKML(exportKML());
                } catch (Exception e){
                    Utils.showMessage(getApplicationContext(), "Error");
                }
                return true;


            default:
                return super.onOptionsItemSelected(item);

        }
    }

    /**
     * Get currentTrack
     *
     * @param objectId
     * @return
     */
    private boolean getTrackByObjectId(String objectId) {
        Utils.logInfo("getTrackByObjectId()");

        progress = new ProgressDialog(context);
        progress.setMessage(getResources().getString(R.string.loading_track));
        progress.show();

        boolean ret = false;
        LatLng prevPos = null;
        LatLng actualPos = null;
        TrackPoint previousTrackPoint = new TrackPoint();


        ParseQuery<ParseObject> queryTrackObject = ParseQuery.getQuery("track");
        queryTrackObject.whereEqualTo("objectId", objectId);


        try {
            List<ParseObject> parseQueriesTrackObject = queryTrackObject.find();
            trackObject = parseQueriesTrackObject.get(0);
            currentTrack.setObjectId(parseQueriesTrackObject.get(0).getObjectId());
            currentTrack.setDate((Date) parseQueriesTrackObject.get(0).get("date"));
            currentTrack.setDateEnd((Date) parseQueriesTrackObject.get(0).get("dateEnd"));
            currentTrack.setDistance((float) parseQueriesTrackObject.get(0).getDouble("distance"));
            currentTrack.setInfo((String) parseQueriesTrackObject.get(0).get("info"));
            currentTrack.setFavorite(parseQueriesTrackObject.get(0).getBoolean("favorite"));
            currentTrack.setVehicle(parseQueriesTrackObject.get(0).getInt("vehicle"));
            if (currentTrack.getVehicle() == 0){
                currentTrack.setVehicle(1);
            }
            Utils.logInfo("Track Vehicle: " + currentTrack.getVehicle());
            Utils.logInfo("Track ID: " + trackObject.getObjectId());
            ret = true;
        } catch (ParseException e) {
            Utils.logInfo("Error: " + e.toString());
            ret = false;
        }

        if (ret == true) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("trackPoint");
            query.whereEqualTo("track", trackObject);
            try {
                trackPointsCount = query.count();
                Utils.logInfo("TrackPoints count: " + trackPointsCount);
            } catch (Exception e){

            }

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.SECOND, 5);
            Date date = calendar.getTime();
            query.whereLessThan("date", date);
            query.setLimit(1000);
            query.orderByAscending("date");
            int cont = 0;
            try {
                List<ParseObject> parseQueries = query.find();
                for (ParseObject parseObject : parseQueries) {
                    cont++;
                    TrackPoint trackPoint = new TrackPoint();
                    trackPoint.setObjectId(parseObject.getObjectId());
                    trackPoint.setDate((Date) parseObject.get("date"));
                    trackPoint.setPosition((ParseGeoPoint) parseObject.get("position"));
                    //trackPoint.setAccuracy((float) parseObject.getDouble("accuracy"));
                    //trackPoint.setProvider(parseObject.getString("provider"));
                    //Utils.logInfo(trackPoint.getProvider());
                    //Utils.logInfo("DATE TR: " + parseObject.get("date"));
                    //trackPoints.add(trackPoint);
                    actualPos = new LatLng(trackPoint.getPosition().getLatitude(), trackPoint.getPosition().getLongitude());
                    if (prevPos != null) {
                        if(previousTrackPoint != null) {

                            Location selected_location=new Location("locationA");
                            selected_location.setLatitude(trackPoint.getPosition().getLatitude());
                            selected_location.setLongitude( trackPoint.getPosition().getLongitude());
                            Location near_locations=new Location("locationA");
                            near_locations.setLatitude(previousTrackPoint.getPosition().getLatitude());
                            near_locations.setLongitude(previousTrackPoint.getPosition().getLongitude());

                            double distance = selected_location.distanceTo(near_locations);

                            double kilometers = distance / 1000.0;
                            //Utils.logInfo("TRACKPOINT DISTANCE      :" + distance);
                            //Utils.logInfo("TRACKPOINT DATE          :" + trackPoint.getDate().getTime());
                            //Utils.logInfo("PREVIOUSTRACKPOINT DATE  :" + previousTrackPoint.getDate().getTime());
                            long microsecs = (trackPoint.getDate().getTime() - previousTrackPoint.getDate().getTime());
                            double hours = microsecs / 1000.0 / 3600.0;
                            double speed = kilometers / hours;
                            //Utils.logInfo("" + speed + "km/h");

                            trackPoint.setSpeed(speed);
                            drawTrackPoint(prevPos, actualPos, speed, currentTrack.getVehicle());


                        }
                    } else {
                        // Centrar en primera localización
                        if (mMap != null){

                            LatLng auxLatLng = new LatLng(actualPos);
                            CameraPosition camPos= new CameraPosition.Builder()
                                    .target(auxLatLng)
                                    .zoom(6)
                                    .tilt((float)mMap.getTilt())
                                    .bearing((float)mMap.getBearing())
                                    .build();

                            if (mMap != null) {
                               // mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
                            }

                        }
                    }

                    prevPos = actualPos;
                    previousTrackPoint = trackPoint;
                    trackPoints.add(trackPoint);
                }
                Utils.logInfo("TOTAL TrackPoints: " + cont);
                ret = true;
            } catch (ParseException e) {
                Utils.logInfo("Error: " + e.toString());
                ret = false;
            }
        }
        progress.dismiss();
        return ret;
    }



    /**
     *
     */
    private void updateTrackInfo() {
        Date currentDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(currentTrack.getDate());

        // Info
        if (currentTrack.getInfo() != null && currentTrack.getInfo().length() > 0) {
            info.setText(currentTrack.getInfo());
            editInfo.setText(currentTrack.getInfo());
        } else {
            info.setText(getResources().getString(R.string.insert_track_info));
            editInfo.setText(getResources().getString(R.string.insert_track_info));
            info.setTextColor(ContextCompat.getColor(this, R.color.liodevel_grey));
        }

        date.setText(currentTrack.getDate().toString());

        // Date
        if (currentDate.getTime() - currentTrack.getDate().getTime() < TimeUnit.MILLISECONDS.convert(6, TimeUnit.DAYS)) {
            String weekDay = "";
            if (c.get(Calendar.DAY_OF_WEEK) == 1) {
                weekDay = this.getResources().getString(R.string.sunday);
            } else if (c.get(Calendar.DAY_OF_WEEK) == 2) {
                weekDay = this.getResources().getString(R.string.monday);
            } else if (c.get(Calendar.DAY_OF_WEEK) == 3) {
                weekDay = this.getResources().getString(R.string.tuesday);
            } else if (c.get(Calendar.DAY_OF_WEEK) == 4) {
                weekDay = this.getResources().getString(R.string.wednesday);
            } else if (c.get(Calendar.DAY_OF_WEEK) == 5) {
                weekDay = this.getResources().getString(R.string.thursday);
            } else if (c.get(Calendar.DAY_OF_WEEK) == 6) {
                weekDay = this.getResources().getString(R.string.friday);
            } else if (c.get(Calendar.DAY_OF_WEEK) == 7) {
                weekDay = this.getResources().getString(R.string.saturday);
            }

            //getActionBar().setTitle(new SimpleDateFormat("HH:mm").format(currentTrack.getDate()) + "   " + weekDay);
        } else {
            //getActionBar().setTitle(new SimpleDateFormat("HH:mm").format(currentTrack.getDate()) + "   " + new SimpleDateFormat("MM-dd-yyyy").format(currentTrack.getDate()));
        }

        // Distancia
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        if (currentTrack.getDistance() < 1000) {
            distanceInfo.setText(df.format(currentTrack.getDistance()) + " m");
        } else {
            distanceInfo.setText(df.format((currentTrack.getDistance() / 1000)) + " km");
        }

        // Duration
        double durationDouble = 0.0;
        if (currentTrack.getDateEnd() != null) {
            Long durationLong = currentTrack.getDateEnd().getTime() - currentTrack.getDate().getTime();
            // duracion en minutos;
            durationDouble = durationLong / 1000 / 60;
            durationInfo.setText(Utils.minutesToHour(durationDouble));

        } else {
            durationInfo.setText("");
        }

        // Velocidad media
        double averageSpeedFloat = 0.0f;
        averageSpeedFloat = (currentTrack.getDistance() / 1000.0f) / (durationDouble / 60.0);

        averageSpeed.setText(df.format(averageSpeedFloat) + " km/h");





    }

    /**
     * Oculta Info y muestra el EditInfo
     * @param v
     */
    public void toggleVisibilityInfo(View v) {
        editInfo.setVisibility(View.VISIBLE);
        info.setVisibility(View.INVISIBLE);
    }

    /**
     * Actualiza campo info en la Base de datos
     */
    private void updateTrack() {

        if (!editInfo.getText().toString().equals(info.getText().toString())) {
            trackObject.put("info", editInfo.getText().toString());
            trackObject.saveInBackground(new SaveCallback() {
                @Override
                public void done(com.parse.ParseException e) {
                    if (e == null) {
                        Utils.logInfo("SAVE startTrack OK");
                        //Utils.showMessage(getApplicationContext(), "Track info successfully saved");
                    } else {
                        Utils.logInfo("SAVE startTrack ERROR: " + e.toString());
                        Utils.showMessage(getApplicationContext(), getResources().getString(R.string.error_saving_track));

                    }
                }
            });
        }
    }


    /**
     * Actualiza campo info en la Base de datos
     */
    private void updateTrackFavorite() {

        if (currentTrack.isFavorite()) {
            currentTrack.setFavorite(false);
            trackObject.put("favorite", false);
            actionBarMenu.findItem(R.id.track_action_favorite).setIcon(R.drawable.ic_action_action_favorite_outline);;
        } else {
            currentTrack.setFavorite(true);
            trackObject.put("favorite", true);
            actionBarMenu.findItem(R.id.track_action_favorite).setIcon(R.drawable.ic_action_action_favorite);;
        }

        trackObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Utils.logInfo("Update favorite");
                    //Utils.showMessage(getApplicationContext(), "Track info successfully saved");
                } else {
                    Utils.logInfo("ERROR: " + e.toString());
                    Utils.showMessage(getApplicationContext(), getResources().getString(R.string.error_saving_track));

                }
            }
        });

    }


    /**
     * Cambiar tipo de mapa
     */
    private void toggleMapType(){

        if (mapStyle.equals(Style.MAPBOX_STREETS)){
            mMap.setStyle(Style.DARK);
            mapStyle = Style.DARK;

        } else if (mapStyle.equals(Style.DARK)){
            mMap.setStyle(Style.EMERALD);
            mapStyle = Style.EMERALD;

        } else if (mapStyle.equals(Style.EMERALD)){
            mMap.setStyle(Style.LIGHT);
            mapStyle = Style.LIGHT;

        } else if (mapStyle.equals(Style.LIGHT)){
            mMap.setStyle(Style.SATELLITE_STREETS);
            mapStyle = Style.SATELLITE_STREETS;

        } else {
            mMap.setStyle(Style.MAPBOX_STREETS);
            mapStyle = Style.MAPBOX_STREETS;
        }


    }




    /**
     * Dibuja una linea en el mapa
     * @param start Coordenadas inicio
     * @param end Coordenadas final
     * @param speed velocidad en KM/H
     * @param vehicle 1-Coche; 2-Moto; 3-Bici; 4-Andando; 5-Corriendo
     */
    private void drawTrackPoint(LatLng start, LatLng end, double speed, int vehicle) {
        int colorTrack;

        // Coche o Moto
        if (vehicle == 1 || vehicle == 2) {
            if (speed < 10) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_black);
            } else if (speed < 20) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_red);
            } else if (speed < 30) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_orange);
            } else if (speed < 40) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_yellow);
            } else if (speed < 50) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_green);
            } else if (speed < 70) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_dark_green);
            } else if (speed < 90) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_blue);
            } else if (speed < 120) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_cyan);
            } else {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_magenta);
            }

            // Bici
        } else if (vehicle == 3) {
            if (speed < 5) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_black);
            } else if (speed < 10) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_red);
            } else if (speed < 15) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_orange);
            } else if (speed < 20) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_yellow);
            } else if (speed < 25) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_green);
            } else if (speed < 35) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_dark_green);
            } else if (speed < 45) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_blue);
            } else if (speed < 55) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_cyan);
            } else {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_magenta);
            }
            // Andando
        } else if (vehicle == 4) {
            if (speed < 2) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_black);
            } else if (speed < 4) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_red);
            } else if (speed < 6) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_orange);
            } else if (speed < 8) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_yellow);
            } else if (speed < 10) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_green);
            } else if (speed < 12) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_dark_green);
            } else if (speed < 14) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_blue);
            } else if (speed < 16) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_cyan);
            } else {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_magenta);
            }

            // Corriendo
        } else {
            if (speed < 5) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_black);
            } else if (speed < 10) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_red);
            } else if (speed < 15) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_orange);
            } else if (speed < 20) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_yellow);
            } else if (speed < 25) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_green);
            } else if (speed < 30) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_dark_green);
            } else if (speed < 35) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_blue);
            } else if (speed < 40) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_cyan);
            } else {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_magenta);
            }
        }

        if (mMap != null) {
            PolylineOptions line =
                    new PolylineOptions().add(start, end)
                            .width(8).color(colorTrack);
            mMap.addPolyline(line);
        }
    }


    private File exportKML() {

        Utils.logInfo("exportKML()");
        XmlSerializer xmlSerializer;

        //File newxmlfile = new File("/sdcard/new.kml");
        File newxmlfile = new File("/sdcard/Android/data/com.liodevel.lioapp_1/" + currentTrack.getDate().toString().replace(" ", "_").replace(":","_") + ".kml");
        //Utils.logInfo("PATH: " + getApplicationInfo().dataDir + "/new.xml");

        try{
            newxmlfile.createNewFile();
        }catch(IOException e){
            Utils.logError("IOException: Exception in create new File(");
        }

        FileOutputStream fileos = null;
        try{
            fileos = new FileOutputStream(newxmlfile);

        }catch(FileNotFoundException e){
            Log.e("FileNotFoundException",e.toString());
        }

        try {

            xmlSerializer = Xml.newSerializer();

            xmlSerializer.setOutput(fileos, "UTF-8");
            xmlSerializer.startDocument("UTF-8", true);

            //xmlSerializer.setFeature( "http://xmlpull.org/v1/doc/features.html#indent-output", true);

            xmlSerializer.startTag(null, "kml");
            xmlSerializer.attribute(null, "xmlns", "http://earth.google.com/kml/2.2");
            xmlSerializer.startTag(null, "Document");

            xmlSerializer.startTag(null, "name");
            xmlSerializer.text(currentTrack.getDate().toString() + " - " + currentTrack.getInfo());
            xmlSerializer.endTag(null, "name");

            xmlSerializer.startTag(null, "description");
            xmlSerializer.cdsect("Your personal GPS tracker");
            xmlSerializer.endTag(null, "description");

            // STYLES

            // Style NEGRO
            xmlSerializer.startTag(null, "Style");
            xmlSerializer.attribute(null, "id", "black");
            xmlSerializer.startTag(null, "LineStyle");
            xmlSerializer.startTag(null, "color");
            xmlSerializer.text(Utils.BLACK);
            xmlSerializer.endTag(null, "color");
            xmlSerializer.startTag(null, "width");
            xmlSerializer.text("4");
            xmlSerializer.endTag(null, "width");
            xmlSerializer.endTag(null, "LineStyle");
            xmlSerializer.endTag(null, "Style");

            // Style ROJO
            xmlSerializer.startTag(null, "Style");
            xmlSerializer.attribute(null, "id", "red");
            xmlSerializer.startTag(null, "LineStyle");
            xmlSerializer.startTag(null, "color");
            xmlSerializer.text(Utils.RED);
            xmlSerializer.endTag(null, "color");
            xmlSerializer.startTag(null, "width");
            xmlSerializer.text("4");
            xmlSerializer.endTag(null, "width");
            xmlSerializer.endTag(null, "LineStyle");
            xmlSerializer.endTag(null, "Style");

            // Style AMARILLO
            xmlSerializer.startTag(null, "Style");
            xmlSerializer.attribute(null, "id", "yellow");
            xmlSerializer.startTag(null, "LineStyle");
            xmlSerializer.startTag(null, "color");
            xmlSerializer.text(Utils.YELLOW);
            xmlSerializer.endTag(null, "color");
            xmlSerializer.startTag(null, "width");
            xmlSerializer.text("4");
            xmlSerializer.endTag(null, "width");
            xmlSerializer.endTag(null, "LineStyle");
            xmlSerializer.endTag(null, "Style");

            // Style VERDE
            xmlSerializer.startTag(null, "Style");
            xmlSerializer.attribute(null, "id", "green");
            xmlSerializer.startTag(null, "LineStyle");
            xmlSerializer.startTag(null, "color");
            xmlSerializer.text(Utils.GREEN);
            xmlSerializer.endTag(null, "color");
            xmlSerializer.startTag(null, "width");
            xmlSerializer.text("4");
            xmlSerializer.endTag(null, "width");
            xmlSerializer.endTag(null, "LineStyle");
            xmlSerializer.endTag(null, "Style");

            // Style CYAN
            xmlSerializer.startTag(null, "Style");
            xmlSerializer.attribute(null, "id", "cyan");
            xmlSerializer.startTag(null, "LineStyle");
            xmlSerializer.startTag(null, "color");
            xmlSerializer.text(Utils.CYAN);
            xmlSerializer.endTag(null, "color");
            xmlSerializer.startTag(null, "width");
            xmlSerializer.text("4");
            xmlSerializer.endTag(null, "width");
            xmlSerializer.endTag(null, "LineStyle");
            xmlSerializer.endTag(null, "Style");

            // Style AZUL
            xmlSerializer.startTag(null, "Style");
            xmlSerializer.attribute(null, "id", "blue");
            xmlSerializer.startTag(null, "LineStyle");
            xmlSerializer.startTag(null, "color");
            xmlSerializer.text(Utils.BLUE);
            xmlSerializer.endTag(null, "color");
            xmlSerializer.startTag(null, "width");
            xmlSerializer.text("4");
            xmlSerializer.endTag(null, "width");
            xmlSerializer.endTag(null, "LineStyle");
            xmlSerializer.endTag(null, "Style");

            // Style MAGENTA
            xmlSerializer.startTag(null, "Style");
            xmlSerializer.attribute(null, "id", "magenta");
            xmlSerializer.startTag(null, "LineStyle");
            xmlSerializer.startTag(null, "color");
            xmlSerializer.text(Utils.MAGENTA);
            xmlSerializer.endTag(null, "color");
            xmlSerializer.startTag(null, "width");
            xmlSerializer.text("4");
            xmlSerializer.endTag(null, "width");
            xmlSerializer.endTag(null, "LineStyle");
            xmlSerializer.endTag(null, "Style");


            // Placemark

                xmlSerializer.startTag(null, "Placemark");
                xmlSerializer.startTag(null, "styleUrl");
                xmlSerializer.text("#red");
                xmlSerializer.endTag(null, "styleUrl");
                /*
                xmlSerializer.startTag(null, "name");

                if (currentTrack.getInfo() != null && currentTrack.getInfo().length() > 0){
                    xmlSerializer.text(currentTrack.getInfo());
                } else {
                    xmlSerializer.text("My Tracker");
                }
                xmlSerializer.endTag(null, "name");
                xmlSerializer.startTag(null, "description");
                xmlSerializer.cdsect("");
                xmlSerializer.endTag(null, "description");
                */


                xmlSerializer.startTag(null, "LineString");

                xmlSerializer.startTag(null, "coordinates");
            for (TrackPoint tr:trackPoints) {

                // Insertar Coordenadas
                xmlSerializer.text(Double.toString(tr.getPosition().getLongitude()) + "," + Double.toString(tr.getPosition().getLatitude()) + "\n");
            }


            xmlSerializer.endTag(null, "coordinates");
            xmlSerializer.endTag(null, "LineString");

            xmlSerializer.endTag(null, "Placemark");
            xmlSerializer.endTag(null, "Document");
            xmlSerializer.endTag(null, "kml");
            xmlSerializer.endDocument();
            xmlSerializer.flush();
            fileos.close();

            //fileos.close();
            //Utils.logInfo("FILE KML:" + writer.toString());

        } catch (Exception e) {
            e.printStackTrace();
            // Log.e("Exception", "Exception occured in wroting");
        }

        return newxmlfile;
    }


    private void shareKML(File file) {

        try {

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            //shareIntent.setType("application/xml");
            shareIntent.setType("application/vnd.google-earth.kml+xml");
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)));


        } catch (Exception e){
            Utils.showMessage(getApplicationContext(), "Error");
            Utils.logError(e.toString());
        }


    }


    private String getStyle(double speed){
        String ret;

        if (speed < 10) {
            ret = "#black";
        } else if (speed < 20) {
            ret = "#red";
        } else if (speed < 30) {
            ret = "#yellow";
        } else if (speed < 60) {
            ret = "#green";
        } else if (speed < 90) {
            ret = "#cyan";
        } else if (speed < 120) {
            ret = "#blue";
        } else {
            ret = "#magenta";
        }

        return ret;
    }



    private void toggleVehicle(int vehicle){

        if (vehicle == 2){
            Utils.logInfo("Vehicle -> 2");
           vehicleIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_motorcycle_black_36dp));
            leyenda2.bringToFront();
            leyendaColores.bringToFront();
        } else if(vehicle == 3){
            Utils.logInfo("Vehicle -> 3");
            vehicleIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_directions_bike_black_36dp));
            leyenda3.bringToFront();
            leyendaColores.bringToFront();

        } else if(vehicle == 4){
            Utils.logInfo("Vehicle -> 4");
            vehicleIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_directions_walk_black_36dp));
            leyenda4.bringToFront();
            leyendaColores.bringToFront();

        } else if(vehicle == 5){
            Utils.logInfo("Vehicle -> 5");
            vehicleIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_directions_run_black_36dp));
            leyenda5.bringToFront();
            leyendaColores.bringToFront();

        } else if(vehicle == 1){
            Utils.logInfo("Vehicle -> 1");
            vehicleIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_directions_car_black_36dp));
            leyenda1.bringToFront();
            leyendaColores.bringToFront();
        }
    }



    @TargetApi(21)
    private void changeNotificationBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Utils.logInfo("Notif.Bar.Coloured");
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(this.getResources().getColor(R.color.liodevel_dark_green));
        } else {
            Utils.logInfo("Ap");
        }
    }

}
