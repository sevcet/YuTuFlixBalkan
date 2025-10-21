package com.ssait.yutuflixbalkantv.tv;

import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;

public class TvActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_browse_fragment, new MainBrowseFragment())
                .commit();
    }
}