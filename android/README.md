# МузСтудия — Android APK (v0.1.0)

Package `ru.raidopert.muzstudia`, versionCode 1, versionName 0.1.0,
minSdk 24 / targetSdk 34.

Разрешение одно — `RECORD_AUDIO` (микрофон для кнопки «Запись»).
Приложение полностью офлайн: INTERNET не запрашивается, локальная
оболочка (index.html + иконки) встроена в APK (`assets/`) и раздаётся
`StudioWebViewClient` (чистый android.webkit) по адресу
`https://appassets.androidplatform.net/assets/...`; топ-левел
навигация на внешние адреса блокируется. androidx.webkit
(WebViewAssetLoader) не используется: локальный d8 8.2.2-dev
(build-tools 34) падает с NPE при дексировании его classes.jar —
приём унаследован от POPCORN PAWS.

Отличие от CatScribbles: страница запрашивает микрофон
(getUserMedia), а WebView отдаёт его только после явного grant().
`StudioWebChromeClient.onPermissionRequest` выдаёт RESOURCE_AUDIO_CAPTURE
после runtime-разрешения RECORD_AUDIO (API 23+). Классы верхнего
уровня, без анонимных внутренних — тот же NPE в d8.

## Пересборка (из корня репозитория)

```sh
TOOLS=<путь>/android-build-tools; BT=$TOOLS/android-14; AJ=$TOOLS/android-34/android.jar
export PATH=<путь>/jdk21/bin:$PATH

mkdir -p android-build/assets && cp index.html icon-192.png icon-512.png android-build/assets/
$BT/aapt2 compile --dir android/res -o android-build/res.zip
$BT/aapt2 link -o android-build/base-unsigned.apk -I $AJ --manifest android/AndroidManifest.xml \
  -A android-build/assets --min-sdk-version 24 --target-sdk-version 34 \
  --version-code 1 --version-name 0.1.0 --auto-add-overlay android-build/res.zip
mkdir -p android-build/obj && javac -nowarn -source 11 -target 11 -cp $AJ \
  -d android-build/obj $(find android/java -name '*.java')
mkdir -p android-build/dex && $BT/d8 --release --lib $AJ --min-api 24 \
  --output android-build/dex $(find android-build/obj -name '*.class')
python3 -c "import zipfile; z=zipfile.ZipFile('android-build/base-unsigned.apk','a'); z.write('android-build/dex/classes.dex','classes.dex',compress_type=zipfile.ZIP_DEFLATED); z.close()"
$BT/zipalign -f 4 android-build/base-unsigned.apk android-build/base-aligned.apk
$BT/apksigner sign --ks android/keystore/muzstudio-release.jks --ks-key-alias muzstudio \
  --ks-pass pass:muzstudio2026 --key-pass pass:muzstudio2026 \
  --out android-build/MuzStudio-0.1.0.apk android-build/base-aligned.apk
$BT/apksigner verify --print-certs android-build/MuzStudio-0.1.0.apk
```

Готовый скрипт: `scripts/build-apk.sh` (в корне репозитория, если
добавлен) — выполняет все шаги подряд.

Keystore: `android/keystore/muzstudio-release.jks`, alias `muzstudio`,
пароли (store и key): `muzstudio2026`.
Ключ терять нельзя: обновления устанавливаются поверх старых только
при совпадении подписи (same signature).
