package com.animesh.notfallapp.fragments;

import android.content.DialogInterface;
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
import com.animesh.notfallapp.commons.UserLocationAndStatus;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

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
        userDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference userRef = userDatabase.child("status");
        listView = v.findViewById(R.id.firebase_result);
        userLocationAndStatuses = new ArrayList<>();

        CustomListAdapter resultListAdapter = new CustomListAdapter(requireActivity(),
                R.layout.list_row,
                userLocationAndStatuses);
        listView.setAdapter(resultListAdapter);
        BottomSheetFragment bottomSheetFragment = BottomSheetFragment.newInstance();

        listView.setOnItemClickListener((adapter, v1, position, arg3) -> {
            UserLocationAndStatus value = (UserLocationAndStatus) adapter.getItemAtPosition(position);

          //  Toast.makeText(requireContext(), String.valueOf(position) , Toast.LENGTH_SHORT).show();
                    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
                    bottomSheetDialog.setContentView(R.layout.bottom_sheet);

                    statusTextView = bottomSheetDialog.findViewById(R.id.status_bottom_sheet);
                    distanceTextView = bottomSheetDialog.findViewById(R.id.distance_bottom_sheet);
                    phoneTextView = bottomSheetDialog.findViewById(R.id.phone_bottom_sheet);
                    addressTextView = bottomSheetDialog.findViewById(R.id.location_bottom_sheet);
                    callButton = bottomSheetDialog.findViewById(R.id.call_bottom_button);
                    sendTextButton = bottomSheetDialog.findViewById(R.id.text_bottom_button);

                    statusTextView.setText(value.getStatus());
                    distanceTextView.setText("No Data");
                    phoneTextView.setText(value.getPhoneNumber());
                    addressTextView.setText(value.getAddress());

                    bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {

                        }
                    });
                    bottomSheetDialog.show();

/*
            Fragment fragment = requireActivity().getSupportFragmentManager().findFragmentByTag("dialog_playback" + latLng.toString());
                if(fragment != null)
                requireActivity().getSupportFragmentManager().beginTransaction().remove(fragment).commit();
*/
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
                        userLocationAndStatuses.add(userLocationAndStatus);
                        Log.i("SSSSSSSSSSSSSSSS", "THIS SHIT IS FIRING");
                    }

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
