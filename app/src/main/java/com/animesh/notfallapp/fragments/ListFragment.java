package com.animesh.notfallapp.fragments;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.animesh.notfallapp.R;
import com.animesh.notfallapp.adapters.CustomListAdapter;
import com.animesh.notfallapp.commons.MapsDisplayItem;
import com.animesh.notfallapp.commons.UserLocationAndStatus;
import com.animesh.notfallapp.dialogs.BottomSheetDialog;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListFragment extends Fragment {
    private static final String ARG_POSITION = "position";
    private DatabaseReference userDatabase;
    private List<UserLocationAndStatus> userLocationAndStatuses;
    private ListView listView;

    private EditText statusTextView;
    private EditText distanceTextView;
    private EditText phoneTextView;
    private EditText addressTextView;
    private Button callButton;
    private Button sendTextButton;

    private Double user_latitude;
    private Double user_longitude;
    private float[] results;
    private FusedLocationProviderClient fusedLocationClient;

    public ListFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.list_fragment, container, false);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        Context context = getContext();

        //if last know location is known proceed
        if (context != null) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), location -> {

                        user_latitude = location.getLatitude();
                        user_longitude = location.getLongitude();

                        //set up firebase data
                        userDatabase = FirebaseDatabase.getInstance().getReference();
                        DatabaseReference userRef = userDatabase.child("status");
                        listView = v.findViewById(R.id.firebase_result);
                        userLocationAndStatuses = new ArrayList<>();

                        //set up adapter
                        CustomListAdapter resultListAdapter = new CustomListAdapter(requireActivity(),
                                R.layout.list_row,
                                userLocationAndStatuses);
                        listView.setAdapter(resultListAdapter);

                        //set on click listener
                        listView.setOnItemClickListener((adapter, v1, position, arg3) -> {
                            UserLocationAndStatus value = (UserLocationAndStatus) adapter.getItemAtPosition(position);
                            LatLng latLng = new LatLng(value.getLatitude(), value.getLongitude());
                            MapsDisplayItem mapsDisplayItem = new MapsDisplayItem(value.getStatus(),
                                    latLng,
                                    value.getAddress(),
                                    value.getPhoneNumber());
                            BottomSheetDialog.showDialog(mapsDisplayItem, requireContext(), requireActivity());
                        });

                        userRef.addValueEventListener(new ValueEventListener() {

                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {

                                    requireActivity().runOnUiThread(() -> {
                                        userLocationAndStatuses.clear();
                                        resultListAdapter.clear();
                                    });


                                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                        UserLocationAndStatus userLocationAndStatus = userSnapshot.getValue(UserLocationAndStatus.class);

                                        //compare Location of each user to another other location
                                        if (userLocationAndStatus != null && user_latitude != null && user_longitude != null && userLocationAndStatus.getLatitude() != null && userLocationAndStatus.getLongitude() != null) {

                                            results = new float[3];

                                            Location locationLhs = new Location("");
                                            Location locationRhs = new Location("");

                                            locationLhs.setLatitude(user_latitude);
                                            locationLhs.setLongitude(user_longitude);

                                            locationRhs.setLatitude(userLocationAndStatus.getLatitude());
                                            locationRhs.setLongitude(userLocationAndStatus.getLongitude());

                                            // float distanceInMetersOne = locationLhs.distanceTo(locationRhs);
                                            Location.distanceBetween(user_latitude, user_longitude, userLocationAndStatus.getLatitude(), userLocationAndStatus.getLongitude(), results);
                                            //  Log.i("Important_correct", String.valueOf(results[0]));
                                            //  Log.i("Important_correct_1", String.valueOf(finaleDistance));
                                            userLocationAndStatus.setDistanceToUserLocation(Math.round(results[0]));
                                        }

                                        userLocationAndStatuses.add(userLocationAndStatus);
                                    }

                                    // sort in ascending order
                                    Collections.sort(userLocationAndStatuses, (lhs, rhs) -> {
                                        if (lhs != null && rhs != null) {
                                            return Integer.compare(lhs.getDistanceToUserLocation(), rhs.getDistanceToUserLocation());
                                        }
                                        return 1;
                                    });

                                    // refresh
                                    requireActivity().runOnUiThread(() -> {
                                        resultListAdapter.notifyDataSetChanged();
                                        listView.invalidateViews();
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }

                        });
                    });

        }

        return v;
    }


    public static ListFragment newInstance(int position) {
        ListFragment f = new ListFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }

}
