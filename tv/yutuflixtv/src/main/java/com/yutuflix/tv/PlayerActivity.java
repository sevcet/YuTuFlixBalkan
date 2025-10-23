package com.yutuflix.tv;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class PlayerActivity extends Activity {

    private WebView webView;
    private Handler hideHandler = new Handler();
    private static final int HIDE_DELAY = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_tv);

        // Sakrij sistem UI za TV
        hideSystemUI();

        setupWebView();
        loadVideo();
    }

    private void setupWebView() {
        webView = findViewById(R.id.webView);

        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setLoadWithOverviewMode(true);
        ws.setUseWideViewPort(true);
        ws.setMediaPlaybackRequiresUserGesture(false);

        // Dodaj JavaScript interfejs
        webView.addJavascriptInterface(new WebAppInterface(), "AndroidInterface");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                startHideTimer();
            }
        });

        // Fokus za TV
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        webView.requestFocus();
    }

    private void loadVideo() {
        Intent intent = getIntent();
        String videoUrl = intent.getStringExtra("videoUrl");
        String videoTitle = intent.getStringExtra("videoTitle");

        if (videoUrl != null && !videoUrl.isEmpty()) {
            // Koristi isti HTML kao mobilna verzija
            String htmlContent = createHtmlPlayer(videoUrl);
            webView.loadDataWithBaseURL("https://sevcet.github.io/", htmlContent, "text/html", "UTF-8", null);
        } else {
            Toast.makeText(this, "Error: No video URL", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private String createHtmlPlayer(String videoUrl) {
        return "<!DOCTYPE html>" +
                "<html lang=\"en\">" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<title>YouTube Player</title>" +
                "<style>" +
                "body, html { margin: 0; padding: 0; background: #000; overflow: hidden; width: 100%; height: 100%; }" +
                "#player { width: 100%; height: 100%; border: none; }" +
                "#closeBtn { " +
                "position: absolute; top: 50%; transform: translateY(-50%); right: 20px; " +
                "background: rgba(0,0,0,0.8); color: #fff; border: none; font-size: 24px; " +
                "border-radius: 50%; width: 50px; height: 50px; cursor: pointer; z-index: 10000; " +
                "transition: opacity 0.3s, background 0.3s; display: flex; align-items: center; " +
                "justify-content: center; box-shadow: 0 2px 10px rgba(0,0,0,0.5); font-weight: bold; }" +
                "#closeBtn:hover { background: rgba(255,0,0,0.9); transform: translateY(-50%) scale(1.1); }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<button id=\"closeBtn\" title=\"Zatvori\">✕</button>" +
                "<iframe id=\"player\" src=\"" + videoUrl + "?autoplay=1&rel=0&modestbranding=1&controls=1&showinfo=1&fs=1\" " +
                "frameborder=\"0\" allow=\"autoplay; encrypted-media\" allowfullscreen></iframe>" +
                "<script>" +
                "let hideTimeout;" +
                "function hideCloseButton() {" +
                "   const closeBtn = document.getElementById('closeBtn');" +
                "   if (closeBtn) closeBtn.style.opacity = '0';" +
                "}" +
                "function showCloseButton() {" +
                "   const closeBtn = document.getElementById('closeBtn');" +
                "   if (closeBtn) {" +
                "       closeBtn.style.opacity = '1';" +
                "       clearTimeout(hideTimeout);" +
                "       hideTimeout = setTimeout(hideCloseButton, 10000);" +
                "   }" +
                "}" +
                "document.addEventListener('click', function(event) {" +
                "   if (event.target.id !== 'closeBtn') showCloseButton();" +
                "});" +
                "document.getElementById('closeBtn').addEventListener('click', function() {" +
                "   if (window.AndroidInterface) AndroidInterface.closePlayer();" +
                "   else window.history.back();" +
                "});" +
                "document.addEventListener('keydown', function(event) {" +
                "   showCloseButton();" +
                "   if (event.key === 'Backspace' || event.key === 'Escape') {" +
                "       if (window.AndroidInterface) AndroidInterface.closePlayer();" +
                "   }" +
                "});" +
                "setTimeout(hideCloseButton, 10000);" +
                "showCloseButton();" +
                "</script>" +
                "</body>" +
                "</html>";
    }

    public class WebAppInterface {
        @JavascriptInterface
        public void closePlayer() {
            runOnUiThread(() -> {
                finish();
            });
        }
    }

    // TV KEY EVENTS
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_ESCAPE:
            case KeyEvent.KEYCODE_B:
                finish();
                return true;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                // Pokaži close button kada se pritisne enter/center
                webView.loadUrl("javascript:showCloseButton()");
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void startHideTimer() {
        hideHandler.removeCallbacks(hideRunnable);
        hideHandler.postDelayed(hideRunnable, HIDE_DELAY);
    }

    private final Runnable hideRunnable = new Runnable() {
        @Override
        public void run() {
            webView.loadUrl("javascript:hideCloseButton()");
        }
    };

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideHandler.removeCallbacks(hideRunnable);
        // Pauziraj video
        webView.loadUrl("javascript:document.getElementById('player').contentWindow.postMessage('{\"event\":\"command\",\"func\":\"pauseVideo\",\"args\":\"\"}', '*')");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideHandler.removeCallbacks(hideRunnable);
        if (webView != null) {
            webView.loadUrl("about:blank");
            webView.destroy();
        }
    }
}