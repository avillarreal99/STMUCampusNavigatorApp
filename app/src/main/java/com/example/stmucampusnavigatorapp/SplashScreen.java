// StMU Campus Navigator
// SplashScreen.java
// By Alex Montes
// Our apps loading screen
// --------------------------------------------------------------------------------------------

package com.example.stmucampusnavigatorapp;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity
{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        startActivity(new Intent(this, MapsActivity.class));
        finish();
    }
}