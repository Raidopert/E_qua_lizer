; ============================================================
; МузСтудия (MuzStudio) — NSIS-инсталлятор
; Мини-студия для записи треков · работает офлайн
; (c) 2025–2026 Raido Pert Studio
; Версия 0.1.0
; ============================================================

Unicode true
SetCompressor /SOLID lzma

; Явные пути к include/плагинам NSIS (сборка на Linux из локального тулчейна)
!addincludedir "/home/z/my-project/.tools/nsis-root/merged/usr/share/nsis/Include"
!addplugindir "/home/z/my-project/.tools/nsis-root/merged/usr/share/nsis/Plugins/x86-unicode"

!include "MUI2.nsh"

; --- Брендинг MUI2 ---
!define MUI_ICON            "/home/z/my-project/github/music-studio/branding/icon.ico"
!define MUI_UNICON          "/home/z/my-project/github/music-studio/branding/icon.ico"
!define MUI_WELCOMEFINISHPAGE_BITMAP "/home/z/my-project/github/music-studio/branding/nsis-welcome.bmp"
!define MUI_HEADERIMAGE
!define MUI_HEADERIMAGE_BITMAP       "/home/z/my-project/github/music-studio/branding/nsis-header.bmp"
!define MUI_ABORTWARNING

Name "МузСтудия"
OutFile "/home/z/my-project/github/music-studio/desktop-build/MUZSTUDIO-Setup-0.1.0.exe"
InstallDir "$PROGRAMFILES64\Raido Pert Studio\MuzStudio"
InstallDirRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MUZSTUDIA" "InstallLocation"
RequestExecutionLevel admin

; --- Версия и метаданные установщика ---
VIProductVersion "0.1.0.0"
VIAddVersionKey "ProductName"     "MuzStudio"
VIAddVersionKey "CompanyName"     "Raido Pert Studio"
VIAddVersionKey "LegalCopyright"  "© 2025–2026 Raido Pert Studio"
VIAddVersionKey "FileDescription" "МузСтудия — мини-студия для записи треков"
VIAddVersionKey "FileVersion"     "0.1.0.0"
VIAddVersionKey "ProductVersion"  "0.1.0.0"

; --- Страницы установщика ---
!define MUI_FINISHPAGE_RUN "$INSTDIR\MuzStudio.exe"

!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE "/home/z/my-project/github/music-studio/branding/license-notice.txt"

; Дисклеймер — отдельная страница (в MUI2 нет MUI_PAGE_README,
; используем вторую страницу лицензии с собственным заголовком).
!define MUI_PAGE_CUSTOMFUNCTION_PRE "PreDisclaimerPage"
!define MUI_LICENSEPAGE_TEXT_TOP "Прочтите дисклеймер перед продолжением установки."
!insertmacro MUI_PAGE_LICENSE "/home/z/my-project/github/music-studio/branding/disclaimer.txt"

!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

; --- Страницы деинсталлятора ---
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

!insertmacro MUI_LANGUAGE "Russian"

Function PreDisclaimerPage
  !insertmacro MUI_HEADER_TEXT "Дисклеймер" "Важная информация об использовании МузСтудии"
FunctionEnd

; ============================================================
; Установка
; ============================================================
Section "Install"

  SetOutPath "$INSTDIR"

  ; Все файлы приложения (Electron + оболочка МузСтудии)
  File /r "/home/z/my-project/github/music-studio/desktop-build/win-unpacked/MuzStudio/*.*"

  ; Ярлыки
  CreateDirectory "$SMPROGRAMS\Raido Pert Studio"
  CreateShortCut  "$SMPROGRAMS\Raido Pert Studio\МузСтудия.lnk" "$INSTDIR\MuzStudio.exe"
  CreateShortCut  "$DESKTOP\МузСтудия.lnk" "$INSTDIR\MuzStudio.exe"

  ; Деинсталлятор
  WriteUninstaller "$INSTDIR\Uninstall.exe"

  ; Регистрация в «Установка и удаление программ»
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MUZSTUDIA" "DisplayName"     "MuzStudio (МузСтудия)"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MUZSTUDIA" "DisplayVersion"  "0.1.0"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MUZSTUDIA" "Publisher"       "Raido Pert Studio"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MUZSTUDIA" "DisplayIcon"     "$INSTDIR\MuzStudio.exe"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MUZSTUDIA" "UninstallString" "$INSTDIR\Uninstall.exe"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MUZSTUDIA" "InstallLocation" "$INSTDIR"

SectionEnd

; ============================================================
; Удаление
; ============================================================
Section "Uninstall"

  RMDir /r "$INSTDIR"

  Delete "$SMPROGRAMS\Raido Pert Studio\МузСтудия.lnk"
  Delete "$DESKTOP\МузСтудия.lnk"
  RMDir  "$SMPROGRAMS\Raido Pert Studio"

  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MUZSTUDIA"

SectionEnd
