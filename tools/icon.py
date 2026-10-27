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
