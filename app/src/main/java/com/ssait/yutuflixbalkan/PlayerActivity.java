package com.ssait.yutuflixbalkan;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class PlayerActivity extends AppCompatActivity {

    private WebView webView;
    private Handler hideHandler = new Handler();
    private static final int HIDE_DELAY = 10000;
    private InterstitialAd mInterstitialAd;
    private boolean adShown = false;
    private boolean isClosing = false;

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_player);
        hideSystemUI();

        setupBackPressedCallback();

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                loadInterstitialAd();
            }
        });

        webView = findViewById(R.id.webViewPlayer);

        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setLoadWithOverviewMode(true);
        ws.setUseWideViewPort(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            ws.setMediaPlaybackRequiresUserGesture(false);
        }

        webView.addJavascriptInterface(new WebAppInterface(), "AndroidInterface");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                startHideTimer();
            }
        });

        String videoId = getIntent().getStringExtra("videoId");
        if (videoId == null) videoId = "";
        String url = "https://sevcet.github.io/sait.html?videoId=" + videoId + "&autoplay=1";

        webView.loadUrl(url);
    }

    private void setupBackPressedCallback() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                stopVideoAndSound();
                showInterstitialAd();
            }
        });
    }

    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this, "ca-app-pub-9646614878051651/2623469616", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;

                        interstitialAd.setFullScreenContentCallback(new com.google.android.gms.ads.FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                mInterstitialAd = null;
                                returnToPreviousActivity();
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {
                                mInterstitialAd = null;
                                returnToPreviousActivity();
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(com.google.android.gms.ads.LoadAdError loadAdError) {
                        mInterstitialAd = null;
                    }
                });
    }

    public class WebAppInterface {
        @JavascriptInterface
        public void closePlayer() {
            runOnUiThread(() -> {
                stopVideoAndSound();
                showInterstitialAd();
            });
        }
    }

    private void showInterstitialAd() {
        if (isClosing) {
            return;
        }

        if (!adShown && mInterstitialAd != null) {
            adShown = true;
            mInterstitialAd.show(PlayerActivity.this);
        } else {
            returnToPreviousActivity();
        }
    }

    private void returnToPreviousActivity() {
        if (isClosing) {
            return;
        }
        isClosing = true;

        stopVideoAndSound();

        if (webView != null) {
            webView.loadUrl("about:blank");
        }

        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void stopVideoAndSound() {
        if (webView != null) {
            String stopScript = "javascript:(" +
                    "function() {" +
                    "   try {" +
                    "       var iframe = document.querySelector('iframe');" +
                    "       if (iframe) {" +
                    "           iframe.src = '';" +
                    "       }" +
                    "       " +
                    "       var player = document.getElementById('movie_player');" +
                    "       if (player) {" +
                    "           player.contentWindow.postMessage('{\"event\":\"command\",\"func\":\"pauseVideo\",\"args\":\"\"}', '*');" +
                    "       }" +
                    "       " +
                    "       var audioElements = document.getElementsByTagName('audio');" +
                    "       for (var i = 0; i < audioElements.length; i++) {" +
                    "           audioElements[i].pause();" +
                    "           audioElements[i].currentTime = 0;" +
                    "       }" +
                    "       " +
                    "       var videoElements = document.getElementsByTagName('video');" +
                    "       for (var i = 0; i < videoElements.length; i++) {" +
                    "           videoElements[i].pause();" +
                    "           videoElements[i].currentTime = 0;" +
                    "       }" +
                    "   } catch (e) {" +
                    "   }" +
                    "}" +
                    ")()";

            webView.loadUrl(stopScript);
            webView.onPause();
        }
    }

    private void startHideTimer() {
        hideHandler.removeCallbacks(hideRunnable);
        hideHandler.postDelayed(hideRunnable, HIDE_DELAY);
    }

    private final Runnable hideRunnable = new Runnable() {
        @Override
        public void run() {
            String hideScript = "javascript:(" +
                    "function() {" +
                    "   var closeBtn = document.getElementById('closeBtn');" +
                    "   if(closeBtn) closeBtn.style.opacity = '0';" +
                    "}" +
                    ")()";
            webView.loadUrl(hideScript);
        }
    };

    private void showCloseButton() {
        String showScript = "javascript:(" +
                "function() {" +
                "   var closeBtn = document.getElementById('closeBtn');" +
                "   if(closeBtn) closeBtn.style.opacity = '1';" +
                "}" +
                ")()";
        webView.loadUrl(showScript);
        startHideTimer();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            showCloseButton();
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            showCloseButton();
        }
        return super.dispatchTouchEvent(ev);
    }

    private void hideSystemUI() {
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopVideoAndSound();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideHandler.removeCallbacks(hideRunnable);
        stopVideoAndSound();
        if (webView != null) {
            webView.loadUrl("about:blank");
            webView.destroy();
        }
    }
}