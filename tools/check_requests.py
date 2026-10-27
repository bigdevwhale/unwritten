#!/usr/bin/env python3
"""Validate app/src/main/assets/requests.tsv (customer notes for the counter).

Format: id <TAB> kind <TAB> rule <TAB> ru <TAB> en, UTF-8, LF, no header.
Usage: python tools/check_requests.py [path]
"""
import re
import sys
from collections import Counter
from pathlib import Path

KINDS = {
    "NOVEL", "NOVELLA", "STORIES", "POEMS", "TALES", "PHRASEBOOK", "FIELD_GUIDE",
    "MANUAL", "DREAMBOOK", "GUIDEBOOK", "COOKBOOK", "ENCYCLOPEDIA", "INDEX",
    "TIMETABLE", "LETTERS", "TEXTBOOK", "ATLAS",
}
FICTION = {"NOVEL", "NOVELLA", "STORIES", "POEMS", "TALES"}
RULES = {"", "six", "toc", "lipo", "ending"}
MIN_PER_KIND = 12
RULE_SHARE = (0.08, 0.15)
LEN = (40, 140)
OPENER_MAX = 0.10  # share of lines starting with the same word, per language

CYR = re.compile(r"[Ѐ-ӿ]")


def first_word(s):
    m = re.match(r"[\w-]+", s)
    return m.group(0).lower() if m else s[:1]


def main():
    root = Path(__file__).resolve().parent.parent
    path = Path(sys.argv[1]) if len(sys.argv) > 1 else root / "app/src/main/assets/requests.tsv"
    raw = path.read_bytes()
    errors = []
    if raw.startswith(b"\xef\xbb\xbf"):
        errors.append("file starts with a BOM")
    if b"\r" in raw:
        errors.append("CR characters found (must be LF only)")
    text = raw.decode("utf-8")
    if not text.endswith("\n"):
        errors.append("file must end with a newline")
    lines = text.split("\n")
    if lines and lines[-1] == "":
        lines.pop()

    kinds, rules = Counter(), Counter()
    seen_ru, seen_en = {}, {}
    ru_open, en_open = Counter(), Counter()
    for n, line in enumerate(lines, 1):
        where = f"line {n}"
        if not line.strip():
            errors.append(f"{where}: empty line")
            continue
        if line != line.rstrip():
            errors.append(f"{where}: trailing whitespace")
        cols = line.split("\t")
        if len(cols) != 5:
            errors.append(f"{where}: {len(cols)} columns, expected 5")
            continue
        id_, kind, rule, ru, en = cols
        if id_ != f"r{n:03d}":
            errors.append(f"{where}: id {id_!r}, expected r{n:03d}")
        if kind not in KINDS:
            errors.append(f"{where}: unknown kind {kind!r}")
        if rule not in RULES:
            errors.append(f"{where}: unknown rule {rule!r}")
        kinds[kind] += 1
        rules[rule] += 1
        for lang, s, seen in (("ru", ru, seen_ru), ("en", en, seen_en)):
            if s != s.strip():
                errors.append(f"{where}: {lang} has surrounding spaces")
            if not (LEN[0] <= len(s) <= LEN[1]):
                errors.append(f"{where}: {lang} length {len(s)} outside {LEN}")
            key = s.lower()
            if key in seen:
                errors.append(f"{where}: duplicate {lang} text (first at line {seen[key]})")
            seen.setdefault(key, n)
        if not CYR.search(ru):
            errors.append(f"{where}: ru has no Cyrillic")
        if CYR.search(en):
            errors.append(f"{where}: en contains Cyrillic")
        if rule == "lipo" and "«о»" not in ru:
            errors.append(f"{where}: lipo note must mention «о» in ru")
        if rule == "lipo" and "“e”" not in en and '"e"' not in en:
            errors.append(f"{where}: lipo note must mention \"e\" in en")
        if rule == "six" and "шест" not in ru.lower():
            errors.append(f"{where}: six note must mention six words in ru")
        ru_open[first_word(ru)] += 1
        en_open[first_word(en)] += 1

    total = len(lines)
    for k in sorted(KINDS):
        if kinds[k] < MIN_PER_KIND:
            errors.append(f"kind {k}: {kinds[k]} lines, need >= {MIN_PER_KIND}")
    if total:
        share = (total - rules[""]) / total
        if not (RULE_SHARE[0] <= share <= RULE_SHARE[1]):
            errors.append(f"rule share {share:.1%} outside {RULE_SHARE[0]:.0%}-{RULE_SHARE[1]:.0%}")
        for lang, c in (("ru", ru_open), ("en", en_open)):
            w, cnt = c.most_common(1)[0]
            if cnt / total > OPENER_MAX:
                errors.append(f"{lang}: {cnt} lines ({cnt / total:.1%}) start with {w!r}")

    fic = sum(kinds[k] for k in FICTION)
    print(f"{path.name}: {total} lines, fiction {fic} ({fic / max(total, 1):.0%})")
    print("kinds: " + ", ".join(f"{k} {kinds[k]}" for k in sorted(kinds)))
    print("rules: " + ", ".join(f"{r or '-'} {rules[r]}" for r in sorted(rules)))
    print("top ru openers: " + ", ".join(f"{w} {c}" for w, c in ru_open.most_common(6)))
    print("top en openers: " + ", ".join(f"{w} {c}" for w, c in en_open.most_common(6)))
    if errors:
        print(f"\n{len(errors)} problem(s):")
        for e in errors:
            print("  " + e)
        sys.exit(1)
    print("OK")


if __name__ == "__main__":
    main()
