# МузСтудия — Windows EXE (Electron + NSIS), v0.1.0

Установщик `MUZSTUDIO-Setup-0.1.0.exe` (~82 МБ) собирается на Linux
из портативного Electron (win32-x64) + NSIS 3 (makensis) — той же
схемой, что и CatScribbles.

## Структура

- `app-electron/main.js` — Electron-обёртка: локальный http-сервер на
  127.0.0.1 раздаёт `web/`, окно BrowserWindow, внешние ссылки — в
  системный браузер, devtools заблокированы, single instance lock.
- `app-electron/package.json` — манифест приложения (productName MuzStudio).
- `app-electron/icon.ico` — иконка exe (BMP-фреймы, генерируется
  `scripts/make-branding.py`).
- `installer.nsi` — NSIS-скрипт: MUI2, русский язык, страницы лицензии
  и дисклеймера, ярлыки, запись в «Установка и удаление программ»,
  деинсталлятор.
- `../branding/` — icon.ico, nsis-welcome.bmp (164x314),
  nsis-header.bmp (150x57), license-notice.txt, disclaimer.txt
  (генерируются `scripts/make-branding.py`).

## Сборка

1. Брендинг:
   ```sh
   python3 scripts/make-branding.py
   ```
2. Портативный Electron: скачать `electron-v31.7.7-win32-x64.zip`,
   распаковать в `desktop-build/win-unpacked/MuzStudio/`,
   переименовать `electron.exe` → `MuzStudio.exe`.
3. Приложение: `resources/app/` ← `app-electron/{package.json, main.js,
   icon.ico}` + `resources/app/web/` ← `index.html` из корня репо.
4. NSIS (данные в `/usr/share/nsis`; при локальной распаковке deb —
   bind-mount внутри `unshare -Ur --mount`):
   ```sh
   unshare -Ur --mount sh -c \
     'mount --bind <nsis-root>/merged/usr/share /usr/share && \
      <nsis-root>/merged/usr/bin/makensis desktop/installer.nsi'
   # → desktop-build/MUZSTUDIO-Setup-0.1.0.exe
   ```

## Особенности

- `MUI_ICON`/`MUI_UNICON` требуют ICO с **BMP-фреймами** (PIL:
  `bitmap_format="bmp"`).
- Приложение полностью офлайн: интернет нужен только Electron'у при
  самом первом запуске (и то не обязательно). Микрофон запрашивается
  Windows'ом при первой записи.
