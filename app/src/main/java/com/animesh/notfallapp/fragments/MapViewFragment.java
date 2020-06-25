package com.animesh.notfallapp.fragments;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.animesh.notfallapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class MapViewFragment extends Fragment {

    private MapView mMapView;
    private GoogleMap googleMap;
    private static final String ARG_POSITION = "position";
    private FragmentActivity myContext;
    private Location[] location_array;
    private ArrayList<Double> latitiudeList;
    private ArrayList<Double> longitudeList;
    private HashMap<String,Object> userIdAndstatus;

    private DatabaseReference userDatabase;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.maps_fragment, container, false);
        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        location_array = new Location[1];
        userDatabase = FirebaseDatabase.getInstance().getReference();
        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(requireActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                googleMap.setMyLocationEnabled(true);

                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
                        bottomSheetFragment.show(myContext.getSupportFragmentManager(), bottomSheetFragment.getTag());
                        return true;
                    }
                });
            }
        });


        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        myContext=(FragmentActivity) activity;
        super.onAttach(activity);
    }

    public static MapViewFragment newInstance(int position) {
        MapViewFragment f = new MapViewFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }

    public void getUserLocationAndStatusData(){
        latitiudeList = new ArrayList<Double>();
        longitudeList = new ArrayList<Double>();
        userIdAndstatus = new HashMap<>();

        DatabaseReference userRef = userDatabase.child("status");
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    userIdAndstatus = new HashMap<>();
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        userIdAndstatus.put(userSnapshot.getKey(), Objects.requireNonNull(userSnapshot.child("status").getValue()).toString());
                        latitiudeList.add((Double) userSnapshot.child("latitude").getValue());
                        longitudeList.add((Double) userSnapshot.child("longitude").getValue());
                    }

                } else {
                    Toast.makeText(myContext, "No users were found",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(myContext, databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
