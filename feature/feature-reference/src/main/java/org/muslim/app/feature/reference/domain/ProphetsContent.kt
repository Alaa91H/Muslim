package org.muslim.app.feature.reference.domain

/**
 * كتاب «قصص الأنبياء» — قصص الأنبياء المذكورين في القرآن الكريم، مرتبة
 * ترتيبًا زمنيًا تقريبيًا، مبنية على القرآن والسنة وما صح من الآثار. تخضع
 * للمراجعة الشرعية قبل النشر (§10).
 */
object ProphetsContent {

    val book: ReferenceBook = ReferenceBook(
        id = "prophets",
        titleAr = "قصص الأنبياء",
        titleEn = "Stories of the Prophets",
        subtitleAr = "قصص أنبياء الله المذكورين في القرآن الكريم كاملة",
        subtitleEn = "The complete stories of God's prophets mentioned in the Quran",
        topics = listOf(
            RefTopic(
                id = "adam",
                titleAr = "آدم عليه السلام",
                titleEn = "Adam (peace be upon him)",
                summaryAr = "أول البشر وأول الأنبياء، خلقه الله بيده وعلّمه الأسماء.",
                summaryEn = "The first human and first prophet, created by God's hand and taught the names.",
                sections = listOf(
                    RefSection(
                        id = "creation",
                        titleAr = "الخلق",
                        titleEn = "The creation",
                        paragraphs = listOf(
                            RefParagraph(
                                "خلق الله آدم من تراب، ونفخ فيه من روحه، وعلّمه الأسماء كلها، وأمر الملائكة بالسجود له سجود تكريم، فسجدوا إلا إبليس استكبر وكان من الكافرين.",
                                "God created Adam from clay, breathed into him of His spirit, taught him all the names, and commanded the angels to prostrate to him in honour; they all prostrated except Iblis, who was arrogant and became of the disbelievers.",
                            ),
                            RefParagraph(
                                "أسكن الله آدم وزوجه الجنة، وقال: «وَكُلَا مِنْهَا رَغَدًا حَيْثُ شِئْتُمَا وَلَا تَقْرَبَا هَٰذِهِ الشَّجَرَةَ». فأغواهما الشيطان، فأكلا منها، فأنزلهما الله إلى الأرض.",
                                "God settled Adam and his wife in Paradise, saying: “And eat from it in relaxation wherever you wish, but do not approach this tree.” Satan seduced them, they ate from it, and God sent them down to the earth.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "repentance",
                        titleAr = "التوبة",
                        titleEn = "The repentance",
                        paragraphs = listOf(
                            RefParagraph(
                                "تلقى آدم من ربه كلمات فتاب عليه: «رَبَّنَا ظَلَمْنَا أَنْفُسَنَا وَإِنْ لَمْ تَغْفِرْ لَنَا وَتَرْحَمْنَا لَنَكُونَنَّ مِنَ الْخَاسِرِينَ». فتاب الله عليه — وهو أول دروس التوبة والرجوع إلى الله.",
                                "Adam received words from his Lord and repented: “Our Lord, we have wronged ourselves, and if You do not forgive us and have mercy upon us, we will surely be among the losers” (Quran 7:23). God accepted his repentance — the first lesson of repentance and return to God.",
                            ),
                            RefParagraph(
                                "عاش آدم على الأرض نبيًا، وعلّم بنيه التوحيد، وأنجب هابيل وقابيل وشيث وغيرهم.",
                                "Adam lived on earth as a prophet, teaching his children monotheism, and had Habil, Qabil, Seth and others.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "lessons",
                        titleAr = "دروس وعبر",
                        titleEn = "Lessons and wisdom",
                        paragraphs = listOf(
                            RefParagraph(
                                "من قصة آدم نتعلم أن الكبرياء أصل الهلاك، وأن طاعة الله واجبة وإن خالفت هوى النفس؛ فقد أهلك إبليسَ كبرياؤه ولم يضره خلقه ولا عبادته القديمة. كما نتعلم أن المعصية تليها التوبة النصوح، وأن باب الله مفتوح لمن رجع: «ثُمَّ اجْتَبَاهُ رَبُّهُ فَتَابَ عَلَيْهِ وَهَدَىٰ».",
                                "From Adam's story we learn that pride is the origin of destruction and that obedience to God is obligatory even when it conflicts with the soul's desire — Iblis was ruined by his arrogance, and neither his creation nor his long worship availed him. We also learn that sin is followed by sincere repentance, and that God's door remains open for whoever returns: “Then his Lord chose him and turned to him in forgiveness and guided him” (Quran 20:122).",
                            ),
                            RefParagraph(
                                "في قصة آدم بيان لعداوة الشيطان للإنسان وسبل وقايتها: أعداؤه يريدون إخراجنا من الجنة كما أخرجوا أبينا، فلا يُستهان بوسوسته ولا يُتخذ سبيله، قال تعالى: «إِنَّ الشَّيْطَانَ لَكُمْ عَدُوٌّ فَاتَّخِذُوهُ عَدُوًّا».",
                                "Adam's story clarifies Satan's enmity toward mankind and how to guard against it: the enemy wants to expel us from Paradise as he expelled our father, so his whisper must not be taken lightly nor his path adopted. God said: “Indeed, Satan is to you an enemy, so take him as an enemy” (Quran 35:6).",
                            ),
                        ),
                    ),

                ),
            ),
            RefTopic(
                id = "idris",
                titleAr = "إدريس عليه السلام",
                titleEn = "Idris (peace be upon him)",
                summaryAr = "نبي صدّيق رفعه الله مكانًا عليًا، وأول من خط بالقلم.",
                summaryEn = "A truthful prophet whom God raised to a high station; he was the first to write with the pen.",
                sections = listOf(
                    RefSection(
                        id = "story",
                        titleAr = "قصته",
                        titleEn = "His story",
                        paragraphs = listOf(
                            RefParagraph(
                                "إدريس عليه السلام من أنبياء بني آدم، وصفه الله بالصدق: «وَاذْكُرْ فِي الْكِتَابِ إِدْرِيسَ إِنَّهُ كَانَ صِدِّيقًا نَبِيًّا». يُقال إنه أول من خط بالقلم وأول من نظر في علم النجوم والحساب.",
                                "Idris (peace be upon him) is among the prophets of the descendants of Adam, described by God as truthful: “And mention in the Book, Idris. Indeed, he was a truthful man and a prophet” (Quran 19:56). He is said to have been the first to write with the pen and to study astronomy and calculation.",
                            ),
                            RefParagraph(
                                "قال تعالى: «وَرَفَعْنَاهُ مَكَانًا عَلِيًّا» — رفع الله ذكره ومكانته، وقيل رفعه إلى السماء.",
                                "God said: “And We raised him to a high station” (Quran 19:57) — raising his mention and rank, and it is said he was raised to heaven.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "lessons",
                        titleAr = "دروس وعبر",
                        titleEn = "Lessons and wisdom",
                        paragraphs = listOf(
                            RefParagraph(
                                "في إدريس بيان فضل الصدق والاجتهاد في العلم والعمل؛ وصفه الله بالصدق ورفعه مكانًا عليًا. فالعلم النافع الذي يُعمل به يرفع صاحبه عند الله، ويكون سببًا لعلوّ الذكر والمنزلة.",
                                "In Idris we see the virtue of truthfulness and diligence in knowledge and action; God described him as truthful and raised him to a high station. Beneficial knowledge that is acted upon raises its owner with God and elevates his mention and rank.",
                            ),
                            RefParagraph(
                                "الاشتغال بما ينفع الناس من العلوم الدنيوية المباحة (كالكتابة والحساب) ليس مذمومًا إذا صاحبه التوحيد والصدق؛ فإدريس كان أول من خط بالقلم مع كونه نبيًا صديقًا.",
                                "Occupying oneself with permissible worldly sciences (like writing and calculation) is not blameworthy when accompanied by monotheism and truthfulness; Idris was the first to write with the pen while being a truthful prophet.",
                            ),
                        ),
                    ),

                ),
            ),
            RefTopic(
                id = "nuh",
                titleAr = "نوح عليه السلام",
                titleEn = "Noah (peace be upon him)",
                summaryAr = "أول أولي العزم، دعا قومه 950 سنة، وأنجاه الله في السفينة.",
                summaryEn = "The first of the resolute messengers; he called his people for 950 years and God saved him in the ark.",
                sections = listOf(
                    RefSection(
                        id = "call",
                        titleAr = "دعوته",
                        titleEn = "His call",
                        paragraphs = listOf(
                            RefParagraph(
                                "بعث الله نوحًا إلى قومه يعبدون الأصنام، فدعاهم ليلًا ونهارًا سرًا وجهارًا تسعمائة وخمسين سنة، فما آمن معه إلا قليل، وكانوا يسخرون منه ويضعون أصابعهم في آذانهم.",
                                "God sent Noah to his people who worshipped idols. He called them night and day, secretly and openly, for nine hundred and fifty years, yet only a few believed, and they mocked him and put their fingers in their ears.",
                            ),
                            RefParagraph(
                                "دعا نوح ربه: «أَنِّي مَغْلُوبٌ فَانْتَصِرْ» فأوحى الله إليه أن يصنع السفينة، فصنعها بأمر الله، وسخر منه قومه: «وَيَصْنَعُ الْفُلْكَ وَكُلَّمَا مَرَّ عَلَيْهِ مَلَأٌ مِنْ قَوْمِهِ سَخِرُوا مِنْهُ».",
                                "Noah called to his Lord: “Indeed, I am overpowered, so help me” (Quran 54:10). God revealed to him to build the ark; he built it by God's command while his people mocked: “And he constructed the ship, and whenever an assembly of his people passed by him they ridiculed him” (Quran 11:38).",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "flood",
                        titleAr = "الطوفان",
                        titleEn = "The flood",
                        paragraphs = listOf(
                            RefParagraph(
                                "جاء أمر الله، وفار التنور، وحمل نوح في السفينة من كل زوجين اثنين وأهله ومن آمن — وكان ابنه من الغارقين لأنه لم يؤمن. وغرق الكافرون جميعًا.",
                                "God's command came and the oven overflowed. Noah carried into the ark a pair of every kind, his family and the believers — his son was among the drowned for not believing. All the disbelievers perished.",
                            ),
                            RefParagraph(
                                "قال تعالى: «وَقِيلَ يَا أَرْضُ ابْلَعِي مَاءَكِ وَيَا سَمَاءُ أَقْلِعِي». واستوت السفينة على الجودي، ونادى نوح ربه وقال: «رَبِّ إِنَّ ابْنِي مِنْ أَهْلِي» فقال الله: «إِنَّهُ لَيْسَ مِنْ أَهْلِكَ».",
                                "God said: “And it was said: O earth, swallow your water, and O sky, withhold.” The ship settled on Mount Judi, and Noah called to his Lord: “My Lord, indeed my son is of my family,” and God said: “Indeed, he is not of your family” (Quran 11:44–46).",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "lessons",
                        titleAr = "دروس وعبر",
                        titleEn = "Lessons and wisdom",
                        paragraphs = listOf(
                            RefParagraph(
                                "في نوح أعظم مثال على الصبر في الدعوة: تسعمائة وخمسون سنة من الجهد والدعوة سرًا وجهارًا، لا يفتر ولا يمل، مع قلة المستجيبين وكثرة الساخرين. فالعبرة في الدعوة بالثبات على الحق لا بكثرة الأتباع.",
                                "In Noah is the greatest example of patience in calling to God: nine hundred and fifty years of effort and calling secretly and openly, without weakening or tiring, amid few responders and many mockers. The measure of da'wah is steadfastness upon the truth, not the number of followers.",
                            ),
                            RefParagraph(
                                "في الطوفان عبرة بأن العاقبة للمؤمنين وإن تأخر النصر، وبأن القرابة لا تنفع مع الكفر: ابن نوح كان من الغارقين لأنه لم يؤمن، فالأعمال هي التي ترفع أو تخفض، لا الأنساب.",
                                "In the flood is a lesson that the outcome belongs to the believers even when victory is delayed, and that kinship avails nothing alongside disbelief: Noah's son was among the drowned for not believing. It is deeds that raise or lower, not lineage.",
                            ),
                        ),
                    ),

                ),
            ),
            RefTopic(
                id = "hud",
                titleAr = "هود عليه السلام",
                titleEn = "Hud (peace be upon him)",
                summaryAr = "نبي عاد، القوم الأشداء الذين أهلكهم الله بريح صرصر عاتية.",
                summaryEn = "The prophet of 'Ad, the mighty people destroyed by God with a furious wind.",
                sections = listOf(
                    RefSection(
                        id = "story",
                        titleAr = "قصته",
                        titleEn = "His story",
                        paragraphs = listOf(
                            RefParagraph(
                                "أرسل الله هودًا إلى قوم عاد، وكانوا أصحاب قوة وبأس شديد، يسكنون الأحقاف، ويعبدون الأصنام. دعاهم إلى عبادة الله وحده: «يَا قَوْمِ اعْبُدُوا اللَّهَ مَا لَكُمْ مِنْ إِلَٰهٍ غَيْرُهُ».",
                                "God sent Hud to the people of 'Ad, who were of great strength and might, dwelling in al-Ahqaf and worshipping idols. He called them to worship God alone: “O my people, worship Allah; you have no god other than Him” (Quran 11:50).",
                            ),
                            RefParagraph(
                                "كذبوه وتوعدوه: «إِنْ نَقُولُ إِلَّا اعْتَرَاكَ بَعْضُ آلِهَتِنَا بِسُوءٍ». فلم يزدهم إلا عتوًا وتكبرًا.",
                                "They denied him and threatened him: “We say not except that some of our gods have afflicted you with evil” (Quran 11:54). He only increased them in arrogance and transgression.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "punishment",
                        titleAr = "العذاب",
                        titleEn = "The punishment",
                        paragraphs = listOf(
                            RefParagraph(
                                "أرسل الله عليهم ريحًا صرصرًا عاتية سبع ليالٍ وثمانية أيام حسومًا، فدمرتهم: «فَأَمَّا عَادٌ فَأُهْلِكُوا بِرِيحٍ صَرْصَرٍ عَاتِيَةٍ». ونجّى الله هودًا والذين آمنوا معه.",
                                "God sent upon them a furious screaming wind for seven nights and eight days in succession, destroying them: “As for 'Ad, they were destroyed by a screaming, violent wind” (Quran 69:6). God saved Hud and those who believed with him.",
                            ),
                            RefParagraph(
                                "في قصة عاد عبرة: أن القوة لا تنفع مع الكفر، وأن العاقبة للمتقين.",
                                "In the story of 'Ad is a lesson: strength avails nothing against disbelief, and the outcome belongs to the God-fearing.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "lessons",
                        titleAr = "دروس وعبر",
                        titleEn = "Lessons and wisdom",
                        paragraphs = listOf(
                            RefParagraph(
                                "في قصة عاد عبرة أن القوة والبأس لا يغنيان عن صاحبهما إذا كفر بربه؛ كانوا أشد الناس قوة وأعظمهم بنيانًا، فأهلكتهم ريح صرصر عاتية سبع ليالٍ وثمانية أيام. فمن ركن إلى قوته خسر، ومن ركن إلى ربه نجا.",
                                "The story of 'Ad teaches that strength and might avail nothing when one disbelieves in his Lord; they were the strongest of people and the greatest in construction, yet a screaming furious wind destroyed them over seven nights and eight days. Whoever leans on his own strength loses, and whoever leans on his Lord is saved.",
                            ),
                            RefParagraph(
                                "«فَأَمَّا عَادٌ فَاسْتَكْبَرُوا فِي الْأَرْضِ بِغَيْرِ الْحَقِّ» — الاستكبار عن الحق سنة الهالكين، والنجاة للذين آمنوا مع هود، وكانوا قلة في قومهم، قال تعالى: «وَمَا آمَنَ مَعَهُ إِلَّا قَلِيلٌ».",
                                "“As for 'Ad, they were arrogant in the land without right” (Quran 41:15) — arrogance against the truth is the way of the destroyed, and deliverance was for the few who believed with Hud. God said: “And none believed with him except a few” (Quran 11:40).",
                            ),
                        ),
                    ),

                ),
            ),
            RefTopic(
                id = "salih",
                titleAr = "صالح عليه السلام",
                titleEn = "Salih (peace be upon him)",
                summaryAr = "نبي ثمود، أرسل الله لهم الناقة آية فعقروها فأهلكوا بالصيحة.",
                summaryEn = "The prophet of Thamud; God sent them the she-camel as a sign, they hamstrung it, and were destroyed by the blast.",
                sections = listOf(
                    RefSection(
                        id = "story",
                        titleAr = "قصته",
                        titleEn = "His story",
                        paragraphs = listOf(
                            RefParagraph(
                                "أرسل الله صالحًا إلى ثمود، قوم ينحتون الجبال بيوتًا، فدعاهم إلى التوحيد، فطلبوا منه آية، فأخرج الله لهم ناقة من الصخرة آية عظيمة، وقال لهم: «ذَرُوهَا تَأْكُلْ فِي أَرْضِ اللَّهِ».",
                                "God sent Salih to Thamud, a people who carved homes in the mountains. He called them to monotheism, and they asked him for a sign, so God brought forth from the rock a she-camel as a great sign, and he told them: “Let her eat in God's land” (Quran 7:73).",
                            ),
                            RefParagraph(
                                "عقروا الناقة — وأعظمهم فيها أشقاهم — فتوعّدهم صالح بالعذاب: «تَمَتَّعُوا فِي دَارِكُمْ ثَلَاثَةَ أَيَّامٍ».",
                                "They hamstrung the she-camel — the most wretched among them being the one who did it — and Salih threatened them with punishment: “Enjoy yourselves in your homes for three days” (Quran 11:65).",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "punishment",
                        titleAr = "الهلاك",
                        titleEn = "The destruction",
                        paragraphs = listOf(
                            RefParagraph(
                                "جاءهم العذاب صيحة واحدة فأصبحوا في ديارهم جاثمين، ونجّى الله صالحًا والذين آمنوا معه: «فَأَخَذَتْهُمُ الصَّيْحَةُ فَأَصْبَحُوا فِي دَارِهِمْ جَاثِمِينَ».",
                                "The punishment came as a single blast and they became in their homes fallen dead, while God saved Salih and those who believed with him: “So the blast seized them, and they became within their home fallen” (Quran 11:67).",
                            ),
                            RefParagraph(
                                "في قصة ثمود عبرة: أن تكذيب الآيات بعد ظهورها أشد هلاكًا.",
                                "In the story of Thamud is a lesson: denying the signs after their appearance is the most destructive.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "lessons",
                        titleAr = "دروس وعبر",
                        titleEn = "Lessons and wisdom",
                        paragraphs = listOf(
                            RefParagraph(
                                "في قصة ثمود عبرة أن الآيات إذا جاءت ثم كُذبت كانت الهلاك؛ أخرج الله لهم الناقة آية عظيمة فكفروا بها وعقروها، فأخذتهم الصيحة وهم في ديارهم جاثمين.",
                                "The story of Thamud teaches that when the signs come and are then denied, destruction follows; God brought forth the she-camel as a great sign, yet they disbelieved and hamstrung it, and the blast seized them fallen dead in their homes.",
                            ),
                            RefParagraph(
                                "في تمكين الصالحين آية أن الله يدفع عن عباده المؤمنين: «قَالَ يَا قَوْمِ هَٰذِهِ نَاقَةُ اللَّهِ لَكُمْ آيَةً فَذَرُوهَا تَأْكُلْ فِي أَرْضِ اللَّهِ». فمن تعرض لآيات الله وآذى أولياءه فقد باء بغضب الله.",
                                "The story of the believers' empowerment shows that God repels harm from His believing servants: “He said: O my people, this is the she-camel of Allah — a sign for you, so let her eat upon Allah's land” (Quran 11:64). Whoever opposes God's signs and harms His allies has earned His wrath.",
                            ),
                        ),
                    ),

                ),
            ),
            RefTopic(
                id = "ibrahim",
                titleAr = "إبراهيم عليه السلام",
                titleEn = "Abraham (peace be upon him)",
                summaryAr = "خليل الرحمن وأبو الأنبياء، حطم الأصنام، وابتلاه الله بالذبح ففداه.",
                summaryEn = "The friend of the Merciful and father of the prophets; he broke the idols and was tested with the sacrifice, which God ransomed.",
                sections = listOf(
                    RefSection(
                        id = "call",
                        titleAr = "دعوته",
                        titleEn = "His call",
                        paragraphs = listOf(
                            RefParagraph(
                                "دعا إبراهيم أباه وقومه إلى التوحيد، وقال عن الأصنام: «أُفٍّ لَكُمْ وَلِمَا تَعْبُدُونَ مِنْ دُونِ اللَّهِ أَفَلَا تَعْقِلُونَ». ثم حطم الأصنام إلا كبيرها، ليُري قومه بطلانها.",
                                "Abraham called his father and people to monotheism, saying of the idols: “Ugh to you and to what you worship instead of Allah. Then will you not use reason?” (Quran 21:67). He then broke the idols except the largest, to show his people their futility.",
                            ),
                            RefParagraph(
                                "أراد قومه إحراقه، فألقوه في النار، فقال الله: «يَا نَارُ كُونِي بَرْدًا وَسَلَامًا عَلَىٰ إِبْرَاهِيمَ». فخرج منها سالمًا — من أعظم آيات الله.",
                                "His people sought to burn him and cast him into the fire, and God said: “O fire, be coolness and safety upon Abraham” (Quran 21:69). He emerged unharmed — one of God's greatest signs.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "tests",
                        titleAr = "الابتلاءات",
                        titleEn = "The trials",
                        paragraphs = listOf(
                            RefParagraph(
                                "ابتلى الله إبراهيم بكلمات فأتمهن، ورفعه إمامًا للناس. وابتلاه بذبح ابنه إسماعيل، فلما أسلما وتله للجبين ناداه ربه: «يَا إِبْرَاهِيمُ * قَدْ صَدَّقْتَ الرُّؤْيَا» وفداه بذبح عظيم — وهي سنة الأضحية.",
                                "God tested Abraham with words which he fulfilled, and made him a leader for the people. He tested him with the sacrifice of his son Ismail; when they both submitted and he laid him upon his forehead, his Lord called: “O Abraham, you have fulfilled the vision” (Quran 37:104–105) and ransomed him with a great sacrifice — the origin of the Eid sacrifice.",
                            ),
                            RefParagraph(
                                "بنى إبراهيم وإسماعيل الكعبة، ودعا بالبركة، ورفعا القواعد: «رَبَّنَا تَقَبَّلْ مِنَّا إِنَّكَ أَنْتَ السَّمِيعُ الْعَلِيمُ». واتخذ الله إبراهيم خليلًا.",
                                "Abraham and Ismail built the Kaaba, raising its foundations and praying: “Our Lord, accept from us; indeed You are the Hearing, the Knowing” (Quran 2:127). God took Abraham as a close friend (khalil).",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "lessons",
                        titleAr = "دروس وعبر",
                        titleEn = "Lessons and wisdom",
                        paragraphs = listOf(
                            RefParagraph(
                                "في إبراهيم بيان كمال التوحيد: بدأ بالأقرب فالأقرب — أباه — ثم قومه، وحطم الأصنام ليكشف بطلانها، وسلّم نفسه لله في النار فكانت بردًا وسلامًا عليه. فالتوكل على الله يقلب المحال ممكنًا: «وَمَنْ يَتَوَكَّلْ عَلَى اللَّهِ فَهُوَ حَسْبُهُ».",
                                "In Abraham we see the perfection of monotheism: he began with the closest — his father — then his people, broke the idols to expose their futility, and surrendered himself to God in the fire, which became coolness and safety upon him. Reliance on God turns the impossible into the possible: “And whoever relies upon Allah — then He is sufficient for him” (Quran 65:3).",
                            ),
                            RefParagraph(
                                "في قصة الذبح أعظم دروس التسليم: امتثل الابن لأمر ربه وأبيه، فجعل الله الطاعة فداءً له. ومنه استُحبت الأضحية سنة باقية في أمة محمد ﷺ، وفي بنائه الكعبة دعاء أن يجعل الله مكة بلدًا آمنًا ويرزق أهلها من الثمرات.",
                                "The story of the sacrifice holds the greatest lessons of submission: the son complied with the command of his Lord and his father, and God made obedience his ransom. From it the sacrifice (udhiyah) became an enduring sunnah in the nation of Muhammad ﷺ, and in building the Kaaba he prayed that God make Mecca a secure land and provide its people with fruits.",
                            ),
                        ),
                    ),

                ),
            ),
            RefTopic(
                id = "lut",
                titleAr = "لوط عليه السلام",
                titleEn = "Lot (peace be upon him)",
                summaryAr = "نبي قرى سدوم، أنكر عليهم الفاحشة، وأهلكهم الله بقلب ديارهم.",
                summaryEn = "The prophet of the towns of Sodom; he denounced their abomination, and God destroyed them by overturning their cities.",
                sections = listOf(
                    RefSection(
                        id = "story",
                        titleAr = "قصته",
                        titleEn = "His story",
                        paragraphs = listOf(
                            RefParagraph(
                                "أرسل الله لوطًا إلى قوم يأتون الفاحشة التي ما سبقهم بها أحد من العالمين، فدعاهم إلى الفطرة والطهارة: «أَتَأْتُونَ الْفَاحِشَةَ مَا سَبَقَكُمْ بِهَا مِنْ أَحَدٍ مِنَ الْعَالَمِينَ».",
                                "God sent Lot to a people who committed an abomination no one in the worlds had committed before them. He called them to natural purity: “Do you commit such immorality as no one has preceded you with from among the worlds?” (Quran 7:80).",
                            ),
                            RefParagraph(
                                "كذبوه وتوعدوه بالإخراج، وجاءته الملائكة ضيوفًا في صورة رجال، فخاف عليهم، وهمّ قومه بهم.",
                                "They denied him and threatened to expel him. The angels came to him as guests in the form of men, and he feared for them while his people sought them.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "destruction",
                        titleAr = "الهلاك",
                        titleEn = "The destruction",
                        paragraphs = listOf(
                            RefParagraph(
                                "أمر الله الملائكة أن تخرج لوطًا وأهله ليلًا إلا امرأته، فلما خرجوا جعل الله عالي قرى قومه سافلها وأمطر عليهم حجارة من سجيل: «فَأَخَذَتْهُمُ الصَّيْحَةُ مُشْرِقِينَ».",
                                "God commanded the angels to take Lot and his family out by night except his wife. When they left, God turned the towns upside down and rained upon them stones of baked clay: “So the blast seized them at sunrise” (Quran 15:73).",
                            ),
                            RefParagraph(
                                "في قصة لوط تحريم الفاحشة، وأن العقوبة على من تمادى فيها بعد قيام الحجة.",
                                "The story of Lot forbids the abomination and shows that punishment comes upon those who persist after the proof is established.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "lessons",
                        titleAr = "دروس وعبر",
                        titleEn = "Lessons and wisdom",
                        paragraphs = listOf(
                            RefParagraph(
                                "في قصة لوط تقرير تحريم الفاحشة والتحذير من عاقبتها في الدنيا والآخرة، وأن المجتمع الذي يفشيها يسلبه الله الأمن ويقلب عليه نعمه: «فَأَخَذَتْهُمُ الصَّيْحَةُ مُشْرِقِينَ».",
                                "The story of Lot affirms the prohibition of immorality and warns of its consequences in this life and the next, and that a society in which it spreads is stripped of security and has its blessings overturned: “So the blast seized them at sunrise” (Quran 15:73).",
                            ),
                            RefParagraph(
                                "وفي القصة أن نصرة النبي للضعيف ودفعه عن المضطر — لما جاءته الملائكة ضيوفًا وخاف عليهم من قومه — مقام عظيم؛ والله لا يضيع أجر من أحسن عملاً.",
                                "The story also shows that a prophet's defence of the weak and his protection of the guest in need — when the angels came to him as guests and he feared his people would harm them — is a great station, and God does not waste the reward of those who do good.",
                            ),
                        ),
                    ),

                ),
            ),
            RefTopic(
                id = "ismail",
                titleAr = "إسماعيل عليه السلام",
                titleEn = "Ismail (peace be upon him)",
                summaryAr = "الذبيح، بُني على يديه البيت الحرام، وأب العرب المستعربة.",
                summaryEn = "The one of the sacrifice; the Sacred House was built by his hands, and he is the father of the Arabised Arabs.",
                sections = listOf(
                    RefSection(
                        id = "story",
                        titleAr = "قصته",
                        titleEn = "His story",
                        paragraphs = listOf(
                            RefParagraph(
                                "ولد إسماعيل من هاجر، وأسكنه إبراهيم بوادي مكة غير ذي زرع، ففاض له بئر زمزم، وكان صادق الوعد: «وَاذْكُرْ فِي الْكِتَابِ إِسْمَاعِيلَ إِنَّهُ كَانَ صَادِقَ الْوَعْدِ وَكَانَ رَسُولًا نَبِيًّا».",
                                "Ismail was born to Hajar, and Abraham settled him in the valley of Mecca with no cultivation; the well of Zamzam gushed forth for them. He was true to his promise: “And mention in the Book, Ismail. Indeed, he was true to his promise, and he was a messenger and a prophet” (Quran 19:54).",
                            ),
                            RefParagraph(
                                "لما بلغ معه السعي، قال له أبوه: «يَا بُنَيَّ إِنِّي أَرَىٰ فِي الْمَنَامِ أَنِّي أَذْبَحُكَ فَانْظُرْ مَاذَا تَرَىٰ» فقال: «يَا أَبَتِ افْعَلْ مَا تُؤْمَرُ سَتَجِدُنِي إِنْ شَاءَ اللَّهُ مِنَ الصَّابِرِينَ». ففداه الله.",
                                "When he reached the age of striving, his father said: “O my son, indeed I have seen in a dream that I sacrifice you, so see what you think.” He replied: “O my father, do as you are commanded. You will find me, if God wills, of the patient” (Quran 37:102). God ransomed him.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "legacy",
                        titleAr = "أثره",
                        titleEn = "His legacy",
                        paragraphs = listOf(
                            RefParagraph(
                                "شارك أباه في بناء الكعبة، وامتحنه الله بالصبر على طاعته، ومن نسله جاء النبي محمد ﷺ.",
                                "He helped his father build the Kaaba, was tested by God with patience in obedience, and from his lineage came Prophet Muhammad ﷺ.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "lessons",
                        titleAr = "دروس وعبر",
                        titleEn = "Lessons and wisdom",
                        paragraphs = listOf(
                            RefParagraph(
                                "في إسماعيل بيان فضل الصدق في الوعد والتسليم لأمر الله؛ وصفه الله بأنه صادق الوعد، وجعل من طاعته وطاعة أبيه فداءً عظيمًا. ومنه نتعلم أن بر الوالدين مقرون بطاعة الله.",
                                "In Ismail we see the virtue of truthfulness to promises and submission to God's command; God described him as true to his promise and made his obedience and his father's a great ransom. From him we learn that honouring parents is joined with obedience to God.",
                            ),
                            RefParagraph(
                                "بئر زمزم التي فاضت للرضيع في صحراء مكة عبرة بأن الله يرزق عباده من حيث لا يحتسبون: «وَمَنْ يَتَّقِ اللَّهَ يَجْعَلْ لَهُ مَخْرَجًا * وَيَرْزُقْهُ مِنْ حَيْثُ لَا يَحْتَسِبُ».",
                                "The well of Zamzam, which gushed in the desert of Mecca for the infant, is a lesson that God provides for His servants from where they do not expect: “And whoever fears Allah — He will make for him a way out and will provide for him from where he does not expect” (Quran 65:2–3).",
                            ),
                        ),
                    ),

                ),
            ),
            RefTopic(
                id = "ishaq_yaqub",
                titleAr = "إسحاق ويعقوب عليهما السلام",
                titleEn = "Isaac and Jacob (peace be upon them)",
                summaryAr = "ابنا وحفيد إبراهيم، أنبياء كرام، ويعقوب أبو الأسباط.",
                summaryEn = "The son and grandson of Abraham, noble prophets; Jacob is the father of the tribes.",
                sections = listOf(
                    RefSection(
                        id = "story",
                        titleAr = "قصتهما",
                        titleEn = "Their story",
                        paragraphs = listOf(
                            RefParagraph(
                                "بشّرت الملائكة إبراهيم بإسحاق نبيًا من الصالحين، ومن بعده يعقوب. قال تعالى: «فَبَشَّرْنَاهُ بِإِسْحَاقَ نَبِيًّا مِنَ الصَّالِحِينَ». وكلاهما نبي مكرم.",
                                "The angels gave Abraham glad tidings of Isaac, a prophet among the righteous, and after him Jacob. God said: “So We gave him good tidings of Isaac, a prophet among the righteous” (Quran 37:112). Both are honoured prophets.",
                            ),
                            RefParagraph(
                                "يعقوب هو «إسرائيل»، وأبناؤه الأسباط الذين خرج منهم أنبياء بني إسرائيل، ومن نسله يوسف وموسى وعيسى عليهم السلام.",
                                "Jacob is “Israel,” and his sons are the tribes from whom the prophets of the Children of Israel came, and from his lineage came Joseph, Moses and Jesus, peace be upon them.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "lessons",
                        titleAr = "دروس وعبر",
                        titleEn = "Lessons and wisdom",
                        paragraphs = listOf(
                            RefParagraph(
                                "في إسحاق ويعقوب بيان أن النبوة تنتقل في الذرية الطيبة: «فَبَشَّرْنَاهُ بِإِسْحَاقَ نَبِيًّا مِنَ الصَّالِحِينَ»، وأن صلاح الآباء سبب لحفظ الأبناء.",
                                "In Isaac and Jacob we see that prophethood passes through the righteous lineage: “So We gave him good tidings of Isaac, a prophet among the righteous” (Quran 37:112), and that the righteousness of the fathers is a cause of preservation for the children.",
                            ),
                            RefParagraph(
                                "يعقوب وصية لأبنائه بالثبات على الدين: «وَوَصَّىٰ بِهَا إِبْرَاهِيمُ بَنِيهِ وَيَعْقُوبُ يَا بَنِيَّ إِنَّ اللَّهَ اصْطَفَىٰ لَكُمُ الدِّينَ فَلَا تَمُوتُنَّ إِلَّا وَأَنْتُمْ مُسْلِمُونَ» — فلا خير في الحياة إلا على الإسلام.",
                                "Jacob counselled his sons to remain firm upon the religion: “And Abraham instructed his sons and Jacob: O my sons, indeed Allah has chosen for you the religion, so do not die except while you are Muslims” (Quran 2:132) — there is no good in life except upon Islam.",
                            ),
                        ),
                    ),

                ),
            ),
            RefTopic(
                id = "yusuf",
                titleAr = "يوسف عليه السلام",
                titleEn = "Joseph (peace be upon him)",
                summaryAr = "أحسن القصص: من الجبّ إلى العرش، صبر وعفاف وتأويل رؤيا.",
                summaryEn = "The best of stories: from the well to the throne — patience, chastity and the interpretation of dreams.",
                sections = listOf(
                    RefSection(
                        id = "story",
                        titleAr = "قصته",
                        titleEn = "His story",
                        paragraphs = listOf(
                            RefParagraph(
                                "رأى يوسف في منامه أحد عشر كوكبًا والشمس والقمر له ساجدين، فحسده إخوته، وألقوه في الجب، فالتقطته سيارة وباعوه في مصر، فاشتراه عزيز مصر.",
                                "Joseph saw in a dream eleven stars and the sun and moon prostrating to him. His brothers envied him, cast him into the well, and a caravan drew him out and sold him in Egypt, where the governor of Egypt bought him.",
                            ),
                            RefParagraph(
                                "راوغته امرأة العزيز عن نفسه فاستعصم: «مَعَاذَ اللَّهِ إِنَّهُ رَبِّي أَحْسَنَ مَثْوَايَ». فسُجن ظلمًا، وفي السجن كان يدعو إلى الله ويعلّم تأويل الرؤيا.",
                                "The wife of the governor seduced him, and he sought refuge: “I seek refuge in Allah. Indeed, he is my master, who has made good my residence” (Quran 12:23). He was imprisoned unjustly, and in prison he called to God and taught the interpretation of dreams.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "rise",
                        titleAr = "تمكينه",
                        titleEn = "His rise",
                        paragraphs = listOf(
                            RefParagraph(
                                "خرج من السجن بعد أن عبّر رؤيا الملك، وجعله الملك على خزائن الأرض، ثم اجتمع به إخوته وأبواه فخرّوا له سجّدًا، فتحققت رؤياه، وقال: «رَبِّ قَدْ آتَيْتَنِي مِنَ الْمُلْكِ وَعَلَّمْتَنِي مِنْ تَأْوِيلِ الْأَحَادِيثِ».",
                                "He left prison after interpreting the king's dream, and the king placed him over the storehouses of the land. Then his brothers and parents came and fell prostrate to him, fulfilling his vision, and he said: “My Lord, You have given me authority and taught me the interpretation of dreams” (Quran 12:101).",
                            ),
                            RefParagraph(
                                "في سورة يوسف — أحسن القصص — دروس في الصبر والعفة والعفو: «لَا تَثْرِيبَ عَلَيْكُمُ الْيَوْمَ يَغْفِرُ اللَّهُ لَكُمْ».",
                                "In Surah Yusuf — the best of stories — are lessons in patience, chastity and forgiveness: “No blame will there be upon you today. May Allah forgive you” (Quran 12:92).",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "lessons",
                        titleAr = "دروس وعبر",
                        titleEn = "Lessons and wisdom",
                        paragraphs = listOf(
                            RefParagraph(
                                "سورة يوسف أحسن القصص، وفيها دروس جامعة: الحسد يحمل الإخوة على الكيد لأخيهم، والصبر على البلاء يرفع صاحبه، والعفة عن الحرام تورث العزة والملك: «كَذَٰلِكَ لِنَصْرِفَ عَنْهُ السُّوءَ وَالْفَحْشَاءَ».",
                                "Surah Yusuf is the best of stories, containing comprehensive lessons: envy drives brothers to plot against their brother, patience through affliction raises its bearer, and chastity from the forbidden earns honour and authority: “Thus it was, that We might avert from him evil and immorality” (Quran 12:24).",
                            ),
                            RefParagraph(
                                "في نهاية القصة أعظم درس في العفو: «لَا تَثْرِيبَ عَلَيْكُمُ الْيَوْمَ يَغْفِرُ اللَّهُ لَكُمْ وَهُوَ أَرْحَمُ الرَّاحِمِينَ» — فالعفو عن المسيء مع القدرة من صفات الكرام، والله يغفر لمن عفا.",
                                "At the end of the story is the greatest lesson in forgiveness: “No blame will there be upon you today. May Allah forgive you, and He is the most merciful of the merciful” (Quran 12:92) — pardoning the wrongdoer when able is a trait of the noble, and God forgives those who forgive.",
                            ),
                        ),
                    ),

                ),
            ),
            RefTopic(
                id = "ayyub",
                titleAr = "أيوب عليه السلام",
                titleEn = "Job (peace be upon him)",
                summaryAr = "مثل الصبر، ابتلاه الله في ماله وولده وجسده فصبر.",
                summaryEn = "The example of patience; God tested him in wealth, children and body, and he was patient.",
                sections = listOf(
                    RefSection(
                        id = "story",
                        titleAr = "قصته",
                        titleEn = "His story",
                        paragraphs = listOf(
                            RefParagraph(
                                "كان أيوب نبيًا كثير المال والولد، فابتلاه الله بذهاب ماله وولده ومرض جسده، فصبر صبرًا جميلًا ولم يشكُ إلا إلى الله: «أَنِّي مَسَّنِيَ الضُّرُّ وَأَنْتَ أَرْحَمُ الرَّاحِمِينَ».",
                                "Job was a prophet of great wealth and many children. God tested him with the loss of his wealth and children and illness of his body, and he bore it with beautiful patience, complaining only to God: “Indeed, adversity has touched me, and You are the most merciful of the merciful” (Quran 21:83).",
                            ),
                            RefParagraph(
                                "استجاب الله له، فأمره أن يضرب برجله، فنبعت عين، وردّ الله عليه أهله ومثلهم معهم رحمة: «وَآتَيْنَاهُ أَهْلَهُ وَمِثْلَهُمْ مَعَهُمْ رَحْمَةً مِنْ عِنْدِنَا وَذِكْرَىٰ لِلْعَابِدِينَ».",
                                "God answered him, commanding him to strike with his foot, and a spring gushed forth. God restored his family to him and the like of them with them as mercy: “And We gave him back his family and the like of them with them as mercy from Us and a reminder for the worshippers” (Quran 21:84).",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "lessons",
                        titleAr = "دروس وعبر",
                        titleEn = "Lessons and wisdom",
                        paragraphs = listOf(
                            RefParagraph(
                                "في أيوب المثل الأعلى في الصبر على البلاء: جمع الله له الابتلاء في المال والولد والجسد، فصبر صبرًا جميلًا ولم يسخط ولم يتبرم، ودعا ربه بضراعة لا بجزع: «أَنِّي مَسَّنِيَ الضُّرُّ وَأَنْتَ أَرْحَمُ الرَّاحِمِينَ».",
                                "In Job is the highest example of patience under trial: God gathered for him affliction in wealth, children and body, yet he bore it with beautiful patience without resentment, calling upon his Lord with humility rather than panic: “Indeed, adversity has touched me, and You are the most merciful of the merciful” (Quran 21:83).",
                            ),
                            RefParagraph(
                                "وفي عاقبته عبرة أن الصبر عقباه الفرج: رد الله عليه أهله ومثلهم معهم رحمة وذكرى للعابدين، وأن البلاء إذا نزل صبرًا كان تمحيصًا ورفعة لا هوانًا.",
                                "His outcome teaches that the end of patience is relief: God restored his family to him and the like of them with them, as mercy and a reminder for the worshippers, and that affliction borne with patience becomes purification and elevation, not humiliation.",
                            ),
                        ),
                    ),

                ),
            ),
            RefTopic(
                id = "shuayb",
                titleAr = "شعيب عليه السلام",
                titleEn = "Shu'ayb (peace be upon him)",
                summaryAr = "نبي مدين، دعاهم إلى إيفاء الكيل والميزان، فكذبوه فأهلكوا.",
                summaryEn = "The prophet of Madyan; he called them to fair measure and balance, they denied him, and they were destroyed.",
                sections = listOf(
                    RefSection(
                        id = "story",
                        titleAr = "قصته",
                        titleEn = "His story",
                        paragraphs = listOf(
                            RefParagraph(
                                "أرسل الله شعيبًا إلى أهل مدين، وكانوا يبخسون المكيال والميزان ويقطعون السبيل، فقال لهم: «أَوْفُوا الْكَيْلَ وَلَا تَكُونُوا مِنَ الْمُخْسِرِينَ * وَزِنُوا بِالْقِسْطَاسِ الْمُسْتَقِيمِ».",
                                "God sent Shu'ayb to the people of Madyan, who cheated in measure and balance and blocked the roads. He told them: “Give full measure and do not be of those who cause loss, and weigh with an even balance” (Quran 26:181–182).",
                            ),
                            RefParagraph(
                                "كذبوه وتوعدوه بالرجم والإخراج، فجاءهم العذاب: «فَأَخَذَتْهُمُ الرَّجْفَةُ فَأَصْبَحُوا فِي دَارِهِمْ جَاثِمِينَ».",
                                "They denied him and threatened to stone and expel him, so the punishment came: “So the earthquake seized them, and they became within their home fallen” (Quran 7:91).",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "lessons",
                        titleAr = "دروس وعبر",
                        titleEn = "Lessons and wisdom",
                        paragraphs = listOf(
                            RefParagraph(
                                "في شعيب تقرير حرمة الكسب الحرام: كان قومه يبخسون المكيال والميزان ويقطعون السبيل، فأمرهم بإيفاء الكيل والوزن بالقسطاس المستقيم، فكذبوه فأخذتهم الرجفة.",
                                "In Shu'ayb we see the prohibition of unlawful earnings: his people cheated in measure and balance and blocked the roads, so he commanded them to give full measure and weigh with an even balance; they denied him and the earthquake seized them.",
                            ),
                            RefParagraph(
                                "في القصة أيضًا أن النصح في المال من الدين، وأن من يتولّ الكبير من الله هلك كما هلك من قبله: «وَمَا أُرِيدُ أَنْ أُخَالِفَكُمْ إِلَىٰ مَا أَنْهَاكُمْ عَنْهُ» — فلا يأمر ناصح إلا بما يفعل.",
                                "The story also shows that honest advice in matters of wealth is part of the religion, and that whoever turns away from the great command of God perishes as those before him perished: “And I do not intend to differ from you in that which I have forbidden you” (Quran 11:88) — the sincere adviser commands only what he himself does.",
                            ),
                        ),
                    ),

                ),
            ),
            RefTopic(
                id = "musa",
                titleAr = "موسى عليه السلام",
                titleEn = "Moses (peace be upon him)",
                summaryAr = "كليم الله، أرسله إلى فرعون، وأيده الله بآيات عظيمة.",
                summaryEn = "The one to whom God spoke; he was sent to Pharaoh, supported by great signs.",
                sections = listOf(
                    RefSection(
                        id = "story",
                        titleAr = "قصته",
                        titleEn = "His story",
                        paragraphs = listOf(
                            RefParagraph(
                                "وُلد موسى في زمن يذبح فيه فرعون أبناء بني إسرائيل، فألقته أمه في اليم، فالتقطه آل فرعون، ورباه الله في بيته. ثم كبر، وقتل رجلًا خطأ، فخرج إلى مدين، وعاد بعد أن كلّمه الله في الوادي المقدس طوى.",
                                "Moses was born when Pharaoh was slaughtering the sons of Israel; his mother cast him into the river, and the household of Pharaoh picked him up, and God raised him in their home. He grew, killed a man by mistake, fled to Madyan, and returned after God spoke to him in the sacred valley of Tuwa.",
                            ),
                            RefParagraph(
                                "أرسله الله إلى فرعون الذي قال: «أَنَا رَبُّكُمُ الْأَعْلَىٰ»، فأيده الله بآيات: العصا واليد، ثم بالطوفان والجراد والقمل والضفادع والدم، فكذب واستكبر.",
                                "God sent him to Pharaoh, who said: “I am your most high lord” (Quran 79:24). God supported him with signs: the staff and the hand, then the flood, locusts, lice, frogs and blood — yet he denied and was arrogant.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "exodus",
                        titleAr = "الخروج والنجاة",
                        titleEn = "The exodus and deliverance",
                        paragraphs = listOf(
                            RefParagraph(
                                "أمر الله موسى أن يخرج ببني إسرائيل ليلًا، فتبعهم فرعون بجنوده، فلما تراءى الجمعان أوحى الله إلى موسى: «أَنِ اضْرِبْ بِعَصَاكَ الْبَحْرَ فَانْفَلَقَ» فانفلق البحر، ونجا موسى وقومه، وغرق فرعون وجنوده.",
                                "God commanded Moses to take the Children of Israel out by night, and Pharaoh followed with his soldiers. When the two hosts saw each other, God revealed to Moses: “Strike the sea with your staff,” and it parted. Moses and his people were saved, and Pharaoh and his soldiers drowned.",
                            ),
                            RefParagraph(
                                "أعطى الله موسى التوراة، وفيها هدى ونور، وجعل من ذريته أنبياء. وأكثر الله من ذكره في القرآن.",
                                "God gave Moses the Torah, in which is guidance and light, and made prophets from his descendants. God mentioned him frequently in the Quran.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "lessons",
                        titleAr = "دروس وعبر",
                        titleEn = "Lessons and wisdom",
                        paragraphs = listOf(
                            RefParagraph(
                                "في موسى أعظم أمثلة الشجاعة في الحق: واجه فرعون الطاغية بلسان رقيق وكلمة صدق: «فَقُولَا لَهُ قَوْلًا لَيِّنًا لَعَلَّهُ يَتَذَكَّرُ أَوْ يَخْشَىٰ»، وضرب البحر بعصاه فانفلق بإذن الله، فنجا المؤمنون وغرق الطغاة.",
                                "In Moses is the greatest example of courage upon the truth: he faced the tyrant Pharaoh with a gentle word and truthful speech: “And speak to him with gentle speech that perhaps he may be reminded or fear” (Quran 20:44), and struck the sea with his staff and it parted by God's permission, so the believers were saved and the tyrants drowned.",
                            ),
                            RefParagraph(
                                "وفي قصة موسى دروس في التوكل: لما قال له أصحابه «إِنَّا لَمُدْرَكُونَ» قال: «كَلَّا إِنَّ مَعِيَ رَبِّي سَيَهْدِينِ» — فلا ييأس مؤمن من نصر الله وإن ضاقت السبل، فإن مع العسر يسرًا.",
                                "Moses' story also teaches reliance on God: when his companions said “Indeed, we are overtaken,” he said: “No! Indeed, with me is my Lord; He will guide me” (Quran 26:62) — a believer never despairs of God's help even when paths narrow, for with hardship comes ease.",
                            ),
                        ),
                    ),

                ),
            ),
            RefTopic(
                id = "dawud",
                titleAr = "داود عليه السلام",
                titleEn = "David (peace be upon him)",
                summaryAr = "ملك نبي، آتاه الله الزبور، وألان له الحديد، وقضى بين الناس بالعدل.",
                summaryEn = "A king and prophet; God gave him the Psalms, softened iron for him, and he judged between people with justice.",
                sections = listOf(
                    RefSection(
                        id = "story",
                        titleAr = "قصته",
                        titleEn = "His story",
                        paragraphs = listOf(
                            RefParagraph(
                                "قتل داود جالوت، فآتاه الله الملك والحكمة، وأنزل عليه الزبور: «وَآتَيْنَا دَاوُودَ زَبُورًا». وألان الله له الحديد، وجعل الجبال تسبح معه، وكان يصوم يومًا ويفطر يومًا.",
                                "David killed Goliath, and God gave him kingship and wisdom and revealed to him the Psalms: “And to David We gave the Psalms” (Quran 17:55). God softened iron for him, made the mountains glorify with him, and he fasted alternate days.",
                            ),
                            RefParagraph(
                                "قضى داود بين الناس بالعدل، ومن قصصه المشهورة قصة الخصمين اللذين تسورا المحراب، فتنبّه وطلب المغفرة: «وَظَنَّ دَاوُودُ أَنَّمَا فَتَنَّاهُ فَاسْتَغْفَرَ رَبَّهُ وَخَرَّ رَاكِعًا وَأَنَابَ».",
                                "David judged between people with justice. Among his well-known stories is that of the two litigants who climbed over the sanctuary wall; he realised it was a trial and sought forgiveness: “And David became certain that We had tried him, and he asked forgiveness of his Lord and fell bowing and turned in repentance” (Quran 38:24).",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "lessons",
                        titleAr = "دروس وعبر",
                        titleEn = "Lessons and wisdom",
                        paragraphs = listOf(
                            RefParagraph(
                                "في داود بيان أن القوة مع التقوى أمانة: جمع الله له الملك والنبوة والزبور، وألان له الحديد، وقضى بالعدل بين الناس، فلما فُتن بالخصمين سارع إلى التوبة: «فَاسْتَغْفَرَ رَبَّهُ وَخَرَّ رَاكِعًا وَأَنَابَ».",
                                "In David we see that strength with piety is a trust: God combined for him kingship, prophethood and the Psalms, softened iron for him, and he judged between people with justice; when he was tried by the two litigants he hastened to repentance: “He asked forgiveness of his Lord and fell bowing and turned in repentance” (Quran 38:24).",
                            ),
                            RefParagraph(
                                "وصيام داود — يومًا ويومًا — أعظم الصيام عند الله، كما أن تسبيح الجبال معه آية على أن القلوب إذا صفت سبحت معها الخلائق؛ وفي ذلك حث على صيام داود وقيامه.",
                                "David's fast — a day on, a day off — is the most beloved fasting to God, just as the mountains glorifying with him is a sign that when hearts are pure, all creation glorifies with them; it encourages following his fast and night prayer.",
                            ),
                        ),
                    ),

                ),
            ),
            RefTopic(
                id = "sulayman",
                titleAr = "سليمان عليه السلام",
                titleEn = "Solomon (peace be upon him)",
                summaryAr = "ملك لا ينبغي لأحد من بعده، سُخّرت له الريح والجن، وفهم منطق الطير.",
                summaryEn = "A kingdom not given to anyone after him; the wind and jinn were subjected to him, and he understood the speech of birds.",
                sections = listOf(
                    RefSection(
                        id = "story",
                        titleAr = "قصته",
                        titleEn = "His story",
                        paragraphs = listOf(
                            RefParagraph(
                                "ورث سليمان داود، وسأل ربه ملكًا لا ينبغي لأحد من بعده، فسخّر الله له الريح تجري بأمره، والجن يعملون له، وعُلّم منطق الطير، وكان يفهم لغة النمل.",
                                "Solomon inherited from David and asked his Lord for a kingdom not given to anyone after him. God subjected the wind to run by his command, and the jinn to work for him; he was taught the speech of birds and understood the language of ants.",
                            ),
                            RefParagraph(
                                "من قصصه مع بلقيس ملكة سبأ: أرسل إليها الهدهد، ثم أحضر عرشها قبل أن يرتد إليه طرفه، فلما رأت آيات الله أسلمت: «رَبِّ إِنِّي ظَلَمْتُ نَفْسِي وَأَسْلَمْتُ مَعَ سُلَيْمَانَ لِلَّهِ رَبِّ الْعَالَمِينَ».",
                                "From his stories with Bilqis, the queen of Sheba, he sent the hoopoe to her, then brought her throne before the blink of an eye. When she saw God's signs she submitted: “My Lord, indeed I have wronged myself, and I submit with Solomon to God, Lord of the worlds” (Quran 27:44).",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "lessons",
                        titleAr = "دروس وعبر",
                        titleEn = "Lessons and wisdom",
                        paragraphs = listOf(
                            RefParagraph(
                                "في سليمان بيان فضل الشكر على النعم: سأل ربه ملكًا لا ينبغي لأحد من بعده، فسخّر له الريح والجن، وفُهم منطق الطير، فكان شكورًا: «هَٰذَا مِنْ فَضْلِ رَبِّي لِيَبْلُوَنِي أَأَشْكُرُ أَمْ أَكْفُرُ».",
                                "In Solomon we see the virtue of gratitude for blessings: he asked his Lord for a kingdom not given to anyone after him, and God subjected the wind and jinn to him and taught him the speech of birds, so he was grateful: “This is from the favour of my Lord to test me whether I will be grateful or ungrateful” (Quran 27:40).",
                            ),
                            RefParagraph(
                                "وفي قصته مع بلقيس بيان أن الهداية تبلغ من شاء الله مهما بَعُدت الأسباب: وصلها دعوة التوحيد فأسلمت، وأن كلمة التوحيد هي أساس الملك والدولة الصالحة.",
                                "His story with Bilqis shows that guidance reaches whom God wills however distant the means: the call to monotheism reached her and she submitted, and that the word of monotheism is the foundation of kingship and a righteous state.",
                            ),
                        ),
                    ),

                ),
            ),
            RefTopic(
                id = "ilyas",
                titleAr = "إلياس عليه السلام",
                titleEn = "Elijah (peace be upon him)",
                summaryAr = "نبي دعا قومه إلى ترك عبادة بعل.",
                summaryEn = "A prophet who called his people to abandon the worship of Baal.",
                sections = listOf(
                    RefSection(
                        id = "story",
                        titleAr = "قصته",
                        titleEn = "His story",
                        paragraphs = listOf(
                            RefParagraph(
                                "أرسل الله إلياس إلى قومه الذين عبدوا «بعل»، فقال لهم: «أَتَدْعُونَ بَعْلًا وَتَذَرُونَ أَحْسَنَ الْخَالِقِينَ».",
                                "God sent Elijah to his people who worshipped “Baal.” He told them: “Do you call upon Baal and leave the best of creators?” (Quran 37:125).",
                            ),
                            RefParagraph(
                                "دعاهم إلى عبادة الله وحده، فكذبوه، قال تعالى: «وَإِنَّ إِلْيَاسَ لَمِنَ الْمُرْسَلِينَ».",
                                "He called them to worship God alone, and they denied him. God said: “And indeed, Elijah was among the messengers” (Quran 37:123).",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "lessons",
                        titleAr = "دروس وعبر",
                        titleEn = "Lessons and wisdom",
                        paragraphs = listOf(
                            RefParagraph(
                                "في إلياس إنكار عبادة غير الله أيا كان المعبود: كان قومه يعبدون «بعل»، فدعاهم إلى عبادة الله وحده: «أَتَدْعُونَ بَعْلًا وَتَذَرُونَ أَحْسَنَ الْخَالِقِينَ».",
                                "In Elijah is the rejection of worshipping anything other than God, whatever the object: his people worshipped “Baal,” and he called them to worship God alone: “Do you call upon Baal and leave the best of creators?” (Quran 37:125).",
                            ),
                            RefParagraph(
                                "وفي عاقبة قومه عبرة أن الشرك لا يدوم له سلطان: «فَكَذَّبُوهُ فَإِنَّهُمْ لَمُحْضَرُونَ» — والله محيط بالظالمين، وناصره الأنبياء وأتباعهم.",
                                "The fate of his people teaches that shirk never keeps its dominion: “But they denied him, so indeed they will be brought” (Quran 37:127) — God encompasses the wrongdoers and supports His prophets and their followers.",
                            ),
                        ),
                    ),

                ),
            ),
            RefTopic(
                id = "alyasa",
                titleAr = "اليسع عليه السلام",
                titleEn = "Elisha (peace be upon him)",
                summaryAr = "نبي من أنبياء بني إسرائيل، ذُكر في القرآن بالثناء.",
                summaryEn = "A prophet of the Children of Israel, mentioned in the Quran with praise.",
                sections = listOf(
                    RefSection(
                        id = "story",
                        titleAr = "قصته",
                        titleEn = "His story",
                        paragraphs = listOf(
                            RefParagraph(
                                "اليسع عليه السلام من أنبياء بني إسرائيل، أرسل بعد إلياس، ودعا قومه إلى التوحيد، قال تعالى: «وَاذْكُرْ إِسْمَاعِيلَ وَالْيَسَعَ وَذَا الْكِفْلِ وَكُلٌّ مِنَ الْأَخْيَارِ».",
                                "Elisha (peace be upon him) is among the prophets of the Children of Israel, sent after Elijah, calling his people to monotheism. God said: “And mention Ismail and Elisha and Dhul-Kifl, and all were of the best” (Quran 38:48).",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "lessons",
                        titleAr = "دروس وعبر",
                        titleEn = "Lessons and wisdom",
                        paragraphs = listOf(
                            RefParagraph(
                                "في اليسع بيان أن الله يحفظ أولياءه ويرفع ذكرهم: ذكره الله في القرآن مع الأخيار: «وَاذْكُرْ إِسْمَاعِيلَ وَالْيَسَعَ وَذَا الْكِفْلِ وَكُلٌّ مِنَ الْأَخْيَارِ»، فكفى بالقرآن ذكرًا ورفعة.",
                                "In Elisha we see that God preserves His allies and elevates their mention: God mentioned him in the Quran among the best: “And mention Ismail and Elisha and Dhul-Kifl, and all were of the best” (Quran 38:48) — mention in the Quran suffices as honour.",
                            ),
                            RefParagraph(
                                "وفي ذكره مع ذي الكفل إشارة إلى أن صفوة الله من عباده يشملون أهل العلم والعبادة والصدق، وأن الذكر الجميل أجر عاجل للمؤمنين.",
                                "His mention alongside Dhul-Kifl indicates that God's chosen servants include people of knowledge, worship and truthfulness, and that a good remembrance is an immediate reward for the believers.",
                            ),
                        ),
                    ),

                ),
            ),
            RefTopic(
                id = "yunus",
                titleAr = "يونس عليه السلام",
                titleEn = "Jonah (peace be upon him)",
                summaryAr = "صاحب الحوت، خرج مغاضبًا فالتقمه الحوت، فدعا في الظلمات فنجاه الله.",
                summaryEn = "The companion of the whale; he left in anger, the whale swallowed him, he called in the darkness, and God saved him.",
                sections = listOf(
                    RefSection(
                        id = "story",
                        titleAr = "قصته",
                        titleEn = "His story",
                        paragraphs = listOf(
                            RefParagraph(
                                "أرسل الله يونس إلى أهل نينوى، فدعاهم فلم يؤمنوا، فخرج مغاضبًا وركب السفينة، فسُهم فأُلقي في البحر، فالتقمه الحوت، ونادى في الظلمات: «لَا إِلَٰهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ».",
                                "God sent Jonah to the people of Nineveh. He called them but they did not believe, so he left in anger and boarded a ship; the lot fell on him and he was cast into the sea, and the whale swallowed him. He called in the darkness: “There is no deity except You; exalted are You. Indeed, I have been of the wrongdoers” (Quran 21:87).",
                            ),
                            RefParagraph(
                                "استجاب الله له ونجاه من الغم: «فَاسْتَجَبْنَا لَهُ وَنَجَّيْنَاهُ مِنَ الْغَمِّ وَكَذَٰلِكَ نُنْجِي الْمُؤْمِنِينَ». وأنبت عليه شجرة من يقطين، ثم آمن قومه كلهم فرفع الله عنهم العذاب.",
                                "God answered him and saved him from distress: “So We responded to him and saved him from the distress. And thus do We save the believers” (Quran 21:88). God caused a gourd plant to grow over him, then his people all believed and God removed the punishment from them.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "lessons",
                        titleAr = "دروس وعبر",
                        titleEn = "Lessons and wisdom",
                        paragraphs = listOf(
                            RefParagraph(
                                "في يونس أعظم درس في الدعاء عند الشدة: نادى في الظلمات — ظلمة الليل والبحر وبطن الحوت — «لَا إِلَٰهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ»، فاستجاب الله له ونجاه من الغم، وجعل ذلك منجاة للمؤمنين.",
                                "In Jonah is the greatest lesson in supplication at times of distress: he called in the darkness — of night, sea and the belly of the whale — “There is no deity except You; exalted are You. Indeed, I have been of the wrongdoers” (Quran 21:87), and God answered him, saved him from distress, and made that a means of deliverance for the believers.",
                            ),
                            RefParagraph(
                                "وفي القصة أن توبة الأمة جملة ترفع عنها العذاب: لما آمن أهل نينوى كلهم متعهم الله إلى حين، فالدعاء والتوبة يقلبان مقادير العذاب بإذن الله.",
                                "The story also shows that a nation's collective repentance removes punishment from it: when the people of Nineveh all believed, God granted them enjoyment for a time — supplication and repentance overturn the decrees of punishment by God's permission.",
                            ),
                        ),
                    ),

                ),
            ),
            RefTopic(
                id = "zakariyya_yahya",
                titleAr = "زكريا ويحيى عليهما السلام",
                titleEn = "Zechariah and John (peace be upon them)",
                summaryAr = "دعا زكريا ربه فوهب له يحيى، وكان يحيى سيدًا حصورًا نبيًا.",
                summaryEn = "Zechariah called his Lord and was granted John, who was a master, chaste and a prophet.",
                sections = listOf(
                    RefSection(
                        id = "story",
                        titleAr = "قصتهما",
                        titleEn = "Their story",
                        paragraphs = listOf(
                            RefParagraph(
                                "كان زكريا كافل مريم، فلما رأى عندها رزقًا من غير حول منه قال: «أَنَّىٰ لَكِ هَٰذَا» قالت: «هُوَ مِنْ عِنْدِ اللَّهِ». فهنالك دعا ربه أن يهب له غلامًا، وكان شيخًا كبيرًا وامرأته عاقرًا.",
                                "Zechariah was the guardian of Mary. When he saw provision with her from no effort of his, he asked: “Where is this from?” She said: “It is from Allah.” There he called upon his Lord to grant him a child, though he was an old man and his wife barren.",
                            ),
                            RefParagraph(
                                "فبشرته الملائكة بيحيى: «يَا زَكَرِيَّا إِنَّا نُبَشِّرُكَ بِغُلَامٍ اسْمُهُ يَحْيَىٰ لَمْ نَجْعَلْ لَهُ مِنْ قَبْلُ سَمِيًّا». وكان يحيى نبيًا حصورًا آتاه الله الحكم صبيًا.",
                                "The angels gave him glad tidings of John: “O Zechariah, indeed We give you good tidings of a boy whose name will be John. We have not assigned to any before him this name” (Quran 19:7). John was a prophet and chaste, given judgment while still a boy.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "lessons",
                        titleAr = "دروس وعبر",
                        titleEn = "Lessons and wisdom",
                        paragraphs = listOf(
                            RefParagraph(
                                "في زكريا بيان فضل الدعاء والإلحاح فيه: دعا ربه في السر وهو شيخ كبير وامرأته عاقر، فاستجاب الله له وهبه يحيى: «هُنَالِكَ دَعَا زَكَرِيَّا رَبَّهُ قَالَ رَبِّ هَبْ لِي مِنْ لَدُنْكَ ذُرِّيَّةً طَيِّبَةً».",
                                "In Zechariah we see the virtue of supplication and persistence in it: he called his Lord in secret while an old man with a barren wife, and God answered him and granted him John: “At that, Zechariah called to his Lord, saying: My Lord, grant me from Yourself a good offspring” (Quran 3:38).",
                            ),
                            RefParagraph(
                                "وفي يحيى بيان أن العفة والتقوى مقدمتان على الشهوات: كان حصورًا آتاه الله الحكم صبيًا، وأن الله يجعل للتقوى من كل هم مخرجًا.",
                                "In John we see that chastity and piety come before desires: he was chaste, and God gave him judgment while still a boy — and God makes for piety a way out of every concern.",
                            ),
                        ),
                    ),

                ),
            ),
            RefTopic(
                id = "isa",
                titleAr = "عيسى عليه السلام",
                titleEn = "Jesus (peace be upon him)",
                summaryAr = "كلمة الله ألقاها إلى مريم، وُلد بلا أب، وأيده الله بالمعجزات.",
                summaryEn = "The word of God cast to Mary; born without a father, supported by God with miracles.",
                sections = listOf(
                    RefSection(
                        id = "birth",
                        titleAr = "ميلاده",
                        titleEn = "His birth",
                        paragraphs = listOf(
                            RefParagraph(
                                "بشرت الملائكة مريم بعيسى: «إِنَّ اللَّهَ يُبَشِّرُكِ بِكَلِمَةٍ مِنْهُ اسْمُهُ الْمَسِيحُ عِيسَى ابْنُ مَرْيَمَ وَجِيهًا فِي الدُّنْيَا وَالْآخِرَةِ». فحملت به بلا أب، ووضعته تحت النخلة، ونطق في المهد: «إِنِّي عَبْدُ اللَّهِ آتَانِيَ الْكِتَابَ وَجَعَلَنِي نَبِيًّا».",
                                "The angels gave Mary glad tidings of Jesus: “Indeed, Allah gives you good tidings of a word from Him, whose name will be the Messiah, Jesus, son of Mary, distinguished in this world and the Hereafter” (Quran 3:45). She conceived him without a father, gave birth beneath the palm tree, and he spoke in the cradle: “Indeed, I am the servant of Allah. He has given me the Scripture and made me a prophet” (Quran 19:30).",
                            ),
                            RefParagraph(
                                "أيد الله عيسى بمعجزات: يخلق من الطين كهيئة الطير فينفخ فيه فيكون طيرًا بإذن الله، ويبرئ الأكمه والأبرص، ويحيي الموتى بإذن الله، وينبئ بما يأكلون ويدخرون.",
                                "God supported Jesus with miracles: he formed from clay the likeness of a bird and breathed into it and it became a bird by God's permission, healed the blind and the leper, brought the dead to life by God's permission, and told people what they ate and stored.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "message",
                        titleAr = "رسالته",
                        titleEn = "His message",
                        paragraphs = listOf(
                            RefParagraph(
                                "دعا عيسى إلى عبادة الله وحده: «إِنَّ اللَّهَ رَبِّي وَرَبُّكُمْ فَاعْبُدُوهُ هَٰذَا صِرَاطٌ مُسْتَقِيمٌ». وأنكر أن يكون ابنًا لله أو إلهًا من دون الله.",
                                "Jesus called to the worship of God alone: “Indeed, Allah is my Lord and your Lord, so worship Him. That is the straight path” (Quran 3:51). He denied being a son of God or a god besides God.",
                            ),
                            RefParagraph(
                                "لم يصلب عيسى ولم يُقتل، بل رفعه الله إليه: «وَمَا قَتَلُوهُ وَمَا صَلَبُوهُ وَلَٰكِنْ شُبِّهَ لَهُمْ». وسينزل آخر الزمان ويحكم بشريعة الإسلام.",
                                "Jesus was neither crucified nor killed; God raised him to Himself: “And they did not kill him, nor did they crucify him, but another was made to resemble him to them” (Quran 4:157). He will descend at the end of time and judge by the law of Islam.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "lessons",
                        titleAr = "دروس وعبر",
                        titleEn = "Lessons and wisdom",
                        paragraphs = listOf(
                            RefParagraph(
                                "في عيسى بيان قدرة الله وعظمته: وُلد من غير أب، ونطق في المهد، وأحيا الموتى وأبرأ الأكمه والأبرص بإذن الله، فكل ذلك آيات على أن الله على كل شيء قدير.",
                                "In Jesus we see the power and greatness of God: he was born without a father, spoke in the cradle, brought the dead to life and healed the blind and the leper by God's permission — all signs that God is over all things competent.",
                            ),
                            RefParagraph(
                                "وفي رسالته تقرير التوحيد ورفض الغلو: «لَقَدْ كَفَرَ الَّذِينَ قَالُوا إِنَّ اللَّهَ ثَالِثُ ثَلَاثَةٍ» — فمن غلا في الأنبياء فقد خرج عن دينهم، وعيسى بريء ممن اتخذه إلهًا أو ابنًا.",
                                "His message affirms monotheism and rejects extremism: “They have certainly disbelieved who say that Allah is the third of three” (Quran 5:73) — whoever exaggerates the status of prophets has left their religion, and Jesus is innocent of those who took him as a god or a son.",
                            ),
                        ),
                    ),

                ),
            ),
            RefTopic(
                id = "muhammad",
                titleAr = "محمد ﷺ — خاتم الأنبياء",
                titleEn = "Muhammad ﷺ — the seal of the prophets",
                summaryAr = "أشرف الأنبياء، خاتمهم، وأفضلهم، وسيرته كاملة في كتاب «السيرة النبوية».",
                summaryEn = "The noblest and seal of the prophets; his full biography is in the “Prophetic Biography” book.",
                sections = listOf(
                    RefSection(
                        id = "seal",
                        titleAr = "خاتم النبيين",
                        titleEn = "The seal of the prophets",
                        paragraphs = listOf(
                            RefParagraph(
                                "محمد ﷺ خاتم الأنبياء والمرسلين، بعثه الله رحمة للعالمين، وفضّله على الأنبياء، قال تعالى: «وَمَا أَرْسَلْنَاكَ إِلَّا رَحْمَةً لِلْعَالَمِينَ». ووصفه بأنه: «خَاتَمَ النَّبِيِّينَ».",
                                "Muhammad ﷺ is the seal of the prophets and messengers, sent by God as a mercy to the worlds. God said: “And We have not sent you except as a mercy to the worlds” (Quran 21:107) and described him as the “seal of the prophets” (Quran 33:40).",
                            ),
                            RefParagraph(
                                "سيرته الكاملة من مولده إلى وفاته مفصلة في كتاب «السيرة النبوية» داخل هذا التطبيق.",
                                "His complete biography, from birth to death, is detailed in the “Prophetic Biography” book within this app.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "lessons",
                        titleAr = "دروس وعبر",
                        titleEn = "Lessons and wisdom",
                        paragraphs = listOf(
                            RefParagraph(
                                "محمد ﷺ خاتم الأنبياء وأفضلهم، أرسله الله رحمة للعالمين، وختم به النبوة، ورفع له ذكره، وجعل أمته خير أمة أخرجت للناس. فمحبته من الإيمان، واتباعه سبيل النجاة: «قُلْ إِنْ كُنْتُمْ تُحِبُّونَ اللَّهَ فَاتَّبِعُونِي يُحْبِبْكُمُ اللَّهُ».",
                                "Muhammad ﷺ is the seal and the best of the prophets; God sent him as a mercy to the worlds, sealed prophethood with him, exalted his mention, and made his nation the best nation brought forth for mankind. Loving him is part of faith, and following him is the path of salvation: “Say: If you should love Allah, then follow me, and Allah will love you” (Quran 3:31).",
                            ),
                            RefParagraph(
                                "وفي كونه خاتم النبيين بطلان دعوى نبوة من بعده، وثبوت أن شريعته ناسخة لما قبلها وباقية إلى قيام الساعة: «مَا كَانَ مُحَمَّدٌ أَبَا أَحَدٍ مِنْ رِجَالِكُمْ وَلَٰكِنْ رَسُولَ اللَّهِ وَخَاتَمَ النَّبِيِّينَ».",
                                "His being the seal of the prophets invalidates any claim of prophethood after him and affirms that his law abrogates what came before it and remains until the Hour: “Muhammad is not the father of any of your men, but he is the Messenger of Allah and the seal of the prophets” (Quran 33:40).",
                            ),
                        ),
                    ),

                ),
            ),
        ),
    )
}
