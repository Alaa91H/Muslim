# دليل تشغيل إصدار v1

> **لا تنشئ وسم `v*` كتجربة.** الوسم يعني مرشح إنتاج، وسير العمل سيمنع النشر إذا كان التوقيع أو المحتوى غير مكتملين.

## 1. التحضير قبل RC

| التحقق | الأمر أو الدليل | المسؤول |
|---|---|---|
| شجرة عمل نظيفة ومراجعة التغييرات | `git status` وPull Request مع بطاقة تغيير v1 | قائد تقني |
| تحديث الجرد | `python3 scripts/generate_content_manifest.py` | محتوى/ترخيص |
| تطابق الجرد | `python3 scripts/verify_content_manifest.py` | قائد تقني |
| اعتماد كل أصل إنتاجي | `python3 scripts/verify_content_manifest.py --production` | محتوى/ترخيص |
| توقيع production | `./gradlew :app:verifyProductionSigning` | مسؤول الإصدار |
| بوابة الإنتاج كاملة | `./gradlew :app:verifyProductionRelease` | قائد تقني |
| الجودة | `./gradlew testDebugUnitTest :wear:testDebugUnitTest lintDebug detekt` | QA |
| أجهزة P0 | نتائج موقعة في `docs/qa/p0_test_matrix.md` | QA |
| الخصوصية والمتجر | قائمة `play_console_checklist.md` مكتملة للمسار المطلوب | خصوصية/إصدار |

## 2. إنشاء artifact للإصدار المرشح

```bash
./gradlew :app:bundleProductionRelease :app:assembleRelease :wear:assembleRelease
```

يسمح هذا الأمر فقط إذا كان مفتاح التوقيع موجودًا وكل أصل محتوى مضمّن معتمدًا. يحفظ مسؤول الإصدار: commit، versionCode/versionName، SHA-256 للـAAB، وSHA-256 لشهادة التوقيع.

## 3. الاختبار الداخلي ثم المغلق

1. يرفع الـAAB نفسه إلى **Internal testing** أولًا.
2. يثبت المختبرون من Play وليس من ملف محلي فقط.
3. تُنفذ كل حالات P0 على الأجهزة المسجلة، خصوصًا PRY-001–PRY-008.
4. يراجع مسؤول QA تقرير pre-launch ويصنف كل عيب P0/P1/P2.
5. لا ينتقل الإصدار إلى **Closed testing** قبل إغلاق P0 وتوثيق أي استثناء P1.

## 4. قرار الإنتاج والإطلاق المرحلي

| قرار | المطلوب |
|---|---|
| بدء rollout | توقيع مالك المنتج وQA والخصوصية/المحتوى، ولا P0 مفتوح. |
| إيقاف التوسع | crash/ANR غير مقبول، نمط بلاغات أذان صامت/وقت خاطئ، مخالفة محتوى أو خصوصية، أو إنذار Play policy. |
| التوسع | مراجعة مؤشرات الاستقرار وملاحظات الدعم في كل شريحة قبل زيادة النسبة. |
| hotfix | فرع إصلاح فقط، Smoke tests، تحديث changelog وData safety/الخصوصية إن تغير السلوك. |
| rollback | إيقاف rollout في Play، توثيق commit المتضرر، وترشيح آخر artifact سليم بعد تحقق البوابات. |

## 5. أثر GitHub Release

يمكن نشر APK للمستخدمين الذين يثبتون من خارج المتجر بعد نجاح release workflow، لكن AAB هو artifact المتجر. لا يكفي GitHub Release وحده لاعتماد Google Play أو لتجاوز قائمة Play Console.

## 6. إدارة الأسرار

| السر | مكانه | ممنوع |
|---|---|---|
| مفتاح التوقيع | GitHub Actions secrets أو تخزين محلي آمن خارج المستودع. | إدخاله في Git أو وضع كلمات مرور ضمن scripts. |
| كلمات مرور المفتاح | GitHub secrets أو keystore.properties محلي git-ignored. | طباعتها في logs أو tickets. |
| مفاتيح الخدمات الاختيارية | مزود أسرار مناسب أو إدخال مستخدم محلي عند الحاجة. | تضمينها في التطبيق. |

## 7. مخرجات كل إصدار

- AAB وAPK مع البصمات والشهادة.
- نتائج CI وLint وDetekt واختبارات الأجهزة.
- نسخة من Data safety/التصريحات وقائمة Play Console.
- سجل التغييرات والمحتوى المعتمد.
- قرار rollout وروابط الدعم وخطة rollback.
