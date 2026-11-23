package ru.raidopert.muzstudia;

import android.content.res.AssetManager;
import android.net.Uri;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

/**
 * Отдаёт встроенную оболочку МузСтудии из assets/ APK по адресу
 * https://appassets.androidplatform.net/assets/... (собственный аналог
 * androidx.webkit WebViewAssetLoader: локальный d8 из build-tools 34
 * не умеет дексировать androidx-классы, приём унаследован от POPCORN PAWS).
 *
 * Приложение офлайн: внешних адресов у него нет, но перехват всё равно
 * ограничен локальным хостом — на случай будущих сетевых фич.
 * ВАЖНО: класс намеренно верхнего уровня, без внутренних классов.
 */
public class StudioWebViewClient extends WebViewClient {

    static final String HOST = "appassets.androidplatform.net";
    private static final String PREFIX = "/assets/";

    private final AssetManager am;

    StudioWebViewClient(AssetManager am) {
        this.am = am;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        Uri u = request.getUrl();
        if (!HOST.equals(u.getHost())) {
            return null;
        }
        String path = u.getPath();
        if (path == null || !path.startsWith(PREFIX)) {
            return null;
        }
        String asset = path.substring(PREFIX.length());
        try {
            InputStream in = am.open(asset);
            String mime = guessMime(asset);
            boolean text = mime.startsWith("text/") || mime.equals("application/javascript")
                    || mime.equals("application/json");
            return new WebResourceResponse(mime, text ? "utf-8" : null, in);
        } catch (IOException e) {
            // Файла нет (в т.ч. запрос каталога) - пусть WebView вернёт 404
            return null;
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        // Топ-левел навигацию разрешаем только внутри встроенных ассетов
        return !HOST.equals(request.getUrl().getHost());
    }

    private static String guessMime(String path) {
        String p = path.toLowerCase();
        if (p.endsWith(".html")) return "text/html";
        if (p.endsWith(".js")) return "application/javascript";
        if (p.endsWith(".mjs")) return "text/javascript";
        if (p.endsWith(".css")) return "text/css";
        if (p.endsWith(".json")) return "application/json";
        if (p.endsWith(".webmanifest")) return "application/manifest+json";
        if (p.endsWith(".png")) return "image/png";
        if (p.endsWith(".jpg") || p.endsWith(".jpeg")) return "image/jpeg";
        if (p.endsWith(".webp")) return "image/webp";
        if (p.endsWith(".svg")) return "image/svg+xml";
        if (p.endsWith(".ico")) return "image/x-icon";
        if (p.endsWith(".txt")) return "text/plain";
        if (p.endsWith(".wasm")) return "application/wasm";
        return URLConnection.guessContentTypeFromName(p);
    }
}
