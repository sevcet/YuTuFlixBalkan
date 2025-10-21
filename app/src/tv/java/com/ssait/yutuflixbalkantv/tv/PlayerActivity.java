package com.ssait.yutuflixbalkantv;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PlayerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        String url = getIntent().getStringExtra("VIDEO_URL");
        Toast.makeText(this, "Puštam: " + url, Toast.LENGTH_LONG).show();
        // ovde ćeš kasnije ubaciti ExoPlayer
    }
}