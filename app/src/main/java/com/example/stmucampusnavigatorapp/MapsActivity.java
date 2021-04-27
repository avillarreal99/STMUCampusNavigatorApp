// St. Mary's Campus Navigator

// STMUCampusNavigatorApp.app
// Created Jan 12, 2021
// Last Updated March 29, 2021
// Version 1
// Project Team: Amanda Villarreal, Alex Montes, Natalie Rankin, Darren Griffin, Joe Flores, and Dat Trinh
// ------------------------------------------------------------------------------------------------------------------

// IMPORTS AND PACKAGES (DO NOT DELETE)
package com.example.stmucampusnavigatorapp;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.health.SystemHealthManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.stmucampusnavigatorapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import android.app.AlertDialog;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener, TaskLoaderCallBack, GoogleMap.OnMarkerClickListener
{

    // GLOBAL WIDGETS AND VARIABLES
    private GoogleMap stmuMap;                  // interactive map
    LatLng stMarysUniversity = new LatLng(29.4523, -98.5641);
    private AutoCompleteTextView stmu_search;   // Search bar text field
    TextView informationBarLocationName;        // Location name displayed in Information Bar
    private List<String> locationNameList = new ArrayList<String>();              // what is this for?
    private List<CampusLocation> campusLocationsList = new ArrayList<CampusLocation>();   // to hold campus locations
    private List<String> locationPictures = new ArrayList<>();
    Polyline directionalPolyline;        // draws directions
    LocationManager locationManager;     // for getting user location
    LocationListener locationListener;   // for getting user location
    private String selectedLocationName;          // globals for information bar
    private String selectedLocationPhoneNumber;   // globals for information bar
    private LatLng selectedLocationLatLng;        // globals for information bar
    private BottomSheetBehavior informationBarBehavior;
    private boolean normalMap = true;
    Location userCurrentLocation;        // for use in directions Button

    // MAP SCREEN METHODS --------------------------------------------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Setting up map screen
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //initialize and hide information bar when app first runs
        View bottomSheet = findViewById(R.id.informationBar);
        informationBarBehavior = BottomSheetBehavior.from(bottomSheet);
        setInfoBarState("collapse");

    }

    @Override
    public void onMapReady(GoogleMap googleMap)  // after Map is set up from onCreate()
    {
        System.out.println("App is ready");
        // MAP SET UP
        stmuMap = googleMap;
        informationBarLocationName = findViewById(R.id.locationNametextView);  // initialize location's displayed name in Information Bar
        initializeCampusLocationsList();
        initializeLocationPicturesList();
        initializeSearchBar();
        initializeScrollButtons();
        initializeMarkerListener();
        initializeUPDButton();
        initializeDirectionsButton();
        initializeCallButton();
        initializeMapModeButton();
        initializeStartButton();
        initializePictureButton();

        // Limit the map screen to only display St. Mary's
        final LatLngBounds STMU = new LatLngBounds(new LatLng(29.44945207195666, -98.56892350439986), new LatLng(29.454954521268178, -98.56024923502343)); // Create a LatLngBounds that includes St. Mary's University in United States.
        stmuMap.setLatLngBoundsForCameraTarget(STMU); // Constrain the camera target to St. Mary's University.


        //  move the camera to StMU
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
                userCurrentLocation = lastKnownLocation;
                initalizeRecenterbutton(lastKnownLocation);
            }
        }
    }



    // SETTING UP CAMPUSLOCATIONSLIST METHODS -----------------------------------------------------------------------------------------------------------

    // sets up the campusLocationsList ArrayList by reading from our asset file (By Darren Griffin)
    public void initializeCampusLocationsList()
    {
        // read text from asset CampusLocations.txt and store in an object of CampusLocation; add each CampusLocation to campusLocationsList
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("CampusLocations.txt")));
            String line;

            while((line = br.readLine()) != null) // reads each line of the .txt file
            {
                String[] vals = line.split(";"); // stores each line of the .txt file into an array.
                String a = vals[0];
                String[] b = a.split(","); // splits each row into columns and sets each of the values.
                String val0 = b[0];  // locationName
                String val1 = b[1];  // latitude
                String val2 = b[2];  // longitude
                String val3 = b[3];  // phoneNumber
                String val4 = b[4];  // category
                CampusLocation campusLocationObj = new CampusLocation(val0, val1, val2, val3, val4); //Creates new object for each row of the .txt file
                campusLocationsList.add(campusLocationObj); //stores all locations into an ArrayList.
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    //Sets up the LocationPictures ArrayList by reading from our assets file
    private void initializeLocationPicturesList()
    {
        // read text from asset LocationPictureNames.txt and store in an object of CampusLocation; add each CampusLocation to campusLocationsList
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("LocationPictureNames")));
            String line;

            while((line = br.readLine()) != null) // reads each line of the .txt file
            {

                locationPictures.add(line); //stores all locations pictures into an ArrayList.
            }
            System.out.println(locationPictures);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    // RECENTER BUTTON METHODS ------------------------------------------------------------------------------------------------------------------------

    // When recenter button is pressed, user location will be obtained (by Natalie Rankin)
    private void initalizeRecenterbutton(Location lastKnownLocation)
    {
        Button recenterButton = findViewById(R.id.recenterButton);
        recenterButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                //Checks if location permission is enabled and if so centers map on user location
                centerMapOnLocation(lastKnownLocation,"User Location");
            }
        });
    }

    // Moves map to users location (by Natalie Rankin)
    public void centerMapOnLocation(Location location, String title)
    {
        //If location permissions not enabled, display AlertDialog to user
        if(location == null)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Location Permissions Not Enabled");
            builder.setMessage("Please go into your device's settings and enable location permissions");
            builder.setPositiveButton("OK", (dialog, which) -> dialog.cancel());
            builder.show();
        }
        else  //recenter map
        {
            setInfoBarState("collapse");  // close information bar in case it's open
            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
            stmuMap.clear();
            stmuMap.addMarker(new MarkerOptions().position(userLocation).title("You are here!").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))).showInfoWindow();
            stmuMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 17f));
        }
    }

    // Requests location permissions and calls centerMapOnLocation if permission is granted (by Natalie Rankin)
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

    //initializes and listens for the map mode button press (By Alex Montes)
    private void initializeMapModeButton() {
       Button mapMode = findViewById(R.id.mapMode);

        mapMode.setOnClickListener(new View.OnClickListener() //listens for button click
        {
            public void onClick(View v)
            {
                //if current map mode on normal mode
                if(normalMap)
                {
                    normalMap = false; //no longer on normal map mode
                    stmuMap.setMapType(GoogleMap.MAP_TYPE_HYBRID); //change map mode
                }
                else //not on normal map mode
                {
                    normalMap = true; //on normal map mode
                    stmuMap.setMapType(GoogleMap.MAP_TYPE_NORMAL); //change map mode
                }

            }
        });
    }


    // SEARCH BAR METHODS -----------------------------------------------------------------------------------------------------------------------------

    // initializes and listens for activity in SearchBar (By Amanda Villarreal, Alex Montes, and Darren Griffen)
    private void initializeSearchBar() //test success
    {
        //New Search Bar Function
        stmu_search = findViewById(R.id.stmu_search);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MapsActivity.this, android.R.layout.simple_list_item_1, locationNameList); //test success
        stmu_search.setAdapter(adapter); //test success
        HorizontalScrollView pictureScroll = findViewById(R.id.PictureScroll);


        //Darren Comment
        for(CampusLocation location : campusLocationsList)
        {
            locationNameList.add(location.getLocationName());
        }

        stmu_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setInfoBarState("collapse");
                
                if(pictureScroll.getVisibility()==View.VISIBLE)
                {
                    pictureScroll.setVisibility(View.GONE);
                    LinearLayout linearLayout = findViewById(R.id.locationImages);
                    linearLayout.removeAllViews();
                    showButtons(findViewById(R.id.mapMode), findViewById(R.id.recenterButton));
                }
            }
        });

        //Listens for key press
        stmu_search.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                //if key press is a search
                if((actionId == EditorInfo.IME_ACTION_SEARCH) || (actionId == EditorInfo.IME_ACTION_DONE)
                        || (event.getAction() == KeyEvent.ACTION_DOWN) || (event.getAction() == KeyEvent.KEYCODE_ENTER))
                {
                    hideKeyboard();
                    stmu_search.dismissDropDown(); //get rid of drop down bar
                    searchCampusLocation(stmu_search.getText().toString(), false); //search for campus locations
                }
                return false;
            }
        });

        //listens for suggestion clicks
        stmu_search.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
            {
                // store location name globally
                selectedLocationName = adapter.getItem(position);
                informationBarLocationName.setText(selectedLocationName);


                // Find the selected drop-down's LatLng and phone number, and store the global values for later use
                for(CampusLocation location : campusLocationsList)
                {
                    if(location.getLocationName() == selectedLocationName)
                    {
                        selectedLocationPhoneNumber = location.getPhoneNumber();
                        selectedLocationLatLng = new LatLng(Float.parseFloat(location.getLatitude()), Float.parseFloat(location.getLongitude()));
                        stmuMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocationLatLng, 17f));
                    }
                }
                hideKeyboard();
                setInfoBarState("expand");

                // if the COVID testing kiosk is selected
                if (selectedLocationName.contains("COVID"))
                {
                    covid19AlertDialog();  // display info on COVID
                }

                searchCampusLocation(adapter.getItem(position), false); //search for specific location
            }
        });
    }

    // conducts a search based on user inputted text (By Amanda Villarreal, Darren Griffin, and Alex Montes)
    private void searchCampusLocation(String searchText, boolean searchingByCategory)
    {
        stmuMap.clear(); // empty map of all markers
        // search for a match between the user's inputted string and a campus location
        for(CampusLocation location : campusLocationsList)
        {
            if(searchingByCategory) // selected scrollbar button
            {
                if(location.getCategory().toLowerCase().contains(searchText.toLowerCase()))
                {
                    LatLng locationPosition = new LatLng(Float.parseFloat(location.getLatitude()), Float.parseFloat(location.getLongitude())); // lat and lng are flipped for some reason
                    stmuMap.addMarker(new MarkerOptions().position(locationPosition).title(location.getLocationName()));
                }
            }
            else  // searched in search bar
            {
                if (location.getLocationName().toLowerCase().contains(searchText.toLowerCase()))
                {
                    LatLng locationPosition = new LatLng(Float.parseFloat(location.getLatitude()), Float.parseFloat(location.getLongitude())); // lat and lng are flipped for some reason
                    stmuMap.addMarker(new MarkerOptions().position(locationPosition).title(location.getLocationName())).showInfoWindow();
                }
            }
        }
    }

    // hides the soft keyboard (Implemented by Amanda Villarreal, method written by a user on GeeksforGeeks)
    public void hideKeyboard()
    {
        View view = this.getCurrentFocus();
        if (view != null)
        {
            // now assign the system service to InputMethodManager
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    //CATEGORIES SCROLLBAR METHODS --------------------------------------------------------------------------------------------------------------------

    //Initializes and listens for one of the buttons in the categories bar (By Alex Montes)
    private void initializeScrollButtons()
    {
        Button academicsBttn  = findViewById(R.id.academicsButton);
        Button amenitiesBttn  = findViewById(R.id.amenitiesButton);
        Button athleticsBttn  = findViewById(R.id.athleticsButton);
        Button foodBttn       = findViewById(R.id.foodButton);
        Button gatheringsBttn = findViewById(R.id.gatheringButton);
        Button libraryBttn    = findViewById(R.id.libraryButton);
        Button parkingBttn    = findViewById(R.id.parkingButton);
        Button residenceBttn  = findViewById(R.id.residenceButton);
        Button sacredBttn     = findViewById(R.id.sacredButton);
        Button safetyBttn     = findViewById(R.id.safetyButton);


        //listens for one of the buttons called and calls onClick()
        academicsBttn.setOnClickListener(this);
        amenitiesBttn.setOnClickListener(this);
        athleticsBttn.setOnClickListener(this);
        foodBttn.setOnClickListener(this);
        gatheringsBttn.setOnClickListener(this);
        libraryBttn.setOnClickListener(this);
        parkingBttn.setOnClickListener(this);
        residenceBttn.setOnClickListener(this);
        sacredBttn.setOnClickListener(this);
        safetyBttn.setOnClickListener(this);
    }

    //Function that determines what the specified Scroll Button does (By Alex Montes)
    @Override
    public void onClick(View bttn)
    {
        HorizontalScrollView pictureScroll = findViewById(R.id.PictureScroll);
        setInfoBarState("collapse"); //hide info bar
        stmuMap.moveCamera(CameraUpdateFactory.newLatLngZoom(stMarysUniversity, 15f));
        hideKeyboard();

        if(pictureScroll.getVisibility()==View.VISIBLE)
        {
            LinearLayout linearLayout = findViewById(R.id.locationImages);
            linearLayout.removeAllViews();
            pictureScroll.setVisibility(View.GONE);
            showButtons(findViewById(R.id.mapMode), findViewById(R.id.recenterButton));
        }

        //Searches for campus location based on the location category that was selected
        switch (bttn.getId())
        {
            case R.id.academicsButton:
                Button academicsBttn = findViewById(R.id.academicsButton);
                searchCampusLocation(academicsBttn.getText().toString(), true);
                break;
            case R.id.amenitiesButton:
                Button amenitiesBttn = findViewById(R.id.amenitiesButton);
                searchCampusLocation(amenitiesBttn.getText().toString(), true);
                break;
            case R.id.athleticsButton:
                Button athleticsBttn = findViewById(R.id.athleticsButton);
                searchCampusLocation(athleticsBttn.getText().toString(), true);
                break;
            case R.id.foodButton:
                Button foodBttn = findViewById(R.id.foodButton);
                searchCampusLocation(foodBttn.getText().toString(), true);
                break;
            case R.id.gatheringButton:
                Button gatheringsBttn = findViewById(R.id.gatheringButton);
                searchCampusLocation(gatheringsBttn.getText().toString(), true);
                break;
            case R.id.libraryButton:
                Button libraryBttn = findViewById(R.id.libraryButton);
                searchCampusLocation(libraryBttn.getText().toString(), true);
                break;
            case R.id.parkingButton:
                Button parkingBttn = findViewById(R.id.parkingButton);
                searchCampusLocation(parkingBttn.getText().toString(), true);
                break;
            case R.id.residenceButton:
                Button residenceBttn = findViewById(R.id.residenceButton);
                searchCampusLocation(residenceBttn.getText().toString(), true);
                break;
            case R.id.sacredButton:
                Button sacredBttn = findViewById(R.id.sacredButton);
                searchCampusLocation(sacredBttn.getText().toString(), true);
                break;
            case R.id.safetyButton:
                Button safetyBttn = findViewById(R.id.safetyButton);
                searchCampusLocation(safetyBttn.getText().toString(), true);
                break;
            default:
                System.out.println("Invalid button press");
        }

    }


    // UPD BUTTON METHODS-----------------------------------------------------------------------------------------------------------------------------

    // listens for button click for UPD button, initializes all global location variables to values of UPD (By Amanda Villarreal)
    public void initializeUPDButton()
    {
        Button UPDButton = findViewById(R.id.UPD);
        HorizontalScrollView pictureScroll = findViewById(R.id.PictureScroll);

        UPDButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                hideKeyboard();
                if(pictureScroll.getVisibility()==View.VISIBLE)
                {
                    pictureScroll.setVisibility(View.GONE);
                    LinearLayout linearLayout = findViewById(R.id.locationImages);
                    linearLayout.removeAllViews();
                    showButtons((Button) findViewById(R.id.mapMode), (Button) findViewById(R.id.recenterButton));
                }

                // search through campus locations to find UPD
                for(CampusLocation location : campusLocationsList)
                {
                    if(location.getLocationName().contains("UPD")) // UPD found
                    {
                        // set all global variables to values of UPD location
                        selectedLocationName = location.getLocationName();
                        selectedLocationPhoneNumber = location.getPhoneNumber();
                        selectedLocationLatLng = new LatLng(Float.parseFloat(location.getLatitude()), Float.parseFloat(location.getLongitude()));
                        // change information bar name to UPD
                        informationBarLocationName.setText(selectedLocationName);
                        // add a marker of only UPD on map
                        stmuMap.clear();
                        stmuMap.addMarker(new MarkerOptions().position(selectedLocationLatLng).title(selectedLocationName)).showInfoWindow();
                        stmuMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocationLatLng, 17f));
                        setInfoBarState("expand");
                    }
                }
            }
        });
    }


    // INFORMATION BAR METHODS------------------------------------------------------------------------------------------------------------------------

    // Change name of location on Information Bar depending on selected marker (by Amanda Villarreal)
    public void initializeMarkerListener()
    {
        stmuMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
        {
            @Override
            public boolean onMarkerClick(Marker marker)
            {
                HorizontalScrollView pictureScroll = findViewById(R.id.PictureScroll);

                if(pictureScroll.getVisibility()==View.VISIBLE)
                {
                    pictureScroll.setVisibility(View.GONE);
                    showButtons((Button) findViewById(R.id.mapMode), (Button) findViewById(R.id.recenterButton));
                }

                LinearLayout linearLayout = findViewById(R.id.locationImages);
                linearLayout.removeAllViews();

                selectedLocationName = marker.getTitle();

                // if the COVID testing kiosk is selected
                if (selectedLocationName.contains("COVID"))
                {
                    covid19AlertDialog();  // display info on COVID
                }

                // Find the selected marker's LatLng and phone number, and store the global values for later use
                for(CampusLocation location : campusLocationsList)
                {
                    if(location.getLocationName().contains(selectedLocationName))
                    {
                        selectedLocationPhoneNumber = location.getPhoneNumber();
                        selectedLocationLatLng = new LatLng(Float.parseFloat(location.getLatitude()), Float.parseFloat(location.getLongitude()));
                    }
                }

                if (selectedLocationName.equals("You are here!")) // make user's location un-selectable
                {
                    setInfoBarState("collapse");  // do not display info bar
                }
                else
                {
                    setInfoBarState("expand");
                    informationBarLocationName.setText(selectedLocationName); // change Location Name Text View to selected marker name
                }
                return false;
            }
        });
    }

    // This method is required by OnMarkerClickListener but is not used
    @Override
    public boolean onMarkerClick(Marker marker)
    {
        return false;
    }

    // initializes directions button in Information Bar
    public void initializeDirectionsButton()
    {
        Button directionsButton = findViewById(R.id.directButton);

        directionsButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if(userCurrentLocation == null)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                    builder.setTitle("Location Permissions Not Enabled");
                    builder.setMessage("Please go into your device's settings and enable location permissions");
                    builder.setPositiveButton("OK", (dialog, which) -> dialog.cancel());
                    builder.show();
                    System.out.println("location not enabled");
                }
                else
                {
                    LatLng userLocation = new LatLng(userCurrentLocation.getLatitude(), userCurrentLocation.getLongitude());

                    // make a request for polyline
                    stmuMap.clear();
                    stmuMap.addMarker(new MarkerOptions().position(userLocation).title("You are here!").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))).showInfoWindow();
                    stmuMap.addMarker(new MarkerOptions().position(selectedLocationLatLng).title(selectedLocationName)).showInfoWindow();
                    stmuMap.moveCamera(CameraUpdateFactory.newLatLngZoom(stMarysUniversity, 15.5f));
                    String url = getDirectionsURL(userLocation, selectedLocationLatLng, "walking");
                    new FetchURL(MapsActivity.this).execute(url, "walking");  // create a directions request
                }
            }
        });
    }

    // initializes call button in Information Bar (By Alex Montes)
    public void initializeCallButton()
    {
        Button callButton = findViewById(R.id.callButton);

        callButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                //if phone number exists
                if(!selectedLocationPhoneNumber.equals("NULL"))
                {
                    Intent call = new Intent(Intent.ACTION_DIAL);

                    //parse the telephone number ("tel:" is needed to avoided errors)
                    call.setData(Uri.parse("tel:" + selectedLocationPhoneNumber));
                    startActivity(call);
                }
                else
                {
                    AlertDialog.Builder noNumberAlert = new AlertDialog.Builder(MapsActivity.this);
                    noNumberAlert.setTitle("No Phone Number Found");
                    noNumberAlert.setMessage("This location has no phone number");
                    noNumberAlert.setPositiveButton("OK", (dialog, which) -> dialog.cancel());
                    noNumberAlert.show();
                }

            }
        });
    }

    // initializes start button in Information Bar (By Amanda Villarreal)
    public void initializeStartButton()
    {
        Button callButton = findViewById(R.id.startButton);

        callButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                // will be sent into Uri.parse as the user's destination
                String destinationCoordinates = selectedLocationLatLng.latitude + "," + selectedLocationLatLng.longitude;

                // create the intent to navigate to destination, send intent to Google
                Intent navigationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + destinationCoordinates + "&mode=w"));
                navigationIntent.setPackage("com.google.android.apps.maps");

                // make sure the user has Google Maps App installed on their device
                if(navigationIntent.resolveActivity(getPackageManager()) != null)
                {
                    // start navigation through Google Maps
                    startActivity(navigationIntent);
                }
                else
                {
                    // display an Alert Dialog to download google maps
                    AlertDialog.Builder googleMapsAlert = new AlertDialog.Builder(MapsActivity.this);
                    googleMapsAlert.setTitle("Cannot Start Navigation");
                    googleMapsAlert.setMessage("To use this feature, this device must have Google Maps installed. Please visit " +
                                               "the Google Play Store on this device, search for the Google Maps app, and install it.");
                    googleMapsAlert.setPositiveButton("OK", (dialog, which) -> dialog.cancel());
                    googleMapsAlert.show();
                }
            }
        });
    }

    // Initialization of the picture button (by Joe Flores)
    public void initializePictureButton()
    {
        //Declaration of the buttons and scroll
        Button pictureBttn = findViewById(R.id.PictureButton);
        Button mapModeBttn = findViewById(R.id.mapMode);
        Button recenterBttn = findViewById(R.id.recenterButton);
        HorizontalScrollView pictureScroll = findViewById(R.id.PictureScroll);
        pictureBttn.setOnClickListener(this);
        pictureScroll.setOnClickListener(this);

        //Sets the scroll to be GONE and take up no space unless called upon when user clicks button
        pictureScroll.setVisibility(View.GONE);
        pictureScroll.setClickable(false);

        pictureBttn.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                //  make the picture scroll become visible as long as it was invisible to begin with
                if(pictureScroll.getVisibility()==View.GONE)
                {
                    pictureScroll.setVisibility(View.VISIBLE);
                    pictureScroll.setClickable(true);
                    hideButtons(mapModeBttn, recenterBttn);
                    showLocationPictures();
                    pictureBttn.setPressed(true);
                }
                else // makes it go back to invisible when clicked again
                {
                    LinearLayout linearLayout = findViewById(R.id.locationImages);
                    linearLayout.removeAllViews();
                    pictureScroll.setVisibility(View.GONE);
                    pictureScroll.setClickable(false);
                    showButtons(mapModeBttn, recenterBttn);
                    pictureBttn.setPressed(false);
                }
            }
        });
    }

    //function that hides and disables an n amount of buttons passed in
    private void hideButtons(Button ... buttons)
    {

        for(Button bttn : buttons)
        {
            bttn.setVisibility(View.GONE);
            bttn.setClickable(false);
        }
    }

    //function that shows and enables an n amount of buttons passed in
    private void showButtons(Button ... buttons)
    {
        for(Button bttn : buttons)
        {
            bttn.setVisibility(View.VISIBLE);
            bttn.setClickable(true);
        }
    }

    //set the state of the information bar (By Alex Montes)
    public void setInfoBarState(String state)
    {
        state = state.toLowerCase(); //just in case

        switch (state) {
            case "expand":
                informationBarBehavior.setPeekHeight(150);
                informationBarBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                break;
            case "collapse":
                informationBarBehavior.setPeekHeight(0);
                informationBarBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                break;
            default:
                System.out.println("invalid state: Throw an error later");
        }
    }

    // DIRECTIONS METHODS -----------------------------------------------------------------------------------------------------------------------------

    // sets up the URL to be sent to google to create directions (by Amanda Villarreal)
    private String getDirectionsURL(LatLng userLocation, LatLng destination, String directionMode)
    {
        // Users current location as a string
        String userLocationString = "origin=" + userLocation.latitude + "," + userLocation.longitude;

        // Destination as a string
        String destinationString = "destination=" + destination.latitude + "," + destination.longitude;

        // Travel mode as a string
        String mode = "mode=" + directionMode;

        // build parameters section of URL
        String parameters = userLocationString + "&" + destinationString + "&" + mode;

        // now building complete URL to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/json?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    // Draws polylines (by Amanda Villarreal)
    @Override
    public void onTaskDone(Object... values)
    {
        // remove existing polylines
        if(directionalPolyline != null)
            directionalPolyline.remove();

        // draw new polyline
        directionalPolyline = stmuMap.addPolyline((PolylineOptions) values[0]);

    }

    public void covid19AlertDialog()
    {
        // display an Alert Dialog to mention COVID-19
        AlertDialog.Builder googleMapsAlert = new AlertDialog.Builder(MapsActivity.this);
        googleMapsAlert.setTitle("COVID-19 Information for your Safety");
        googleMapsAlert.setMessage(" StMU has partnered with Curative, Inc., to provide free COVID-19 testing for our campus community. " +
                                   "Our kiosk in Lot D offers no-cost, contactless, self-administered COVID-19 testing. Testing is available by " +
                                   "appointment from 8 a.m. to 6 p.m. on weekdays until May 15, 2021. COVID-19 test results are estimated to be " +
                                   "available in 36-48 hours. Anyone already with COVID-19 symptoms is asked to contact your physician before " +
                                   "using the kiosk. For those not displaying symptoms, appointments for testing at the kiosk can be " +
                                   "made through the Curative website.");

        googleMapsAlert.setPositiveButton("OK", (dialog, which) -> dialog.cancel());
        googleMapsAlert.show();
    }

    //Function that shows a locations pictures (By Alex Montes)
    /*
      This function is causing the app to do heavy processing and is currently not efficient as
      its causing the running program to skip 128 frames (130 if the less efficient search method
      is used). Use of java.util.concurrent may need to be implemented somewhere
     */
    public void showLocationPictures() {
        LinearLayout linearLayout = findViewById(R.id.locationImages);
        String infoBarText = "";
        int imageID = 0; //holds image file ID
        int found = -1; //picture not found by default
        int count = 0;

        //following three lines gets rid of white spaces, special chars, and turns to lower case of the info bar text
        infoBarText = informationBarLocationName.getText().toString().replaceAll("\\s", "");
        infoBarText = infoBarText.replaceAll("[-.'&/()]", "");
        infoBarText = infoBarText.toLowerCase();
        System.out.println(infoBarText);

        String finalInfoBarText = infoBarText; //needed to use stream()
        List<String> matchingElements = null; //empty list

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) { //if user version is API24 or higher
            //use more efficient search
            //search through the locationPictures arrayList and fill an empty arraylist with all the matching photo names
            matchingElements = locationPictures.stream().filter(str -> str.trim().contains(finalInfoBarText)).collect(Collectors.toList());

            //while not at the end of matching pictures list
            while (matchingElements.size() > count) {
                //set up the parameters of the imageView
                LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(1000, LinearLayout.LayoutParams.WRAP_CONTENT,
                        1.0f);
                param.setMargins(40, 0, 40, 200); //set spacing between pics
                imageID = getImage(matchingElements.get(count)); //get id of the resource from the drawable folder
                ImageView imageView = new ImageView(MapsActivity.this); //create imageView
                imageView.setLayoutParams(param); //set up parameters
                imageView.setScaleType(ImageView.ScaleType.FIT_XY); //fit the imageView in parent

                //getDrawable() is deprecated and needs this
                @SuppressLint("UseCompatLoadingForDrawables") Drawable drawable = getResources().getDrawable(imageID);
                imageView.setImageDrawable(drawable);
                linearLayout.addView(imageView);
                count++;
            }
        }
        else {
            for (String campusLocation : locationPictures) //go through all pictures
            {
                if (campusLocation.contains(infoBarText)) //if picture name matches location name
                {
                    LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(1000, LinearLayout.LayoutParams.WRAP_CONTENT,
                            1.0f);
                    param.setMargins(40, 0, 40, 200);
                    imageID = getImage(campusLocation); //get id of the resource from the drawable folder
                    ImageView imageView = new ImageView(MapsActivity.this);
                    imageView.setLayoutParams(param);
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);

                    //getDrawable() is deprecated and needs this
                    @SuppressLint("UseCompatLoadingForDrawables") Drawable drawable = getResources().getDrawable(imageID);
                    imageView.setImageDrawable(drawable);
                    linearLayout.addView(imageView);
                }
            }
        }



    }

    //function that looks for a specified resource in the drawable's folder (By Alex Montes)
    private int getImage(String imageName){
        //holds the id of the resource
        int drawableResourceID = this.getResources().getIdentifier(imageName, "drawable", this.getPackageName());

        return drawableResourceID;
    }


    //the bottom commented code were plans to get campus pictures from an online URL. Left for future work
    /*
    public void showOnlineLocationPictures()
    {
        LinearLayout linearLayout = findViewById(R.id.locationImages);

        LoadImage loadImage = new LoadImage(image2);
        loadImage.execute("https://i.imgur.com/AxbXf23.jpg");
        for(CampusLocation location : campusLocationsList){
            if(location.getLocationName().contains(informationBarLocationName.toString()))
            {
                String locationName = location.getLocationName();
                String URL = "";
                ImageView image = new ImageView(this);

                //how this will work
                     1.Turn locationName to lowercase and get rid of white space
                     2.search for the folder containing the images (on google drive)
                     3.check if the locationName matches the file name (This will try to FIND any matches of the location name in the file name)

            }
        }

    }

    private class LoadImage extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;
        public LoadImage(ImageView ivResult){
            this.imageView = ivResult;
        }


        @Override
        protected Bitmap doInBackground(String... strings) {
            String urlLink = strings[0];
            Bitmap bitmap = null;
            try {
                InputStream inputStream = new java.net.URL(urlLink).openStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            image2.setImageBitmap(bitmap);
        }
    }
    */
}