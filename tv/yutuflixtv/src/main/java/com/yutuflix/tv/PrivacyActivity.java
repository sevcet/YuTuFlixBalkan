package com.yutuflix.tv;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

public class PrivacyActivity extends Activity {

    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_tv);

        btnBack = findViewById(R.id.btnBack);
        ScrollView scrollView = findViewById(R.id.scrollView);
        TextView privacyText = findViewById(R.id.privacyText);

        // Postavi tekst o privatnosti
        privacyText.setText(
                "Politika Privatnosti YuTuFlix TV\n\n" +
                        "Ova aplikacija poštuje vašu privatnost i ne prikuplja lične podatke.\n\n" +
                        "1. Podaci koje prikupljamo:\n" +
                        "   - Aplikacija ne prikuplja nikakve lične podatke\n" +
                        "   - Svi video zapisi se reprodukuju direktno sa YouTube servera\n" +
                        "   - Lokalno se čuvaju samo vaši omiljeni sadržaji\n\n" +
                        "2. Korišćenje podataka:\n" +
                        "   - Nemamo pristup vašim ličnim podacima\n" +
                        "   - Ne delimo podatke sa trećim stranama\n" +
                        "   - Svi metapodaci se učitavaju sa eksternih XML izvora\n\n" +
                        "3. YouTube uslovi korišćenja:\n" +
                        "   - Aplikacija koristi YouTube embed API\n" +
                        "   - Korišćenjem aplikacije prihvatate YouTube uslove korišćenja\n\n" +
                        "4. Kontakt:\n" +
                        "   Za sva pitanja o privatnosti kontaktirajte nas putem Google Play Store."
        );

        // Dugme za nazad
        btnBack.setOnClickListener(v -> {
            finish();
        });
    }
}