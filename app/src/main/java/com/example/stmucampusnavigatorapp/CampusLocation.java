// StMU Campus Navigator
// CampusLocation.java
// By Darren Griffin
// Feb 2021
// Class for holding the data of each campus location
// ----------------------------------------------------------------------------------------------

package com.example.stmucampusnavigatorapp;

import java.util.ArrayList;
import java.util.List;

public class CampusLocation
{
    private String location;
    private String longitude;
    private String latitude;
    private String category;
    private String phoneNumber; // 0 or NULL (No phone number available)

    public CampusLocation(String locationName, String latitudeCoord, String longitudeCoord, String locationPhoneNum, String  locationCategory)
    {
        location = locationName;
        latitude = latitudeCoord;
        longitude = longitudeCoord;
        phoneNumber = locationPhoneNum;
        category = locationCategory;
    }

    public String getLocationName()
    {
        return location;
    }

    public String getLongitude()
    {
        return longitude;
    }

    public String getLatitude()
    {
        return latitude;
    }

    public String getCategory()
    {
        return category;
    }

    public String getPhoneNumber(){return phoneNumber;}

    @Override
    public String toString()
    {
        return this.getLocationName() + " " + this.getLongitude() + " " + this.getLatitude() + " " + this.getPhoneNumber() + " " + this.getCategory();
    }
}