package com.animesh.notfallapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.animesh.notfallapp.R;

public class AboutUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        Button contactButton = findViewById(R.id.contact_about_us);

        contactButton.setOnClickListener(v -> {

            String[] email_address = new String[] {getString(R.string.an571sha_htwg_kosntanz_de)};
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", getString(R.string.an571sha_htwg_kosntanz_de), null));
            intent.putExtra(Intent.EXTRA_EMAIL, email_address);
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            startActivity(Intent.createChooser(intent, "Send Email"));

        });

    }
}
