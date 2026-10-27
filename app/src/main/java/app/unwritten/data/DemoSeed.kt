package app.unwritten.data

import app.unwritten.AppContainer
import app.unwritten.domain.Book
import app.unwritten.domain.Kind
import app.unwritten.domain.Rule
import app.unwritten.domain.Style
import app.unwritten.localized
import app.unwritten.ru
import java.time.LocalDate
import java.time.ZoneId

/**
 * The demo build's past: two months of the shop played through the real repository with a wound-back clock,
 * so both shelves have books, critics have called, one book has a second edition, one sits in the bargain box
 * and one has pencil in its margins. The welcome page still comes first, as on a real first launch.
 */
object DemoSeed {
    private class D(
        val ago: Int, val kind: Kind, val style: Style, val title: Pair<String, String>, val author: Pair<String, String>,
        val first: Pair<String, String>, val blurb: Pair<String, String>, val pages: Int,
        val note: Pair<String, String>? = null, val visitor: Int = -1, val rule: Rule = Rule.NONE,
        val epigraph: Pair<String, String>? = null, val toc: Pair<String, String>? = null, val middle: Pair<String, String>? = null,
        val last: Pair<String, String>? = null, val margin: Pair<String, String>? = null, val opens: List<Int> = emptyList(),
        val critics: Int = 0, val bargain: Boolean = false, val preface: Pair<String, String>? = null, val chapter: Pair<String, String>? = null,
        val ref: Int = -1,
    )

    private val BOOKS = listOf(
        D(
            58, Kind.NOVEL, Style.SERIES, "Зонт, который остался" to "The Umbrella That Stayed", "И. Забытов" to "I. M. Forgotten",
            "В то утро его забыли в третий раз, и он решил, что это знак." to "That morning it was forgotten for the third time, and it took that as a sign.",
            "Трогательная и смешная история о вещи, которая ждала. Гардероб городской библиотеки, бюро находок, чужие руки — и один дождливый четверг, который всё меняет." to
                "A funny, tender story about a thing that waited. The cloakroom of a city library, a lost property office, strangers' hands, and one rainy Thursday that changes everything.",
            312, "Мне бы роман, где главный герой — забытый зонт." to "A novel whose hero is a forgotten umbrella, if you have one.", 0,
            epigraph = "«Все вещи помнят руки». — Из разговора в гардеробе" to "‘All things remember hands.’ — Overheard in a cloakroom",
            toc = "Гардероб\nДождь в четверг\nБюро находок\nЧужая рука\nВозвращение" to "The Cloakroom\nRain on Thursday\nLost Property\nA Stranger's Hand\nThe Return",
            middle = "Он висел на крючке под номером сорок семь и слушал, как наверху шуршат страницы. Номерок от него ушёл ещё в обед — в кармане пальто, в автобусе, в другой район. Гардеробщица Вера Павловна сказала: «Ну и стой», — и это было почти ласково." to
                "It hung on peg forty-seven and listened to pages rustling upstairs. Its ticket had left at lunchtime, in a coat pocket, on a bus, to another part of town. The attendant, Vera Pavlovna, said, ‘Well, stand there then,’ and it was almost kind.",
            last = "Дождь начался, как только его раскрыли." to "The rain began the moment it was opened.",
            margin = "вот тут я заплакал, честно" to "this is where I cried, honestly",
            opens = listOf(58, 51, 40, 33, 20, 9, 2), critics = 3,
            preface = "Со времени первого издания автор нашёл свой зонт. Он висел в том же гардеробе, на том же крючке, и не сказал ни слова." to
                "Since the first edition the author has found their own umbrella. It hung in the same cloakroom, on the same peg, and said not a word.",
            chapter = "Послесловие гардеробщицы" to "The Attendant's Afterword",
        ),
        D(
            53, Kind.PHRASEBOOK, Style.TRICOLOUR, "Кошачий для начинающих" to "Cat for Beginners", "Мурзина и Ко" to "Purrkins & Co",
            "Урок первый: никогда не начинайте разговор первым." to "Lesson one: never be the first to speak.",
            "Триста фраз на все случаи жизни: от «я не голоден, я просто посмотрю» до «эта коробка теперь моя». С транскрипцией и жестами хвоста." to
                "Three hundred phrases for every occasion, from ‘I'm not hungry, I'm just looking’ to ‘this box is mine now’. With transcription and tail gestures.",
            96, "Разговорник кошачьего, пожалуйста. Кот меня не понимает." to "A Cat phrasebook, please. My cat doesn't understand me.", 9,
            toc = "Приветствия\nЕда (её отсутствие)\nКоробки\nДверь: туда и обратно\nПрощения не просят" to "Greetings\nFood (its absence)\nBoxes\nThe Door: In and Out Again\nApologies Are Not Made",
            opens = listOf(53, 30, 12), critics = 2, ref = 0,
        ),
        D(
            47, Kind.TIMETABLE, Style.SAMIZDAT, "Пригородные до несуществующих станций" to "Local Trains to Stations That Aren't There", "сост. неизв." to "comp. unknown",
            "Поезд 6:12 до платформы «Позавчера» отменён по техническим причинам." to "The 6:12 to Day-Before-Yesterday is cancelled for technical reasons.",
            "Летнее расписание. Действительно до тех пор, пока помните." to "Summer timetable. Valid for as long as you remember.",
            24, toc = "Позавчера\nДача детства\nПолустанок «Если бы»\nКонечная" to "Day-Before-Yesterday\nChildhood Dacha\nWhat-If Halt\nTerminus",
            opens = listOf(47, 22), critics = 1,
        ),
        D(
            41, Kind.NOVEL, Style.PULP, "Смерть не пришла" to "Death Didn't Show", "Р. Тихонравов" to "R. Quietly",
            "Инспектор ждал труп всё утро, но к обеду стало ясно: все живы." to "The inspector waited all morning for a body, but by lunch it was clear: everyone was alive.",
            "Самый невероятный детектив года! Никто не умер — но кто-то всё-таки соврал." to "The most unlikely mystery of the year! Nobody died, but somebody lied all the same.",
            188, "Детектив, в котором никто не умер." to "A detective novel where nobody dies.", 3,
            last = "Убийцей оказался понедельник." to "The murderer turned out to be Monday.", opens = listOf(41, 18), critics = 2,
        ),
        D(
            36, Kind.FIELD_GUIDE, Style.SERIES, "Птицы, которые прилетают только во сне" to "Birds That Only Come in Dreams", "проф. Н. Перьева" to "Prof. N. Feathers",
            "Узнать их легко: они поют голосом тех, кого вы давно не слышали." to "They're easy to tell: they sing in the voices of people you haven't heard in years.",
            "Сорок семь видов с цветными таблицами. Где гнездятся, чем питаются (воспоминаниями), как не спугнуть, проснувшись." to
                "Forty-seven species with colour plates. Where they nest, what they eat (memories), how not to scare them off by waking.",
            164, "Определитель птиц, которые прилетают только во сне." to "A field guide to the birds that only come in dreams.", 6,
            middle = "Сонная зарянка. Прилетает под утро, садится на подоконник детской. Поёт мамиными словами, путая их местами. Если ответить ей, улетает навсегда — поэтому не отвечайте, просто слушайте." to
                "The sleepy robin. Arrives towards morning and perches on a nursery sill. Sings in your mother's words, muddling their order. Answer it and it flies off for good, so don't. Just listen.",
            opens = listOf(36, 14, 4), critics = 1,
        ),
        D(
            30, Kind.COOKBOOK, Style.TRICOLOUR, "Суп для призраков" to "Soup for Ghosts", "мадам Бланш" to "Madame Blanche",
            "Призраки не едят — они вдыхают. Поэтому главное в этой книге — пар." to "Ghosts don't eat, they breathe in. So the main thing in this book is steam.",
            "Двадцать рецептов для тех, кто живёт на чердаке и не любит шума. Всё готовится на свече, всё подаётся в полночь." to
                "Twenty recipes for those who live in the attic and dislike noise. Everything cooked over a candle, everything served at midnight.",
            72, "Поваренную книгу для призраков, пожалуйста. Для подруги." to "A cookbook for ghosts, please. It's for a friend.", 10,
            toc = "Бульон из тумана\nКисель из лунного света\nПирог, которого нет\nЧай, который остыл давно" to "Fog Broth\nMoonlight Jelly\nThe Pie That Isn't\nTea That Went Cold Long Ago",
            opens = listOf(30, 8), critics = 1,
        ),
        D(
            26, Kind.POEMS, Style.SAMIZDAT, "Стихи для стоматологической очереди" to "Poems for the Dentist's Waiting Room", "Анна Нервная" to "Anne Nerve",
            "Сверло поёт за стенкой, как шмель в стеклянной банке." to "The drill sings through the wall like a bumblebee in a jar.",
            "Тридцать коротких стихотворений, каждое — на одного пациента. Последнее читается уже в кресле." to
                "Thirty short poems, one per patient. The last one is read in the chair.",
            40, "Что-нибудь, чтобы читать в очереди к стоматологу." to "Something to read in the queue at the dentist's.", 4,
            opens = listOf(26), bargain = true,
        ),
        D(
            21, Kind.GUIDEBOOK, Style.SERIES, "Путеводитель по одному подъезду" to "A Guide to One Stairwell", "В. Лестничный" to "V. Landing",
            "Начнём осмотр с почтовых ящиков: третий слева не открывали с девяносто восьмого года." to "We begin at the letterboxes: the third from the left hasn't been opened since 1998.",
            "Пять этажей, двадцать дверей, одна лампочка. Достопримечательности, легенды и маршруты для неторопливого подъёма." to
                "Five floors, twenty doors, one light bulb. Sights, legends and routes for an unhurried climb.",
            128, "Путеводитель по одному подъезду. По моему." to "A guidebook to one stairwell. Mine.", 13,
            toc = "Почтовые ящики\nЛифт, который думает\nПодоконник третьего этажа\nДверь с котом\nЧердак (закрыт)" to "The Letterboxes\nThe Lift That Thinks\nThe Third-Floor Sill\nThe Door With a Cat\nThe Attic (Closed)",
            opens = listOf(21, 7), critics = 1, ref = 1,
        ),
        D(
            17, Kind.TALES, Style.SERIES, "Сказки для взрослых, которые устали" to "Fairy Tales for Tired Grown-ups", "Бабушка Нина" to "Granny Nina",
            "Жила-была одна взрослая, и ей ничего не надо было делать." to "Once upon a time there was a grown-up who didn't have to do anything at all.",
            "Двенадцать сказок, в которых никто не спешит, не спасает мир и не становится лучше. Все просто ложатся спать вовремя." to
                "Twelve tales in which nobody hurries, saves the world or improves. Everyone just goes to bed on time.",
            144, "Сказку для взрослого, который устал." to "A fairy tale for a grown-up who's tired.", 21,
            last = "И они уснули, а утро подождало." to "And they fell asleep, and the morning waited.", opens = listOf(17, 3),
        ),
        D(
            11, Kind.LETTERS, Style.TRICOLOUR, "Как ответить на письмо через двадцать лет" to "How to Answer a Letter Twenty Years Late", "А. Почтовая" to "A. Post",
            "Не извиняйтесь в первой строке: двадцать лет — это не опоздание, это выдержка." to "Don't apologise in the first line: twenty years isn't lateness, it's ageing.",
            "Образцы писем для тех, кто так и не ответил. Бывшим соседям, первой учительнице, другу из лагеря, себе самому." to
                "Sample letters for those who never replied. To old neighbours, a first teacher, a friend from summer camp, yourself.",
            88, "Письмовник: как ответить на письмо через двадцать лет." to "A letter writer: how to answer a letter twenty years on.", 7,
            opens = listOf(11, 1), critics = 1,
        ),
        D(
            6, Kind.STORIES, Style.PULP, "Перчатки, которые нашлись" to "The Gloves That Turned Up", "К. Варежкин" to "K. Mitten",
            "Левая перчатка ушла в марте, правая ждала до ноября." to "The left glove left in March; the right one waited till November.",
            "Девять рассказов о потерянных перчатках и о том, куда они на самом деле уходят. Спойлер: в другие истории." to
                "Nine stories about lost gloves and where they really go. Spoiler: into other stories.",
            136, "Сборник рассказов о потерянных перчатках." to "Short stories about lost gloves.", 15, opens = listOf(6),
        ),
        D(
            3, Kind.NOVEL, Style.SERIES, "Маяк, где всё хорошо" to "The Lighthouse Where All Is Well", "Т. Смотрителева" to "T. Keeper",
            "Шторма не было уже сорок лет, и смотритель научился печь хлеб." to "There hadn't been a storm for forty years, and the keeper had learned to bake bread.",
            "Роман о маяке, где ничего не случается, — и о том, как это много." to "A novel about a lighthouse where nothing happens, and how much that is.",
            420, "Роман о маяке, где всё хорошо." to "A novel about a lighthouse where everything's fine.", 11, opens = listOf(3, 1),
        ),
    )

    suspend fun seedIfEmpty(c: AppContainer) {
        if (c.repo.refresh().books.isNotEmpty()) return
        val ru = c.app.localized().ru
        fun p(x: Pair<String, String>?) = x?.let { if (ru) it.first else it.second } ?: ""
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val repo = c.repo
        val ids = mutableListOf<Long>()
        for ((i, d) in BOOKS.withIndex()) {
            val at = today.minusDays(d.ago.toLong()).atTime(18, 10 + i * 3).atZone(zone).toInstant().toEpochMilli()
            repo.clock = { at }
            val id = repo.shelve(
                Book(
                    created = at, kind = d.kind, style = d.style, title = p(d.title), author = p(d.author), first = p(d.first), blurb = p(d.blurb),
                    pages = d.pages, note = p(d.note), requestId = if (d.note != null) "demo-$i" else "", visitor = d.visitor, rule = d.rule,
                    epigraph = p(d.epigraph), toc = p(d.toc), middle = p(d.middle), last = p(d.last),
                    refId = if (d.ref >= 0) ids[d.ref] else 0,
                ),
            )
            ids += id
            for ((k, ago) in d.opens.drop(1).withIndex()) {
                val o = today.minusDays(ago.toLong()).atTime(20, 5 + k).atZone(zone).toInstant().toEpochMilli()
                repo.clock = { o }
                repo.open(id)
                if (k < d.critics) repo.review(id) { used, seed -> Math.floorMod(seed / 3 + k, 6L).toInt() to ((i * 3 + k * 7) % 20).let { t -> if (t in used) (t + 1) % 20 else t } }
            }
            if (d.critics > d.opens.size - 1) repeat(d.critics - (d.opens.size - 1)) { repo.review(id) { used, _ -> 4 to (0 until 20).first { it !in used } } }
            if (d.margin != null) repo.margin(id, p(d.margin))
            if (d.preface != null) repo.reissue(id, p(d.preface), p(d.chapter))
            if (d.bargain) repo.bargain(id, true)
        }
        repo.clock = System::currentTimeMillis
    }
}
