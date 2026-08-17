package org.muslim.app.feature.reference.domain

/**
 * كتاب «السيرة النبوية» — سيرة النبي محمد ﷺ كاملةً مرتبةً زمنيًا، من مولده
 * إلى وفاته، مبنية على المصادر المعتمدة (ابن هشام، الطبري، البخاري، مسلم
 * وغيرها). تخضع للمراجعة الشرعية قبل النشر (§10).
 */
object SiraContent {

    val book: ReferenceBook = ReferenceBook(
        id = "sira",
        titleAr = "السيرة النبوية",
        titleEn = "The Prophetic Biography",
        subtitleAr = "سيرة النبي محمد ﷺ كاملة مرتبة زمنيًا",
        subtitleEn = "The complete biography of Prophet Muhammad ﷺ, arranged chronologically",
        topics = listOf(
            RefTopic(
                id = "lineage_birth",
                titleAr = "نسب النبي ﷺ ومولده",
                titleEn = "The Prophet's lineage and birth",
                summaryAr = "نسب شريف يصل إلى إسماعيل بن إبراهيم، وولد يتيمًا في عام الفيل.",
                summaryEn = "A noble lineage reaching Ismail son of Abraham; he was born an orphan in the Year of the Elephant.",
                sections = listOf(
                    RefSection(
                        id = "lineage",
                        titleAr = "النسب الشريف",
                        titleEn = "The noble lineage",
                        paragraphs = listOf(
                            RefParagraph(
                                "هو محمد بن عبد الله بن عبد المطلب بن هاشم بن عبد مناف بن قصي بن كلاب بن مرة بن كعب بن لؤي بن غالب بن فهر بن مالك بن النضر بن كنانة بن خزيمة بن مدركة بن إلياس بن مضر بن نزار بن معد بن عدنان، وينتهي نسبه إلى إسماعيل بن إبراهيم عليهما السلام.",
                                "He is Muhammad ibn Abdullah ibn Abdul-Muttalib ibn Hashim ibn Abd Manaf ibn Qusayy ibn Kilab ibn Murrah ibn Ka'b ibn Lu'ayy ibn Ghalib ibn Fihr ibn Malik ibn an-Nadr ibn Kinanah ibn Khuzaymah ibn Mudrikah ibn Ilyas ibn Mudar ibn Nizar ibn Ma'ad ibn Adnan, with his lineage reaching Ismail son of Abraham, peace be upon them.",
                            ),
                            RefParagraph(
                                "وُلد ﷺ في مكة في عام الفيل (حوالي 570م)، في شهر ربيع الأول، يوم الاثنين — وهو اليوم الذي قال فيه ﷺ: «ذلك يوم وُلدت فيه». وقد توفي أبوه عبد الله قبل ولادته.",
                                "He ﷺ was born in Mecca in the Year of the Elephant (about 570 CE), in the month of Rabi' al-Awwal, on a Monday — the day about which he said: “That is the day on which I was born.” His father Abdullah had died before his birth.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "birth",
                        titleAr = "المولد والرضاعة",
                        titleEn = "Birth and suckling",
                        paragraphs = listOf(
                            RefParagraph(
                                "أرضعته في البادية حليمة السعدية، فنشأ في بني سعد فصيح اللسان قوي البنية. ثم عاد إلى أمه آمنة، فلما بلغ ست سنوات توفيت أمه بالأبواء، فكفله جده عبد المطلب.",
                                "He was suckled in the desert by Halimah al-Sa'diyyah and grew up among Banu Sa'd, eloquent and strong. He returned to his mother Aminah, and when he was six she died at al-Abwa, so his grandfather Abdul-Muttalib took him in.",
                            ),
                            RefParagraph(
                                "توفي عبد المطلب وبقي النبي ﷺ في كفالة عمه أبي طالب وهو في الثامنة، فحفظه ورعاه أحسن رعاية، وكان النبي ﷺ يرعى الغنم لأهل مكة وهو صغير.",
                                "Abdul-Muttalib died and the Prophet ﷺ, at eight years old, came under the care of his uncle Abu Talib, who protected and raised him well. As a youth the Prophet ﷺ shepherded flocks for the people of Mecca.",
                            ),
                        ),
                    ),
                ),
            ),
            RefTopic(
                id = "youth",
                titleAr = "نشأته قبل البعثة",
                titleEn = "His life before the mission",
                summaryAr = "شباب صادق أمين، اتُّهم بالكذب قط، وشارك في حلف الفضول وبناء الكعبة.",
                summaryEn = "A truthful, trustworthy youth, never accused of lying; he took part in the Pact of the Virtuous and the rebuilding of the Kaaba.",
                sections = listOf(
                    RefSection(
                        id = "youth",
                        titleAr = "شبابه",
                        titleEn = "His youth",
                        paragraphs = listOf(
                            RefParagraph(
                                "عُرف ﷺ في مكة بالصادق الأمين، فكان الناس يودعون عنده أماناتهم. عمل في التجارة، وسافر إلى الشام في تجارة خديجة رضي الله عنها، فأعجبته أمانته وصدقه.",
                                "In Mecca he ﷺ was known as the truthful and trustworthy, and people entrusted their belongings to him. He worked in trade and travelled to Syria with Khadijah's caravan (may God be pleased with her), who was impressed by his honesty and truthfulness.",
                            ),
                            RefParagraph(
                                "شارك ﷺ في «حلف الفضول» الذي تعاهد فيه أهل مكة على نصرة المظلوم وردّ الحقوق، وقال عنه بعد البعثة: «حضرت في دار عبد الله بن جدعان حلفًا ما أحب أن لي به حمر النعم».",
                                "He ﷺ took part in the “Pact of the Virtuous,” in which the people of Mecca pledged to support the oppressed and restore rights. After his mission he said: “I attended a pact in the house of Abdullah ibn Jud'an which I would not exchange for red camels.”",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "marriage",
                        titleAr = "زواجه من خديجة",
                        titleEn = "His marriage to Khadijah",
                        paragraphs = listOf(
                            RefParagraph(
                                "تزوج ﷺ خديجة بنت خويلد رضي الله عنها وهو في الخامسة والعشرين وهي في الأربعين، فكانت أول من آمن به وأعظم الناس له معونةً ومواساةً، وأنجبت له أولاده كلهم إلا إبراهيم.",
                                "He ﷺ married Khadijah bint Khuwaylid (may God be pleased with her) at twenty-five while she was forty. She was the first to believe in him and his greatest supporter and comforter, and bore him all his children except Ibrahim.",
                            ),
                            RefParagraph(
                                "كان ﷺ قبل البعثة يتعبد في غار حراء، يتحنث الليالي ذوات العدد، يتأمل في خلق السماوات والأرض، حتى جاءه الحق وهو في الأربعين.",
                                "Before the mission he ﷺ used to worship alone in the cave of Hira, contemplating the creation of the heavens and the earth, until the truth came to him at the age of forty.",
                            ),
                        ),
                    ),
                ),
            ),
            RefTopic(
                id = "revelation",
                titleAr = "البدء بالوحي",
                titleEn = "The beginning of revelation",
                summaryAr = "نزول جبريل في غار حراء بأول آيات سورة العلق.",
                summaryEn = "Gabriel descended in the cave of Hira with the first verses of Surah al-Alaq.",
                sections = listOf(
                    RefSection(
                        id = "first_revelation",
                        titleAr = "أول الوحي",
                        titleEn = "The first revelation",
                        paragraphs = listOf(
                            RefParagraph(
                                "في ليلة القدر من رمضان، نزل جبريل على النبي ﷺ في غار حراء وقال: «اقرأ»، فقال: «ما أنا بقارئ»، حتى قال له: «اقْرَأْ بِاسْمِ رَبِّكَ الَّذِي خَلَقَ * خَلَقَ الْإِنْسَانَ مِنْ عَلَقٍ * اقْرَأْ وَرَبُّكَ الْأَكْرَمُ».",
                                "On the Night of Decree in Ramadan, Gabriel descended upon the Prophet ﷺ in the cave of Hira and said: “Recite.” He replied: “I am not one who recites,” until Gabriel said: “Recite in the name of your Lord who created — created man from a clinging clot. Recite, and your Lord is the most Generous” (Quran 96:1–3).",
                            ),
                            RefParagraph(
                                "رجع ﷺ إلى خديجة ترجف بوادره، فقالت له: «كلا والله ما يخزيك الله أبدًا؛ إنك لتصل الرحم، وتحمل الكل، وتكسب المعدوم، وتقري الضيف، وتعين على نوائب الحق». ثم ذهب به ورقة بن نوفل الذي بشرّه بأنه نبي هذه الأمة.",
                                "He ﷺ returned to Khadijah with his heart trembling, and she said: “No, by God, God will never disgrace you; you keep family ties, carry the burden of others, earn for the destitute, honour the guest and help in the causes of truth.” She took him to Waraqah ibn Nawfal, who told him he was the prophet of this nation.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "fathra",
                        titleAr = "فترة الوحي",
                        titleEn = "The pause of revelation",
                        paragraphs = listOf(
                            RefParagraph(
                                "بعد أول نزول فتر الوحي فترة، فحزن النبي ﷺ حزنًا شديدًا، حتى نزل قوله تعالى: «وَالضُّحَىٰ * وَاللَّيْلِ إِذَا سَجَىٰ * مَا وَدَّعَكَ رَبُّكَ وَمَا قَلَىٰ».",
                                "After the first revelation the revelation paused for a time, and the Prophet ﷺ grieved deeply until God revealed: “By the morning brightness, and by the night when it covers with darkness, your Lord has not taken leave of you, nor has He detested you” (Quran 93:1–3).",
                            ),
                            RefParagraph(
                                "ثم تتابع الوحي، وكان أول ما أُمر به بعد النبوة الصلاة، وكان جبريل يعلمه الوضوء والصلاة.",
                                "Then revelation followed in succession, and the first thing commanded after prophethood was prayer, with Gabriel teaching him ablution and prayer.",
                            ),
                        ),
                    ),
                ),
            ),
            RefTopic(
                id = "secret_call",
                titleAr = "الدعوة السرية",
                titleEn = "The secret call",
                summaryAr = "بدأ النبي ﷺ دعوته سرًا، فأسلم الخواص من أهل بيته وأصحابه.",
                summaryEn = "The Prophet ﷺ began his call in secret, and the elite of his family and companions embraced Islam.",
                sections = listOf(
                    RefSection(
                        id = "first_believers",
                        titleAr = "أوائل المسلمين",
                        titleEn = "The first Muslims",
                        paragraphs = listOf(
                            RefParagraph(
                                "أول من آمن من الرجال أبو بكر الصديق، ومن النساء خديجة، ومن الصبيان علي بن أبي طالب، ومن الموالي زيد بن حارثة. ثم أسلم على يد أبي بكر عثمان والزبير وعبد الرحمن بن عوف وسعد بن أبي وقاص وطلحة وغيرهم.",
                                "The first to believe among men was Abu Bakr al-Siddiq, among women Khadijah, among youths Ali ibn Abi Talib, and among freed slaves Zayd ibn Harithah. Through Abu Bakr, Uthman, al-Zubayr, Abd al-Rahman ibn Awf, Sa'd ibn Abi Waqqas, Talhah and others embraced Islam.",
                            ),
                            RefParagraph(
                                "كان النبي ﷺ يجتمع بالمسلمين سرًا في دار الأرقم بن أبي الأرقم، يعلّمهم القرآن ويصلي بهم، مدة ثلاث سنين تقريبًا.",
                                "The Prophet ﷺ gathered with the Muslims secretly in the house of al-Arqam ibn Abi al-Arqam, teaching them the Quran and leading them in prayer, for about three years.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "dar_arqam",
                        titleAr = "دار الأرقم",
                        titleEn = "The house of al-Arqam",
                        paragraphs = listOf(
                            RefParagraph(
                                "دار الأرقم كانت مدرسة النبوة الأولى، خرج منها جيل الصحابة الأول الذين حملوا الدعوة، ومنهم عمر بن الخطاب الذي أسلم بعد ثلاث سنين من البعثة فقويت به الدعوة.",
                                "The house of al-Arqam was the first school of prophethood, from which emerged the first generation of companions who carried the call, including Umar ibn al-Khattab, whose conversion three years after the mission strengthened the call.",
                            ),
                            RefParagraph(
                                "لمّا كثر المسلمون، أمر الله نبيه ﷺ بالجهر بالدعوة: «فَاصْدَعْ بِمَا تُؤْمَرُ وَأَعْرِضْ عَنِ الْمُشْرِكِينَ».",
                                "When the Muslims grew numerous, God commanded His Prophet to proclaim the call openly: “Then proclaim what you are commanded and turn away from the polytheists” (Quran 15:94).",
                            ),
                        ),
                    ),
                ),
            ),
            RefTopic(
                id = "public_call",
                titleAr = "الدعوة الجهرية",
                titleEn = "The public call",
                summaryAr = "صعد النبي ﷺ الصفا ودعا قومه، فقوبل بالصدود والتعذيب.",
                summaryEn = "The Prophet ﷺ climbed Mount Safa and called his people, meeting rejection and persecution.",
                sections = listOf(
                    RefSection(
                        id = "safa",
                        titleAr = "نداء الصفا",
                        titleEn = "The call at Safa",
                        paragraphs = listOf(
                            RefParagraph(
                                "صعد النبي ﷺ على الصفا ونادى: «يا صباحاه»، فلما اجتمع الناس قال: «أرأيتم لو أخبرتكم أن خيلًا تخرج من سفح هذا الجبل أكنتم مصدقي؟» قالوا: نعم. قال: «فإني نذير لكم بين يدي عذاب شديد». فقام أبو لهب وقال: «تبًّا لك».",
                                "The Prophet ﷺ climbed Safa and called: “O people of the morning!” When the people gathered he said: “If I told you that horsemen were about to emerge from the foot of this mountain, would you believe me?” They said yes. He said: “Then I am a warner to you before a severe punishment.” Abu Lahab stood and said: “May you perish.”",
                            ),
                            RefParagraph(
                                "نزلت في أبي لهب سورة المسد: «تَبَّتْ يَدَا أَبِي لَهَبٍ وَتَبَّ». وبدأت الدعوة الجهرية، فأسلم كثيرون، وكذّب كثيرون.",
                                "Surah al-Masad was revealed about Abu Lahab: “May the hands of Abu Lahab be ruined, and ruined is he” (Quran 111:1). The public call began; many believed and many denied.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "persecution",
                        titleAr = "الأذى والتعذيب",
                        titleEn = "Persecution and torture",
                        paragraphs = listOf(
                            RefParagraph(
                                "عذب المشركون المستضعفين من المسلمين: بلال كان يُضطجع على الرمضاء ويوضع الصخر على صدره وهو يقول: «أحد أحد». وعذبت عائلة ياسر حتى قُتل ياسر وسُمية — أول شهيدة في الإسلام.",
                                "The polytheists tortured the weak among the Muslims: Bilal was laid on the scorching sand with a rock on his chest, repeating: “One, One.” The family of Yasir was tortured until Yasir and Sumayyah were killed — she being the first martyr in Islam.",
                            ),
                            RefParagraph(
                                "أمر النبي ﷺ أصحابه بالهجرة إلى الحبشة فرارًا بدينهم، وقال: «إن بها ملكًا لا يظلم عنده أحد».",
                                "The Prophet ﷺ ordered his companions to migrate to Abyssinia to save their religion, saying: “There is a king there with whom no one is wronged.”",
                            ),
                        ),
                    ),
                ),
            ),
            RefTopic(
                id = "abyssinia",
                titleAr = "الهجرة إلى الحبشة",
                titleEn = "The migration to Abyssinia",
                summaryAr = "هاجر المسلمون إلى الحبشة، وحمى النجاشي المستضعفين.",
                summaryEn = "Muslims migrated to Abyssinia, and the Negus protected the oppressed.",
                sections = listOf(
                    RefSection(
                        id = "first_second",
                        titleAr = "الهجرتان",
                        titleEn = "The two migrations",
                        paragraphs = listOf(
                            RefParagraph(
                                "هاجرت مجموعة إلى الحبشة في السنة الخامسة من البعثة، ثم هاجرت مجموعة ثانية أكبر، فاجتمع فيها نحو ثمانين رجلًا وامرأة، منهم جعفر بن أبي طالب.",
                                "A group migrated to Abyssinia in the fifth year of the mission, followed by a larger second group, numbering about eighty men and women, including Ja'far ibn Abi Talib.",
                            ),
                            RefParagraph(
                                "أرسلت قريش عمرو بن العاص وعبد الله بن أبي ربيعة إلى النجاشي ليُعيدوا المهاجرين، فسألهم النجاشي عن دينهم، فتلا جعفر صدر سورة مريم، فبكى النجاشي وقال: «إن هذا والذي جاء به عيسى ليخرج من مشكاة واحدة».",
                                "Quraysh sent Amr ibn al-As and Abdullah ibn Abi Rabi'ah to the Negus to bring the migrants back. The Negus asked them about their religion, and Ja'far recited the opening of Surah Maryam; the Negus wept and said: “This and what Jesus brought come from the same source.”",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "negus",
                        titleAr = "عدالة النجاشي",
                        titleEn = "The justice of the Negus",
                        paragraphs = listOf(
                            RefParagraph(
                                "رفض النجاشي تسليم المهاجرين، وقال: «لا والله لا أسلمهم إليه أبدًا». وعاش المسلمون في الحبشة آمنين حتى عادوا بعد الهجرة إلى المدينة.",
                                "The Negus refused to hand over the migrants, saying: “By God, I will never hand them over to him.” The Muslims lived safely in Abyssinia until they returned after the migration to Medina.",
                            ),
                            RefParagraph(
                                "هذه الهجرة أظهرت سماحة الإسلام وكرم أخلاقه، وأن الإسلام لا يفرض نفسه، وأنه يحترم الأديان والملوك العادلين.",
                                "This migration showed Islam's tolerance and noble ethics — that Islam does not impose itself and respects religions and just rulers.",
                            ),
                        ),
                    ),
                ),
            ),
            RefTopic(
                id = "year_of_grief",
                titleAr = "عام الحزن",
                titleEn = "The Year of Grief",
                summaryAr = "وفاة خديجة وأبي طالب في عام واحد، فاشتد أذى قريش.",
                summaryEn = "The deaths of Khadijah and Abu Talib in one year, after which Quraysh's persecution intensified.",
                sections = listOf(
                    RefSection(
                        id = "losses",
                        titleAr = "وفاة خديجة وأبي طالب",
                        titleEn = "The death of Khadijah and Abu Talib",
                        paragraphs = listOf(
                            RefParagraph(
                                "في السنة العاشرة من البعثة توفيت خديجة رضي الله عنها، وكانت سند النبي ﷺ وناصره، ثم توفي عمه أبو طالب الذي كان يحميه من قريش. فسمّى النبي ﷺ ذلك العام «عام الحزن».",
                                "In the tenth year of the mission Khadijah (may God be pleased with her) died — she had been the Prophet's support — then his uncle Abu Talib, who had protected him from Quraysh, also died. The Prophet ﷺ called that year the “Year of Grief.”",
                            ),
                            RefParagraph(
                                "بعد وفاة أبي طالب اشتد أذى قريش للنبي ﷺ، حتى إن سفيهًا منهم نثر التراب على رأسه ﷺ، فجعل يدخل بيته والتراب على رأسه.",
                                "After Abu Talib's death Quraysh's persecution of the Prophet intensified, to the point that a foolish man threw dust on his head, and he entered his house with dust upon it.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "taif",
                        titleAr = "الذهاب إلى الطائف",
                        titleEn = "The journey to Ta'if",
                        paragraphs = listOf(
                            RefParagraph(
                                "خرج النبي ﷺ إلى الطائف يدعو ثقيفًا إلى الإسلام، فقابلوه بأسوأ رد: كذبوه وآذوه وأغروا به سفهاءهم، فرموه بالحجارة حتى أدموا عقبيه.",
                                "The Prophet ﷺ went to Ta'if calling Thaqif to Islam, and they met him with the worst response: they denied him, harmed him and set their foolish ones upon him, stoning him until his heels bled.",
                            ),
                            RefParagraph(
                                "في طريقه عائدًا رفع يديه يدعو: «اللهم إليك أشكو ضعف قوتي وقلة حيلتي وهواني على الناس...»، فبعث الله إليه ملك الجبال يستأذنه أن يطبق الجبلين على أهل مكة، فقال: «بل أرجو أن يخرج الله من أصلابهم من يعبد الله وحده لا يشرك به شيئًا».",
                                "On his way back he raised his hands and prayed: “O God, to You I complain of my weakness, my lack of means, and my insignificance before the people...” God sent him the angel of the mountains, who asked permission to crush the people of Mecca between the two mountains. He replied: “Rather, I hope God will bring forth from their loins those who worship God alone, associating nothing with Him.”",
                            ),
                        ),
                    ),
                ),
            ),
            RefTopic(
                id = "isra_miraj",
                titleAr = "الإسراء والمعراج",
                titleEn = "The Night Journey and Ascension",
                summaryAr = "رحلة الإسراء من مكة إلى القدس، والمعراج إلى السماوات، وفُرضت فيه الصلوات الخمس.",
                summaryEn = "The journey from Mecca to Jerusalem, the ascension through the heavens, and the five daily prayers were prescribed.",
                sections = listOf(
                    RefSection(
                        id = "isra",
                        titleAr = "الإسراء",
                        titleEn = "The Night Journey",
                        paragraphs = listOf(
                            RefParagraph(
                                "أسرى الله بنبيه ﷺ ليلًا من المسجد الحرام إلى المسجد الأقصى: «سُبْحَانَ الَّذِي أَسْرَىٰ بِعَبْدِهِ لَيْلًا مِنَ الْمَسْجِدِ الْحَرَامِ إِلَى الْمَسْجِدِ الْأَقْصَىٰ». وقد صلى ﷺ بالأنبياء إمامًا في بيت المقدس.",
                                "God took His servant by night from the Sacred Mosque to the Farthest Mosque: “Exalted is He who took His servant by night from al-Masjid al-Haram to al-Masjid al-Aqsa” (Quran 17:1). In Jerusalem the Prophet ﷺ led the prophets in prayer.",
                            ),
                            RefParagraph(
                                "كانت رحلة الإسراء والمعراج في السنة الحادية عشرة من البعثة، قبل الهجرة بسنة تقريبًا، وكانت تسليةً للنبي ﷺ بعد عام الحزن.",
                                "The Night Journey and Ascension took place in the eleventh year of the mission, about a year before the migration, and was a comfort to the Prophet after the Year of Grief.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "miraj",
                        titleAr = "المعراج وفرض الصلاة",
                        titleEn = "The Ascension and the prescription of prayer",
                        paragraphs = listOf(
                            RefParagraph(
                                "عرج بالنبي ﷺ إلى السماوات السبع، فرأى الأنبياء فيها، وفُرضت عليه الصلوات الخمس — بعد أن كانت خمسين ثم خُففت إلى خمس بأمر الله — وهي الخمس التي تجري أجرًا بخمسين.",
                                "The Prophet ﷺ was raised through the seven heavens, saw the prophets, and the five prayers were prescribed — originally fifty, then reduced to five by God's command — five in number but rewarded as fifty.",
                            ),
                            RefParagraph(
                                "في المعراج رأى النبي ﷺ الجنة والنار، وجاء في الحديث قوله: «أُتيت بالبراق... فعرج بي إلى السماء الدنيا».",
                                "During the Ascension the Prophet ﷺ saw Paradise and Hell; the hadith relates: “The Buraq was brought to me... and I was taken up to the lowest heaven.”",
                            ),
                        ),
                    ),
                ),
            ),
            RefTopic(
                id = "aqaba_migration",
                titleAr = "بيعتا العقبة والهجرة إلى المدينة",
                titleEn = "The pledges of Aqaba and the migration to Medina",
                summaryAr = "أسلم أهل يثرب، وبايعوا النبي ﷺ، فهاجر إليهم فأنشأ دولة الإسلام.",
                summaryEn = "The people of Yathrib embraced Islam and pledged to the Prophet, so he migrated to them and established the state of Islam.",
                sections = listOf(
                    RefSection(
                        id = "aqaba",
                        titleAr = "بيعتا العقبة",
                        titleEn = "The two pledges of Aqaba",
                        paragraphs = listOf(
                            RefParagraph(
                                "في موسم الحج قابل النبي ﷺ نفرًا من الخزرج، فأسلموا، ثم جاءت بيعة العقبة الأولى (12 من البعثة) التي بايعوا فيها على الإسلام، ثم بيعة العقبة الثانية (13) التي بايعوا فيها على النصرة والمنعة كبيعة الحرب.",
                                "During the pilgrimage season the Prophet met a group of Khazraj who embraced Islam. Then came the first pledge of Aqaba (year 12), pledging to Islam, and the second pledge of Aqaba (year 13), pledging to support and defend him as a pledge of war.",
                            ),
                            RefParagraph(
                                "بعد البيعة الثانية أذن النبي ﷺ لأصحابه بالهجرة إلى المدينة، فهاجروا أرسالًا، وبقي هو وأبو بكر وعلي رضي الله عنهما.",
                                "After the second pledge the Prophet ﷺ permitted his companions to migrate to Medina, and they went in groups while he, Abu Bakr and Ali (may God be pleased with them) remained.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "hijrah",
                        titleAr = "الهجرة النبوية",
                        titleEn = "The Prophetic migration",
                        paragraphs = listOf(
                            RefParagraph(
                                "خرج النبي ﷺ وأبو بكر إلى غار ثور، فمكثا فيه ثلاث ليالٍ، وطاردتهم قريش، فقال أبو بكر: «لو نظر أحدهم تحت قدميه لأبصرنا»، فقال ﷺ: «ما ظنك يا أبا بكر باثنين الله ثالثهما». فنزلت: «إِلَّا تَنْصُرُوهُ فَقَدْ نَصَرَهُ اللَّهُ».",
                                "The Prophet ﷺ and Abu Bakr set out to the cave of Thawr, staying three nights while Quraysh pursued them. Abu Bakr said: “If one of them looked beneath his feet he would see us.” He ﷺ replied: “What do you think, Abu Bakr, of two with whom God is the third?” And it was revealed: “If you do not aid him, God has already aided him” (Quran 9:40).",
                            ),
                            RefParagraph(
                                "وصل النبي ﷺ إلى قباء فبنى مسجد قباء — أول مسجد في الإسلام — ثم دخل المدينة، وأرخّ المسلمون بالهجرة فجعلوها بداية التقويم الهجري.",
                                "The Prophet ﷺ reached Quba and built the mosque of Quba — the first mosque in Islam — then entered Medina. Muslims later took the migration as the start of the Hijri calendar.",
                            ),
                        ),
                    ),
                ),
            ),
            RefTopic(
                id = "medina_state",
                titleAr = "بناء المجتمع المدني",
                titleEn = "Building the Medinan society",
                summaryAr = "المسجد والمؤاخاة بين المهاجرين والأنصار، ودستور المدينة.",
                summaryEn = "The mosque, the brotherhood between the migrants and the helpers, and the Constitution of Medina.",
                sections = listOf(
                    RefSection(
                        id = "mosque_brotherhood",
                        titleAr = "المسجد والمؤاخاة",
                        titleEn = "The mosque and brotherhood",
                        paragraphs = listOf(
                            RefParagraph(
                                "أول ما فعله النبي ﷺ في المدينة بناء المسجد النبوي، ثم آخى بين المهاجرين والأنصار، فكان الأنصار يقاسمون إخوانهم المهاجرين الأموال والمساكن، حتى قال المهاجرون: «ما رأينا قومًا أطيب بذلًا من الأنصار».",
                                "The first thing the Prophet ﷺ did in Medina was build the Prophet's Mosque, then he established brotherhood between the migrants and the helpers (Ansar), who shared their wealth and homes with their migrant brothers — so generous that the migrants said: “We have not seen a people more generous than the Ansar.”",
                            ),
                            RefParagraph(
                                "جعل النبي ﷺ الأذان شعارًا للصلاة بعد أن رأى الرؤيا في ذلك، وبنى السوق، ونظّم شؤون المدينة.",
                                "The Prophet ﷺ established the call to prayer (adhan) as the symbol of prayer after the vision regarding it, built the market, and organised the affairs of the city.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "constitution",
                        titleAr = "دستور المدينة",
                        titleEn = "The Constitution of Medina",
                        paragraphs = listOf(
                            RefParagraph(
                                "كتب النبي ﷺ وثيقة المدينة التي نظمت العلاقة بين المسلمين واليهود وسائر السكان، فجعلتهم أمة واحدة، وكفل حرية الدين، والدفاع المشترك عن المدينة، وتحكيم النبي ﷺ في النزاعات.",
                                "The Prophet ﷺ wrote the document of Medina regulating relations between Muslims, Jews and all residents, making them one nation, guaranteeing freedom of religion, joint defence of the city, and arbitration by the Prophet in disputes.",
                            ),
                            RefParagraph(
                                "هذه الوثيقة من أوائل الدساتير المكتوبة في التاريخ، وتدل على عدالة الإسلام وسماحته في التعامل مع غير المسلمين.",
                                "This document is among the earliest written constitutions in history and shows Islam's justice and tolerance in dealing with non-Muslims.",
                            ),
                        ),
                    ),
                ),
            ),
            RefTopic(
                id = "major_battles",
                titleAr = "الغزوات الكبرى",
                titleEn = "The major expeditions",
                summaryAr = "بدر وأحد والخندق: محطات فاصلة في صراع الدعوة.",
                summaryEn = "Badr, Uhud and the Trench: decisive moments in the struggle of the call.",
                sections = listOf(
                    RefSection(
                        id = "badr",
                        titleAr = "غزوة بدر",
                        titleEn = "The Battle of Badr",
                        paragraphs = listOf(
                            RefParagraph(
                                "في رمضان من السنة الثانية للهجرة، التقى المسلمون (313 رجلًا) بجيش قريش (نحو 1000) عند بئر بدر، فنصرهم الله نصرًا مبينًا، وقُتل صناديد قريش وأُسر كثيرون، وفدى النبي ﷺ الأسرى تعليمًا للقراءة.",
                                "In Ramadan of the second year of the Hijra, the Muslims (313 men) met Quraysh's army (about 1,000) at the well of Badr, and God granted them a decisive victory. The leaders of Quraysh were killed and many taken captive; the Prophet ﷺ ransomed captives by teaching reading.",
                            ),
                            RefParagraph(
                                "في بدر نزل قوله تعالى: «وَلَقَدْ نَصَرَكُمُ اللَّهُ بِبَدْرٍ وَأَنْتُمْ أَذِلَّةٌ». وكانت أول غزوة كبرى، رفعت راية الإسلام.",
                                "At Badr was revealed: “And already had God given you victory at Badr while you were weak” (Quran 3:123). It was the first major battle and raised the banner of Islam.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "uhud_khandaq",
                        titleAr = "أحد والخندق",
                        titleEn = "Uhud and the Trench",
                        paragraphs = listOf(
                            RefParagraph(
                                "في السنة الثالثة خرجت قريش (3000) إلى أحد، فانهزم الرماة وخالفوا أمر النبي ﷺ، فانكشفت المعركة واستُشهد حمزة — سيد الشهداء — وجُرح النبي ﷺ، فقال: «لن يفلح قوم آذوا نبيهم».",
                                "In year three Quraysh (3,000) marched to Uhud. The archers disobeyed the Prophet's order and the battle turned; Hamzah — the master of martyrs — was martyred and the Prophet was wounded. He said: “A people who harm their prophet will never succeed.”",
                            ),
                            RefParagraph(
                                "في السنة الخامسة تحالفت الأحزاب (10,000) على المدينة، فأمر النبي ﷺ بحفر الخندق — وكانت فكرة سلمان الفارسي — فحاصرت الأحزاب المدينة، ثم صرفهم الله بالريح والجنود: «وَكَفَى اللَّهُ الْمُؤْمِنِينَ الْقِتَالَ».",
                                "In year five the confederates (10,000) allied against Medina, and the Prophet ﷺ ordered a trench to be dug — Salman al-Farisi's idea. The confederates besieged the city, then God dispersed them with wind and soldiers: “And Allah sufficed the believers in battle” (Quran 33:25).",
                            ),
                        ),
                    ),
                ),
            ),
            RefTopic(
                id = "conquest",
                titleAr = "الحديبية وفتح مكة",
                titleEn = "Al-Hudaybiyyah and the conquest of Mecca",
                summaryAr = "صلح الحديبية ثم فتح مكة، ودخول الناس في دين الله أفواجًا.",
                summaryEn = "The treaty of Hudaybiyyah, then the conquest of Mecca, and people entering the religion of God in crowds.",
                sections = listOf(
                    RefSection(
                        id = "hudaybiyyah",
                        titleAr = "صلح الحديبية",
                        titleEn = "The treaty of Hudaybiyyah",
                        paragraphs = listOf(
                            RefParagraph(
                                "في السنة السادسة خرج النبي ﷺ معتمرًا، فصده المشركون عند الحديبية، فصالحهم على: وضع الحرب عشر سنين، ورد من جاء مسلمًا، ودخول مكة للعمرة في العام القادم. وكان هذا الصلح «فتحًا مبينًا» كما سماه الله.",
                                "In year six the Prophet ﷺ set out for Umrah, but the polytheists barred him at Hudaybiyyah, so he made a treaty: ten years of peace, return of those who came as Muslims, and entry to Mecca for Umrah the following year. God called this treaty a “clear victory.”",
                            ),
                            RefParagraph(
                                "بفضل الهدنة انتشر الإسلام، فدخل الناس في دين الله أفواجًا، وأسلم خالد بن الوليد وعمرو بن العاص وعثمان بن طلحة.",
                                "Thanks to the truce Islam spread, people entered the religion of God in crowds, and Khalid ibn al-Walid, Amr ibn al-As and Uthman ibn Talhah embraced Islam.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "conquest_mecca",
                        titleAr = "فتح مكة",
                        titleEn = "The conquest of Mecca",
                        paragraphs = listOf(
                            RefParagraph(
                                "نقضت قريش العهد، فخرج النبي ﷺ في عشرة آلاف في السنة الثامنة، ودخل مكة فاتحًا من غير قتال، وهو يقرأ: «إِنَّا فَتَحْنَا لَكَ فَتْحًا مُبِينًا».",
                                "Quraysh broke the treaty, so in year eight the Prophet ﷺ marched with ten thousand, entered Mecca as a conqueror without battle, reciting: “Indeed, We have given you a clear conquest” (Quran 48:1).",
                            ),
                            RefParagraph(
                                "وقف ﷺ على باب الكعبة وقال لأهل مكة وقد كانوا آذوه وأخرجوه: «اذهبوا فأنتم الطلقاء». ثم أمر بتحطيم الأصنام، وقال: «جاء الحق وزهق الباطل». وعفا عن جميع من آذاه — أعظم مثال على العفو عند المقدرة.",
                                "Standing at the door of the Kaaba, he ﷺ said to the people of Mecca who had harmed and expelled him: “Go, for you are free.” He ordered the idols destroyed, saying: “Truth has come and falsehood has vanished.” He forgave all who had wronged him — the greatest example of forgiveness with power.",
                            ),
                        ),
                    ),
                ),
            ),
            RefTopic(
                id = "farewell",
                titleAr = "حجة الوداع ووفاة النبي ﷺ",
                titleEn = "The Farewell Pilgrimage and the Prophet's death",
                summaryAr = "حجة الوداع والخطبة الجامعة، ثم مرض النبي ﷺ ووفاته.",
                summaryEn = "The Farewell Pilgrimage and its comprehensive sermon, then the Prophet's illness and death.",
                sections = listOf(
                    RefSection(
                        id = "farewell_hajj",
                        titleAr = "حجة الوداع",
                        titleEn = "The Farewell Pilgrimage",
                        paragraphs = listOf(
                            RefParagraph(
                                "في السنة العاشرة حج النبي ﷺ حجة الوداع، وحدّث الناس بخطبة عظيمة جمعت أصول الدين: حرمة الدماء والأموال والأعراض، والوصية بالنساء، وترك الأمانة، وقال: «ألا هل بلغت؟ اللهم اشهد».",
                                "In year ten the Prophet ﷺ performed the Farewell Pilgrimage and delivered a great sermon gathering the principles of the religion: the sanctity of life, wealth and honour, kindness to women, and leaving the trust. He said: “Have I conveyed? O God, bear witness.”",
                            ),
                            RefParagraph(
                                "نزلت في حجة الوداع: «الْيَوْمَ أَكْمَلْتُ لَكُمْ دِينَكُمْ وَأَتْمَمْتُ عَلَيْكُمْ نِعْمَتِي وَرَضِيتُ لَكُمُ الْإِسْلَامَ دِينًا».",
                                "During the Farewell Pilgrimage was revealed: “This day I have perfected for you your religion and completed My favour upon you and have chosen for you Islam as religion” (Quran 5:3).",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "death",
                        titleAr = "الوفاة",
                        titleEn = "His death",
                        paragraphs = listOf(
                            RefParagraph(
                                "مرض النبي ﷺ في أواخر صفر من السنة الحادية عشرة للهجرة، فاشتد به المرض، وكان آخر كلامه: «الصلوة الصلوة، وما ملكت أيمانكم». وتوفي ﷺ يوم الاثنين 12 ربيع الأول سنة 11هـ (8 يونيو 632م)، وعمره ثلاث وستون سنة.",
                                "The Prophet ﷺ fell ill at the end of Safar in year eleven of the Hijra; his illness worsened, and his last words were: “The prayer, the prayer, and what your right hands possess.” He died on Monday, 12 Rabi' al-Awwal year 11 AH (8 June 632 CE), at the age of sixty-three.",
                            ),
                            RefParagraph(
                                "لما توفي ﷺ ارتجّ الناس، وقال أبو بكر الصديق: «من كان يعبد محمدًا فإن محمدًا قد مات، ومن كان يعبد الله فإن الله حي لا يموت». ثم ولي الخلافة، ودفن النبي ﷺ في حجرة عائشة حيث قُبض.",
                                "When he died the people were shaken, and Abu Bakr al-Siddiq said: “Whoever worshipped Muhammad — Muhammad has died; whoever worshipped God — God is alive and never dies.” Abu Bakr then assumed the caliphate, and the Prophet ﷺ was buried in Aisha's room where he passed away.",
                            ),
                        ),
                    ),
                ),
            ),
            RefTopic(
                id = "character_qualities",
                titleAr = "صفاته وأخلاقه ﷺ",
                titleEn = "His qualities and character ﷺ",
                summaryAr = "خلق القرآن، أشجع الناس وأكرمهم وأتواضعهم.",
                summaryEn = "His character was the Quran; he was the bravest, most generous and most humble of people.",
                sections = listOf(
                    RefSection(
                        id = "character",
                        titleAr = "خلقه",
                        titleEn = "His character",
                        paragraphs = listOf(
                            RefParagraph(
                                "كان ﷺ خلقه القرآن: يأمر بأمره وينتهي بنهيه. سئلت عائشة عن خلقه فقالت: «كان خلقه القرآن». وكان لا يغضب لنفسه، ولا ينتقم لنفسه، وإنما يغضب إذا انتهكت حرمات الله.",
                                "His character was the Quran, commanding what it commands and refraining from what it forbids. When Aisha was asked about his character she said: “His character was the Quran.” He never became angry for himself nor sought revenge for himself, only when God's sanctities were violated.",
                            ),
                            RefParagraph(
                                "كان أشد الناس تواضعًا: يخدم نفسه، ويخصف نعله، ويرقع ثوبه، ويجيب دعوة المملوك، ويمشي مع الأرملة والمسكين في حاجة.",
                                "He was the most humble of people: he served himself, patched his sandal, mended his garment, answered the invitation of a servant, and walked with the widow and the poor to meet their needs.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "qualities",
                        titleAr = "صفاته",
                        titleEn = "His qualities",
                        paragraphs = listOf(
                            RefParagraph(
                                "كان ﷺ أشجع الناس وأجود الناس وأصدقهم وأعفهم. قال علي رضي الله عنه: «كنا إذا حمي البأس اتقينا برسول الله ﷺ».",
                                "He ﷺ was the bravest, most generous, most truthful and most chaste of people. Ali (may God be pleased with him) said: “When battle grew fierce we sought protection with the Messenger of God.”",
                            ),
                            RefParagraph(
                                "من صفاته الخَلْقية: كان أحسن الناس وجهًا، ليس بالطويل البائن ولا القصير، يختم النبوة على كتفه، وكان لا يضحك إلا تبسمًا.",
                                "Of his physical qualities: he had the most beautiful face, neither very tall nor short, with the seal of prophethood between his shoulders; he never laughed except as a smile.",
                            ),
                        ),
                    ),
                ),
            ),
            RefTopic(
                id = "family",
                titleAr = "أزواجه وأهل بيته وأصحابه",
                titleEn = "His wives, household and companions",
                summaryAr = "أمهات المؤمنين، وأهل البيت، والصحابة حملة الرسالة.",
                summaryEn = "The Mothers of the Believers, the Prophet's household, and the companions who carried the message.",
                sections = listOf(
                    RefSection(
                        id = "wives",
                        titleAr = "أمهات المؤمنين",
                        titleEn = "The Mothers of the Believers",
                        paragraphs = listOf(
                            RefParagraph(
                                "تزوج النبي ﷺ خديجة ثم سودة وعائشة وحفصة وزينب بنت خزيمة وأم سلمة وزينب بنت جحش وجويرية وأم حبيبة وصفية وميمونة رضي الله عنهن. وقد كانت زيجاته لحكم: تقوية الروابط، وحفظ السنن، وإكرام الأرامل.",
                                "The Prophet ﷺ married Khadijah, then Sawdah, Aisha, Hafsa, Zaynab bint Khuzaymah, Umm Salamah, Zaynab bint Jahsh, Juwayriyyah, Umm Habibah, Safiyyah and Maymunah (may God be pleased with them). His marriages served purposes: strengthening ties, preserving the Sunnah and honouring widows.",
                            ),
                            RefParagraph(
                                "عائشة رضي الله عنها كانت أحب الناس إليه، وروت عنه أكثر من ألفي حديث. وخديجة كانت أول من آمن به.",
                                "Aisha (may God be pleased with her) was the dearest of people to him and narrated over two thousand hadiths. Khadijah was the first to believe in him.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "companions",
                        titleAr = "الصحابة الكرام",
                        titleEn = "The noble companions",
                        paragraphs = listOf(
                            RefParagraph(
                                "الصحابة هم الذين رأوا النبي ﷺ وآمنوا به وماتوا على ذلك، وهم خير القرون: «خير القرون قرني». منهم الخلفاء الراشدون أبو بكر وعمر وعثمان وعلي، والعشرة المبشرون بالجنة.",
                                "The companions are those who saw the Prophet ﷺ, believed in him and died upon that; they are the best of generations: “The best of generations is mine.” Among them are the rightly guided caliphs Abu Bakr, Umar, Uthman and Ali, and the ten promised Paradise.",
                            ),
                            RefParagraph(
                                "حفظ الصحابة القرآن والسنة وبلّغوها، وفتحوا البلاد، وأسسوا الحضارة الإسلامية، وهم القدوة في فهم الدين.",
                                "The companions preserved the Quran and the Sunnah and conveyed them, conquered lands, founded Islamic civilisation, and are the model for understanding the religion.",
                            ),
                        ),
                    ),
                ),
            ),
            RefTopic(
                id = "miracles",
                titleAr = "معجزاته ﷺ",
                titleEn = "His miracles ﷺ",
                summaryAr = "القرآن أعظم معجزاته، مع انشقاق القمر وتكثير الطعام وغيرهما.",
                summaryEn = "The Quran is his greatest miracle, along with the splitting of the moon, the multiplication of food and others.",
                sections = listOf(
                    RefSection(
                        id = "quran_miracle",
                        titleAr = "معجزة القرآن",
                        titleEn = "The miracle of the Quran",
                        paragraphs = listOf(
                            RefParagraph(
                                "أعظم معجزات النبي ﷺ القرآن الكريم، تحدى الله به العرب أهل الفصاحة فعجزوا عن الإتيان بمثله: «قُلْ لَئِنِ اجْتَمَعَتِ الْإِنْسُ وَالْجِنُّ عَلَىٰ أَنْ يَأْتُوا بِمِثْلِ هَٰذَا الْقُرْآنِ لَا يَأْتُونَ بِمِثْلِهِ».",
                                "The greatest miracle of the Prophet ﷺ is the Quran, with which God challenged the eloquent Arabs, and they could not produce its like: “Say: If mankind and the jinn gathered to produce the like of this Quran, they could not produce its like” (Quran 17:88).",
                            ),
                            RefParagraph(
                                "القرآن معجزة باقية إلى يوم القيامة، بخلاف معجزات الأنبياء السابقين التي انقضت بوقتها.",
                                "The Quran is a miracle that remains until the Day of Resurrection, unlike the miracles of the earlier prophets which passed with their times.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "other_miracles",
                        titleAr = "معجزات حسية",
                        titleEn = "Sensory miracles",
                        paragraphs = listOf(
                            RefParagraph(
                                "من معجزاته ﷺ: انشقاق القمر بإشارته، وتكثير الطعام القليل حتى أكل منه الجمع الكثير، ونبع الماء من بين أصابعه، وحنين الجذع إليه، وتسليم الحجر عليه.",
                                "Among his miracles ﷺ: the splitting of the moon at his signal, the multiplication of little food until large crowds ate, water springing from between his fingers, the palm trunk longing for him, and the stone greeting him.",
                            ),
                            RefParagraph(
                                "والإسراء والمعراج من معجزاته العظيمة، وقد صدّقه أبو بكر فقالوا له: «الصدّيق».",
                                "The Night Journey and Ascension are among his great miracles, and Abu Bakr believed him, earning the title “al-Siddiq” (the Truthful).",
                            ),
                        ),
                    ),
                ),
            ),
        ),
    )
}
