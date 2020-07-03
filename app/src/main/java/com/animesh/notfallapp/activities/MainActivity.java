package com.animesh.notfallapp.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.animesh.notfallapp.utility.Utility;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.animesh.notfallapp.R;


import com.animesh.notfallapp.adapters.SectionsPagerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;


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
    private String phoneNumber;

    private EditText phoneNumberTextBox;
    private EditText locationTextBox;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    public Button yesButton, noButton;

    private static final Executor mExecutor = Executors.newSingleThreadExecutor();
    private FusedLocationProviderClient fusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        userIdView = findViewById(R.id.user_id);
        currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userDatabase = FirebaseDatabase.getInstance().getReference();
        Toolbar toolbar = (Toolbar) findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

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
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
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

        //enable GPS
        checkAndEnableGps();

        //enable location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (currentFirebaseUser != null && userDatabase != null) {

            //get user id
            user_id = currentFirebaseUser.getUid();

            //get user phone number. It is an important part of this app
            if (currentFirebaseUser.getPhoneNumber() == null || currentFirebaseUser.getPhoneNumber().isEmpty()) {

                DatabaseReference userRef = userDatabase.child("users");
                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {

                            if (userSnapshot.child(user_id).getValue() != null) {

                                phoneNumber = Objects.requireNonNull(userSnapshot.child(user_id).getValue()).toString();

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            } else {

                phoneNumber = currentFirebaseUser.getPhoneNumber();
            }

        }

        //get last location
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        location_array[0] = location;
                    } else {
                        Toast.makeText(this, R.string.cannot_retreive_location, Toast.LENGTH_SHORT).show();
                    }
                });


        fab.setOnClickListener(view -> createDialogBoxForStatus(location_array[0]));
    }


    public void createDialogBoxForStatus(Location location) {

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_dialog);
        yesButton = dialog.findViewById(R.id.btn_yes);
        noButton = dialog.findViewById(R.id.btn_no);
        radioGroup = dialog.findViewById(R.id.status_radio_group);
        phoneNumberTextBox = dialog.findViewById(R.id.phone_dialog);
        locationTextBox = dialog.findViewById(R.id.location_dialog);

        if (location!= null) {

            getCompleteAddressString(
                    location.getLatitude(),
                    location.getLongitude(),
                    gen_address -> {
                        address = gen_address;
                        runOnUiThread(() -> locationTextBox.setText(address));
                    });

            if (phoneNumber != null && !phoneNumber.isEmpty()) {

                phoneNumberTextBox.setText(phoneNumber);

            }

            //get status
            radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                radioButton = dialog.findViewById(checkedId);
                status = radioButton.getText().toString();
            });

            //upload status, address and phone number to Firebase, if given
            yesButton.setOnClickListener(v -> {
                Tasks.call(mExecutor, () -> {

                    phoneNumber = phoneNumberTextBox.getText().toString();

                    Utility.writeNewUserLocationAndStatus(userDatabase,
                            user_id,
                            location.getLatitude(),
                            location.getLongitude(),
                            address,
                            status,
                            phoneNumber);

                    return null;
                });

                dialog.dismiss();
            });

            noButton.setOnClickListener(v -> dialog.dismiss());

            dialog.show();

        } else {
            Toast.makeText(this, R.string.cannot_retieve_restart, Toast.LENGTH_SHORT).show();
        }

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

    public void getCompleteAddressString(double LATITUDE, double LONGITUDE, Consumer<String> successCallback) {
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
                    Log.w("My Current location address", strReturnedAddress.toString());
                } else {
                    Log.w("My Current location address", "No Address returned!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.w("My Current location address", "Cannot get Address!");
            }
            return strAdd;
        });
    }


}
