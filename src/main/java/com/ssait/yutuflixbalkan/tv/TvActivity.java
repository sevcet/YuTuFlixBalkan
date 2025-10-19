package com.youtuflixbalkan.tv;

import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;

import com.ssait.yutuflixbalkan.R;

public class TvActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_browse_fragment, new com.youtuflixbalkan.tv.MainBrowseFragment())
                    .commit();
        }
    }
}
