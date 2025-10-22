package com.yutuflix.tv;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class PlayerActivity extends Activity {

    private VideoView videoView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_tv);

        videoView = findViewById(R.id.videoView);
        progressBar = findViewById(R.id.progressBar);

        // Dobij URL iz intenta
        Intent intent = getIntent();
        String videoUrl = intent.getStringExtra("videoUrl");
        String videoTitle = intent.getStringExtra("videoTitle");

        if (videoUrl != null && !videoUrl.isEmpty()) {
            setupVideoPlayer(videoUrl);
        } else {
            Toast.makeText(this, "Error: No video URL", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupVideoPlayer(String videoUrl) {
        progressBar.setVisibility(View.VISIBLE);

        try {
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);

            Uri videoUri = Uri.parse(videoUrl);
            videoView.setVideoURI(videoUri);

            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    progressBar.setVisibility(View.GONE);
                    videoView.start();
                }
            });

            videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(PlayerActivity.this, "Video playback error", Toast.LENGTH_LONG).show();
                    finish();
                    return true;
                }
            });

        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Error setting up video player", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView != null) {
            videoView.stopPlayback();
        }
    }
}