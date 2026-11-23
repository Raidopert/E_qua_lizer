package ru.raidopert.muzstudia;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * WebView-обёртка МузСтудии.
 * Оболочка приложения (index.html + иконки) встроена в APK (assets/)
 * и раздаётся StudioWebViewClient по адресу
 * https://appassets.androidplatform.net/assets/...
 *
 * Отличие от CatScribbles: приложение полностью офлайн (INTERNET
 * не запрашивается), но записывает звук с микрофона. Поэтому здесь
 * есть WebChromeClient.onPermissionRequest: WebView отдаёт микрофон
 * странице только после явного grant() от приложения, а приложению
 * нужен runtime-запрос RECORD_AUDIO (API 23+). Без этого блок
 * «Запись» в WebView молча не сработал бы.
 *
 * ВАЖНО: без анонимных и внутренних классов - локальный d8 из
 * build-tools 34 падает на них с NPE (приём унаследован от
 * POPCORN PAWS / CatScribbles).
 */
public class MainActivity extends Activity {

    private static final int MIC_REQ = 42;

    private WebView wv;
    private StudioWebChromeClient chromeClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        wv = new WebView(this);
        WebSettings s = wv.getSettings();
        s.setJavaScriptEnabled(true);
        // localStorage: настройки студии (тема, метроном, BPM)
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(false);
        s.setTextZoom(100);
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setMediaPlaybackRequiresUserGesture(true);

        // Клиент раздаёт встроенные ассеты по "https"-подобному адресу
        wv.setWebViewClient(new StudioWebViewClient(getAssets()));

        // Микрофон для записи: grant только после разрешения RECORD_AUDIO
        chromeClient = new StudioWebChromeClient(this);
        wv.setWebChromeClient(chromeClient);

        setContentView(wv);
        wv.loadUrl("https://appassets.androidplatform.net/assets/index.html");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != MIC_REQ) {
            return;
        }
        boolean granted = grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        chromeClient.onMicResult(granted);
    }

    /** Есть ли уже системное разрешение на запись звука. */
    boolean hasMicPermission() {
        return checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }

    /** Запросить системное разрешение на запись звука. */
    void askMicPermission() {
        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, MIC_REQ);
    }

    @Override
    public void onBackPressed() {
        // Приложение уходит в фон: идущая запись и раскладка блоков
        // не должны теряться мгновенно
        moveTaskToBack(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        wv.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        wv.onResume();
    }

    @Override
    protected void onDestroy() {
        wv.destroy();
        super.onDestroy();
    }
}
