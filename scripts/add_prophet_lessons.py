#!/usr/bin/env python3
"""Inserts a 'Lessons and wisdom' RefSection into every prophet topic of
ProphetsContent.kt. Idempotent: skips topics that already contain a lessons
section."""

import io
import re
import sys

PATH = "feature/feature-reference/src/main/java/org/muslim/app/feature/reference/domain/ProphetsContent.kt"

LESSONS = {
    "adam": (
        "من قصة آدم نتعلم أن الكبرياء أصل الهلاك، وأن طاعة الله واجبة وإن خالفت هوى النفس؛ فقد أهلك إبليسَ كبرياؤه ولم يضره خلقه ولا عبادته القديمة. كما نتعلم أن المعصية تليها التوبة النصوح، وأن باب الله مفتوح لمن رجع: «ثُمَّ اجْتَبَاهُ رَبُّهُ فَتَابَ عَلَيْهِ وَهَدَىٰ».",
        "From Adam's story we learn that pride is the origin of destruction and that obedience to God is obligatory even when it conflicts with the soul's desire — Iblis was ruined by his arrogance, and neither his creation nor his long worship availed him. We also learn that sin is followed by sincere repentance, and that God's door remains open for whoever returns: “Then his Lord chose him and turned to him in forgiveness and guided him” (Quran 20:122).",
        "في قصة آدم بيان لعداوة الشيطان للإنسان وسبل وقايتها: أعداؤه يريدون إخراجنا من الجنة كما أخرجوا أبينا، فلا يُستهان بوسوسته ولا يُتخذ سبيله، قال تعالى: «إِنَّ الشَّيْطَانَ لَكُمْ عَدُوٌّ فَاتَّخِذُوهُ عَدُوًّا».",
        "Adam's story clarifies Satan's enmity toward mankind and how to guard against it: the enemy wants to expel us from Paradise as he expelled our father, so his whisper must not be taken lightly nor his path adopted. God said: “Indeed, Satan is to you an enemy, so take him as an enemy” (Quran 35:6).",
    ),
    "idris": (
        "في إدريس بيان فضل الصدق والاجتهاد في العلم والعمل؛ وصفه الله بالصدق ورفعه مكانًا عليًا. فالعلم النافع الذي يُعمل به يرفع صاحبه عند الله، ويكون سببًا لعلوّ الذكر والمنزلة.",
        "In Idris we see the virtue of truthfulness and diligence in knowledge and action; God described him as truthful and raised him to a high station. Beneficial knowledge that is acted upon raises its owner with God and elevates his mention and rank.",
        "الاشتغال بما ينفع الناس من العلوم الدنيوية المباحة (كالكتابة والحساب) ليس مذمومًا إذا صاحبه التوحيد والصدق؛ فإدريس كان أول من خط بالقلم مع كونه نبيًا صديقًا.",
        "Occupying oneself with permissible worldly sciences (like writing and calculation) is not blameworthy when accompanied by monotheism and truthfulness; Idris was the first to write with the pen while being a truthful prophet.",
    ),
    "nuh": (
        "في نوح أعظم مثال على الصبر في الدعوة: تسعمائة وخمسون سنة من الجهد والدعوة سرًا وجهارًا، لا يفتر ولا يمل، مع قلة المستجيبين وكثرة الساخرين. فالعبرة في الدعوة بالثبات على الحق لا بكثرة الأتباع.",
        "In Noah is the greatest example of patience in calling to God: nine hundred and fifty years of effort and calling secretly and openly, without weakening or tiring, amid few responders and many mockers. The measure of da'wah is steadfastness upon the truth, not the number of followers.",
        "في الطوفان عبرة بأن العاقبة للمؤمنين وإن تأخر النصر، وبأن القرابة لا تنفع مع الكفر: ابن نوح كان من الغارقين لأنه لم يؤمن، فالأعمال هي التي ترفع أو تخفض، لا الأنساب.",
        "In the flood is a lesson that the outcome belongs to the believers even when victory is delayed, and that kinship avails nothing alongside disbelief: Noah's son was among the drowned for not believing. It is deeds that raise or lower, not lineage.",
    ),
    "hud": (
        "في قصة عاد عبرة أن القوة والبأس لا يغنيان عن صاحبهما إذا كفر بربه؛ كانوا أشد الناس قوة وأعظمهم بنيانًا، فأهلكتهم ريح صرصر عاتية سبع ليالٍ وثمانية أيام. فمن ركن إلى قوته خسر، ومن ركن إلى ربه نجا.",
        "The story of 'Ad teaches that strength and might avail nothing when one disbelieves in his Lord; they were the strongest of people and the greatest in construction, yet a screaming furious wind destroyed them over seven nights and eight days. Whoever leans on his own strength loses, and whoever leans on his Lord is saved.",
        "«فَأَمَّا عَادٌ فَاسْتَكْبَرُوا فِي الْأَرْضِ بِغَيْرِ الْحَقِّ» — الاستكبار عن الحق سنة الهالكين، والنجاة للذين آمنوا مع هود، وكانوا قلة في قومهم، قال تعالى: «وَمَا آمَنَ مَعَهُ إِلَّا قَلِيلٌ».",
        "“As for 'Ad, they were arrogant in the land without right” (Quran 41:15) — arrogance against the truth is the way of the destroyed, and deliverance was for the few who believed with Hud. God said: “And none believed with him except a few” (Quran 11:40).",
    ),
    "salih": (
        "في قصة ثمود عبرة أن الآيات إذا جاءت ثم كُذبت كانت الهلاك؛ أخرج الله لهم الناقة آية عظيمة فكفروا بها وعقروها، فأخذتهم الصيحة وهم في ديارهم جاثمين.",
        "The story of Thamud teaches that when the signs come and are then denied, destruction follows; God brought forth the she-camel as a great sign, yet they disbelieved and hamstrung it, and the blast seized them fallen dead in their homes.",
        "في تمكين الصالحين آية أن الله يدفع عن عباده المؤمنين: «قَالَ يَا قَوْمِ هَٰذِهِ نَاقَةُ اللَّهِ لَكُمْ آيَةً فَذَرُوهَا تَأْكُلْ فِي أَرْضِ اللَّهِ». فمن تعرض لآيات الله وآذى أولياءه فقد باء بغضب الله.",
        "The story of the believers' empowerment shows that God repels harm from His believing servants: “He said: O my people, this is the she-camel of Allah — a sign for you, so let her eat upon Allah's land” (Quran 11:64). Whoever opposes God's signs and harms His allies has earned His wrath.",
    ),
    "ibrahim": (
        "في إبراهيم بيان كمال التوحيد: بدأ بالأقرب فالأقرب — أباه — ثم قومه، وحطم الأصنام ليكشف بطلانها، وسلّم نفسه لله في النار فكانت بردًا وسلامًا عليه. فالتوكل على الله يقلب المحال ممكنًا: «وَمَنْ يَتَوَكَّلْ عَلَى اللَّهِ فَهُوَ حَسْبُهُ».",
        "In Abraham we see the perfection of monotheism: he began with the closest — his father — then his people, broke the idols to expose their futility, and surrendered himself to God in the fire, which became coolness and safety upon him. Reliance on God turns the impossible into the possible: “And whoever relies upon Allah — then He is sufficient for him” (Quran 65:3).",
        "في قصة الذبح أعظم دروس التسليم: امتثل الابن لأمر ربه وأبيه، فجعل الله الطاعة فداءً له. ومنه استُحبت الأضحية سنة باقية في أمة محمد ﷺ، وفي بنائه الكعبة دعاء أن يجعل الله مكة بلدًا آمنًا ويرزق أهلها من الثمرات.",
        "The story of the sacrifice holds the greatest lessons of submission: the son complied with the command of his Lord and his father, and God made obedience his ransom. From it the sacrifice (udhiyah) became an enduring sunnah in the nation of Muhammad ﷺ, and in building the Kaaba he prayed that God make Mecca a secure land and provide its people with fruits.",
    ),
    "lut": (
        "في قصة لوط تقرير تحريم الفاحشة والتحذير من عاقبتها في الدنيا والآخرة، وأن المجتمع الذي يفشيها يسلبه الله الأمن ويقلب عليه نعمه: «فَأَخَذَتْهُمُ الصَّيْحَةُ مُشْرِقِينَ».",
        "The story of Lot affirms the prohibition of immorality and warns of its consequences in this life and the next, and that a society in which it spreads is stripped of security and has its blessings overturned: “So the blast seized them at sunrise” (Quran 15:73).",
        "وفي القصة أن نصرة النبي للضعيف ودفعه عن المضطر — لما جاءته الملائكة ضيوفًا وخاف عليهم من قومه — مقام عظيم؛ والله لا يضيع أجر من أحسن عملاً.",
        "The story also shows that a prophet's defence of the weak and his protection of the guest in need — when the angels came to him as guests and he feared his people would harm them — is a great station, and God does not waste the reward of those who do good.",
    ),
    "ismail": (
        "في إسماعيل بيان فضل الصدق في الوعد والتسليم لأمر الله؛ وصفه الله بأنه صادق الوعد، وجعل من طاعته وطاعة أبيه فداءً عظيمًا. ومنه نتعلم أن بر الوالدين مقرون بطاعة الله.",
        "In Ismail we see the virtue of truthfulness to promises and submission to God's command; God described him as true to his promise and made his obedience and his father's a great ransom. From him we learn that honouring parents is joined with obedience to God.",
        "بئر زمزم التي فاضت للرضيع في صحراء مكة عبرة بأن الله يرزق عباده من حيث لا يحتسبون: «وَمَنْ يَتَّقِ اللَّهَ يَجْعَلْ لَهُ مَخْرَجًا * وَيَرْزُقْهُ مِنْ حَيْثُ لَا يَحْتَسِبُ».",
        "The well of Zamzam, which gushed in the desert of Mecca for the infant, is a lesson that God provides for His servants from where they do not expect: “And whoever fears Allah — He will make for him a way out and will provide for him from where he does not expect” (Quran 65:2–3).",
    ),
    "ishaq_yaqub": (
        "في إسحاق ويعقوب بيان أن النبوة تنتقل في الذرية الطيبة: «فَبَشَّرْنَاهُ بِإِسْحَاقَ نَبِيًّا مِنَ الصَّالِحِينَ»، وأن صلاح الآباء سبب لحفظ الأبناء.",
        "In Isaac and Jacob we see that prophethood passes through the righteous lineage: “So We gave him good tidings of Isaac, a prophet among the righteous” (Quran 37:112), and that the righteousness of the fathers is a cause of preservation for the children.",
        "يعقوب وصية لأبنائه بالثبات على الدين: «وَوَصَّىٰ بِهَا إِبْرَاهِيمُ بَنِيهِ وَيَعْقُوبُ يَا بَنِيَّ إِنَّ اللَّهَ اصْطَفَىٰ لَكُمُ الدِّينَ فَلَا تَمُوتُنَّ إِلَّا وَأَنْتُمْ مُسْلِمُونَ» — فلا خير في الحياة إلا على الإسلام.",
        "Jacob counselled his sons to remain firm upon the religion: “And Abraham instructed his sons and Jacob: O my sons, indeed Allah has chosen for you the religion, so do not die except while you are Muslims” (Quran 2:132) — there is no good in life except upon Islam.",
    ),
    "yusuf": (
        "سورة يوسف أحسن القصص، وفيها دروس جامعة: الحسد يحمل الإخوة على الكيد لأخيهم، والصبر على البلاء يرفع صاحبه، والعفة عن الحرام تورث العزة والملك: «كَذَٰلِكَ لِنَصْرِفَ عَنْهُ السُّوءَ وَالْفَحْشَاءَ».",
        "Surah Yusuf is the best of stories, containing comprehensive lessons: envy drives brothers to plot against their brother, patience through affliction raises its bearer, and chastity from the forbidden earns honour and authority: “Thus it was, that We might avert from him evil and immorality” (Quran 12:24).",
        "في نهاية القصة أعظم درس في العفو: «لَا تَثْرِيبَ عَلَيْكُمُ الْيَوْمَ يَغْفِرُ اللَّهُ لَكُمْ وَهُوَ أَرْحَمُ الرَّاحِمِينَ» — فالعفو عن المسيء مع القدرة من صفات الكرام، والله يغفر لمن عفا.",
        "At the end of the story is the greatest lesson in forgiveness: “No blame will there be upon you today. May Allah forgive you, and He is the most merciful of the merciful” (Quran 12:92) — pardoning the wrongdoer when able is a trait of the noble, and God forgives those who forgive.",
    ),
    "ayyub": (
        "في أيوب المثل الأعلى في الصبر على البلاء: جمع الله له الابتلاء في المال والولد والجسد، فصبر صبرًا جميلًا ولم يسخط ولم يتبرم، ودعا ربه بضراعة لا بجزع: «أَنِّي مَسَّنِيَ الضُّرُّ وَأَنْتَ أَرْحَمُ الرَّاحِمِينَ».",
        "In Job is the highest example of patience under trial: God gathered for him affliction in wealth, children and body, yet he bore it with beautiful patience without resentment, calling upon his Lord with humility rather than panic: “Indeed, adversity has touched me, and You are the most merciful of the merciful” (Quran 21:83).",
        "وفي عاقبته عبرة أن الصبر عقباه الفرج: رد الله عليه أهله ومثلهم معهم رحمة وذكرى للعابدين، وأن البلاء إذا نزل صبرًا كان تمحيصًا ورفعة لا هوانًا.",
        "His outcome teaches that the end of patience is relief: God restored his family to him and the like of them with them, as mercy and a reminder for the worshippers, and that affliction borne with patience becomes purification and elevation, not humiliation.",
    ),
    "shuayb": (
        "في شعيب تقرير حرمة الكسب الحرام: كان قومه يبخسون المكيال والميزان ويقطعون السبيل، فأمرهم بإيفاء الكيل والوزن بالقسطاس المستقيم، فكذبوه فأخذتهم الرجفة.",
        "In Shu'ayb we see the prohibition of unlawful earnings: his people cheated in measure and balance and blocked the roads, so he commanded them to give full measure and weigh with an even balance; they denied him and the earthquake seized them.",
        "في القصة أيضًا أن النصح في المال من الدين، وأن من يتولّ الكبير من الله هلك كما هلك من قبله: «وَمَا أُرِيدُ أَنْ أُخَالِفَكُمْ إِلَىٰ مَا أَنْهَاكُمْ عَنْهُ» — فلا يأمر ناصح إلا بما يفعل.",
        "The story also shows that honest advice in matters of wealth is part of the religion, and that whoever turns away from the great command of God perishes as those before him perished: “And I do not intend to differ from you in that which I have forbidden you” (Quran 11:88) — the sincere adviser commands only what he himself does.",
    ),
    "musa": (
        "في موسى أعظم أمثلة الشجاعة في الحق: واجه فرعون الطاغية بلسان رقيق وكلمة صدق: «فَقُولَا لَهُ قَوْلًا لَيِّنًا لَعَلَّهُ يَتَذَكَّرُ أَوْ يَخْشَىٰ»، وضرب البحر بعصاه فانفلق بإذن الله، فنجا المؤمنون وغرق الطغاة.",
        "In Moses is the greatest example of courage upon the truth: he faced the tyrant Pharaoh with a gentle word and truthful speech: “And speak to him with gentle speech that perhaps he may be reminded or fear” (Quran 20:44), and struck the sea with his staff and it parted by God's permission, so the believers were saved and the tyrants drowned.",
        "وفي قصة موسى دروس في التوكل: لما قال له أصحابه «إِنَّا لَمُدْرَكُونَ» قال: «كَلَّا إِنَّ مَعِيَ رَبِّي سَيَهْدِينِ» — فلا ييأس مؤمن من نصر الله وإن ضاقت السبل، فإن مع العسر يسرًا.",
        "Moses' story also teaches reliance on God: when his companions said “Indeed, we are overtaken,” he said: “No! Indeed, with me is my Lord; He will guide me” (Quran 26:62) — a believer never despairs of God's help even when paths narrow, for with hardship comes ease.",
    ),
    "dawud": (
        "في داود بيان أن القوة مع التقوى أمانة: جمع الله له الملك والنبوة والزبور، وألان له الحديد، وقضى بالعدل بين الناس، فلما فُتن بالخصمين سارع إلى التوبة: «فَاسْتَغْفَرَ رَبَّهُ وَخَرَّ رَاكِعًا وَأَنَابَ».",
        "In David we see that strength with piety is a trust: God combined for him kingship, prophethood and the Psalms, softened iron for him, and he judged between people with justice; when he was tried by the two litigants he hastened to repentance: “He asked forgiveness of his Lord and fell bowing and turned in repentance” (Quran 38:24).",
        "وصيام داود — يومًا ويومًا — أعظم الصيام عند الله، كما أن تسبيح الجبال معه آية على أن القلوب إذا صفت سبحت معها الخلائق؛ وفي ذلك حث على صيام داود وقيامه.",
        "David's fast — a day on, a day off — is the most beloved fasting to God, just as the mountains glorifying with him is a sign that when hearts are pure, all creation glorifies with them; it encourages following his fast and night prayer.",
    ),
    "sulayman": (
        "في سليمان بيان فضل الشكر على النعم: سأل ربه ملكًا لا ينبغي لأحد من بعده، فسخّر له الريح والجن، وفُهم منطق الطير، فكان شكورًا: «هَٰذَا مِنْ فَضْلِ رَبِّي لِيَبْلُوَنِي أَأَشْكُرُ أَمْ أَكْفُرُ».",
        "In Solomon we see the virtue of gratitude for blessings: he asked his Lord for a kingdom not given to anyone after him, and God subjected the wind and jinn to him and taught him the speech of birds, so he was grateful: “This is from the favour of my Lord to test me whether I will be grateful or ungrateful” (Quran 27:40).",
        "وفي قصته مع بلقيس بيان أن الهداية تبلغ من شاء الله مهما بَعُدت الأسباب: وصلها دعوة التوحيد فأسلمت، وأن كلمة التوحيد هي أساس الملك والدولة الصالحة.",
        "His story with Bilqis shows that guidance reaches whom God wills however distant the means: the call to monotheism reached her and she submitted, and that the word of monotheism is the foundation of kingship and a righteous state.",
    ),
    "ilyas": (
        "في إلياس إنكار عبادة غير الله أيا كان المعبود: كان قومه يعبدون «بعل»، فدعاهم إلى عبادة الله وحده: «أَتَدْعُونَ بَعْلًا وَتَذَرُونَ أَحْسَنَ الْخَالِقِينَ».",
        "In Elijah is the rejection of worshipping anything other than God, whatever the object: his people worshipped “Baal,” and he called them to worship God alone: “Do you call upon Baal and leave the best of creators?” (Quran 37:125).",
        "وفي عاقبة قومه عبرة أن الشرك لا يدوم له سلطان: «فَكَذَّبُوهُ فَإِنَّهُمْ لَمُحْضَرُونَ» — والله محيط بالظالمين، وناصره الأنبياء وأتباعهم.",
        "The fate of his people teaches that shirk never keeps its dominion: “But they denied him, so indeed they will be brought” (Quran 37:127) — God encompasses the wrongdoers and supports His prophets and their followers.",
    ),
    "alyasa": (
        "في اليسع بيان أن الله يحفظ أولياءه ويرفع ذكرهم: ذكره الله في القرآن مع الأخيار: «وَاذْكُرْ إِسْمَاعِيلَ وَالْيَسَعَ وَذَا الْكِفْلِ وَكُلٌّ مِنَ الْأَخْيَارِ»، فكفى بالقرآن ذكرًا ورفعة.",
        "In Elisha we see that God preserves His allies and elevates their mention: God mentioned him in the Quran among the best: “And mention Ismail and Elisha and Dhul-Kifl, and all were of the best” (Quran 38:48) — mention in the Quran suffices as honour.",
        "وفي ذكره مع ذي الكفل إشارة إلى أن صفوة الله من عباده يشملون أهل العلم والعبادة والصدق، وأن الذكر الجميل أجر عاجل للمؤمنين.",
        "His mention alongside Dhul-Kifl indicates that God's chosen servants include people of knowledge, worship and truthfulness, and that a good remembrance is an immediate reward for the believers.",
    ),
    "yunus": (
        "في يونس أعظم درس في الدعاء عند الشدة: نادى في الظلمات — ظلمة الليل والبحر وبطن الحوت — «لَا إِلَٰهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ»، فاستجاب الله له ونجاه من الغم، وجعل ذلك منجاة للمؤمنين.",
        "In Jonah is the greatest lesson in supplication at times of distress: he called in the darkness — of night, sea and the belly of the whale — “There is no deity except You; exalted are You. Indeed, I have been of the wrongdoers” (Quran 21:87), and God answered him, saved him from distress, and made that a means of deliverance for the believers.",
        "وفي القصة أن توبة الأمة جملة ترفع عنها العذاب: لما آمن أهل نينوى كلهم متعهم الله إلى حين، فالدعاء والتوبة يقلبان مقادير العذاب بإذن الله.",
        "The story also shows that a nation's collective repentance removes punishment from it: when the people of Nineveh all believed, God granted them enjoyment for a time — supplication and repentance overturn the decrees of punishment by God's permission.",
    ),
    "zakariyya_yahya": (
        "في زكريا بيان فضل الدعاء والإلحاح فيه: دعا ربه في السر وهو شيخ كبير وامرأته عاقر، فاستجاب الله له وهبه يحيى: «هُنَالِكَ دَعَا زَكَرِيَّا رَبَّهُ قَالَ رَبِّ هَبْ لِي مِنْ لَدُنْكَ ذُرِّيَّةً طَيِّبَةً».",
        "In Zechariah we see the virtue of supplication and persistence in it: he called his Lord in secret while an old man with a barren wife, and God answered him and granted him John: “At that, Zechariah called to his Lord, saying: My Lord, grant me from Yourself a good offspring” (Quran 3:38).",
        "وفي يحيى بيان أن العفة والتقوى مقدمتان على الشهوات: كان حصورًا آتاه الله الحكم صبيًا، وأن الله يجعل للتقوى من كل هم مخرجًا.",
        "In John we see that chastity and piety come before desires: he was chaste, and God gave him judgment while still a boy — and God makes for piety a way out of every concern.",
    ),
    "isa": (
        "في عيسى بيان قدرة الله وعظمته: وُلد من غير أب، ونطق في المهد، وأحيا الموتى وأبرأ الأكمه والأبرص بإذن الله، فكل ذلك آيات على أن الله على كل شيء قدير.",
        "In Jesus we see the power and greatness of God: he was born without a father, spoke in the cradle, brought the dead to life and healed the blind and the leper by God's permission — all signs that God is over all things competent.",
        "وفي رسالته تقرير التوحيد ورفض الغلو: «لَقَدْ كَفَرَ الَّذِينَ قَالُوا إِنَّ اللَّهَ ثَالِثُ ثَلَاثَةٍ» — فمن غلا في الأنبياء فقد خرج عن دينهم، وعيسى بريء ممن اتخذه إلهًا أو ابنًا.",
        "His message affirms monotheism and rejects extremism: “They have certainly disbelieved who say that Allah is the third of three” (Quran 5:73) — whoever exaggerates the status of prophets has left their religion, and Jesus is innocent of those who took him as a god or a son.",
    ),
    "muhammad": (
        "محمد ﷺ خاتم الأنبياء وأفضلهم، أرسله الله رحمة للعالمين، وختم به النبوة، ورفع له ذكره، وجعل أمته خير أمة أخرجت للناس. فمحبته من الإيمان، واتباعه سبيل النجاة: «قُلْ إِنْ كُنْتُمْ تُحِبُّونَ اللَّهَ فَاتَّبِعُونِي يُحْبِبْكُمُ اللَّهُ».",
        "Muhammad ﷺ is the seal and the best of the prophets; God sent him as a mercy to the worlds, sealed prophethood with him, exalted his mention, and made his nation the best nation brought forth for mankind. Loving him is part of faith, and following him is the path of salvation: “Say: If you should love Allah, then follow me, and Allah will love you” (Quran 3:31).",
        "وفي كونه خاتم النبيين بطلان دعوى نبوة من بعده، وثبوت أن شريعته ناسخة لما قبلها وباقية إلى قيام الساعة: «مَا كَانَ مُحَمَّدٌ أَبَا أَحَدٍ مِنْ رِجَالِكُمْ وَلَٰكِنْ رَسُولَ اللَّهِ وَخَاتَمَ النَّبِيِّينَ».",
        "His being the seal of the prophets invalidates any claim of prophethood after him and affirms that his law abrogates what came before it and remains until the Hour: “Muhammad is not the father of any of your men, but he is the Messenger of Allah and the seal of the prophets” (Quran 33:40).",
    ),
}

def main():
    with io.open(PATH, encoding="utf-8") as f:
        src = f.read()
    lines = src.split("\n")

    # Find each topic: a `RefTopic(` line followed by `id = "..."`; the topic
    # closes when the paren depth returns to the depth at its opening.
    topic_ids = []
    depth_by_topic = {}
    insert_at = {}
    depth = 0
    cur_topic = None
    for i, line in enumerate(lines):
        depth += line.count("(") - line.count(")")
        stripped = line.strip()
        if cur_topic is None and stripped == "RefTopic(":
            cur_topic = "PENDING"
            depth_by_topic["PENDING"] = depth
            continue
        if cur_topic == "PENDING":
            m = re.search(r'id\s*=\s*"([a-z_]+)"', line)
            if m:
                cur_topic = m.group(1)
                topic_ids.append(cur_topic)
                depth_by_topic[cur_topic] = depth_by_topic.pop("PENDING")
                continue
        if cur_topic is not None and cur_topic != "PENDING" and stripped == ")," and depth == depth_by_topic[cur_topic]:
            insert_at[cur_topic] = i
            cur_topic = None

    if not topic_ids:
        print("No topics found — aborting")
        sys.exit(1)

    # Also handle the final topic: its closing may coincide with `),` at depth.
    # Build the replacement by walking backwards from each insert point.
    out = lines[:]
    inserted = 0
    # Insert from the end so indices stay valid.
    for tid in reversed(topic_ids):
        if tid not in insert_at:
            print(f"WARN: no insert point for {tid}")
            continue
        lessons = LESSONS.get(tid)
        if lessons is None:
            print(f"WARN: no lessons content for {tid}, skipping")
            continue
        # Skip if already inserted (idempotency): look before the closing.
        start = insert_at[tid]
        window = "\n".join(out[max(0, start - 400):start])
        if "دروس وعبر" in window:
            continue
        block = (
            "                    RefSection(\n"
            "                        id = \"lessons\",\n"
            "                        titleAr = \"دروس وعبر\",\n"
            "                        titleEn = \"Lessons and wisdom\",\n"
            "                        paragraphs = listOf(\n"
            f"                            RefParagraph(\n                                \"{lessons[0]}\",\n                                \"{lessons[1]}\",\n                            ),\n"
            f"                            RefParagraph(\n                                \"{lessons[2]}\",\n                                \"{lessons[3]}\",\n                            ),\n"
            "                        ),\n"
            "                    ),\n"
        )
        # Insert before the topic's closing `),`.
        out.insert(insert_at[tid], block)
        inserted += 1

    with io.open(PATH, "w", encoding="utf-8") as f:
        f.write("\n".join(out))
    print(f"Inserted lessons sections into {inserted} topics")


if __name__ == "__main__":
    main()
