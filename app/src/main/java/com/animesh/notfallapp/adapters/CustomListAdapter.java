package com.animesh.notfallapp.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.animesh.notfallapp.R;
import com.animesh.notfallapp.commons.UserLocationAndStatus;

import java.util.List;

public class CustomListAdapter extends ArrayAdapter<UserLocationAndStatus> {
    private final Activity context;
    private final List<UserLocationAndStatus> userLocationAndStatuses;
    private final String DELIMITER = ",";


    public CustomListAdapter(@NonNull Activity context, int resource, @NonNull List<UserLocationAndStatus> objects) {
        super(context, resource, objects);
        this.context = context;
        this.userLocationAndStatuses = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.list_row, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.locationText =  rowView.findViewById(R.id.location_list);
            viewHolder.statusText = rowView.findViewById(R.id.status_list);
            viewHolder.phoneText = rowView.findViewById(R.id.phone_list);
            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        UserLocationAndStatus userLocationAndStatus = userLocationAndStatuses.get(position);

        if (userLocationAndStatus.getAddress()!= null && !userLocationAndStatus.getAddress().isEmpty()) {

            String address = userLocationAndStatus.getAddress();
            String[] segments = address.split(DELIMITER);

            if (segments.length == 3) {

                holder.locationText.setText(new StringBuilder().
                        append(segments[segments.length - 2]).append(DELIMITER).
                        append(segments[segments.length - 1]).toString());

            } else if (segments.length > 3){

                holder.locationText.setText(new StringBuilder().
                        append(segments[segments.length - 3]).append(DELIMITER).
                        append(segments[segments.length - 2]).append(DELIMITER).
                        append(segments[segments.length - 1]).toString());
            } else  {

                holder.locationText.setText(address);
            }


        } else {

            holder.locationText.setText("No data");
        }

        holder.locationText.setEnabled(false);

        if (userLocationAndStatus.getPhoneNumber()!= null && !userLocationAndStatus.getPhoneNumber().isEmpty()) {

            holder.phoneText.setText(userLocationAndStatus.getPhoneNumber().toString());

        } else {

            holder.phoneText.setText("No data");
            holder.phoneText.setEnabled(false);

        }

        if (userLocationAndStatus.getStatus()!= null && !userLocationAndStatus.getStatus().isEmpty()) {

            holder.statusText.setText(userLocationAndStatus.getStatus());

            if (holder.statusText.getText().toString().contains("OK")) {
                holder.statusText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_mood_ok_black_18dp, 0, 0, 0);

            } else if (holder.statusText.getText().toString().contains("Sick")) {
                holder.statusText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_sentiment_sad_18dp,0,0,0);

            } else if (holder.statusText.getText().toString().contains("need")) {
                holder.statusText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_accessibility_black_18dp,0,0,0);

            } else if (holder.statusText.getText().toString().contains("can")){
                holder.statusText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_support_black_18dp,0,0,0);

            }

            holder.statusText.setEnabled(false);

        } else {

            holder.statusText.setText("No data");
        }

        return rowView;
    }

    static class ViewHolder {

        public TextView statusText;
        public TextView locationText;
        public TextView phoneText;

    }

}
