# منارة (Manara) — تطبيق إسلامي شامل ومفتوح المصدر

> **الاسم مؤقت** (اقتراح من وثيقة المشروع: "منارة"، "رفيق المسلم"، "وقتي"، "مُصلّي" — القرار النهائي لاحقًا).

تطبيق أندرويد إسلامي شامل، مجاني بالكامل ومفتوح المصدر. يبدأ بالأذان وأوقات الصلاة والقبلة بدقة فلكية عالية، ويتوسّع تدريجيًا ليشمل القرآن الكريم، الأحاديث، الأذكار، رمضان، حاسبة الزكاة، وكل ما يحتاجه المسلم يوميًا.

- مجاني للأبد، بلا إعلانات وبلا اشتراكات
- مفتوح المصدر (GPLv3)
- خصوصية أولًا: لا تتبّع، البيانات تبقى على الجهاز
- Offline-first: يعمل بدون إنترنت بعد الإعداد الأول

> 📄 **المرجع الشامل للمشروع:** اقرأ [`PROJECT_PROMPT.md`](PROJECT_PROMPT.md) — هو "مصدر الحقيقة الوحيد": الرؤية، البنية، نظام التصميم، خارطة الطريق (8 مراحل)، ومعايير الدقة والمحتوى الشرعي.

---

## حالة المشروع (Status)

| الخطوة | الحالة |
|---|---|
| هيكل المشروع متعدد الوحدات (القسم 3.3) | ✅ مكتمل — يُبنى بنجاح |
| المرحلة 1: الأذان، أوقات الصلاة، القبلة، التقويم الهجري | ✅ منفّذة ومُختبَرة (المحرك الفلكي، القبلة، الهجري، جدولة الأذان، الاختصارات، التصدير، Widget شاشة رئيسية بعدّاد تنازلي — التفاصيل في PROJECT_PROMPT.md) |
| المرحلة 2: القرآن الكريم | 🔶 قيد التنفيذ — النص العثماني كاملًا في Room مملوء مسبقًا + قائمة السور والقارئ (حجم خط) + بحث نصي FTS + علامات مرجعية ومتابعة آخر قراءة |
| المراحل 3–8 | ⬜ لم تبدأ بعد |

لا ننتقل إلى مرحلة جديدة قبل أن تكتمل المرحلة الحالية وتُختبر على جهاز حقيقي.

## مصادر المحتوى (Content sources)

- **نص القرآن:** الرسم العثماني من مشروع [Tanzil](https://tanzil.net/) (عبر مجموعة بيانات alquran.cloud) — 6236 آية مع بيانات كل سورة (الاسم، نوع النزول) ومواضع الآية (الجزء/الصفحة). ملاحظة وفق §10 من وثيقة المشروع: مراجعة شرعية نهائية مقابل مصحف مدني مطبوع مطلوبة قبل الإصدار.
- **خوارزميات أوقات الصلاة:** منقولة ومُحقّقة مقابل مكتبة [Adhan](https://github.com/batoulapps/Adhan-Kotlin) (MIT، مع الإسناد).

لا ننتقل إلى مرحلة جديدة قبل أن تكتمل المرحلة الحالية وتُختبر على جهاز حقيقي.

## البنية التقنية

- **Kotlin 100% + Jetpack Compose** (بدون XML Views)
- **Material 3 Expressive** — Dynamic Color (Material You) مع لوحات احتياطية يدوية
- **Clean Architecture** بثلاث طبقات: presentation ← domain ← data
- **MVI/MVVM** مع `StateFlow`/`SharedFlow`
- **Hilt** لحقن التبعيات، **Coroutines + Flow** للبرمجة اللامتزامنة
- **Room** (بيانات مُحمّلة مسبقًا) + **DataStore** (تفضيلات)
- **AlarmManager** (أذان دقيق في الخلفية) + **WorkManager** (مهام غير حرجة)
- **Glance** (Widget شاشة رئيسية للصلاة القادمة بعدّاد تنازلي، 3 مقاسات)
- **Retrofit/OkHttp/kotlinx.serialization** — للشبكة الاختيارية فقط

### الوحدات (18)

```
app  ·  core-common · core-design-system · core-ui · core-database
core-datastore · core-network · core-notifications · core-location
feature-prayer-times · feature-qibla · feature-quran · feature-hadith
feature-adhkar · feature-tasbih · feature-learn · feature-ramadan
feature-zakat · feature-settings
```

كل وحدة feature تعتمد على وحدات core فقط، ولا تعتمد على وحدة feature أخرى.

## بيئة التطوير المعتمدة (2026)

| الأداة | الإصدار |
|---|---|
| Android Gradle Plugin | 9.3.1 (Kotlin مدمج — بدون `kotlin-android` plugin) |
| Gradle | 9.5.0 |
| Kotlin (مدمج في AGP) | 2.2.10 |
| KSP | 2.2.10-2.0.2 |
| Compose BOM | 2026.08.00 |
| compileSdk / targetSdk | 37 (Android 17) |
| minSdk | 26 (Android 8) |
| JDK | 17 |

> كل الإصدارات مركزية في `gradle/libs.versions.toml`.

## البناء

```bash
# يتطلب: JDK 17، Android SDK (platform 37) — المسار في local.properties
./gradlew :app:assembleDebug     # بناء APK التصحيح
./gradlew testDebugUnitTest      # اختبارات الوحدة
./gradlew lintDebug              # فحص lint
./gradlew :app:installDebug      # تثبيت على جهاز/محاكي متصل
```

## المساهمة

راجع [`CONTRIBUTING.md`](CONTRIBUTING.md) و [`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md).

ملاحظة مهمة: المحتوى الديني (نص القرآن، الأحاديث ودرجتها، الأذكار، الأحكام) يخضع لمراجعة شرعية متخصصة منفصلة عن مراجعة الكود — انظر القسم 10 من `PROJECT_PROMPT.md`.

## الترخيص

[GPLv3](LICENSE) — أي نسخة معدّلة من التطبيق تبقى مفتوحة المصدر.
