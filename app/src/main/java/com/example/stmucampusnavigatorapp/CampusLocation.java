package com.example.stmucampusnavigatorapp;

public class CampusLocation
{
    private String location;
    private float longitude;
    private float latitude;
    private String category;
    private int phoneNumber;

    public CampusLocation(String locationName, float longitudeCoord, float latitudeCoord, String locationCategory, int locationPhoneNum)
    {
        location = locationName;
        longitude = longitudeCoord;
        latitude = latitudeCoord;
        category = locationCategory;
        phoneNumber = locationPhoneNum;
    }

    public String getLocationName()
    {
        return location;
    }

    public float getLongitude()
    {
        return longitude;
    }

    public float getLatitude()
    {
        return latitude;
    }

    public String getCategory()
    {
        return category;
    }

    public int getPhoneNumber(){return phoneNumber;}
}