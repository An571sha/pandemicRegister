package com.animesh.notfallapp.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.animesh.notfallapp.R;
import com.animesh.notfallapp.commons.MapsDisplayItem;
import com.animesh.notfallapp.commons.UserLocationAndStatus;
import com.animesh.notfallapp.dialogs.BottomSheetDialog;
import com.animesh.notfallapp.utility.Utility;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class MapViewFragment extends Fragment implements OnMapReadyCallback  {

    private MapView mMapView;
    private GoogleMap googleMap;

    private FragmentActivity myContext;
    private HashMap<String, UserLocationAndStatus> userIdAndstatus;

    private DatabaseReference userDatabase;
    private ClusterManager<MapsDisplayItem> clusterManager;

    private static final String ARG_POSITION = "position";
    private final int CAMERA_PADDING = 10;
    private static String FAILURE_TEXT;
    private LatLngBounds latLngBounds;
    private  LatLng latLng;

    private SupportMapFragment fragment;
    private Bundle mBundle;

    private TextView mapTopNumberOfSick;
    private TextView mapTopNumberNeedingHelp;
    private TextView mapTopNumberWhoCanHelp;

    private List<String> sickList;
    private List<String> needHelpList;
    private List<String> canHelpList;


    public static MapViewFragment newInstance(int position) {
        MapViewFragment f = new MapViewFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.maps_fragment, container, false);
        userDatabase = FirebaseDatabase.getInstance().getReference();

        mapTopNumberOfSick = rootView.findViewById(R.id.number_sick);
        mapTopNumberNeedingHelp = rootView.findViewById(R.id.number_need_help);
        mapTopNumberWhoCanHelp = rootView.findViewById(R.id.number_can_help);

        sickList = new ArrayList<>();
        canHelpList = new ArrayList<>();
        needHelpList= new ArrayList<>();

        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(mBundle);
        mMapView.onResume();
        mMapView.getMapAsync(this);

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBundle = savedInstanceState;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMyLocationEnabled(true);

        clusterManager = new ClusterManager<MapsDisplayItem>(myContext, googleMap);
        googleMap.setOnMarkerClickListener(clusterManager);
        googleMap.setOnCameraIdleListener(clusterManager);

        //display a cluster for more than two markers
        CustomRenderer<MapsDisplayItem> customRenderer = new CustomRenderer<MapsDisplayItem>(myContext, googleMap, clusterManager);
        clusterManager.setRenderer(customRenderer);
        LatLngBounds.Builder latLngbuilder = new LatLngBounds.Builder();


        clusterManager.setOnClusterItemClickListener(mapsDisplayItem -> {

            BottomSheetDialog.showDialog(mapsDisplayItem, requireContext(), requireActivity());

            return true;
        });


        clusterManager.setOnClusterClickListener(cluster -> {
            float currentZoom = googleMap.getCameraPosition().zoom;
            updateCameraPos(googleMap, cluster, currentZoom + 4);
            return true;

        });

        getDataFromFireBase(userIdAndstatus -> {

            clusterManager.clearItems();
            sickList.clear();
            canHelpList.clear();
            needHelpList.clear();
            mapTopNumberOfSick.setText("");
            mapTopNumberNeedingHelp.setText("");
            mapTopNumberWhoCanHelp.setText("");

            for (UserLocationAndStatus userLocationAndStatus : userIdAndstatus.values()) {

                latLng = new LatLng(userLocationAndStatus.getLatitude(), userLocationAndStatus.getLongitude());
                latLngbuilder.include(latLng);

                MapsDisplayItem mapsDisplayItem = new MapsDisplayItem(userLocationAndStatus.getStatus(),
                        latLng,
                        userLocationAndStatus.getAddress(),
                        userLocationAndStatus.getPhoneNumber());

                if (userLocationAndStatus.getStatus()!= null && Utility.checkStringInAllLanguageVariation(myContext, userLocationAndStatus.getStatus(), R.string.i_am_sick)) {

                    sickList.add(userLocationAndStatus.getStatus());

                } else if (userLocationAndStatus.getStatus()!= null &&  Utility.checkStringInAllLanguageVariation(myContext, userLocationAndStatus.getStatus(), R.string.i_need_help)) {
                    needHelpList.add(userLocationAndStatus.getStatus());

                } else if (userLocationAndStatus.getStatus()!= null &&  Utility.checkStringInAllLanguageVariation(myContext, userLocationAndStatus.getStatus(), R.string.i_can_help)) {
                    canHelpList.add(userLocationAndStatus.getStatus());
                }

                clusterManager.addItem(mapsDisplayItem);
            }

            // display data on top of list
            if (sickList != null && !sickList.isEmpty()) {
                mapTopNumberOfSick.setText(String.valueOf(sickList.size()));
            }

            if (needHelpList != null && !needHelpList.isEmpty()) {
                mapTopNumberNeedingHelp.setText(String.valueOf(needHelpList.size()));

            }

            if (canHelpList != null && !canHelpList.isEmpty()) {
                mapTopNumberWhoCanHelp.setText(String.valueOf(canHelpList.size()));

            }

            clusterManager.cluster();

            //center the camera between the bounds
            latLngBounds = latLngbuilder.build();

            googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds,0);
                    googleMap.setMinZoomPreference(1f);
                    googleMap.moveCamera(cameraUpdate);
                    googleMap.animateCamera(cameraUpdate);
                    googleMap.getUiSettings().setZoomControlsEnabled(true);

                    // if no clusters are present in view bounds, move camera to last cluster on the list
                    if(!googleMap.getProjection().getVisibleRegion().latLngBounds.contains(latLng)){
                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    }
                }
            });




        });



    }

    public void getDataFromFireBase(Consumer<HashMap<String, UserLocationAndStatus>> success){
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

                    success.accept(userIdAndstatus);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }


        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        mMapView.getMapAsync(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();

    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }


    @Override
    public void onLowMemory() {
        mMapView.onResume();
        super.onLowMemory();
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        myContext = (FragmentActivity) activity;
        super.onAttach(activity);
    }

    //updating the camera position on zoom

    public void updateCameraPos(GoogleMap googleMap, Cluster<MapsDisplayItem> cluster, float zoomLevel) {
        //LatLngBound for clicked cluster
        LatLngBounds.Builder latLngBuilderForCluster = new LatLngBounds.Builder();

        for (MapsDisplayItem clusterItem : cluster.getItems()) {
            latLngBuilderForCluster.include(clusterItem.getPosition());
        }

        final LatLngBounds latLngBoundsForZoom = latLngBuilderForCluster.build();
        CameraUpdate updateCameraZoom = CameraUpdateFactory.newLatLngZoom(latLngBoundsForZoom.getCenter(), zoomLevel);
        googleMap.setPadding(CAMERA_PADDING, CAMERA_PADDING, CAMERA_PADDING, CAMERA_PADDING);
        googleMap.animateCamera(updateCameraZoom);

    }


    public static class CustomRenderer<T extends ClusterItem> extends DefaultClusterRenderer<T> {

        private Context context;

        public CustomRenderer(Context context, GoogleMap map, ClusterManager<T> clusterManager) {
            super(context, map, clusterManager);
            this.context = context;
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster<T> cluster) {
            return cluster.getSize() > 2;
        }

        @Override
        protected void onBeforeClusterItemRendered(T item, MarkerOptions markerOptions) {


            if (markerOptions.getTitle()!= null && Utility.checkStringInAllLanguageVariation(context, markerOptions.getTitle(), R.string.i_am_ok)) {

                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

            } else if (markerOptions.getTitle()!= null && Utility.checkStringInAllLanguageVariation(context, markerOptions.getTitle(), R.string.i_can_help)){

                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

            } else if (markerOptions.getTitle()!= null && Utility.checkStringInAllLanguageVariation(context, markerOptions.getTitle(), R.string.i_need_help)){

                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));

            } else if (markerOptions.getTitle()!= null && Utility.checkStringInAllLanguageVariation(context, markerOptions.getTitle(), R.string.i_am_sick)){

                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

            } else {

                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            }

        }



        protected int getBucket(Cluster<T> cluster) {
            return cluster.getSize();
        }

    }
}
