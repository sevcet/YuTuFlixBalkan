package com.yutuflix.tv;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class PlayerActivity extends Activity {

    private WebView webView;
    private InterstitialAd interstitialAd;
    private boolean isAdLoaded = false;
    private boolean isBackPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_tv);

        // Initialize AdMob
        initializeAdMob();

        // Sakrij status bar i navigation bar
        hideSystemUI();

        setupWebView();
        loadVideo();
    }

    private void initializeAdMob() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Log.d("ADMOB", "AdMob initialized");
                loadInterstitialAd();
            }
        });
    }

    private void loadInterstitialAd() {
        // Test interstitial ad unit ID
        String adUnitId = "ca-app-pub-9646614878051651/4205297918";

        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this, adUnitId, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(InterstitialAd interstitial) {
                Log.d("ADMOB", "Interstitial ad loaded");
                interstitialAd = interstitial;
                isAdLoaded = true;

                // Set ad events
                interstitialAd.setFullScreenContentCallback(new com.google.android.gms.ads.FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        Log.d("ADMOB", "Interstitial ad dismissed");
                        // Continue with app flow after ad is dismissed
                        if (isBackPressed) {
                            finish();
                        }
                        // Load next interstitial ad
                        loadInterstitialAd();
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {
                        Log.e("ADMOB", "Interstitial ad failed to show: " + adError.getMessage());
                        if (isBackPressed) {
                            finish();
                        }
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        Log.d("ADMOB", "Interstitial ad showed");
                        interstitialAd = null;
                        isAdLoaded = false;
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(com.google.android.gms.ads.LoadAdError loadAdError) {
                Log.e("ADMOB", "Interstitial ad failed to load: " + loadAdError.getMessage());
                interstitialAd = null;
                isAdLoaded = false;
            }
        });
    }

    private void showInterstitialAd() {
        if (interstitialAd != null && isAdLoaded) {
            Log.d("ADMOB", "Showing interstitial ad");
            interstitialAd.show(this);
        } else {
            Log.d("ADMOB", "Interstitial ad not ready, finishing activity");
            finish();
        }
    }

    private void setupWebView() {
        webView = findViewById(R.id.webView);

        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setLoadWithOverviewMode(true);
        ws.setUseWideViewPort(true);
        ws.setMediaPlaybackRequiresUserGesture(false);
        ws.setAllowFileAccess(true);
        ws.setAllowContentAccess(true);
        ws.setCacheMode(WebSettings.LOAD_NO_CACHE);

        // Omogući fullscreen
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
                // Fullscreen mode
            }

            @Override
            public void onHideCustomView() {
                super.onHideCustomView();
                // Izlazak iz fullscreen
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d("PlayerActivity", "Page loaded: " + url);

                // Sakrij sve browser kontrole
                hideBrowserControls();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Uvek otvori u našem WebView-u
                return false;
            }
        });

        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        webView.requestFocus();
    }

    private void loadVideo() {
        Intent intent = getIntent();

        // DEBUG: Proveri sve extra podatke
        Log.d("PLAYER_DEBUG", "=== INTENT EXTRAS ===");
        if (intent != null && intent.getExtras() != null) {
            Bundle extras = intent.getExtras();
            for (String key : extras.keySet()) {
                Log.d("PLAYER_DEBUG", key + " = " + extras.get(key));
            }
        }

        String videoId = null;
        String videoUrl = null;

        // Prvo probaj sa videoUrl
        if (intent.hasExtra("videoUrl")) {
            videoUrl = intent.getStringExtra("videoUrl");
            Log.d("PLAYER_DEBUG", "Found videoUrl from intent: " + videoUrl);
            videoId = extractVideoIdFromUrl(videoUrl);
        }
        // Probaj direktno sa videoId
        else if (intent.hasExtra("videoId")) {
            videoId = intent.getStringExtra("videoId");
            Log.d("PLAYER_DEBUG", "Found videoId from intent: " + videoId);
        }

        if (videoId != null && !videoId.isEmpty()) {
            String externalUrl = "https://sevcet.github.io/saittv.html?videoId=" + videoId;
            Log.d("PLAYER_DEBUG", "Loading TV HTML URL: " + externalUrl);
            webView.loadUrl(externalUrl);
        } else {
            Log.e("PLAYER_DEBUG", "No valid videoId found");
            finish();
        }
    }

    private String extractVideoIdFromUrl(String url) {
        if (url == null) return null;

        Log.d("PLAYER_DEBUG", "Extracting videoId from: " + url);
        String videoId = null;

        if (url.contains("youtube.com/embed/")) {
            String[] split = url.split("embed/");
            if (split.length > 1) {
                videoId = split[1];
                int questionIndex = videoId.indexOf('?');
                if (questionIndex != -1) {
                    videoId = videoId.substring(0, questionIndex);
                }
            }
        }
        else if (url.contains("youtube.com/watch?v=")) {
            String[] split = url.split("v=");
            if (split.length > 1) {
                videoId = split[1];
                int ampersandIndex = videoId.indexOf('&');
                if (ampersandIndex != -1) {
                    videoId = videoId.substring(0, ampersandIndex);
                }
            }
        }
        else if (url.contains("youtu.be/")) {
            String[] split = url.split("youtu.be/");
            if (split.length > 1) {
                videoId = split[1];
                int questionIndex = videoId.indexOf('?');
                if (questionIndex != -1) {
                    videoId = videoId.substring(0, questionIndex);
                }
            }
        }
        else if (url.length() == 11 && !url.contains("/") && !url.contains("?")) {
            videoId = url;
        }

        Log.d("PLAYER_DEBUG", "Extracted videoId: " + videoId);
        return videoId;
    }

    private void hideBrowserControls() {
        // JavaScript koji sakriva sve browser elemente
        String hideScript = "javascript:(function() { " +
                "document.body.style.margin = '0'; " +
                "document.body.style.padding = '0'; " +
                "if(document.documentElement) { " +
                "   document.documentElement.style.margin = '0'; " +
                "   document.documentElement.style.padding = '0'; " +
                "} " +
                "var elements = document.querySelectorAll('header, footer, nav, .toolbar, .navbar'); " +
                "for(var i = 0; i < elements.length; i++) { " +
                "   elements[i].style.display = 'none'; " +
                "} " +
                "})()";

        webView.loadUrl(hideScript);
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) {
            webView.loadUrl("javascript:if(window.player) player.pauseVideo();");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.loadUrl("about:blank");
            webView.destroy();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    @Override
    public void onBackPressed() {
        Log.d("ADMOB", "Back pressed, showing interstitial ad");
        isBackPressed = true;

        // Pause video before showing ad
        if (webView != null) {
            webView.loadUrl("javascript:if(window.player) player.pauseVideo();");
        }

        showInterstitialAd();
    }
}