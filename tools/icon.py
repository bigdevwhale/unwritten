"""Writes the launcher icon vectors: a book in a framed series cover, under lamplight on the shop's wallpaper.

    python tools/icon.py
"""
from pathlib import Path

DRAW = Path(__file__).resolve().parent.parent / "app/src/main/res/drawable"
HEAD = ('<?xml version="1.0" encoding="utf-8"?>\n<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
        '    android:width="108dp"\n    android:height="108dp"\n    android:viewportWidth="108"\n    android:viewportHeight="108">\n')


def rect(x, y, w, h):
    return f"M{x},{y}h{w}v{h}h{-w}z"


def frame(x, y, w, h, t):
    return rect(x, y, w, h) + f"M{x + t},{y + t}v{h - 2 * t}h{w - 2 * t}v{-(h - 2 * t)}z"


def star(cx, cy, r1, r2):
    import math
    pts = []
    for i in range(10):
        a = math.pi * i / 5 - math.pi / 2
        r = r1 if i % 2 == 0 else r2
        pts.append(f"{cx + math.cos(a) * r:.2f},{cy + math.sin(a) * r:.2f}")
    return "M" + "L".join(pts) + "Z"


def main():
    body = [
        ("#26F3C77A", "M54,54m-34,0a34,34 0,1 1,68 0a34,34 0,1 1,-68 0"),
        # The cover, a little shadow at the spine.
        ("#FF4B5E3A", rect(35, 28, 38, 54)),
        ("#40000000", rect(35, 28, 3, 54)),
        ("#FFE8DDB0", frame(39, 32, 30, 46, 1.2)),
        ("#FFE8DDB0", frame(41, 34, 26, 42, .5)),
        ("#FFE8DDB0", rect(45, 42, 18, 2.2) + rect(47, 46.5, 14, 1.6)),
        ("#FFE8DDB0", star(54, 63, 5, 2.1)),
        # The ribbon bookmark.
        ("#FFB5502F", "M63,82v9l2.5,-2.5l2.5,2.5v-9z"),
    ]
    out = [f'    <path android:fillColor="{c}" android:pathData="{p}" />' for c, p in body]
    (DRAW / "ic_launcher_foreground.xml").write_text(HEAD + "\n".join(out) + "\n</vector>\n", encoding="utf-8", newline="\n")
    (DRAW / "ic_launcher_background.xml").write_text(HEAD + '    <path android:fillColor="#FF33261B" android:pathData="M0,0h108v108h-108z" />\n</vector>\n', encoding="utf-8", newline="\n")


if __name__ == "__main__":
    main()


def logo(path="docs/logo.png", size=512):
    """The icon drawn to a PNG for the README and the site, from the same shapes."""
    import re
    from PIL import Image, ImageDraw
    k = size / 108
    im = Image.new("RGBA", (size, size), "#33261B")
    glow = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    ImageDraw.Draw(glow).ellipse([20 * k, 20 * k, 88 * k, 88 * k], fill=(243, 199, 122, 38))
    im.alpha_composite(glow)
    d = ImageDraw.Draw(im)
    d.rectangle([35 * k, 28 * k, 73 * k, 82 * k], fill="#4B5E3A")
    d.rectangle([35 * k, 28 * k, 38 * k, 82 * k], fill="#3A4A2E")
    d.rectangle([39 * k, 32 * k, 69 * k, 78 * k], outline="#E8DDB0", width=max(1, round(1.2 * k)))
    d.rectangle([41 * k, 34 * k, 67 * k, 76 * k], outline="#E8DDB0", width=max(1, round(.5 * k)))
    d.rectangle([45 * k, 42 * k, 63 * k, 44.2 * k], fill="#E8DDB0")
    d.rectangle([47 * k, 46.5 * k, 61 * k, 48.1 * k], fill="#E8DDB0")
    pts = [tuple(float(v) * k for v in p.split(",")) for p in re.findall(r"[\d.]+,[\d.]+", star(54, 63, 5, 2.1))]
    d.polygon(pts, fill="#E8DDB0")
    d.polygon([(63 * k, 82 * k), (63 * k, 91 * k), (65.5 * k, 88.5 * k), (68 * k, 91 * k), (68 * k, 82 * k)], fill="#B5502F")
    out = Path(__file__).resolve().parent.parent / path
    out.parent.mkdir(parents=True, exist_ok=True)
    im.convert("RGB").save(out, optimize=True)


if __name__ == "__main__" and "--logo" in __import__("sys").argv:
    logo()
