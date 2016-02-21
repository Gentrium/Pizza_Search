package com.example.maks.fs_test;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.maks.fs_test.Constants.Constants;
import com.example.maks.fs_test.Tools.ChangeLocationListener;
import com.example.maks.fs_test.Tools.GetLocation;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import br.com.condesales.EasyFoursquareAsync;
import br.com.condesales.constants.FoursquareConstants;
import br.com.condesales.criterias.VenuesCriteria;
import br.com.condesales.listeners.AccessTokenRequestListener;
import br.com.condesales.listeners.FoursquareVenuesRequestListener;
import br.com.condesales.models.Venue;

public class MainAppActivity extends AppCompatActivity implements
        AccessTokenRequestListener, ChangeLocationListener {

    private EasyFoursquareAsync async;
    private RecyclerView rvVenues;
    private VenueAdapter venueAdapter;
    private Context context;
    private ArrayList<Venue> venuesList;
    private SharedPreferences dataSave;
    private double longitude;
    private double latitude;
    private boolean haveAccess;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
        context = this;
        fillData();
        venueAdapter = new VenueAdapter(venuesList, context);
        rvVenues.setAdapter(venueAdapter);
        async = new EasyFoursquareAsync(this);
        if(venuesList.size() == 0){
            requestVenuesNearby(false);
        }
    }
    private void findViews(){
        rvVenues = (RecyclerView) findViewById(R.id.rv_venues);
        rvVenues.setLayoutManager(new LinearLayoutManager(this));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Getting your location", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                getGPSCoordinates();
            }
        });
        dataSave =  getSharedPreferences(FoursquareConstants.SHARED_PREF_FILE, Context.MODE_PRIVATE);
    }
    private void fillData(){
        venuesList = new ArrayList<>();
        if(dataSave.contains(FoursquareConstants.RESPONSE_JSON)){
            JSONObject venuesJson = null;
            try {
                venuesJson = new JSONObject(dataSave.getString(FoursquareConstants.RESPONSE_JSON,""));
                Gson gson = new Gson();
                JSONArray json = venuesJson.getJSONObject("response")
                        .getJSONArray("venues");
                for (int i = 0; i < json.length(); i++) {
                    Venue venue = gson.fromJson(json.getJSONObject(i)
                            .toString(), Venue.class);
                    venuesList.add(venue);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        latitude = Double.parseDouble(dataSave.getString(Constants.LAT,"40.7065406"));
        longitude = Double.parseDouble(dataSave.getString(Constants.LONGT,"-74.0120587"));
        Collections.sort(venuesList);
    }

    @Override
    public void onError(String errorMsg) {
        // Do something with the error message
        haveAccess = false;
        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAccessGrant(String accessToken) {
        haveAccess = true;
        requestVenuesNearby(false);
    }


    private boolean getGPSCoordinates() {
        LocationManager locationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);


        boolean isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isGPSEnabled) {
            new GetLocation(locationManager,this).start();
            return true;

        } else {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            this.startActivity(intent);
            return false;
        }
    }

    public void requestVenuesNearby(boolean isMoreItems) {
        if(haveAccess) {
            Location location = new Location("");
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            VenuesCriteria criteria = new VenuesCriteria();
            criteria.setLocation(location);

            if (isMoreItems) {
                criteria.setQuantity(venuesList.size() + 10);
                criteria.setRadius(10000);
            } else {
                criteria.setQuantity(venuesList.size() >= 10 ? venuesList.size() : 10);
                criteria.setRadius(5000);
            }
            criteria.setQuery("pizza");

            async.getVenuesNearby(new FoursquareVenuesRequestListener() {
                @Override
                public void onVenuesFetched(ArrayList<Venue> venues) {
                    venuesList = venues;
                    Collections.sort(venues);

                    venueAdapter = new VenueAdapter(venues, context);
                    rvVenues.swapAdapter(venueAdapter, false);
                }

                @Override
                public void onError(String errorMsg) {
                    Toast.makeText(MainAppActivity.this, "error", Toast.LENGTH_LONG).show();
                }
            }, criteria);
        }else {
            async.requestAccess(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.set_default_coordinates) {
            latitude = 40.7065406;
            longitude = -74.0120587;
            venuesList.clear();
            requestVenuesNearby(false);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void locationChanged(double latitude, double longitude) {
        SharedPreferences.Editor editor = dataSave.edit();
        editor.putString(Constants.LAT, String.valueOf(latitude));
        editor.putString(Constants.LONGT, String.valueOf(longitude));
        editor.apply();
        this.longitude = longitude;
        this.latitude = latitude;
        venuesList.clear();
        requestVenuesNearby(false);
    }
}