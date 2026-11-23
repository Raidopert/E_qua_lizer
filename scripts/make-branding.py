#!/usr/bin/env python3
"""Генерация брендинг-ассетов МузСтудии.

Создаёт:
  icon-512.png / icon-192.png                (исходная иконка приложения)
  android/res/mipmap-*/ic_launcher.png        (48..192, квадратный)
  android/res/mipmap-*/ic_launcher_round.png  (48..192, круглый)
  desktop/app-electron/icon.ico               (16..256, Windows, BMP-фреймы)
  branding/nsis-welcome.bmp (164x314) и branding/nsis-header.bmp (150x57)

Дизайн: тёмный квадрат #0A0A0C и пять вертикальных полос эквалайзера
в цветах дорожек приложения (как логотип в самой игре).
"""
from PIL import Image, ImageDraw, ImageFont
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BG = (10, 10, 12, 255)         # #0A0A0C — фон приложения
TRACK = [
    (255, 47, 160),   # Голос    #ff2fa0
    (63, 139, 255),   # Мелодия  #3f8cff
    (47, 213, 124),   # Окружение #2fd57c
    (255, 159, 47),   # Ритм     #ff9f2f
    (255, 209, 47),   # Басы     #ffd12f
]
GOLD = (232, 185, 92)          # #E8B95C — золотой бренда сайта
CREAM = (242, 227, 207)        # #F2E3CF


def draw_icon(size):
    """Иконка: тёмный фон + 5 полос эквалайзера с скруглением."""
    im = Image.new('RGBA', (size, size), BG)
    d = ImageDraw.Draw(im)
    # мягкая тёмно-розовая подсветка снизу
    glow = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow)
    gd.ellipse([size * 0.1, size * 0.55, size * 0.9, size * 1.15],
               fill=(255, 47, 160, 34))
    im.alpha_composite(glow)
    heights = [0.62, 0.92, 0.74, 1.00, 0.55]
    bw = size * 0.104                # ширина полосы
    gap = size * 0.052               # зазор
    total = 5 * bw + 4 * gap
    x0 = (size - total) / 2
    mid = size / 2
    area_h = size * 0.52             # высота зоны полос
    r = bw / 2
    for i, (color, hh) in enumerate(zip(TRACK, heights)):
        bar_h = area_h * hh
        x = x0 + i * (bw + gap)
        y0 = mid - bar_h / 2
        d.rounded_rectangle([x, y0, x + bw, y0 + bar_h], radius=r,
                            fill=color + (255,))
    return im


img = draw_icon(512)

# ---------- исходные PNG ----------
img.save(os.path.join(ROOT, 'icon-512.png'))
img.resize((192, 192), Image.LANCZOS).save(os.path.join(ROOT, 'icon-192.png'))
print('icon-512.png / icon-192.png ok')

# ---------- Android mipmaps ----------
def rounded_square(im, size):
    out = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    mask = Image.new('L', (size, size), 0)
    d = ImageDraw.Draw(mask)
    r = max(4, size // 5)
    d.rounded_rectangle([0, 0, size - 1, size - 1], radius=r, fill=255)
    out.paste(im.resize((size, size), Image.LANCZOS), (0, 0), mask)
    return out

def circle(im, size):
    out = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    mask = Image.new('L', (size, size), 0)
    d = ImageDraw.Draw(mask)
    d.ellipse([0, 0, size - 1, size - 1], fill=255)
    out.paste(im.resize((size, size), Image.LANCZOS), (0, 0), mask)
    return out

MIP = {'mdpi': 48, 'hdpi': 72, 'xhdpi': 96, 'xxhdpi': 144, 'xxxhdpi': 192}
for dpi, size in MIP.items():
    d = os.path.join(ROOT, 'android', 'res', 'mipmap-' + dpi)
    os.makedirs(d, exist_ok=True)
    rounded_square(img, size).save(os.path.join(d, 'ic_launcher.png'))
    circle(img, size).save(os.path.join(d, 'ic_launcher_round.png'))
    print('mipmap-' + dpi, size)

# ---------- Windows .ico (BMP-фреймы: NSIS не читает PNG-фреймы) ----------
ICO_SIZES = [(256, 256), (128, 128), (64, 64), (48, 48), (32, 32), (24, 24), (16, 16)]
ico_path = os.path.join(ROOT, 'desktop', 'app-electron', 'icon.ico')
os.makedirs(os.path.dirname(ico_path), exist_ok=True)
img.resize((256, 256), Image.LANCZOS).save(
    ico_path, format='ICO', sizes=ICO_SIZES, bitmap_format='bmp')
print('icon.ico ok')

# ---------- NSIS bitmaps ----------
DEJAVU = '/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf'
DEJAVU_R = '/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf'

def mini_bars(d, x0, y0, w, h, pad=3):
    """Пять мини-полос эквалайзера (декор в стиле логотипа)."""
    heights = [0.62, 0.92, 0.74, 1.00, 0.55]
    bw = (w - 4 * pad) / 5
    for i, (color, hh) in enumerate(zip(TRACK, heights)):
        bh = h * hh
        x = x0 + i * (bw + pad)
        d.rounded_rectangle([x, y0 + (h - bh) / 2, x + bw, y0 + (h + bh) / 2],
                            radius=bw / 3, fill=color + (255,))

os.makedirs(os.path.join(ROOT, 'branding'), exist_ok=True)

# welcome 164x314
w, h = 164, 314
wm = Image.new('RGB', (w, h), (10, 10, 12))
d = ImageDraw.Draw(wm)
d.rectangle([0, 0, w - 1, h - 1], outline=(42, 42, 50), width=1)
d.line([0, 0, 0, h], fill=GOLD, width=3)
mini_bars(d, 26, 128, 112, 110)
f_big = ImageFont.truetype(DEJAVU, 20)
f_mid = ImageFont.truetype(DEJAVU, 12)
f_sm = ImageFont.truetype(DEJAVU_R, 9)
d.text((16, 38), 'МузСтудия', font=f_big, fill=GOLD)
d.text((16, 66), 'мини-студия в кармане', font=f_mid, fill=CREAM)
d.text((16, 92), 'запиши и собери свой трек', font=f_sm, fill=(154, 154, 166))
d.text((16, 104), 'Raido Pert Studio', font=f_sm, fill=(154, 154, 166))
wm.save(os.path.join(ROOT, 'branding', 'nsis-welcome.bmp'))
print('nsis-welcome.bmp ok')

# header 150x57
w, h = 150, 57
hd = Image.new('RGB', (w, h), (10, 10, 12))
d = ImageDraw.Draw(hd)
d.line([0, h - 2, w, h - 2], fill=GOLD, width=2)
f_h = ImageFont.truetype(DEJAVU, 15)
f_h2 = ImageFont.truetype(DEJAVU_R, 8)
d.text((10, 10), 'МузСтудия', font=f_h, fill=GOLD)
d.text((10, 34), 'Raido Pert Studio', font=f_h2, fill=(154, 154, 166))
mini_bars(d, 86, 8, 56, 36)
hd.save(os.path.join(ROOT, 'branding', 'nsis-header.bmp'))
print('nsis-header.bmp ok')
