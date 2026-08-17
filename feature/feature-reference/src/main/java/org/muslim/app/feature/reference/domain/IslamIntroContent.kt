package org.muslim.app.feature.reference.domain

/**
 * كتاب «التعريف بالإسلام» — منطلق مرجعي شامل ومفهرس يشرح الإسلام للمسلم ولغير
 * المسلم. النصوص مبنية على المصادر المعتمدة (القرآن والسنة وما اتفق عليه أهل
 * العلم) وتُعرض بحياد. تخضع دائمًا لمراجعة شرعية مستقلة قبل النشر (§10).
 */
object IslamIntroContent {

    val book: ReferenceBook = ReferenceBook(
        id = "islam",
        titleAr = "التعريف بالإسلام",
        titleEn = "Introduction to Islam",
        subtitleAr = "مرجع شامل ومفهرس للمسلم ولغير المسلم",
        subtitleEn = "A comprehensive, indexed reference for Muslims and non-Muslims",
        topics = listOf(
            RefTopic(
                id = "what_is_islam",
                titleAr = "ما هو الإسلام؟",
                titleEn = "What is Islam?",
                summaryAr = "الإسلام دين التوحيد الذي بُعثت به الرسل جميعًا، وآخر رسالاته على النبي محمد ﷺ.",
                summaryEn = "Islam is the religion of monotheism brought by all prophets, with its final message revealed to Prophet Muhammad ﷺ.",
                sections = listOf(
                    RefSection(
                        id = "definition",
                        titleAr = "التعريف",
                        titleEn = "Definition",
                        paragraphs = listOf(
                            RefParagraph(
                                "الإسلام لغةً هو الاستسلام والانقياد، واصطلاحًا هو الاستسلام لله بالتوحيد، والانقياد له بالطاعة، والبراءة من الشرك وأهله. وهو الدين الذي ارتضاه الله لعباده، قال تعالى: «إِنَّ الدِّينَ عِنْدَ اللَّهِ الْإِسْلَامُ».",
                                "Linguistically, Islam means submission and surrender. In religious terms it is submitting to God (Allah) through monotheism, obeying Him, and disassociating from polytheism. It is the religion God chose for mankind: “Indeed, the religion in the sight of Allah is Islam” (Quran 3:19).",
                            ),
                            RefParagraph(
                                "الإسلام ليس دينًا جديدًا بمعزل عن الرسالات السابقة، بل هو استكمالها وتصديقها؛ فكل الأنبياء من آدم إلى عيسى دعوا إلى عبادة الله وحده، وجاء النبي محمد ﷺ بالرسالة الخاتمة التي تعمّ الناس جميعًا.",
                                "Islam is not a new religion severed from earlier revelations; it completes and confirms them. Every prophet from Adam to Jesus called to the worship of God alone, and Prophet Muhammad ﷺ brought the final message addressed to all of humanity.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "comprehensiveness",
                        titleAr = "شموخ الإسلام",
                        titleEn = "Comprehensiveness",
                        paragraphs = listOf(
                            RefParagraph(
                                "يتناول الإسلام جوانب الحياة كلها: العقيدة والعبادة، والأخلاق والمعاملات، والأسرة والمجتمع، والاقتصاد والسياسة. فهو دين ودنيا معًا، ينظّم علاقة العبد بربه وعلاقته بالناس والكون.",
                                "Islam addresses every aspect of life: creed and worship, ethics and transactions, family and society, economy and governance. It is both faith and daily life, regulating the servant's relationship with God and with people and the universe.",
                            ),
                            RefParagraph(
                                "من أراد أن يفهم الإسلام فعليه أن يرجع إلى مصدريه الأساسيين: القرآن الكريم، والسنة النبوية (أقوال النبي ﷺ وأفعاله وتقريراته)، على فهم السلف الصالح.",
                                "To understand Islam one should refer to its two primary sources: the Quran and the Sunnah (the sayings, actions and approvals of the Prophet ﷺ), as understood by the righteous predecessors.",
                            ),
                        ),
                    ),
                ),
            ),
            RefTopic(
                id = "islam_meaning",
                titleAr = "معنى كلمة «مسلم» والإيمان",
                titleEn = "The meaning of “Muslim” and faith",
                summaryAr = "المسلم هو من أسلم وجهه لله، والإيمان تصديق بالقلب وقول باللسان وعمل بالجوارح.",
                summaryEn = "A Muslim is one who submits to God; faith is belief in the heart, utterance on the tongue, and action of the limbs.",
                sections = listOf(
                    RefSection(
                        id = "muslim",
                        titleAr = "من هو المسلم؟",
                        titleEn = "Who is a Muslim?",
                        paragraphs = listOf(
                            RefParagraph(
                                "المسلم هو كل من يشهد أن لا إله إلا الله وأن محمدًا رسول الله، ويعمل بمقتضى ذلك. قال تعالى: «وَمَنْ أَحْسَنُ قَوْلًا مِمَّنْ دَعَا إِلَى اللَّهِ وَعَمِلَ صَالِحًا وَقَالَ إِنَّنِي مِنَ الْمُسْلِمِينَ».",
                                "A Muslim is anyone who testifies that there is no god but Allah and that Muhammad is His Messenger, and acts accordingly. God says: “And who is better in speech than one who invites to Allah and does righteousness and says, ‘Indeed, I am of the Muslims’?” (Quran 41:33).",
                            ),
                            RefParagraph(
                                "الإسلام والإيمان مرتبطان: الإسلام هو الاستسلام الظاهر، والإيمان هو التصديق الباطن، ولا يكمل أحدهما إلا بالآخر. وقد جمعهما النبي ﷺ في حديث جبريل المشهور.",
                                "Islam and faith are linked: Islam is outward submission and faith (iman) is inner conviction; neither is complete without the other. The Prophet ﷺ combined both in the well-known hadith of Gabriel.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "iman",
                        titleAr = "ماذا يعني الإيمان؟",
                        titleEn = "What does faith mean?",
                        paragraphs = listOf(
                            RefParagraph(
                                "الإيمان عند أهل السنة: قول باللسان، وتصديق بالقلب، وعمل بالجوارح، يزيد بالطاعة وينقص بالمعصية. وهو من أعمال القلوب قبل أن يكون أعمال الجوارح.",
                                "Faith, according to Ahl al-Sunnah, is speech of the tongue, conviction of the heart, and action of the limbs; it increases with obedience and decreases with disobedience. It begins as a work of the heart before it becomes a work of the limbs.",
                            ),
                            RefParagraph(
                                "الإيمان يتحقق بستة أركان: الإيمان بالله، وملائكته، وكتبه، ورسله، واليوم الآخر، والقدر خيره وشره — وتفصيلها في فصل «أركان الإيمان».",
                                "Faith rests on six pillars: belief in God, His angels, His books, His messengers, the Last Day, and divine decree, good and bad alike — detailed in the “Pillars of faith” chapter.",
                            ),
                        ),
                    ),
                ),
            ),
            RefTopic(
                id = "pillars_islam",
                titleAr = "أركان الإسلام الخمسة",
                titleEn = "The five pillars of Islam",
                summaryAr = "أساس الإسلام الذي بُني عليه: الشهادتان، والصلاة، والزكاة، والصوم، والحج.",
                summaryEn = "The foundation of Islam: the two testimonies, prayer, alms, fasting and pilgrimage.",
                sections = listOf(
                    RefSection(
                        id = "shahada",
                        titleAr = "الشهادتان والصلاة",
                        titleEn = "The testimonies and prayer",
                        paragraphs = listOf(
                            RefParagraph(
                                "قال النبي ﷺ: «بُني الإسلام على خمس: شهادة أن لا إله إلا الله وأن محمدًا رسول الله، وإقام الصلاة، وإيتاء الزكاة، وصوم رمضان، وحج البيت». الشهادتان هما مفتاح الدخول في الإسلام.",
                                "The Prophet ﷺ said: “Islam is built upon five: testifying that there is no god but Allah and that Muhammad is the Messenger of Allah, establishing prayer, giving zakat, fasting Ramadan, and pilgrimage to the House.” The two testimonies are the key to entering Islam.",
                            ),
                            RefParagraph(
                                "الصلاة عمود الدين، وهي خمس صلوات في اليوم والليلة: الفجر والظهر والعصر والمغرب والعشاء، وهي أول ما يُحاسب عليه العبد يوم القيامة.",
                                "Prayer is the pillar of the religion: five prayers each day and night — Fajr, Dhuhr, Asr, Maghrib and Isha — and it is the first thing for which the servant is held to account on the Day of Judgment.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "zakat_sawm_hajj",
                        titleAr = "الزكاة والصوم والحج",
                        titleEn = "Zakat, fasting and pilgrimage",
                        paragraphs = listOf(
                            RefParagraph(
                                "الزكاة حقّ مالي واجب في أموال محددة لمن بلغ النصاب، تُصرف للفقراء والمحتاجين وغيرهم من مصارفها الثمانية، وهي تطهير للمال ونماء له.",
                                "Zakat is an obligatory financial duty on specified wealth above the nisab, distributed to the poor, the needy and the other seven categories; it purifies and grows wealth.",
                            ),
                            RefParagraph(
                                "الصوم هو الإمساك عن المفطرات من طلوع الفجر إلى غروب الشمس في شهر رمضان، وهو تزكية للنفس وتعويد على التقوى. أما الحج فهو واجب مرة في العمر على المستطيع، إلى بيت الله الحرام في مكة.",
                                "Fasting means abstaining from what breaks the fast from dawn to sunset during the month of Ramadan, purifying the soul and instilling piety. Hajj is the pilgrimage to the Sacred House in Mecca, obligatory once in a lifetime for those able.",
                            ),
                        ),
                    ),
                ),
            ),
            RefTopic(
                id = "pillars_faith",
                titleAr = "أركان الإيمان الستة",
                titleEn = "The six pillars of faith",
                summaryAr = "الإيمان بالله وملائكته وكتبه ورسله واليوم الآخر والقدر.",
                summaryEn = "Belief in God, His angels, His books, His messengers, the Last Day and divine decree.",
                sections = listOf(
                    RefSection(
                        id = "god_angels_books",
                        titleAr = "الله والملائكة والكتب",
                        titleEn = "God, the angels and the books",
                        paragraphs = listOf(
                            RefParagraph(
                                "الإيمان بالله يتضمن الإيمان بوجوده وربوبيته وألوهيته وأسمائه وصفاته، وأنه الواحد الأحد الفرد الصمد، لا شريك له. والإيمان بالملائكة هو التصديق بهم كخلق مكرم لا يعصون الله ما أمرهم ويفعلون ما يؤمرون.",
                                "Belief in God includes belief in His existence, lordship, divinity, names and attributes, and that He is One, Unique, Self-Sufficient, with no partner. Belief in the angels is affirming them as honoured beings who never disobey God and do what they are commanded.",
                            ),
                            RefParagraph(
                                "الإيمان بالكتب هو التصديق بأن الله أنزل على رسله كتبًا هادية، منها التوراة والإنجيل والزبور وصحف إبراهيم، وأن القرآن الكريم آخرها وأشملها، مهيمن عليها ومصدّق لها.",
                                "Belief in the books is affirming that God revealed guiding scriptures to His messengers, including the Torah, the Gospel, the Psalms and the scrolls of Abraham, and that the Quran is the last and most comprehensive, confirming and guarding them.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "messengers_lastday_decree",
                        titleAr = "الرسل واليوم الآخر والقدر",
                        titleEn = "Messengers, the Last Day and decree",
                        paragraphs = listOf(
                            RefParagraph(
                                "الإيمان بالرسل هو التصديق بجميع رسل الله، أولهم آدم وأولوا العزم نوح وإبراهيم وموسى وعيسى، وخاتمهم محمد ﷺ. يجب الإيمان بهم كلهم ولا يصح الإيمان ببعض دون بعض.",
                                "Belief in the messengers is affirming all of God's messengers, the first being Adam, the resolute Noah, Abraham, Moses and Jesus, and the seal being Muhammad ﷺ. One must believe in all of them, not some and not others.",
                            ),
                            RefParagraph(
                                "الإيمان باليوم الآخر يتضمن الإيمان بالبعث والحساب والجنة والنار. والإيمان بالقدر هو الإيمان بأن كل شيء بقضاء الله وقدره، مع إيمان العبد بمسؤوليته عن أفعاله الاختيارية.",
                                "Belief in the Last Day includes belief in resurrection, reckoning, Paradise and Hell. Belief in divine decree is affirming that everything happens by God's will and decree, while the servant remains responsible for his voluntary actions.",
                            ),
                        ),
                    ),
                ),
            ),
            RefTopic(
                id = "quran",
                titleAr = "القرآن الكريم",
                titleEn = "The Noble Quran",
                summaryAr = "كلام الله المنزل على محمد ﷺ، المعجز بلفظه، المتعبد بتلاوته، المحفوظ من التحريف.",
                summaryEn = "The word of God revealed to Muhammad ﷺ, miraculous in its wording, worshipped through its recitation, preserved from corruption.",
                sections = listOf(
                    RefSection(
                        id = "revelation",
                        titleAr = "الوحي والحفظ",
                        titleEn = "Revelation and preservation",
                        paragraphs = listOf(
                            RefParagraph(
                                "القرآن الكريم هو كلام الله تعالى أنزله على نبيه محمد ﷺ بواسطة جبريل عليه السلام، منجّمًا على ثلاث وعشرين سنة، متعبدًا بتلاوته، متحدىً به العرب وغيرهم على أن يأتوا بمثله فعجزوا.",
                                "The Quran is the word of God revealed to Prophet Muhammad ﷺ through the angel Gabriel, in stages over twenty-three years. Its recitation is an act of worship, and it challenged the Arabs and all others to produce its like, and they could not.",
                            ),
                            RefParagraph(
                                "حفظ الله القرآن من التحريف: نزل بلغة العرب، وحُفظ بالكتابة في عهد النبي ﷺ وجُمع في مصحف واحد في عهد أبي بكر وعثمان رضي الله عنهما، وتواتر نقلًا جيلًا بعد جيل حتى اليوم، ووُعد بحفظه: «إِنَّا نَحْنُ نَزَّلْنَا الذِّكْرَ وَإِنَّا لَهُ لَحَافِظُونَ».",
                                "God preserved the Quran from corruption: it was revealed in the language of the Arabs, written down during the Prophet's life, compiled into one volume under Abu Bakr and Uthman (may God be pleased with them), and transmitted continuously generation after generation to this day. God promised: “Indeed, it is We who sent down the Reminder, and indeed, We will be its guardian” (Quran 15:9).",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "content",
                        titleAr = "محتواه وعلومه",
                        titleEn = "Its content and sciences",
                        paragraphs = listOf(
                            RefParagraph(
                                "القرآن 114 سورة، يبدأ بالفاتحة وينتهي بالناس، ويشتمل على العقيدة والعبادات والمعاملات والقصص والمواعظ والأحكام، وهو هداية للناس جميعًا: «هُدًى لِلنَّاسِ وَبَيِّنَاتٍ مِنَ الْهُدَى وَالْفُرْقَانِ».",
                                "The Quran consists of 114 chapters, beginning with al-Fatiha and ending with an-Nas. It covers creed, worship, transactions, stories, exhortations and rulings, and is guidance for all people: “Guidance for the people and clear proofs of guidance and criterion” (Quran 2:185).",
                            ),
                            RefParagraph(
                                "من علوم القرآن: التجويد (إتقان التلاوة)، والتفسير (بيان المعاني)، وأسباب النزول، والناسخ والمنسوخ، والمحكم والمتشابه، والإعجاز العلمي والبلاغي.",
                                "Among the Quranic sciences are tajwid (perfecting recitation), tafsir (explanation of meanings), the occasions of revelation, abrogating and abrogated verses, decisive and ambiguous verses, and its scientific and rhetorical miracles.",
                            ),
                        ),
                    ),
                ),
            ),
            RefTopic(
                id = "prophet",
                titleAr = "النبي محمد ﷺ",
                titleEn = "Prophet Muhammad ﷺ",
                summaryAr = "خاتم الأنبياء والمرسلين، بعثه الله رحمة للعالمين، وبلغ الرسالة وأدى الأمانة.",
                summaryEn = "The seal of the prophets and messengers, sent by God as a mercy to the worlds; he delivered the message and fulfilled the trust.",
                sections = listOf(
                    RefSection(
                        id = "mission",
                        titleAr = "رسالته",
                        titleEn = "His mission",
                        paragraphs = listOf(
                            RefParagraph(
                                "محمد بن عبد الله ﷺ وُلد في مكة سنة 570م تقريبًا، وبُعث نبيًا في سن الأربعين، فدعا إلى عبادة الله وحده وترك عبادة الأصنام، وصبر على الأذى ثلاثًا وعشرين سنة حتى أكمل الله به الدين.",
                                "Muhammad ibn Abdullah ﷺ was born in Mecca around 570 CE and was sent as a prophet at the age of forty. He called to the worship of God alone and to abandoning idolatry, enduring hardship for twenty-three years until God perfected the religion through him.",
                            ),
                            RefParagraph(
                                "قال تعالى: «وَمَا أَرْسَلْنَاكَ إِلَّا رَحْمَةً لِلْعَالَمِينَ». وكان ﷺ خاتم النبيين، لا نبي بعده، ودينه ناسخ لما قبله، وهو مبعوث إلى الناس كافة.",
                                "God said: “And We have not sent you except as a mercy to the worlds” (Quran 21:107). He ﷺ is the seal of the prophets — no prophet after him — his religion supersedes what came before, and he was sent to all of humanity.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "character",
                        titleAr = "أخلاقه",
                        titleEn = "His character",
                        paragraphs = listOf(
                            RefParagraph(
                                "كان ﷺ أحسن الناس خلقًا، صادقًا أمينًا قبل البعثة وبعدها، عُرف بـ«الصادق الأمين». يصل الرحم، ويكرم الضيف، ويعفو عمن ظلمه، ويجيب دعوة الداعي.",
                                "He ﷺ had the finest character, truthful and trustworthy before and after his mission, known as “the truthful, the trustworthy.” He kept family ties, honoured guests, forgave those who wronged him and answered the caller.",
                            ),
                            RefParagraph(
                                "قال تعالى في شأنه: «وَإِنَّكَ لَعَلَىٰ خُلُقٍ عَظِيمٍ». وتفصيل سيرته كاملة في كتاب «السيرة النبوية».",
                                "God said of him: “And indeed, you are upon a great moral character” (Quran 68:4). His full biography is detailed in the “Prophetic Biography” book.",
                            ),
                        ),
                    ),
                ),
            ),
            RefTopic(
                id = "god",
                titleAr = "الله في الإسلام",
                titleEn = "God in Islam",
                summaryAr = "التوحيد حقيقة الإسلام: إفراد الله بالعبادة، والإيمان بأسمائه وصفاته.",
                summaryEn = "Monotheism is the essence of Islam: devoting worship to God alone and believing in His names and attributes.",
                sections = listOf(
                    RefSection(
                        id = "tawhid",
                        titleAr = "التوحيد",
                        titleEn = "Monotheism (Tawhid)",
                        paragraphs = listOf(
                            RefParagraph(
                                "التوحيد هو إفراد الله بالربوبية والألوهية والأسماء والصفات. قال تعالى: «قُلْ هُوَ اللَّهُ أَحَدٌ * اللَّهُ الصَّمَدُ * لَمْ يَلِدْ وَلَمْ يُولَدْ * وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدٌ».",
                                "Tawhid is devoting to God alone His lordship, divinity and names and attributes. God said: “Say: He is Allah, the One. Allah, the Eternal Refuge. He neither begets nor is born, nor is there to Him any equivalent” (Quran 112:1–4).",
                            ),
                            RefParagraph(
                                "أعظم ذنب في الإسلام هو الشرك بالله — صرف العبادة لغير الله — وهو الذنب الوحيد الذي لا يغفره الله لمن مات عليه: «إِنَّ اللَّهَ لَا يَغْفِرُ أَنْ يُشْرَكَ بِهِ».",
                                "The gravest sin in Islam is shirk — directing worship to other than God — and it is the only sin God does not forgive if one dies upon it: “Indeed, Allah does not forgive association with Him” (Quran 4:48).",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "names",
                        titleAr = "أسماء الله الحسنى",
                        titleEn = "The beautiful names of God",
                        paragraphs = listOf(
                            RefParagraph(
                                "لله تسعة وتسعون اسمًا، منها الرحمن والرحيم والملك والقدوس والسلام والمؤمن والمهيمن والعزيز والجبار والمتكبر والخالق والرازق. قال ﷺ: «إن لله تسعةً وتسعين اسمًا، من أحصاها دخل الجنة».",
                                "God has ninety-nine names, including the Most Merciful, the King, the Holy, the Peace, the Giver of Faith, the Guardian, the Mighty, the Compeller, the Creator and the Provider. The Prophet ﷺ said: “God has ninety-nine names; whoever memorises them enters Paradise.”",
                            ),
                            RefParagraph(
                                "الإيمان بالأسماء والصفات يكون بإثبات ما أثبته الله لنفسه من غير تحريف ولا تعطيل ولا تكييف ولا تمثيل.",
                                "Belief in God's names and attributes affirms what God affirmed for Himself without distortion, denial, asking how, or likening Him to creation.",
                            ),
                        ),
                    ),
                ),
            ),
            RefTopic(
                id = "worship",
                titleAr = "العبادات في الإسلام",
                titleEn = "Worship in Islam",
                summaryAr = "العبادة اسم جامع لكل ما يحبه الله ويرضاه من الأقوال والأفعال الظاهرة والباطنة.",
                summaryEn = "Worship is a comprehensive term for everything God loves and is pleased with, in words and deeds, outward and inward.",
                sections = listOf(
                    RefSection(
                        id = "meaning",
                        titleAr = "معنى العبادة",
                        titleEn = "The meaning of worship",
                        paragraphs = listOf(
                            RefParagraph(
                                "العبادة في الإسلام أوسع من الصلاة والصيام؛ فهي تشمل الذكر والدعاء والنية والصدقة وبر الوالدين وطلب العلم وكظم الغيظ. كل عمل صالح يقصد به وجه الله فهو عبادة.",
                                "Worship in Islam is broader than prayer and fasting; it includes remembrance, supplication, intention, charity, kindness to parents, seeking knowledge and restraining anger. Every good deed done for the sake of God is worship.",
                            ),
                            RefParagraph(
                                "أعظم العبادات وأجلّها الصلاة، ثم الزكاة والصيام والحج، ثم الذكر والدعاء وتلاوة القرآن. وقد شرعت هذه العبادات لتحقيق التقوى وإصلاح القلب.",
                                "The greatest acts of worship are prayer, then zakat, fasting and pilgrimage, then remembrance, supplication and reciting the Quran. These were prescribed to achieve piety and reform the heart.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "dua",
                        titleAr = "الدعاء والذكر",
                        titleEn = "Supplication and remembrance",
                        paragraphs = listOf(
                            RefParagraph(
                                "الدعاء هو مخ العبادة، وهو صلة العبد بربه في كل حين. قال تعالى: «ادْعُونِي أَسْتَجِبْ لَكُمْ». ويُستجاب الدعاء بإخلاص القلب وحضوره واجتناب الحرام.",
                                "Supplication is the essence of worship and the servant's link to his Lord at all times. God said: “Call upon Me; I will respond to you” (Quran 40:60). Supplication is answered with sincerity, presence of heart and avoiding the unlawful.",
                            ),
                            RefParagraph(
                                "الذكر تلاوة القرآن، والتسبيح والتحميد والتكبير، وأذكار الصباح والمساء وأدبار الصلوات. قال ﷺ: «أفضل الكلام أربع: سبحان الله، والحمد لله، ولا إله إلا الله، والله أكبر».",
                                "Remembrance includes reciting the Quran, glorifying God, praising Him and magnifying Him, and the morning, evening and post-prayer adhkar. The Prophet ﷺ said: “The best words are four: glory be to God, praise be to God, there is no god but God, and God is greatest.”",
                            ),
                        ),
                    ),
                ),
            ),
            RefTopic(
                id = "ethics",
                titleAr = "الأخلاق في الإسلام",
                titleEn = "Ethics in Islam",
                summaryAr = "بُعث النبي ﷺ ليتمم مكارم الأخلاق، والأخلاق من الدين لا خارجه.",
                summaryEn = "The Prophet ﷺ was sent to perfect noble character; ethics are part of the religion, not outside it.",
                sections = listOf(
                    RefSection(
                        id = "foundation",
                        titleAr = "الأخلاق أساس الدين",
                        titleEn = "Ethics as the foundation of religion",
                        paragraphs = listOf(
                            RefParagraph(
                                "قال ﷺ: «إنما بُعثت لأتمم صالح الأخلاق». فالصدق والأمانة والعدل والرحمة والعفو والحياء والكرم من صميم الإسلام، وقد جمعها الله في قوله: «إِنَّ اللَّهَ يَأْمُرُ بِالْعَدْلِ وَالْإِحْسَانِ».",
                                "The Prophet ﷺ said: “I was sent only to perfect noble character.” Truthfulness, trustworthiness, justice, mercy, forgiveness, modesty and generosity are at the core of Islam, gathered in God's words: “Indeed, Allah commands justice and excellence” (Quran 16:90).",
                            ),
                            RefParagraph(
                                "الأخلاق في الإسلام عبادة يؤجر عليها المسلم، وسبب لدخول الجنة: قال ﷺ: «أكثر ما يدخل الناس الجنة تقوى الله وحسن الخلق».",
                                "Ethics in Islam are acts of worship rewarded by God and a means to enter Paradise. The Prophet ﷺ said: “The thing that admits most people to Paradise is piety towards God and good character.”",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "neighbours",
                        titleAr = "حقوق الناس",
                        titleEn = "The rights of people",
                        paragraphs = listOf(
                            RefParagraph(
                                "أمر الإسلام بالإحسان إلى الوالدين والأقارب والجيران واليتامى والمساكين، وبالوفاء بالعهد والعدل حتى مع المخالفين: «وَلَا يَجْرِمَنَّكُمْ شَنَآنُ قَوْمٍ عَلَىٰ أَلَّا تَعْدِلُوا اعْدِلُوا هُوَ أَقْرَبُ لِلتَّقْوَىٰ».",
                                "Islam commands kindness to parents, relatives, neighbours, orphans and the needy, and fulfilling promises and justice even toward opponents: “And let not hatred of a people prevent you from being just. Be just; that is nearer to righteousness” (Quran 5:8).",
                            ),
                            RefParagraph(
                                "حرم الإسلام الظلم والغيبة والنميمة والكذب والغش والربا وأكل أموال الناس بالباطل، وجعل حفظ الدماء والأعراض والأموال من الضروريات الخمس.",
                                "Islam forbids injustice, backbiting, slander, lying, cheating, usury and consuming people's wealth unjustly, and makes the protection of life, honour and property among the five necessities.",
                            ),
                        ),
                    ),
                ),
            ),
            RefTopic(
                id = "women",
                titleAr = "المرأة في الإسلام",
                titleEn = "Women in Islam",
                summaryAr = "كرّم الإسلام المرأة أمًا وبنتًا وزوجة وأختًا، وكفل حقوقها المالية والاجتماعية.",
                summaryEn = "Islam honoured women as mothers, daughters, wives and sisters, securing their financial and social rights.",
                sections = listOf(
                    RefSection(
                        id = "dignity",
                        titleAr = "كرامة المرأة",
                        titleEn = "The dignity of women",
                        paragraphs = listOf(
                            RefParagraph(
                                "رفع الإسلام مكانة المرأة بعد أن كانت تُوأد البنات في الجاهلية. قال تعالى: «وَإِذَا الْمَوْءُودَةُ سُئِلَتْ». وجعل الجنة تحت أقدام الأمهات: قال ﷺ: «الجنة تحت أقدام الأمهات».",
                                "Islam raised the status of women after female infants were buried alive in pre-Islamic times. God said: “And when the girl buried alive is asked” (Quran 81:8), and the Prophet ﷺ said: “Paradise lies under the feet of mothers.”",
                            ),
                            RefParagraph(
                                "للمرأة في الإسلام شخصيتها المالية المستقلة: تملك وترث وتتصدق وتتاجر بمالها، وقد عملت النساء في عهد النبي ﷺ في التجارة والرعاية والتعليم.",
                                "In Islam a woman has independent financial personality: she owns, inherits, gives charity and trades with her own wealth. Women in the Prophet's era worked in trade, care and education.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "rights",
                        titleAr = "حقوقها",
                        titleEn = "Her rights",
                        paragraphs = listOf(
                            RefParagraph(
                                "كفل الإسلام للمرأة حق التعليم، والميراث، والمهر، والنفقة، والرضا في الزواج، وحسن المعاشرة: «وَعَاشِرُوهُنَّ بِالْمَعْرُوفِ». ومنع إكراهها على الزواج.",
                                "Islam secured for women the right to education, inheritance, dowry, maintenance, consent in marriage and kind companionship: “And live with them in kindness” (Quran 4:19). It forbade forcing her into marriage.",
                            ),
                            RefParagraph(
                                "الحجاب في الإسلام صون للمرأة وعفاف، ولباس ساتر لا يلفت النظر، وهو من أوامر الشريعة للمحافظة على المجتمع. واختلاف الثقافات لا يغيّر أصل الحكم.",
                                "The hijab in Islam is protection and modesty for women — a concealing garment that does not attract attention — and is a command of the sharia for the welfare of society. Cultural differences do not change its essence.",
                            ),
                        ),
                    ),
                ),
            ),
            RefTopic(
                id = "science",
                titleAr = "الإسلام والعلوم والحضارة",
                titleEn = "Islam, science and civilisation",
                summaryAr = "حضّ الإسلام على طلب العلم، وقاد المسلمون نهضة علمية أسست للعلوم الحديثة.",
                summaryEn = "Islam urged the pursuit of knowledge, and Muslims led a scientific renaissance that founded the modern sciences.",
                sections = listOf(
                    RefSection(
                        id = "knowledge",
                        titleAr = "طلب العلم فريضة",
                        titleEn = "Seeking knowledge is an obligation",
                        paragraphs = listOf(
                            RefParagraph(
                                "أول ما نزل من القرآن: «اقْرَأْ بِاسْمِ رَبِّكَ الَّذِي خَلَقَ». وقال ﷺ: «طلب العلم فريضة على كل مسلم». فالعلم في الإسلام عبادة وسبيل إلى الجنة.",
                                "The first revelation was: “Recite in the name of your Lord who created” (Quran 96:1). The Prophet ﷺ said: “Seeking knowledge is an obligation upon every Muslim.” Knowledge in Islam is worship and a path to Paradise.",
                            ),
                            RefParagraph(
                                "شجّع الإسلام على النظر في الكون والتفكر في خلقه: «سَنُرِيهِمْ آيَاتِنَا فِي الْآفَاقِ وَفِي أَنْفُسِهِمْ»، فكان ذلك دافعًا للبحث العلمي.",
                                "Islam encouraged observing the universe and reflecting on its creation: “We will show them Our signs in the horizons and within themselves” (Quran 41:53), which drove scientific inquiry.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "civilisation",
                        titleAr = "الحضارة الإسلامية",
                        titleEn = "Islamic civilisation",
                        paragraphs = listOf(
                            RefParagraph(
                                "أسس المسلمون نهضة علمية في الطب والرياضيات والفلك والكيمياء والجغرافيا، فكان ابن سينا والرازي وابن الهيثم والخوارزمي وابن خلدون من أعلامها، ونقلوا المعرفة إلى أوروبا.",
                                "Muslims built a scientific renaissance in medicine, mathematics, astronomy, chemistry and geography; Ibn Sina, al-Razi, Ibn al-Haytham, al-Khwarizmi and Ibn Khaldun were among its luminaries, and they transmitted knowledge to Europe.",
                            ),
                            RefParagraph(
                                "بنى المسلمون حضارة تقوم على الأخلاق والعدل والرحمة، وشيدوا المساجد والمكتبات والمستشفيات والجامعات، كجامعة القرويين والزيتونة والأزهر.",
                                "Muslims built a civilisation founded on ethics, justice and mercy, and established mosques, libraries, hospitals and universities such as al-Qarawiyyin, al-Zaytuna and al-Azhar.",
                            ),
                        ),
                    ),
                ),
            ),
            RefTopic(
                id = "misconceptions",
                titleAr = "مفاهيم خاطئة شائعة",
                titleEn = "Common misconceptions",
                summaryAr = "إجابات صريحة عن أكثر الأسئلة التي ترد في أذهان غير المسلمين.",
                summaryEn = "Straight answers to the questions most often asked by non-Muslims.",
                sections = listOf(
                    RefSection(
                        id = "jihad_terror",
                        titleAr = "الجهاد والإرهاب",
                        titleEn = "Jihad and terrorism",
                        paragraphs = listOf(
                            RefParagraph(
                                "الجهاد في الإسلام هو بذل الجهد في نصرة الحق، وأعظمه جهاد النفس على الطاعة. وقتال المعتدي مقيد بشروط صارمة: لا قتل لغير المقاتلين، ولا للنساء والأطفال والشيوخ والرهبان، ولا تخريب للمزارع والمعابد.",
                                "Jihad in Islam means striving in the cause of truth, the greatest of which is striving against one's own soul in obedience. Fighting an aggressor is bound by strict rules: no killing of non-combatants, women, children, the elderly or monks, and no destruction of farms or places of worship.",
                            ),
                            RefParagraph(
                                "الإرهاب — الترويع والقتل العشوائي للمدنيين — حرام ومحرم في الإسلام، وهو من أكبر الكبائر، وقد لعن النبي ﷺ من فزّع مسلمًا. الإرهابيون لا يمثلون الإسلام ولا المسلمين.",
                                "Terrorism — intimidation and the indiscriminate killing of civilians — is forbidden and a major sin in Islam; the Prophet ﷺ cursed whoever terrorises a Muslim. Terrorists do not represent Islam or Muslims.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "other_misconceptions",
                        titleAr = "تعدد الزوجات والمرأة وغيرها",
                        titleEn = "Polygamy, women and other topics",
                        paragraphs = listOf(
                            RefParagraph(
                                "تعدد الزوجات في الإسلام مباح بشروط صارمة (العدل بين الزوجات، والقدرة)، وهو استثناء لحالات اجتماعية، والأصل في الإسلام الزواج بواحدة، والعدل شرط لا يُغتفر: «فَإِنْ خِفْتُمْ أَلَّا تَعْدِلُوا فَوَاحِدَةً».",
                                "Polygamy in Islam is permitted under strict conditions (justice among wives and capability); it is an exception for social situations, and the rule is marriage to one. Justice is a non-negotiable condition: “But if you fear that you will not be just, then one” (Quran 4:3).",
                            ),
                            RefParagraph(
                                "الإسلام لا يفرض قسرًا على أحد: «لَا إِكْرَاهَ فِي الدِّينِ». والمسلمون ليسوا كلهم عربًا؛ فالإسلام دين عالمي يعتنقه أكثر من ملياري مسلم في كل القارات.",
                                "Islam does not force anyone: “There is no compulsion in religion” (Quran 2:256). And Muslims are not all Arabs; Islam is a universal religion followed by over two billion Muslims across every continent.",
                            ),
                        ),
                    ),
                ),
            ),
            RefTopic(
                id = "afterlife",
                titleAr = "اليوم الآخر",
                titleEn = "The Hereafter",
                summaryAr = "الموت ليس نهاية، بل انتقال إلى حياة أبدية: بعث وحساب وجنة ونار.",
                summaryEn = "Death is not the end but a passage to an eternal life: resurrection, reckoning, Paradise and Hell.",
                sections = listOf(
                    RefSection(
                        id = "resurrection",
                        titleAr = "البعث والحساب",
                        titleEn = "Resurrection and reckoning",
                        paragraphs = listOf(
                            RefParagraph(
                                "يؤمن المسلم بأن الله يبعث الخلق بعد موتهم للحساب، فيجازي المحسن بإحسانه والمسيء بإساءته: «فَمَنْ يَعْمَلْ مِثْقَالَ ذَرَّةٍ خَيْرًا يَرَهُ * وَمَنْ يَعْمَلْ مِثْقَالَ ذَرَّةٍ شَرًّا يَرَهُ».",
                                "The Muslim believes that God will resurrect all creation for reckoning, rewarding the good for their good and the evil for their evil: “So whoever does an atom's weight of good will see it, and whoever does an atom's weight of evil will see it” (Quran 99:7–8).",
                            ),
                            RefParagraph(
                                "من أشراط الساعة التي أخبر بها النبي ﷺ: انتشار الفتن، ورفع العلم، وظهور الدجال، ونزول عيسى، وطلوع الشمس من مغربها، والنفخ في الصور.",
                                "Among the signs of the Hour the Prophet ﷺ told of are the spread of tribulations, the lifting of knowledge, the appearance of the Dajjal, the descent of Jesus, the sun rising from the west, and the blowing of the Trumpet.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "paradise_hell",
                        titleAr = "الجنة والنار",
                        titleEn = "Paradise and Hell",
                        paragraphs = listOf(
                            RefParagraph(
                                "الجنة دار النعيم الأبدي أعدها الله للمتقين، فيها ما لا عين رأت ولا أذن سمعت ولا خطر على قلب بشر، وأعلى نعيمها رؤية الله تعالى.",
                                "Paradise is the eternal abode of bliss prepared for the God-fearing, containing what no eye has seen, no ear has heard and no heart has conceived; its greatest delight is seeing God.",
                            ),
                            RefParagraph(
                                "النار دار العذاب لمن كفر بالله وعصاه، نسأل الله العافية. والرحمة الإلهية واسعة: «قُلْ يَا عِبَادِيَ الَّذِينَ أَسْرَفُوا عَلَىٰ أَنْفُسِهِمْ لَا تَقْنَطُوا مِنْ رَحْمَةِ اللَّهِ».",
                                "Hell is the abode of torment for those who disbelieved in God and disobeyed Him — may God grant us safety. God's mercy is vast: “Say: O My servants who have transgressed against themselves, do not despair of the mercy of Allah” (Quran 39:53).",
                            ),
                        ),
                    ),
                ),
            ),
            RefTopic(
                id = "sources",
                titleAr = "مصادر التشريع والمذاهب",
                titleEn = "Sources of law and the schools",
                summaryAr = "القرآن ثم السنة ثم الإجماع والقياس، والمذاهب الفقهية فهم تطبيقي للنصوص.",
                summaryEn = "The Quran, then the Sunnah, then consensus and analogy; the schools of jurisprudence are practical applications of the texts.",
                sections = listOf(
                    RefSection(
                        id = "sources",
                        titleAr = "مصادر التشريع",
                        titleEn = "Sources of legislation",
                        paragraphs = listOf(
                            RefParagraph(
                                "مصادر التشريع الإسلامي مرتبة: القرآن الكريم، ثم السنة النبوية، ثم الإجماع (اتفاق المجتهدين)، ثم القياس (إلحاق الفرع بالأصل بجامع). وأهل العلم يقدّمون النص على الرأي.",
                                "The sources of Islamic law in order are: the Quran, then the Sunnah, then consensus (agreement of the scholars), then analogy (likening a new case to an original one by a shared reason). Scholars give precedence to the text over opinion.",
                            ),
                            RefParagraph(
                                "الاجتهاد باب مفتوح لمن توفرت فيه أدواته، والأحكام الشرعية تنقسم إلى: واجب ومندوب ومباح ومكروه وحرام.",
                                "Ijtihad (independent reasoning) is an open door for those qualified, and the rulings of sharia divide into: obligatory, recommended, permissible, disliked and forbidden.",
                            ),
                        ),
                    ),
                    RefSection(
                        id = "madhahib",
                        titleAr = "المذاهب الفقهية",
                        titleEn = "The juristic schools",
                        paragraphs = listOf(
                            RefParagraph(
                                "المذاهب الفقهية الأربعة — الحنفي والمالكي والشافعي والحنبلي — مدارس فهم للنصوص، يجمعها أصل واحد، وتختلف في الفروع يسيرًا. لا يقدّم المشروع مذهبًا على آخر، ويُعرض الخلاف بحياد.",
                                "The four juristic schools — Hanafi, Maliki, Shafi'i and Hanbali — are schools of understanding the texts, united in fundamentals and differing slightly in branches. This project does not favour one school over another and presents differences neutrally.",
                            ),
                            RefParagraph(
                                "المسلم يقلّد مذهبًا أو يجتهد بضوابط، والمطلوب منه اتباع الدليل حيث كان، مع احترام أهل العلم وعدم التنازع في الفروع.",
                                "A Muslim may follow a school or reason within the rules; what is required is to follow the evidence wherever it leads, while respecting scholars and avoiding conflict over branches.",
                            ),
                        ),
                    ),
                ),
            ),
        ),
    )
}
