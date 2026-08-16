package org.example.islamicapp.feature.learn.data

import org.example.islamicapp.feature.learn.domain.LearnStep
import org.example.islamicapp.feature.learn.domain.LearnTopic
import org.example.islamicapp.feature.learn.domain.MadhhabNote
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Learning guides (PROJECT_PROMPT.md §6 Phase 5): wudu, ghusl, tayammum,
 * the five daily prayers and special prayers. Madhhab differences are
 * presented neutrally without preference (§10).
 */
@Singleton
class LearnRepository @Inject constructor(
    private val islamIntroRepository: IslamIntroRepository,
    private val sirahRepository: SirahRepository,
    private val prophetsStoriesRepository: ProphetsStoriesRepository,
    private val namesOfAllahRepository: NamesOfAllahRepository,
) {

    val topics: List<LearnTopic> by lazy {
        listOf(
            islamIntroRepository.topic,
            sirahRepository.topic,
            prophetsStoriesRepository.topic,
            namesOfAllahRepository.topic,
            wudu,
            ghusl,
            tayammum,
            salah,
            specialPrayers,
        )
    }

    fun topic(id: String): LearnTopic? = topics.firstOrNull { it.id == id }

    private val wudu = LearnTopic(
        id = "wudu",
        titleAr = "الوضوء",
        titleEn = "Wudu (ablution)",
        steps = listOf(
            LearnStep(
                "النيّة", "Intention",
                "استحضار نية الوضوء لرفع الحدث، والنيّة عمل قلبي لا تُنطق باللسان.",
                "Resolve in the heart to perform wudu; intention is an act of the heart.",
            ),
            LearnStep(
                "التسمية", "Basmalah",
                "التسمية عند البدء: «بسم الله».",
                "Begin with 'Bismillah'.",
            ),
            LearnStep(
                "غسل الكفين", "Wash the hands",
                "غسل الكفين ثلاثًا مع التخليل.",
                "Wash the hands three times, passing between the fingers.",
            ),
            LearnStep(
                "المضمضة والاستنشاق", "Mouth and nose",
                "المضمضة ثلاثًا والاستنشاق بالماء ثم الاستنثار ثلاثًا.",
                "Rinse the mouth and nose three times.",
            ),
            LearnStep(
                "غسل الوجه", "Wash the face",
                "غسل الوجه ثلاثًا من منابت شعر الرأس إلى الذقن ومن الأذن إلى الأذن.",
                "Wash the entire face three times, from hairline to chin, ear to ear.",
            ),
            LearnStep(
                "غسل اليدين إلى المرفقين", "Wash the arms",
                "غسل اليدين إلى المرفقين ثلاثًا، اليمنى ثم اليسرى.",
                "Wash both arms up to (and including) the elbows three times, right then left.",
            ),
            LearnStep(
                "مسح الرأس والأذنين", "Wipe the head and ears",
                "مسح الرأس بالأذنين مرة واحدة.",
                "Wipe the head and ears once.",
            ),
            LearnStep(
                "غسل الرجلين إلى الكعبين", "Wash the feet",
                "غسل الرجلين إلى الكعبين ثلاثًا مع التخليل، اليمنى ثم اليسرى.",
                "Wash both feet up to the ankles three times with the toes, right then left.",
            ),
            LearnStep(
                "الذكر بعده", "Closing dhikr",
                "قول: «أشهد أن لا إله إلا الله وحده لا شريك له وأشهد أن محمدًا عبده ورسوله، اللهم اجعلني من التوابين واجعلني من المتطهرين».",
                "Say the closing shahadah supplication.",
            ),
        ),
        differences = listOf(
            MadhhabNote(
                "المضمضة والاستنشاق: فرضان مستقلان عند الحنفية والحنابلة (رواية)، وهما من الوجه عند الشافعية والمالكية.",
                "Rinsing mouth/nose: independent obligations (Hanafi, Hanbali) vs part of washing the face (Shafi'i, Maliki).",
            ),
            MadhhabNote(
                "مسح الرأس: فرضه ربع الرأس عند الحنفية، وكل الرأس عند الجمهور.",
                "Head wipe: a quarter of the head suffices per Hanafis; the whole head per the majority.",
            ),
            MadhhabNote(
                "الترتيب والموالاة: شرطان عند الشافعية والحنابلة، وسنة عند الحنفية، والموالاة مستحبة عند المالكية.",
                "Sequence and continuity: conditions per Shafi'is/Hanbalis; sunnah per Hanafis.",
            ),
            MadhhabNote(
                "التسمية: واجبة عند الحنفية عند الذكر، وسنة عند الجمهور.",
                "Basmalah: obligatory when remembered per Hanafis; sunnah per the majority.",
            ),
        ),
    )

    private val ghusl = LearnTopic(
        id = "ghusl",
        titleAr = "الغسل",
        titleEn = "Ghusl (full bath)",
        steps = listOf(
            LearnStep(
                "موجبات الغسل", "What requires ghusl",
                "يوجب الغسل: خروج المني، والجماع، وانقطاع الحيض والنفاس، والموت والمغسل منه.",
                "Ghusl is required after: sexual discharge, intercourse, end of menstruation/postpartum, and for the deceased.",
            ),
            LearnStep(
                "النيّة والتسمية", "Intention and basmalah",
                "النية بالقلل والتسمية عند البدء.",
                "Intend ghusl in the heart and begin with Bismillah.",
            ),
            LearnStep(
                "غسل أثر الجنابة أولًا", "Remove impurity first",
                "غسل ما على البدن من أثر أو نجاسة مع اليدين.",
                "Wash any impurity from the body, washing the hands first.",
            ),
            LearnStep(
                "الوضوء الكامل", "Complete wudu",
                "الوضوء الكامل كما في وضوء الصلاة.",
                "Perform a complete wudu as for prayer.",
            ),
            LearnStep(
                "إفاضة الماء على الرأس ثم البدن", "Pour water over head and body",
                "ثلاث حثات: على الرأس ثم على بقية البدن مع تعميم الماء وتدليك ما تحت الشعر.",
                "Three pours: over the head, then over the whole body, ensuring water reaches the skin under the hair.",
            ),
        ),
        differences = listOf(
            MadhhabNote(
                "المضمضة والاستنشاق في الغسل: واجبتان عند الحنفية، وسنتان عند الجمهور.",
                "Mouth/nose rinsing in ghusl: obligatory (Hanafi); recommended (majority).",
            ),
        ),
    )

    private val tayammum = LearnTopic(
        id = "tayammum",
        titleAr = "التيمم",
        titleEn = "Tayammum (dry ablution)",
        steps = listOf(
            LearnStep(
                "متى يُشرع", "When permitted",
                "عند فقد الماء أو العجز عن استعماله لمرض أو برد شديد.",
                "When water is unavailable or its use is harmful (illness, extreme cold).",
            ),
            LearnStep(
                "التراب الطهور", "Pure earth",
                "ضرب الأرض الطاهرة ذات الغبار بكفيك مرة.",
                "Strike pure dusty earth once with both palms.",
            ),
            LearnStep(
                "مسح الوجه واليدين", "Wipe face and hands",
                "مسح الوجه ثم الكفين إلى الرسغين (والذكر المشهور إلى المرفقين في مذهب الحنابلة).",
                "Wipe the face, then the hands — to the wrists (Hanbali: to the elbows).",
            ),
        ),
        differences = listOf(
            MadhhabNote(
                "حد مسح اليدين: إلى الرسغين عند الجمهور، وإلى المرفقين عند الحنابلة.",
                "Hands wiped to the wrists (majority) or elbows (Hanbali).",
            ),
            MadhhabNote(
                "الضربات: ضربة واحدة عند الجمهور، وضربتان عند الحنفية.",
                "One strike (majority) vs two strikes (Hanafi).",
            ),
        ),
    )

    private val salah = LearnTopic(
        id = "salah",
        titleAr = "الصلوات الخمس",
        titleEn = "The five daily prayers",
        steps = listOf(
            LearnStep(
                "الركعات", "Rak'ah counts",
                "الفجر ٢، الظهر ٤، العصر ٤، المغرب ٣، العشاء ٤ — والجمعة بدل الظهر ركعتان مع الخطبة.",
                "Fajr 2, Dhuhr 4, Asr 4, Maghrib 3, Isha 4 — Jumu'ah replaces Dhuhr with 2 plus the khutbah.",
            ),
            LearnStep(
                "شروط الصلاة", "Preconditions",
                "الطهارة من الحدث والخبث، وستر العورة، واستقبال القبلة، ودخول الوقت، والنيّة.",
                "Purity, covering the awrah, facing the qiblah, time entry, and intention.",
            ),
            LearnStep(
                "تكبيرة الإحرام والقيام", "Opening takbir and standing",
                "رفع اليدين والتكبير: «الله أكبر» ثم قراءة الفاتحة في القيام.",
                "Raise the hands, say Allahu akbar, then recite al-Fatihah while standing.",
            ),
            LearnStep(
                "الركوع", "Bowing",
                "الانحناء مع تسبيح: «سبحان ربي العظيم».",
                "Bow saying: Subhana Rabbiya-l-'Adhim.",
            ),
            LearnStep(
                "الرفع والاعتدال", "Rising",
                "«سمع الله لمن حمده، ربنا ولك الحمد».",
                "Rise saying: Sami'a Allahu liman hamidah, Rabbana wa lakal-hamd.",
            ),
            LearnStep(
                "السجود", "Prostration",
                "السجود على سبعة أعضاء مع: «سبحان ربي الأعلى»، ثم الجلوس بين السجدتين، ثم السجود الثاني.",
                "Prostrate on seven limbs saying Subhana Rabbiya-l-A'la, sit between the two prostrations, then prostrate again.",
            ),
            LearnStep(
                "التشهد والتسليم", "Tashahhud and taslim",
                "التشهد في الجلسة الأخيرة ثم الصلاة الإبراهيمية ثم التسليمتان.",
                "Final tashahhud, salawat Ibrahimyyah, then the two taslims.",
            ),
        ),
        differences = listOf(
            MadhhabNote(
                "وضع اليدين: على الصدر عند الشافعية والحنابلة، وتحت السرة عند الحنفية، ومرسلتان عند المالكية — وكلها مروية.",
                "Hand placement: chest (Shafi'i/Hanbali), below navel (Hanafi), or released (Maliki) — all narrated.",
            ),
            MadhhabNote(
                "البسملة: يجهرب بها الشافعية، ويسر بها الحنفية والحنابلة، وتُقرأ سورة لا الفاتحة عند المالكية قبلها.",
                "Basmalah: aloud per Shafi'is, quietly per Hanafis/Hanbalis.",
            ),
            MadhhabNote(
                "الفاتحة على المأموم: تلزمه عند الشافعية والحنابلة، وتسقط عنه عند الحنفية والمالكية لقراءة الإمام.",
                "Fatihah for the follower: required (Shafi'i/Hanbali) or waived behind the imam (Hanafi/Maliki).",
            ),
            MadhhabNote(
                "قنوت الفجر: مستحب عند الحنفية، ومذهب الشافعية في قنوت الصبح مشهور، ولا يُستحب عند المالكية والحنابلة في الفريضة غير الوتر.",
                "Qunut of Fajr: recommended per Hanafis (and one Shafi'i view), not practiced per Malikis/Hanbalis.",
            ),
            MadhhabNote(
                "التشهد: أصح صيغه ما ثبت عن ابن مسعود في البخاري ومسلم، وهي المعتمدة عند المذاهب الأربعة، وما عداها من الصيغ الصحيحة يجوز العمل به عند الجمهور.",
                "The tashahhud from Ibn Mas'ud (Bukhari & Muslim) is relied upon by all four schools; other authentic wordings are accepted by the majority.",
            ),
        ),
    )

    private val specialPrayers = LearnTopic(
        id = "special_prayers",
        titleAr = "صلوات خاصة — جدول مرجعي",
        titleEn = "Special prayers — quick reference",
        steps = listOf(
            LearnStep(
                "الجمعة", "Jumu'ah",
                "ركعتان بعد الخطبة بدل صلاة الظهر، مع غسل وتبكير وإنصات للخطبة.",
                "Two rak'ahs after the khutbah, replacing Dhuhr; ghusl, early arrival and listening are emphasized.",
            ),
            LearnStep(
                "التراويح", "Tarawih",
                "صلاة الليل في رمضان بعد العشاء، وإحدى عشرة وثلاث عشرة ركعة هي المشهور من فعل النبي ﷺ، والزيادة عليها جائزة عند الجمهور.",
                "Night prayer in Ramadan after Isha; 11 or 13 rak'ahs are the prophetic practice; more is permitted.",
            ),
            LearnStep(
                "الوتر", "Witr",
                "ركعة واحدة أو ثلاث بعد صلاة الليل، ختم الليل بوتر مستحب.",
                "One or three rak'ahs closing the night prayer.",
            ),
            LearnStep(
                "صلاة الجنازة", "Janazah",
                "أربع تكبيرات: الفاتحة ثم الصلاة على النبي ﷺ ثم الدعاء للميت ثم السلام — بلا ركوع ولا سجود.",
                "Four takbirs with Fatihah, salawat, du'a for the deceased, then taslim — no bowing/prostration.",
            ),
            LearnStep(
                "صلاة العيدين", "Eid prayers",
                "ركعتان بتكبيرات زوائد: الأولى سبعًا والثانية خمسًا بعد القيام، ثم خطبة.",
                "Two rak'ahs with extra takbirs (7 then 5), followed by a khutbah.",
            ),
            LearnStep(
                "الاستخارة", "Istikharah",
                "ركعتان ثم دعاء الاستخارة الوارد عند التردد في أمر مباح.",
                "Two rak'ahs then the transmitted istikharah du'a when undecided over a permissible matter.",
            ),
            LearnStep(
                "الضحى", "Duha",
                "ركعتان فأكثر بعد ارتفاع الشمس إلى قبيل الزوال.",
                "Two or more rak'ahs after the sun rises until before noon.",
            ),
            LearnStep(
                "التهجد", "Tahajjud",
                "صلاة الليل بعد استيقاظ من نوم، وأفضل وقت الثلث الأخير.",
                "Night prayer after sleeping; the last third of the night is best.",
            ),
            LearnStep(
                "صلاة المسافر (القصر والجمع)", "Traveler's prayer",
                "قصر الرباعية إلى ركعتين يشرع عند الجمهور في السفر المبيح، ويجوز الجمع بين الظهرين والعشاءين للحاجة.",
                "Shortening 4-rak'ah prayers to 2 in valid travel (majority); combining Dhuhr/Asr and Maghrib/Isha when needed.",
            ),
        ),
        differences = listOf(
            MadhhabNote(
                "مدة القصر: ثلاثة أيام عند الحنفية والحنابلة، وخمسة عشر يومًا عند الشافعية، وأربعة عند المالكية.",
                "Duration permitting qasr: 3 days (Hanafi/Hanbali), 15 (Shafi'i), 4 (Maliki).",
            ),
            MadhhabNote(
                "عدد التراويح: إحدى عشرة وثلاث عشرة هي الواردة عن النبي ﷺ وعليها الجمهور، وعشرون وثلاثون مشهورة في عمل الأمة عند الحنفية وغيرهم.",
                "Tarawih count: 11/13 are the prophetic narrations adopted by the majority; 20/30 established in Hanafi and wider practice.",
            ),
        ),
    )
}
