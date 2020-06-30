package com.animesh.notfallapp.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.animesh.notfallapp.utility.Utility;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.animesh.notfallapp.R;


import com.animesh.notfallapp.adapters.SectionsPagerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static java.lang.String.*;


public class MainActivity extends AppCompatActivity {

    private static final String[] LOCATION_PERMS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private static final int TAG_CODE_PERMISSION_LOCATION = 0;
    private static final String USER_ID = "user_id";

    private String user_id;
    private String address;
    private String status;
    private TextView userIdView;
    private DatabaseReference userDatabase;
    private FirebaseUser currentFirebaseUser;
    private Location[] location_array;
    private Double latitude;
    private Double longitude;

    private static final Executor mExecutor = Executors.newSingleThreadExecutor();
    private FusedLocationProviderClient fusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userIdView = findViewById(R.id.user_id);

        currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userDatabase = FirebaseDatabase.getInstance().getReference();

        userIdView.setText(user_id);
        location_array = new Location[1];

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {

            loadActivity();

        } else {

            ActivityCompat.requestPermissions(this, LOCATION_PERMS, TAG_CODE_PERMISSION_LOCATION);
        }


    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {

            loadActivity();

        } else {

            ActivityCompat.requestPermissions(this, LOCATION_PERMS, TAG_CODE_PERMISSION_LOCATION);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                //
                return true;
            case R.id.action_help:
                //
                return true;
            case R.id.action_about:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void loadActivity() {

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        FloatingActionButton fab = findViewById(R.id.fab);

        checkAndEnableGps();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (currentFirebaseUser != null) {
            user_id = currentFirebaseUser.getUid();
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        location_array[0] = location;
                    } else {
                        Toast.makeText(this, "Cannot retrieve location data", Toast.LENGTH_SHORT).show();
                    }
                });


        fab.setOnClickListener(view -> createDialogBoxForStatus(location_array[0]));
    }

    public void createDialogBoxForStatus(Location location) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose your status");

        String[] status = {"I am OK", "I am Sick", "I need Help", "I can Help"};
        int checkedItem = 1;

        builder.setSingleChoiceItems(status, checkedItem, (dialog, which) -> this.status = status[which]);

        builder.setPositiveButton("OK", (dialog, which) -> {
            Tasks.call(mExecutor, () -> {
                getCompleteAddressString(
                        location.getLatitude(),
                        location.getLongitude(),
                        gen_address -> {
                            address = gen_address;
                            Utility.writeNewUserLocationAndStatus(userDatabase,
                                    user_id,
                                    location.getLatitude(),
                                    location.getLongitude(),
                                    address,
                                    this.status);
                });

                return null;
            });
            userIdView.setText(this.status);
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    public void checkAndEnableGps(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        boolean GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!GpsStatus) {
            Intent gpsEnableIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(gpsEnableIntent);
        }
    }

    private void getCompleteAddressString(double LATITUDE, double LONGITUDE, Consumer<String> successCallback) {
        Task<String> searchTask = Tasks.call(mExecutor, () -> {
            String strAdd = "";
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
                if (addresses != null) {
                    Address returnedAddress = addresses.get(0);
                    StringBuilder strReturnedAddress = new StringBuilder("");

                    for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                        strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                    }
                    strAdd = strReturnedAddress.toString();
                    successCallback.accept(strAdd);
                    Log.w("My Current loction address", strReturnedAddress.toString());
                } else {
                    Log.w("My Current loction address", "No Address returned!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.w("My Current location address", "Cannot get Address!");
            }
            return strAdd;
        });
    }


}