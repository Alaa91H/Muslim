package org.muslim.app.feature.family.domain

data class FamilyChecklistItem(
    val id: String,
    val title: LocalizedFamilyText,
    val description: LocalizedFamilyText? = null,
)

data class FamilyChecklist(
    val id: String,
    val title: LocalizedFamilyText,
    val description: LocalizedFamilyText,
    val items: List<FamilyChecklistItem>,
)

object FamilyUtilityContent {
    val checklists: List<FamilyChecklist> = listOf(
        FamilyChecklist(
            id = "weekly_family_meeting",
            title = LocalizedFamilyText("اجتماع الأسرة الأسبوعي", "Weekly family meeting"),
            description = LocalizedFamilyText(
                "قائمة قصيرة لمراجعة الأسبوع واتخاذ قرارات صغيرة قبل تراكمها.",
                "A short weekly review so small issues are handled before they accumulate.",
            ),
            items = listOf(
                item("wins", "اذكر شيئًا سار جيدًا هذا الأسبوع", "Name something that went well this week"),
                item("calendar", "راجع مواعيد الأسبوع القادم", "Review next week's schedule"),
                item("chores", "وزع المهام المنزلية", "Assign household tasks"),
                item("budget", "راجع المصروفات غير المعتادة", "Review unusual expenses"),
                item("one_issue", "اختر مشكلة واحدة تحتاج قرارًا", "Choose one issue that needs a decision"),
                item("shared_time", "حدد وقتًا مشتركًا بلا أجهزة", "Schedule device-free family time"),
            ),
        ),
        FamilyChecklist(
            id = "newborn_first_week",
            title = LocalizedFamilyText("الأسبوع الأول للمولود", "Newborn first week"),
            description = LocalizedFamilyText(
                "تنظيم عملي لأول أيام المولود دون تحويل القائمة إلى بديل عن المتابعة الطبية.",
                "Practical organisation for the first days without replacing medical follow-up.",
            ),
            items = listOf(
                item("medical_followup", "تأكد من مواعيد المتابعة الطبية", "Confirm medical follow-up"),
                item("feeding_plan", "جهز خطة التغذية والدعم عند الصعوبة", "Prepare feeding support if difficulties arise"),
                item("safe_sleep", "راجع مكان النوم الآمن", "Review the safe sleeping space"),
                item("documents", "اجمع وثائق التسجيل الضرورية", "Gather required registration documents"),
                item("visitors", "اتفق على حدود الزيارات والراحة", "Agree visitor and rest boundaries"),
                item("support_person", "حدد شخصًا يمكن طلب مساعدته", "Identify a person who can help"),
            ),
        ),
        FamilyChecklist(
            id = "premarital_practical",
            title = LocalizedFamilyText("حوارات عملية قبل الزواج", "Practical premarital conversations"),
            description = LocalizedFamilyText(
                "موضوعات ينبغي أن تكون واضحة قبل العقد دون تحويل الحوار إلى تحقيق.",
                "Topics worth clarifying before marriage without turning the conversation into interrogation.",
            ),
            items = listOf(
                item("housing", "ناقشا السكن ومكان الإقامة", "Discuss housing and location"),
                item("income_debt", "ناقشا الدخل والديون والالتزامات", "Discuss income, debt and obligations"),
                item("work_study", "ناقشا العمل والدراسة", "Discuss work and study"),
                item("children", "ناقشا التوقعات المتعلقة بالأطفال", "Discuss expectations around children"),
                item("families", "ناقشا حدود العلاقة مع العائلتين", "Discuss boundaries with both families"),
                item("conflict", "اتفقا على طريقة إدارة الخلاف", "Agree how conflict will be handled"),
                item("documentation", "راجعا التوثيق والشروط المهمة", "Review documentation and important conditions"),
            ),
        ),
        FamilyChecklist(
            id = "family_digital_safety",
            title = LocalizedFamilyText("مراجعة السلامة الرقمية للأسرة", "Family digital-safety review"),
            description = LocalizedFamilyText(
                "مراجعة دورية للخصوصية والحسابات والأجهزة تناسب أعمار أفراد الأسرة.",
                "A periodic privacy and device review appropriate to each family member's age.",
            ),
            items = listOf(
                item("updates", "حدّث الأجهزة والتطبيقات", "Update devices and apps"),
                item("passwords", "راجع كلمات المرور والتحقق بخطوتين", "Review passwords and two-factor authentication"),
                item("permissions", "راجع صلاحيات الكاميرا والموقع والميكروفون", "Review camera, location and microphone permissions"),
                item("children_rules", "راجع قواعد أجهزة الأطفال", "Review children's device rules"),
                item("photos", "راجع سياسة نشر صور الأطفال", "Review the family's child-photo policy"),
                item("purchases", "راجع الشراء داخل التطبيقات", "Review in-app purchasing controls"),
                item("reporting", "ذكّر الأطفال بكيفية الإبلاغ عن الرسائل المقلقة", "Remind children how to report concerning messages"),
            ),
        ),
        FamilyChecklist(
            id = "household_emergency_info",
            title = LocalizedFamilyText("معلومات الطوارئ المنزلية", "Household emergency information"),
            description = LocalizedFamilyText(
                "حفظ معلومات أساسية يسهل الوصول إليها عند الحاجة دون وضع بيانات حساسة في مكان مكشوف.",
                "Keep essential information accessible in an emergency without exposing sensitive data.",
            ),
            items = listOf(
                item("contacts", "حدّث أرقام الاتصال المهمة", "Update important contact numbers"),
                item("medications", "حدّث قائمة الأدوية والحساسيات", "Update medicines and allergies"),
                item("documents", "حدد مكان الوثائق الأساسية", "Know where essential documents are kept"),
                item("first_aid", "راجع حقيبة الإسعاف الأولي", "Review the first-aid kit"),
                item("meeting_point", "اتفق على نقطة تواصل أو لقاء عند الطوارئ", "Agree an emergency contact or meeting point"),
                item("trusted_person", "حدد شخصًا موثوقًا يمكن التواصل معه", "Identify a trusted contact person"),
            ),
        ),
    )

    fun checklistById(id: String): FamilyChecklist? = checklists.firstOrNull { it.id == id }

    fun completionKey(checklistId: String, itemId: String): String = "$checklistId:$itemId"

    private fun item(
        id: String,
        titleAr: String,
        titleEn: String,
    ) = FamilyChecklistItem(
        id = id,
        title = LocalizedFamilyText(titleAr, titleEn),
    )
}
