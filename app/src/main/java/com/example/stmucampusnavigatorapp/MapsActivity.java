// St. Mary's Campus Navigator
// STMUCampusNavigatorApp.app
// Created Jan 12, 2021
// Last Updated Feb 21, 2021
// Version 1
// Project Team: Amanda Villarreal, Alex Montes, Natalie Rankin, Darren Griffin, Joe Flores, and Dat Trinh
// ------------------------------------------------------------------------------------------------------------------

// IMPORTS AND PACKAGES (DO NOT DELETE)
package com.example.stmucampusnavigatorapp;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.example.stmucampusnavigatorapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
{


    // GLOBAL WIDGETS AND VARIABLES
    private GoogleMap stmuMap;          // interactive map
    private EditText campusSearchBar;   // Search bar text field
    public List<CampusLocation> campusLocationsList = new ArrayList<CampusLocation>();
    LocationManager locationManager;    // for getting user location
    LocationListener locationListener;  // for getting user location
    private boolean permissionGranted = false;   // for determining user allowing location permissions

    // MAP SCREEN FUNCTIONS --------------------------------------------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Setting up map screen
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // XML setup
        campusSearchBar = (EditText) findViewById(R.id.stmu_search);

    }


    @Override
    public void onMapReady(GoogleMap googleMap)  // after Map is set up from onCreate()
    {
        // MAP SET UP
        stmuMap = googleMap;
        //initializeSearchBar();

        // Limit the map screen to only display St. Mary's
        final LatLngBounds STMU = new LatLngBounds(new LatLng(29.44945207195666, -98.56892350439986), new LatLng(29.454954521268178, -98.56024923502343)); // Create a LatLngBounds that includes St. Mary's University in United States.
        stmuMap.setLatLngBoundsForCameraTarget(STMU); // Constrain the camera target to St. Mary's University.


        // Add a marker in St. Mary's University and move the camera
        LatLng stMarysUniversity = new LatLng(29.4523, -98.5641);
        stmuMap.addMarker(new MarkerOptions().position(stMarysUniversity).title("St. Mary's University"));
        stmuMap.moveCamera(CameraUpdateFactory.newLatLngZoom(stMarysUniversity, 15f));  // Zoom in on STMU (1 for world, 15 for streets, 20 for buildings)

        // Use intent to signal to system that location is being requested
        Intent intent = getIntent();
        if(intent.getIntExtra("intent value",0) == 0)
        {
            // For obtaining user location
            locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener()
            {
                // These methods are required for LocationListener()
                // We don't use them because we have to get user location permission
                @Override
                public void onLocationChanged(Location location){}
                @Override
                public void onStatusChanged(String s, int i, Bundle bundle){}
                @Override
                public void onProviderEnabled(String s){}
                @Override
                public void onProviderDisabled(String s){}
            };

            // Location permissions granted then obtain location and send to initalizeRecenterbutton
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                permissionGranted = true;
                initalizeRecenterbutton(lastKnownLocation);
            }
            else
            {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
                permissionGranted = false;
            }
        }
    }

    // RECENTER BUTTON FUNCTIONS-------------------------------------------------------------------------------------------------------------------------

    // When recenter button is pressed, user location will be obtained (by Natalie)
    private void initalizeRecenterbutton(Location lastKnownLocation)
    {
        Button recenterButton = (Button) findViewById(R.id.recenterButton);
        recenterButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (permissionGranted) // If user allows location permissions, get location
                {
                    centerMapOnLocation(lastKnownLocation,"User Location");
                }
                else {
                    System.out.println("Location permissions must be enabled\n");
                }
            }
        });
    }

    // Moves map to users location (by Natalie)
    public void centerMapOnLocation(Location location, String title)
    {
        LatLng userLocation = new LatLng(location.getLatitude(),location.getLongitude());
        stmuMap.clear();
        stmuMap.addMarker(new MarkerOptions().position(userLocation).title("You are here!"));
        stmuMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,20f));
    }

    // Requests location permissions and calls centerMapOnLocation if permission is granted (by Natalie)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // If permission granted
        if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            // Make sure that PERMISSION_GRANTED is true before we ACCESS_FINE_LOCATION
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                // Store location
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centerMapOnLocation(lastKnownLocation,"User Location");
            }
        }
    }

    // SEARCH BAR FUNCTIONS -----------------------------------------------------------------------------------------------------------------------------

    /*
    private void initializeSearchBar()   // ignore until we start search functionality
    {
        campusSearchBar.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int searchActionId, KeyEvent searchEvent)
            {
                if((searchActionId == EditorInfo.IME_ACTION_SEARCH) || (searchActionId == EditorInfo.IME_ACTION_DONE)
                        || (searchEvent.getAction() == KeyEvent.ACTION_DOWN) || (searchEvent.getAction() == KeyEvent.KEYCODE_ENTER))
                {
                    // This is where we search for campus locations
                    searchCampusLocation();
                }
                return false;
            }
        });
    }

    private void searchCampusLocation()
    {
        String searchBarString = campusSearchBar.getText().toString();
    }
    */
}