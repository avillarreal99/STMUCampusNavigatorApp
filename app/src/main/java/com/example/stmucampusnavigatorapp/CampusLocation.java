// CampusLocation.java
// By Darren Griffin
// Class for holding the data of each campus location

package com.example.stmucampusnavigatorapp;

public class CampusLocation
{
    private String location;
    private String longitude;
    private String latitude;
    private String category;
    private String phoneNumber; // 0 for NULL (No phone number available)

    public CampusLocation(String locationName, String longitudeCoord, String latitudeCoord, String locationPhoneNum, String  locationCategory)
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




