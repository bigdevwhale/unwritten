package app.unwritten.ui

import app.unwritten.domain.Kind
import app.unwritten.domain.Rule
import app.unwritten.domain.Style
import app.unwritten.domain.Weather
import java.time.LocalDate
import kotlin.math.abs

/** The words that come from data rather than resources, in both languages: kinds, visitors, critics, series. */
class Words(val ru: Boolean) {
    fun t(r: String, e: String) = if (ru) r else e

    fun plural(n: Int, one: String, few: String, many: String): String {
        val a = abs(n) % 100; val b = a % 10
        return when { a in 11..19 -> many; b == 1 -> one; b in 2..4 -> few; else -> many }
    }

    fun n(n: Int, one: String, few: String, many: String, en1: String, enN: String) = if (ru) "$n ${plural(n, one, few, many)}" else "$n ${if (n == 1) en1 else enN}"

    fun pages(n: Int) = n(n, "страница", "страницы", "страниц", "page", "pages")
    fun books(n: Int) = n(n, "книга", "книги", "книг", "book", "books")

    fun kind(k: Kind) = when (k) {
        Kind.NOVEL -> t("Роман", "Novel")
        Kind.NOVELLA -> t("Повесть", "Novella")
        Kind.STORIES -> t("Рассказы", "Stories")
        Kind.POEMS -> t("Стихи", "Poems")
        Kind.TALES -> t("Сказки", "Fairy tales")
        Kind.PHRASEBOOK -> t("Разговорник", "Phrasebook")
        Kind.FIELD_GUIDE -> t("Определитель", "Field guide")
        Kind.MANUAL -> t("Инструкция", "Manual")
        Kind.DREAMBOOK -> t("Сонник", "Dream book")
        Kind.GUIDEBOOK -> t("Путеводитель", "Guidebook")
        Kind.COOKBOOK -> t("Поваренная книга", "Cookbook")
        Kind.ENCYCLOPEDIA -> t("Энциклопедия", "Encyclopedia")
        Kind.INDEX -> t("Предметный указатель", "Index")
        Kind.TIMETABLE -> t("Расписание", "Timetable")
        Kind.LETTERS -> t("Письмовник", "Letter writer")
        Kind.TEXTBOOK -> t("Учебник", "Textbook")
        Kind.ATLAS -> t("Атлас", "Atlas")
    }

    /** What a critic says the author has never held: «зонта», «a kettle». */
    fun thing(k: Kind) = when (k) {
        Kind.NOVEL, Kind.NOVELLA, Kind.STORIES -> t("живого человека", "a living person")
        Kind.POEMS -> t("рифмы", "a rhyme")
        Kind.TALES -> t("волшебной палочки", "a magic wand")
        Kind.PHRASEBOOK -> t("собеседника", "anyone to talk to")
        Kind.FIELD_GUIDE -> t("бинокля", "binoculars")
        Kind.MANUAL -> t("отвёртки", "a screwdriver")
        Kind.DREAMBOOK -> t("подушки", "a pillow")
        Kind.GUIDEBOOK -> t("карты", "a map")
        Kind.COOKBOOK -> t("кастрюли", "a saucepan")
        Kind.ENCYCLOPEDIA -> t("ни одного факта", "a single fact")
        Kind.INDEX -> t("самой книги", "the book itself")
        Kind.TIMETABLE -> t("билета", "a ticket")
        Kind.LETTERS -> t("конверта", "an envelope")
        Kind.TEXTBOOK -> t("мела", "a piece of chalk")
        Kind.ATLAS -> t("глобуса", "a globe")
    }

    fun style(s: Style) = when (s) {
        Style.SERIES -> t("Серия", "Series")
        Style.TRICOLOUR -> t("Три полосы", "Three bands")
        Style.PULP -> t("Бульварное", "Pulp")
        Style.SAMIZDAT -> t("Самиздат", "Samizdat")
    }

    fun rule(r: Rule) = when (r) {
        Rule.NONE -> ""
        Rule.SIX -> t("Вся книга — в первой фразе, не длиннее шести слов.", "The whole book in its first sentence, six words at most.")
        Rule.TOC -> t("От книги нужно только оглавление.", "Only the table of contents is wanted.")
        Rule.LIPO -> t("Без буквы «о» — ни в названии, ни в первой фразе, ни в аннотации.", "No letter «e» — not in the title, the first line or the blurb.")
        Rule.ENDING -> t("Покупателю важно, чем кончится: нужна последняя фраза.", "The customer wants to know how it ends: the last line is a must.")
    }

    val visitors = listOf(
        t("Старушка в мокром пальто", "An old lady in a wet coat"),
        t("Мальчик с ранцем", "A boy with a school bag"),
        t("Студентка, пахнет красками", "A student who smells of paint"),
        t("Господин с тростью", "A gentleman with a cane"),
        t("Почтальон, не снимая сумки", "The postman, bag still on"),
        t("Девушка, которая торопится", "A girl in a hurry"),
        t("Мужчина, похоже, из сна", "A man who seems to be from a dream"),
        t("Пожилой учитель", "An elderly teacher"),
        t("Таксист, на минутку", "A taxi driver, just for a minute"),
        t("Дама с таксой", "A lady with a dachshund"),
        t("Девочка в жёлтом дождевике", "A girl in a yellow raincoat"),
        t("Смотритель маяка, проездом", "A lighthouse keeper, passing through"),
        t("Молодой отец с коляской", "A young father with a pram"),
        t("Библиотекарша на пенсии", "A retired librarian"),
        t("Человек в шарфе до пола", "Someone in a floor-length scarf"),
        t("Кондуктор последнего трамвая", "The conductor of the last tram"),
        t("Пекарь, весь в муке", "A baker, all over flour"),
        t("Двое, держатся за руки", "Two people holding hands"),
        t("Мальчик, который ищет кота", "A boy looking for his cat"),
        t("Незнакомка с чемоданом", "A stranger with a suitcase"),
        t("Часовщик из соседнего дома", "The watchmaker from next door"),
        t("Бабушка с внуком", "A grandmother with her grandson"),
    )

    val series = listOf(
        t("Библиотека странствий", "Library of Wanderings"), t("Мир приключений", "World of Adventure"),
        t("Голубая серия", "The Blue Series"), t("Тихие книги", "Quiet Books"),
        t("Литературные памятники", "Literary Monuments"), t("Библиотека забытых вещей", "Library of Forgotten Things"),
        t("Книги на ночь", "Books for the Night"), t("Карманная серия", "Pocket Series"),
        t("Полка путешественника", "The Traveller's Shelf"), t("Сокровища полки", "Treasures of the Shelf"),
    )

    fun imprint(own: String) = own.ifBlank { t("Ненаписанное", "Unwritten Press") }

    val critics = listOf(
        t("А. Желчевский, «Вечерний листок»", "A. Bile, The Evening Leaf"),
        t("М. Сухарева, «Книжное обозрение»", "M. Dry, The Book Review"),
        t("Анонимный читатель с карандашом", "An anonymous reader with a pencil"),
        t("Проф. Б. Т. Несогласный", "Prof. B. T. Disagree"),
        t("Кот букиниста", "The bookseller's cat"),
        t("Литературный кружок «Среда»", "The Wednesday Reading Circle"),
    )

    /** What critics write; {t} is the title, {a} the author, {k} something the author has never held. */
    private val reviewTemplates = listOf(
        "«{t}» — книга, которую трудно отложить, потому что её трудно найти. Впрочем, автор явно никогда не держал в руках {k}." to
            "{t} is hard to put down, because it's hard to find. The author, mind you, has clearly never held {k}.",
        "{a} снова пишет о том же, о чём молчал в прошлый раз. Молчал он лучше." to "{a} again writes about what they kept silent on last time. The silence was better.",
        "Первую фразу «{t}» я перечитал четыре раза. Дальше не продвинулся — и не жалею." to "I read the first line of {t} four times. Got no further, and no regrets.",
        "Смело, но непонятно, зачем. Хотя, если подумать, зачем вообще всё." to "Bold, but to what end? Then again, to what end is anything.",
        "Эту книгу нужно запретить — тогда её наконец прочтут." to "This book should be banned. Then at last people would read it.",
        "{a} обладает редким даром: заканчивать раньше, чем надоест." to "{a} has a rare gift: stopping before it gets dull.",
        "Аннотация обещает больше, чем книга. Книга обещает больше, чем жизнь. Жизнь обещает чай." to "The blurb promises more than the book. The book promises more than life. Life promises tea.",
        "Я плакал. Возможно, из-за лука — читал «{t}» на кухне." to "I wept. Possibly the onions; I read {t} in the kitchen.",
        "Хорошая бумага. Шрифт с засечками. Остальное — дело вкуса, а вкуса у меня нет." to "Good paper. A serif face. The rest is a matter of taste, and I have none.",
        "Неожиданно, трогательно, местами грамматически. Четыре звезды из пяти несуществующих." to "Unexpected, touching, grammatical in places. Four stars out of five that don't exist.",
        "После «{t}» я пересмотрел свои взгляды. Потом пересмотрел обратно." to "After {t} I changed my views. Then changed them back.",
        "Автор слишком любит своих героев. Это непрофессионально и очень приятно." to "The author loves their characters too much. Unprofessional and very pleasant.",
        "Купил «{t}» в подарок тёще. Теперь она со мной разговаривает. Спасибо, {a}." to "Bought {t} for my mother-in-law. She speaks to me now. Thank you, {a}.",
        "Мурр. (Лежал на этой книге весь вечер. Тёплая.)" to "Purr. (Lay on this book all evening. Warm.)",
        "Кружок спорил о «{t}» до полуночи. Никто не читал. Лучшая встреча сезона." to "The circle argued about {t} till midnight. Nobody had read it. Best meeting of the season.",
        "Оглавление — поэма. Жаль, что дальше оглавления у всех нас не хватило духу." to "The contents page is a poem. A pity none of us had the nerve to go past it.",
        "{a} пишет так, будто за окном дождь. Даже когда за окном не дождь." to "{a} writes as if it's raining outside. Even when it isn't.",
        "Последняя фраза «{t}» осталась со мной в трамвае, в очереди и во сне. Отдайте." to "The last line of {t} followed me onto the tram, into the queue and into my sleep. Give it back.",
        "Слишком коротко. Или я слишком медленно моргнул." to "Too short. Or I blinked too slowly.",
        "Ставлю на полку рядом с настоящими книгами. Пусть учатся." to "Shelving it next to real books. Let them learn something.",
    )
    val reviewCount get() = reviewTemplates.size

    fun review(i: Int, title: String, author: String, kind: Kind): String {
        val (r, e) = reviewTemplates[Math.floorMod(i, reviewTemplates.size)]
        return t(r, e).replace("{t}", if (ru) title else "“$title”").replace("{a}", author.ifBlank { t("Автор", "The author") }).replace("{k}", thing(kind))
    }

    fun weather(w: Weather) = when (w) {
        Weather.CLEAR -> t("ясно", "clear")
        Weather.CLOUDY -> t("облачно", "cloudy")
        Weather.OVERCAST -> t("пасмурно", "overcast")
        Weather.FOG -> t("туман", "fog")
        Weather.DRIZZLE -> t("морось", "drizzle")
        Weather.RAIN -> t("дождь", "rain")
        Weather.STORM -> t("гроза", "thunderstorm")
        Weather.SNOW -> t("снег", "snow")
        Weather.SLEET -> t("мокрый снег", "sleet")
    }

    /** «Издание второе, исправленное». */
    fun edition(n: Int) = if (ru) "Издание ${ORD_RU.getOrElse(n - 1) { "$n-е" }}, исправленное" else "${ORD_EN.getOrElse(n - 1) { "${n}th" }} edition, revised"

    fun date(d: LocalDate) = if (ru) "${d.dayOfMonth} ${MONTHS_GEN[d.monthValue - 1]}" else "${MONTHS_EN[d.monthValue - 1]} ${d.dayOfMonth}"
    fun dateY(d: LocalDate) = if (ru) "${d.dayOfMonth} ${MONTHS_GEN[d.monthValue - 1]} ${d.year}" else "${MONTHS_EN[d.monthValue - 1]} ${d.dayOfMonth}, ${d.year}"

    private companion object {
        val ORD_RU = listOf("первое", "второе", "третье", "четвёртое", "пятое", "шестое", "седьмое", "восьмое", "девятое", "десятое")
        val ORD_EN = listOf("First", "Second", "Third", "Fourth", "Fifth", "Sixth", "Seventh", "Eighth", "Ninth", "Tenth")
        val MONTHS_GEN = listOf("января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября", "октября", "ноября", "декабря")
        val MONTHS_EN = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    }
}
