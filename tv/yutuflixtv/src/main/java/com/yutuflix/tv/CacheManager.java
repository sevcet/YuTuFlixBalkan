package com.yutuflix.tv;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CacheManager {
    private Context context;
    private SharedPreferences cachePrefs;

    public CacheManager(Context context) {
        this.context = context;
        this.cachePrefs = context.getSharedPreferences("xml_cache", Context.MODE_PRIVATE);
    }

    // PREUZMI I FILTRIRAJ XML - PROVERA NA SVAKOM POKRETANJU (MIN 8H)
    public String getFilteredXml(String xmlUrl, String cacheKey, SharedPreferences blockedVideosPrefs) {
        // PROVERI DA LI TREBA AŽURIRATI (MIN 8 ČASA)
        boolean shouldUpdate = shouldUpdateCache(cacheKey);

        // PRVO PROVERI DA LI IMAMO MODIFIKOVANI XML
        if (!shouldUpdate) {
            String filteredXml = readFromCache(cacheKey + "_filtered");
            if (filteredXml != null) {
                Log.d("CacheManager", "Koristim filtriran XML za: " + cacheKey);
                return filteredXml;
            }
        }

        // AŽURIRAJ XML
        Log.d("CacheManager", "Ažuriram i filtriram XML za: " + cacheKey);
        try {
            // PREUZMI NOVI XML SA GITHUB-A
            Document originalDoc = Jsoup.connect(xmlUrl)
                    .timeout(30000)
                    .parser(org.jsoup.parser.Parser.xmlParser())
                    .get();

            // PROČITAJ POSTOJEĆI FILTRIRANI XML (AKO POSTOJI)
            Document filteredDoc = null;
            String existingFilteredXml = readFromCache(cacheKey + "_filtered");
            if (existingFilteredXml != null) {
                filteredDoc = Jsoup.parse(existingFilteredXml);
                Log.d("CacheManager", "Koristim postojeći filtriran XML kao osnovu");
            }

            // AKO NEMA POSTOJEĆEG, KREIRAJ NOVI FILTRIRANI XML
            if (filteredDoc == null) {
                filteredDoc = originalDoc.clone();
                // INICIJALNO FILTRIRANJE - UKLONI SVE BLOKIRANE FILMOVE
                initialFiltering(filteredDoc, blockedVideosPrefs);
                Log.d("CacheManager", "Kreiran novi filtriran XML");
            }

            // AŽURIRAJ SA NOVIM FILMOVIMA KOJI NEMAJU ISTI VIDEO ID
            int newVideosAdded = updateWithNewMovies(originalDoc, filteredDoc, blockedVideosPrefs);

            String filteredXml = filteredDoc.toString();

            // TRAJNO SAČUVAJ U CACHE
            saveToCachePermanent(cacheKey + "_filtered", filteredXml);

            // SAČUVAJ VREME POSLEDNJEG AŽURIRANJA
            updateCacheTime(cacheKey);

            Log.d("CacheManager", "XML uspešno ažuriran. Novih filmova: " + newVideosAdded);
            return filteredXml;

        } catch (Exception e) {
            Log.e("CacheManager", "Greška pri ažuriranju XML: " + e.getMessage());
            // POKUŠAJ DA KORISTIŠ POSTOJEĆI FILTRIRANI XML
            String existingXml = readFromCache(cacheKey + "_filtered");
            if (existingXml != null) {
                return existingXml;
            }
            return "";
        }
    }

    // TRAJNO ČUVANJE U CACHE-U - OVAJ CACHE SE NE BRIŠE
    private void saveToCachePermanent(String cacheKey, String xmlContent) {
        try {
            File cacheFile = new File(context.getFilesDir(), cacheKey + ".xml");
            FileWriter writer = new FileWriter(cacheFile);
            writer.write(xmlContent);
            writer.close();

            Log.d("CacheManager", "XML TRAJNO sačuvan: " + cacheKey);
        } catch (IOException e) {
            Log.e("CacheManager", "Greška pri čuvanju XML: " + e.getMessage());
        }
    }

    // INICIJALNO FILTRIRANJE - UKLONI SVE BLOKIRANE FILMOVE
    private void initialFiltering(Document doc, SharedPreferences blockedVideosPrefs) {
        // UKLONI BLOKIRANE FILMOVE IZ GLAVNIH KATEGORIJA (movies > category)
        Elements mainCategories = doc.select("movies > category");
        for (Element category : mainCategories) {
            removeBlockedMoviesFromCategory(category, blockedVideosPrefs);

            // UKLONI PRAZNE KATEGORIJE
            if (category.select("movie").isEmpty()) {
                category.remove();
            }
        }

        // UKLONI BLOKIRANE FILMOVE IZ POJEDINAČNIH KATEGORIJA (category)
        Elements singleCategories = doc.select("category");
        for (Element category : singleCategories) {
            removeBlockedMoviesFromCategory(category, blockedVideosPrefs);

            // UKLONI PRAZNE KATEGORIJE
            if (category.select("movie").isEmpty()) {
                category.remove();
            }
        }
    }

    // UKLONI BLOKIRANE FILMOVE IZ KATEGORIJE
    private void removeBlockedMoviesFromCategory(Element category, SharedPreferences blockedVideosPrefs) {
        Elements movies = category.select("movie");
        List<Element> moviesToRemove = new ArrayList<>();

        for (Element movie : movies) {
            String videoId = getVideoIdFromMovie(movie);

            // UKLONI CEO MOVIE ELEMENT AKO JE VIDEO BLOKIRAN
            if (isVideoBlocked(videoId, blockedVideosPrefs)) {
                moviesToRemove.add(movie);
                Log.d("CacheManager", "Uklonjen blokiran film: " + getMovieTitle(movie));
            } else {
                // PROVERI DOSTUPNOST I BLOKIRAJ AKO NIJE DOSTUPAN
                boolean isAvailable = checkAndBlockVideo(videoId, blockedVideosPrefs);
                if (!isAvailable) {
                    moviesToRemove.add(movie);
                    Log.d("CacheManager", "Uklonjen nedostupan film: " + getMovieTitle(movie));
                }
            }
        }

        // UKLONI BLOKIRANE FILMOVE
        for (Element movieToRemove : moviesToRemove) {
            movieToRemove.remove();
        }
    }

    // AŽURIRAJ FILTRIRANI XML SA NOVIM FILMOVIMA KOJI NEMAJU ISTI VIDEO ID
    private int updateWithNewMovies(Document originalDoc, Document filteredDoc, SharedPreferences blockedVideosPrefs) {
        int newVideosAdded = 0;

        // PRIKUPI SVE VIDEO ID-JEVE IZ FILTRIRANOG XML-A
        Set<String> existingVideoIds = getAllVideoIdsFromDocument(filteredDoc);

        // PROVERI GLAVNE KATEGORIJE (movies > category)
        Elements originalMainCategories = originalDoc.select("movies > category");
        Elements filteredMainCategories = filteredDoc.select("movies > category");

        for (Element originalCat : originalMainCategories) {
            String catName = originalCat.attr("name");
            Element filteredCat = findCategoryByName(filteredMainCategories, catName);

            if (filteredCat == null) {
                // DODAJ NOVU KATEGORIJU
                Element moviesElement = filteredDoc.select("movies").first();
                if (moviesElement != null) {
                    moviesElement.appendChild(originalCat.clone());
                    filteredCat = findCategoryByName(filteredDoc.select("movies > category"), catName);
                    Log.d("CacheManager", "Dodata nova kategorija: " + catName);
                }
            }

            if (filteredCat != null) {
                // DODAJ NOVE FILMOVE U KATEGORIJU
                newVideosAdded += addNewMoviesToCategory(originalCat, filteredCat, existingVideoIds, blockedVideosPrefs);
            }
        }

        // PROVERI POJEDINAČNE KATEGORIJE (category)
        Elements originalSingleCategories = originalDoc.select("category");
        Elements filteredSingleCategories = filteredDoc.select("category");

        for (Element originalCat : originalSingleCategories) {
            String catName = originalCat.attr("name");
            if (catName.isEmpty()) continue;

            Element filteredCat = findCategoryByName(filteredSingleCategories, catName);

            if (filteredCat == null) {
                // DODAJ NOVU KATEGORIJU
                filteredDoc.body().appendChild(originalCat.clone());
                filteredCat = findCategoryByName(filteredDoc.select("category"), catName);
                Log.d("CacheManager", "Dodata nova pojedinačna kategorija: " + catName);
            }

            if (filteredCat != null) {
                // DODAJ NOVE FILMOVE U KATEGORIJU
                newVideosAdded += addNewMoviesToCategory(originalCat, filteredCat, existingVideoIds, blockedVideosPrefs);
            }
        }

        return newVideosAdded;
    }

    // DODAJ NOVE FILMOVE U KATEGORIJU KOJI NEMAJU ISTI VIDEO ID
    private int addNewMoviesToCategory(Element originalCat, Element filteredCat, Set<String> existingVideoIds, SharedPreferences blockedVideosPrefs) {
        int newVideosAdded = 0;
        Elements originalMovies = originalCat.select("movie");

        for (Element originalMovie : originalMovies) {
            String videoId = getVideoIdFromMovie(originalMovie);

            // PRESKOČI AKO VIDEO ID VEĆ POSTOJI U FILTRIRANOM XML-U
            if (existingVideoIds.contains(videoId)) {
                continue;
            }

            // PRESKOČI BLOKIRANE VIDEO ID-JEVE
            if (isVideoBlocked(videoId, blockedVideosPrefs)) {
                continue;
            }

            // PROVERI DOSTUPNOST NOVOG VIDEO ZAPISA
            boolean isAvailable = checkAndBlockVideo(videoId, blockedVideosPrefs);
            if (!isAvailable) {
                // BLOKIRAJ VIDEO I PRESKOČI DODAVANJE
                Log.d("CacheManager", "Preskočen nedostupan film: " + getMovieTitle(originalMovie));
                continue;
            }

            // DODAJ NOVI FILM (CEO MOVIE ELEMENT)
            filteredCat.appendChild(originalMovie.clone());
            newVideosAdded++;

            Log.d("CacheManager", "Dodat novi film: " + getMovieTitle(originalMovie) + " (VideoID: " + videoId + ")");
        }

        return newVideosAdded;
    }

    // PRIKUPI SVE VIDEO ID-JEVE IZ DOKUMENTA
    private Set<String> getAllVideoIdsFromDocument(Document doc) {
        Set<String> videoIds = new HashSet<>();

        // VIDEO ID-JEVI IZ GLAVNIH KATEGORIJA
        Elements mainMovies = doc.select("movies > category > movie");
        for (Element movie : mainMovies) {
            String videoId = getVideoIdFromMovie(movie);
            if (videoId != null && !videoId.isEmpty()) {
                videoIds.add(videoId);
            }
        }

        // VIDEO ID-JEVI IZ POJEDINAČNIH KATEGORIJA
        Elements singleMovies = doc.select("category > movie");
        for (Element movie : singleMovies) {
            String videoId = getVideoIdFromMovie(movie);
            if (videoId != null && !videoId.isEmpty()) {
                videoIds.add(videoId);
            }
        }

        return videoIds;
    }

    // DOBAVI VIDEO ID IZ MOVIE ELEMENTA
    private String getVideoIdFromMovie(Element movie) {
        Element videoIdElement = movie.selectFirst("videoId");
        return videoIdElement != null ? videoIdElement.text() : "";
    }

    // DOBAVI NASLOV FILMA
    private String getMovieTitle(Element movie) {
        Element titleElement = movie.selectFirst("title");
        return titleElement != null ? titleElement.text() : "Bez naslova";
    }

    // PRONAĐI KATEGORIJU PO IMENU
    private Element findCategoryByName(Elements categories, String name) {
        for (Element category : categories) {
            if (category.attr("name").equals(name)) {
                return category;
            }
        }
        return null;
    }

    // PROVERI DA LI JE VIDEO BLOKIRAN
    private boolean isVideoBlocked(String videoId, SharedPreferences blockedVideosPrefs) {
        if (videoId == null || videoId.isEmpty()) {
            return true; // UKLONI FILMOVE BEZ VIDEO ID-JA
        }
        return blockedVideosPrefs.getBoolean(videoId, false);
    }

    // PROVERI DOSTUPNOST VIDEA I BLOKIRAJ GA AKO NIJE DOSTUPAN
    public boolean checkAndBlockVideo(String videoId, SharedPreferences blockedVideosPrefs) {
        if (videoId == null || videoId.isEmpty()) {
            return false;
        }

        // PRVO PROVERI DA LI JE VEĆ BLOKIRAN
        if (blockedVideosPrefs.getBoolean(videoId, false)) {
            return false;
        }

        try {
            // Provera YouTube dostupnosti
            String youtubeUrl = "https://www.youtube.com/oembed?url=http://www.youtube.com/watch?v=" + videoId + "&format=json";
            int responseCode = Jsoup.connect(youtubeUrl)
                    .ignoreContentType(true)
                    .timeout(10000)
                    .execute()
                    .statusCode();

            boolean isAvailable = (responseCode == 200);

            // AKO JE VIDEO NEDOSTUPAN, BLOKIRAJ GA
            if (!isAvailable) {
                SharedPreferences.Editor editor = blockedVideosPrefs.edit();
                editor.putBoolean(videoId, true);
                editor.apply();
                Log.d("CacheManager", "Video blokiran: " + videoId);
            }

            return isAvailable;
        } catch (Exception e) {
            Log.e("CacheManager", "Greška pri proveri videa: " + videoId);
            return false;
        }
    }

    // PROVERI DA LI TREBA AŽURIRATI KEŠ (MIN 8 ČASA)
    public boolean shouldUpdateCache(String cacheKey) {
        long lastUpdate = cachePrefs.getLong(cacheKey + "_update_time", 0);
        long currentTime = System.currentTimeMillis();
        long eightHours = 8 * 60 * 60 * 1000; // 8 časa u milisekundama

        // AŽURIRAJ NA SVAKOM POKRETANJU, ALI NE ČEŠĆE OD 8 ČASA
        boolean shouldUpdate = (currentTime - lastUpdate) >= eightHours;

        if (shouldUpdate) {
            Log.d("CacheManager", "Vreme za ažuriranje keša: " + cacheKey);
        } else {
            Log.d("CacheManager", "Koristim keširan XML: " + cacheKey);
        }

        return shouldUpdate;
    }

    // SAČUVAJ XML U INTERNU MEMORIJU APLIKACIJE
    private void saveToCache(String cacheKey, String xmlContent) {
        try {
            File cacheFile = new File(context.getFilesDir(), cacheKey + ".xml");
            FileWriter writer = new FileWriter(cacheFile);
            writer.write(xmlContent);
            writer.close();

            Log.d("CacheManager", "XML sačuvan u internu memoriju: " + cacheKey);
        } catch (IOException e) {
            Log.e("CacheManager", "Greška pri čuvanju XML: " + e.getMessage());
        }
    }

    // PROČITAJ XML IZ INTERNE MEMORIJE APLIKACIJE
    public String readFromCache(String cacheKey) {
        try {
            File cacheFile = new File(context.getFilesDir(), cacheKey + ".xml");
            if (cacheFile.exists()) {
                Document doc = Jsoup.parse(cacheFile, "UTF-8");
                Log.d("CacheManager", "XML pročitan iz interne memorije: " + cacheKey);
                return doc.toString();
            }
        } catch (Exception e) {
            Log.e("CacheManager", "Greška pri čitanju XML: " + e.getMessage());
        }
        return null;
    }

    // AŽURIRAJ VREME KEŠA
    private void updateCacheTime(String cacheKey) {
        SharedPreferences.Editor editor = cachePrefs.edit();
        editor.putLong(cacheKey + "_update_time", System.currentTimeMillis());
        editor.apply();
    }

    // OČISTI KEŠ ZA ODREDJENI KLJUČ
    public void clearCacheForKey(String cacheKey) {
        try {
            // Obriši filtriranu verziju
            File filteredFile = new File(context.getFilesDir(), cacheKey + "_filtered.xml");
            if (filteredFile.exists()) {
                boolean deleted = filteredFile.delete();
                Log.d("CacheManager", "Filtrirani cache obrisan za " + cacheKey + ": " + deleted);
            }

            // Obriši originalnu verziju (ako postoji)
            File originalFile = new File(context.getFilesDir(), cacheKey + ".xml");
            if (originalFile.exists()) {
                originalFile.delete();
            }

            // Obriši vreme ažuriranja
            SharedPreferences.Editor editor = cachePrefs.edit();
            editor.remove(cacheKey + "_update_time");
            editor.apply();

            Log.d("CacheManager", "Potpuno očišćen cache za: " + cacheKey);

        } catch (Exception e) {
            Log.e("CacheManager", "Greška pri čišćenju cache: " + e.getMessage());
        }
    }

    // FORSIRAJ AŽURIRANJE KEŠA
    public void forceCacheUpdate(String cacheKey) {
        SharedPreferences.Editor editor = cachePrefs.edit();
        editor.remove(cacheKey + "_update_time");
        editor.apply();
        Log.d("CacheManager", "Forsirano ažuriranje keša: " + cacheKey);
    }

    // OČISTI SVE KEŠEVE
    public void clearAllCache() {
        File filesDir = context.getFilesDir();
        if (filesDir.exists() && filesDir.isDirectory()) {
            File[] files = filesDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".xml")) {
                        file.delete();
                    }
                }
            }
        }

        SharedPreferences.Editor editor = cachePrefs.edit();
        editor.clear();
        editor.apply();

        Log.d("CacheManager", "Svi keševi očišćeni");
    }

    // PROVERI DA LI XML POSTOJI U INTERNOJ MEMORIJI
    public boolean isXmlCached(String cacheKey) {
        File cacheFile = new File(context.getFilesDir(), cacheKey + "_filtered.xml");
        return cacheFile.exists();
    }

    // PREUZMI VELIČINU KEŠA
    public long getCacheSize() {
        long size = 0;
        File filesDir = context.getFilesDir();
        if (filesDir.exists() && filesDir.isDirectory()) {
            File[] files = filesDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".xml")) {
                        size += file.length();
                    }
                }
            }
        }
        return size;
    }

    // PROVERI DA LI JE CACHE STARIJI OD DATOG BROJA DANA
    public boolean isCacheOlderThanDays(String cacheKey, int days) {
        long lastUpdate = cachePrefs.getLong(cacheKey + "_update_time", 0);
        long currentTime = System.currentTimeMillis();
        long daysInMillis = days * 24L * 60 * 60 * 1000;

        return (currentTime - lastUpdate) > daysInMillis;
    }

    // DOBAVI VREME POSLEDNJEG AŽURIRANJA CACHE-A
    public long getLastUpdateTime(String cacheKey) {
        return cachePrefs.getLong(cacheKey + "_update_time", 0);
    }
}