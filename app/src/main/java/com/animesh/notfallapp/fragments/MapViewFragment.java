package com.animesh.notfallapp.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.animesh.notfallapp.R;
import com.animesh.notfallapp.commons.MapsDisplayItems;
import com.animesh.notfallapp.commons.UserLocationAndStatus;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class MapViewFragment extends Fragment {

    private MapView mMapView;
    private GoogleMap googleMap;

    private FragmentActivity myContext;
    private HashMap<String, UserLocationAndStatus> userIdAndstatus;

    private DatabaseReference userDatabase;
    private ClusterManager<MapsDisplayItems> clusterManager;

    private static final Executor mExecutor = Executors.newSingleThreadExecutor();
    private static final String ARG_POSITION = "position";
    private static String FAILURE_TEXT;
    private LatLngBounds latLngBounds;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.maps_fragment, container, false);
        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        userDatabase = FirebaseDatabase.getInstance().getReference();
        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(requireActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(mMap -> {

            googleMap = mMap;

            googleMap.setMyLocationEnabled(true);

            googleMap.setOnMarkerClickListener(marker -> {
                BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
                bottomSheetFragment.show(myContext.getSupportFragmentManager(), bottomSheetFragment.getTag());
                return true;
            });

            //display the markers if retrieved properly. otherwise throw exception toast
            getUserLocationAndStatusData(success-> {

                clusterManager =  new ClusterManager<MapsDisplayItems>(getContext(), googleMap);
                CustomRenderer<MapsDisplayItems> customRenderer = new CustomRenderer<MapsDisplayItems>(getContext(), googleMap, clusterManager);
                clusterManager.setRenderer(customRenderer);
                LatLngBounds.Builder latLngbuilder = new LatLngBounds.Builder();

                for (UserLocationAndStatus userLocationAndStatus: userIdAndstatus.values()) {
                    LatLng latLng = new LatLng(userLocationAndStatus.getLatitude(), userLocationAndStatus.getLongitude());
                    latLngbuilder.include(latLng);

                    MapsDisplayItems mapsDisplayItems = new MapsDisplayItems("Marker #", latLng);
                    clusterManager.addItem(mapsDisplayItems);
                }

                //center the camera between the bounds
                latLngBounds = latLngbuilder.build();

                googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback(){

                    @Override
                    public void onMapLoaded() {
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds,0);
                        googleMap.setMinZoomPreference(1f);
                        googleMap.moveCamera(cameraUpdate);
                        googleMap.animateCamera(cameraUpdate);
                        googleMap.getUiSettings().setZoomControlsEnabled(true);
                    }
                });

                }, failure -> {

                FAILURE_TEXT = failure;
                Toast.makeText(getContext(), FAILURE_TEXT, Toast.LENGTH_SHORT).show();
            });

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

    public void getUserLocationAndStatusData(Consumer<String> successCallback, Consumer<String> faliureCallback ){
        Tasks.call(mExecutor, () -> {

            String done = "done with retrieving data";
            String noUserFound = "No users were found";

            DatabaseReference userRef = userDatabase.child("status");

            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {

                        userIdAndstatus = new HashMap<String, UserLocationAndStatus>();

                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            UserLocationAndStatus userLocationAndStatus = userSnapshot.getValue(UserLocationAndStatus.class);
                            userIdAndstatus.put(userSnapshot.getKey(), userLocationAndStatus);

                        }

                        successCallback.accept(done);

                    } else {
                        Toast.makeText(myContext, noUserFound,
                                Toast.LENGTH_SHORT).show();
                        faliureCallback.accept(noUserFound);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    faliureCallback.accept(databaseError.getMessage());
                }
            });
            return null;
        });
    }


    public static class CustomRenderer<T extends ClusterItem> extends DefaultClusterRenderer<T> {

        public CustomRenderer(Context context, GoogleMap map, ClusterManager<T> clusterManager) {
            super(context, map, clusterManager);
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster<T> cluster) {
            return cluster.getSize() > 2;
        }

        protected int getBucket(Cluster<T> cluster){
            return cluster.getSize();
        }
    }
}
