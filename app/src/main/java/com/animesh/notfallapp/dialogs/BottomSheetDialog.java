package com.animesh.notfallapp.dialogs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.ActionMenuView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.animesh.notfallapp.R;
import com.animesh.notfallapp.commons.MapsDisplayItem;

public class BottomSheetDialog {

    private static EditText statusTextView;
    private static EditText distanceTextView;
    private static EditText phoneTextView;
    private static EditText addressTextView;
    private static Button callButton;
    private static Button sendTextButton;

    public static void showDialog(MapsDisplayItem mapsDisplayItem, Context context, Activity activity){
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog = new com.google.android.material.bottomsheet.BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet);

        statusTextView = bottomSheetDialog.findViewById(R.id.status_bottom_sheet);
        distanceTextView = bottomSheetDialog.findViewById(R.id.distance_bottom_sheet);
        phoneTextView = bottomSheetDialog.findViewById(R.id.phone_bottom_sheet);
        addressTextView = bottomSheetDialog.findViewById(R.id.location_bottom_sheet);
        callButton = bottomSheetDialog.findViewById(R.id.call_bottom_button);
        sendTextButton = bottomSheetDialog.findViewById(R.id.text_bottom_button);

        statusTextView.setText(mapsDisplayItem.getTitle());
        distanceTextView.setText(R.string.no_data);
        phoneTextView.setText(mapsDisplayItem.getPhoneNumber());
        addressTextView.setText(mapsDisplayItem.getAddress());

        callButton.setOnClickListener(v1 -> {

            if (phoneTextView.getText() != null && !phoneTextView.getText().toString().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneTextView.getText()));

                if (intent.resolveActivity(activity.getPackageManager()) != null) {
                    activity.startActivity(intent);
                }

            } else {
                Toast.makeText(context, R.string.no_phone, Toast.LENGTH_SHORT).show();
            }
        });

        sendTextButton.setOnClickListener(v2->{
            if (phoneTextView.getText() != null && !phoneTextView.getText().toString().isEmpty()) {

                Intent intent = new Intent(Intent.ACTION_VIEW,  Uri.parse("sms:" + phoneTextView.getText()));
                intent.putExtra("sms_body", R.string.hello_how_are);

                if (intent.resolveActivity(activity.getPackageManager()) != null) {
                    activity.startActivity(intent);
                }

            } else {

                Toast.makeText(context, R.string.no_phone, Toast.LENGTH_SHORT).show();

            }

        });

        bottomSheetDialog.show();
    }


}
