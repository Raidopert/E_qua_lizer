package ru.raidopert.muzstudia;

import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;

/**
 * Разрешает веб-странице МузСтудии доступ к микрофону.
 *
 * WebView никогда не отдаёт микрофон сам: страница делает
 * getUserMedia, мы получаем onPermissionRequest и обязаны вызвать
 * grant()/deny(). Если системного разрешения RECORD_AUDIO ещё нет -
 * запрашиваем его у пользователя и завершаем после ответа.
 *
 * ВАЖНО: класс верхнего уровня, реализует Runnable вместо анонимных
 * классов - локальный d8 из build-tools 34 падает на них с NPE.
 */
public class StudioWebChromeClient extends WebChromeClient implements Runnable {

    private final MainActivity act;
    private PermissionRequest pending;

    StudioWebChromeClient(MainActivity act) {
        this.act = act;
    }

    @Override
    public void onPermissionRequest(PermissionRequest request) {
        pending = request;
        act.runOnUiThread(this);
    }

    /** Выполняется на UI-потоке: выдать, отказать или спросить систему. */
    @Override
    public void run() {
        if (pending == null) {
            return;
        }
        boolean mic = false;
        for (String r : pending.getResources()) {
            if (PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(r)) {
                mic = true;
            }
        }
        if (!mic) {
            pending.deny();
            pending = null;
        } else if (act.hasMicPermission()) {
            pending.grant(pending.getResources());
            pending = null;
        } else {
            // остаёмся в pending, ответ придёт в onMicResult()
            act.askMicPermission();
        }
    }

    /** Ответ системы на runtime-запрос RECORD_AUDIO. */
    void onMicResult(boolean granted) {
        if (pending == null) {
            return;
        }
        if (granted) {
            pending.grant(pending.getResources());
        } else {
            pending.deny();
        }
        pending = null;
    }
}
