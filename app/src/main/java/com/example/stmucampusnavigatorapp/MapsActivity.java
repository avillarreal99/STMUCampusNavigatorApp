// St. Mary's Campus Navigator
// STMUCampusNavigatorApp.app
// Created Jan 12, 2021
// Last Updated Feb 2, 2021
// Version 1
// Project Team: Amanda Villarreal, Alex Montes, Natalie Rankin, Darren Griffin, Joe Flores, and Dat Trinh
// ------------------------------------------------------------------------------------------------------------------

// IMPORTS AND PACKAGES (DO NOT DELETE)
package com.example.stmucampusnavigatorapp;
import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
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
    // WIDGETS AND VARIABLES
    private GoogleMap stmuMap;          // interactive map
    private EditText campusSearchBar;   // Search bar text field
    public List<CampusLocation> campusLocationsList = new ArrayList<CampusLocation>();


    @Override
    protected void onCreate(Bundle savedInstanceState) // test for amanda
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
    }

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