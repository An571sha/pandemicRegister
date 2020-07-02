package com.animesh.notfallapp.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.animesh.notfallapp.R;
import com.animesh.notfallapp.commons.MapsDisplayItem;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;

import java.util.Objects;

import static java.net.Proxy.Type.HTTP;

public class BottomSheetFragment extends BottomSheetDialogFragment {
    private static final String MY_KEY = "my_key";
    private EditText statusTextView;
    private EditText distanceTextView;
    private EditText phoneTextView;
    private EditText addressTextView;
    private Button callButton;
    private Button sendTextButton;


    public static BottomSheetFragment newInstance(){
        return new BottomSheetFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.bottom_sheet, container, false);

        statusTextView = v.findViewById(R.id.status_bottom_sheet);
        distanceTextView = v.findViewById(R.id.distance_bottom_sheet);
        phoneTextView = v.findViewById(R.id.phone_bottom_sheet);
        addressTextView = v.findViewById(R.id.location_bottom_sheet);
        callButton = v.findViewById(R.id.call_bottom_button);
        sendTextButton = v.findViewById(R.id.text_bottom_button);
        statusTextView.setEnabled(false);
        distanceTextView.setEnabled(false);
        phoneTextView.setEnabled(false);
        addressTextView.setEnabled(false);


        callButton.setOnClickListener(v1 -> {

            if (phoneTextView.getText() != null && !phoneTextView.getText().toString().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneTextView.getText()));

                if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                    startActivity(intent);
                }

            } else {
                Toast.makeText(getContext(), "No Phone number detected", Toast.LENGTH_SHORT).show();
            }
        });

        sendTextButton.setOnClickListener(v2->{
            if (phoneTextView.getText() != null && !phoneTextView.getText().toString().isEmpty()) {

                Intent intent = new Intent(Intent.ACTION_VIEW,  Uri.parse("sms:" + phoneTextView.getText()));
                intent.putExtra("sms_body", "MESSAGE");

                if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                    startActivity(intent);
                }

            } else {

                Toast.makeText(getContext(), "No Phone number detected", Toast.LENGTH_SHORT).show();

            }

        });


        return v;
    }
}

