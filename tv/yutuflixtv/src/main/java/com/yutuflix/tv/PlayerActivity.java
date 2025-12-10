package com.yutuflix.tv;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.util.Arrays;
import java.util.List;

public class PlayerActivity extends Activity {

    private WebView webView;
    private InterstitialAd interstitialAd;
    private boolean isAdLoaded = false;

    private String videoId;
    private String finalLang;

    private static final List<String> SUPPORTED_LANGS = Arrays.asList(
            "en","fr","de","es","it","pt",
            "nl","pl","cs","sk","sl","hr","bs","sr","mk","sq",
            "bg","ro","hu","tr","el","da","sv","fi","no","is",
            "et","lv","lt","uk","ru","ar","fa","hi","bn",
            "zh","ja","ko","vi","id","ms","th"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hideSystemUI();
        setContentView(R.layout.activity_player_tv);

        extractIntentData();
        resolveLanguage();
        initializeAdMob();
        setupWebView();
        loadPlayerHtml();
    }

    private void extractIntentData() {
        Intent intent = getIntent();
        videoId = intent.getStringExtra("videoId");

        if (videoId == null || videoId.isEmpty()) {
            Log.e("PLAYER", "Video ID missing");
            finish();
        }
    }

    private void resolveLanguage() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String userLang = prefs.getString("user_language", "en");

        if (userLang.equals("me")) finalLang = "hr";
        else if (SUPPORTED_LANGS.contains(userLang)) finalLang = userLang;
        else finalLang = "en";

        Log.d("PLAYER_LANG", "FINAL LANG = " + finalLang);
    }

    private void hideSystemUI() {
        View v = getWindow().getDecorView();
        v.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    private void setupWebView() {
        webView = findViewById(R.id.webView);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setUseWideViewPort(true);
        ws.setLoadWithOverviewMode(true);
        ws.setMediaPlaybackRequiresUserGesture(false);
        ws.setCacheMode(WebSettings.LOAD_NO_CACHE);
        ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        ws.setUserAgentString(
                "Mozilla/5.0 (Linux; Android TV) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36"
        );

        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setBackgroundColor(Color.BLACK);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        webView.postDelayed(() -> webView.requestFocus(), 200);
    }

    private void loadPlayerHtml() {
        String url =
                "https://sevcet.github.io/saittvl.html"
                        + "?videoId=" + videoId
                        + "&lang=" + finalLang;
        Log.d("PLAYER_URL", "Loading = " + url);
        webView.loadUrl(url);
    }

    private void initializeAdMob() {
        MobileAds.initialize(this, status -> loadAd());
    }

    private void loadAd() {
        AdRequest req = new AdRequest.Builder().build();
        InterstitialAd.load(this,
                "ca-app-pub-9646614878051651/4205297918",
                req,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(InterstitialAd ad) {
                        interstitialAd = ad;
                        isAdLoaded = true;
                    }
                });
    }

    private void showAdOrExit() {
        if (interstitialAd != null && isAdLoaded) interstitialAd.show(this);
        else finish();
    }

    @Override
    public void onBackPressed() {
        showAdOrExit();
    }

    // -------- DPAD HANDLING --------
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(webView != null){
            switch(keyCode){
                case KeyEvent.KEYCODE_DPAD_UP:
                    webView.evaluateJavascript("handleRemote('up');", null); return true;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    webView.evaluateJavascript("handleRemote('down');", null); return true;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    webView.evaluateJavascript("handleRemote('left');", null); return true;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    webView.evaluateJavascript("handleRemote('right');", null); return true;
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                    webView.evaluateJavascript("handleRemote('enter');", null); return true;
                case KeyEvent.KEYCODE_BACK:
                    webView.evaluateJavascript("handleRemote('back');", null); return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
        webView.requestFocus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try { webView.loadUrl("javascript:try{player.pauseVideo();}catch(e){}"); } catch (Exception ignored) {}
    }

    @Override
    protected void onDestroy() {
        if(webView != null){
            webView.loadUrl("about:blank");
            webView.removeAllViews();
            webView.destroy();
        }
        super.onDestroy();
    }
}
