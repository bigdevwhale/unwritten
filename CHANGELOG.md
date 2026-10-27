# Changelog

<!--
Telegram: end each version's section with a hidden block for the StillCraft chat. Start it with a
line "<!-- tg", then 2-4 short lines in Russian for users (what is new, what is fixed), no markdown,
then close it like any HTML comment. The release notes carry it, and the chat bot posts those lines.
-->

## [1.0.0] - 2026-10-27

### Added
- A bookshop of books that don't exist.
  - The shop has two shelves, a lamp, a counter and a bargain box.
  - A window shows the real time of day: the sun, the moon in its phase, lit windows at night.
  - The window also shows the real weather once you pick a town (Open-Meteo, with MET Norway as a fallback).
- A customer each day with a handwritten note, from a catalogue of 420 in Russian and English.
  - Some notes come with a wish: six words, contents only, no «о» / «e», or only the ending.
  - The bell calls up to three customers a day.
- Writing a book in steps that each fit one screen: a title, a first line and a blurb. Optionally a pen name, an epigraph, the contents, a page from the middle, the last line, a page count and «see also».
- Covers drawn in code in four styles: a classic series, a three-band paperback, pulp and samizdat. Each carries your own imprint.
- The book view: cover, back with a barcode, the pages inside turned one at a time with a pencil note in the margin, a library card with a stamp for every day it was opened, and the critics.
- Critics' reviews, books returned by readers, second editions and the bargain box.
- A book printed as an eight-page A4 zine (PDF) to share.
- A card catalogue with search and a filter by kind.
- `-Pdemo` seeds two months of the shop. `-Prender` draws the screens in English and Russian.

<!-- tg
Новое приложение: лавка книг, которых нет
Каждый день покупатель приносит записку с книгой, которой не существует, а ты её «находишь» — пишешь название, первую фразу и аннотацию
Обложки в четырёх стилях, критики, второе издание, уценка и печать книги в зин
За окном лавки настоящее время суток и погода
-->
