package org.muslim.app.feature.finance.domain

import kotlinx.serialization.Serializable

data class LocalizedFinanceText(
    val arabic: String,
    val english: String,
)

data class TransactionsGuide(
    val id: String,
    val title: LocalizedFinanceText,
    val summary: LocalizedFinanceText,
    val points: List<LocalizedFinanceText>,
    val reference: LocalizedFinanceText,
)

enum class ScreeningProvider(
    val label: LocalizedFinanceText,
    val url: String,
    val availability: LocalizedFinanceText,
) {
    Zoya(
        label = LocalizedFinanceText("Zoya", "Zoya"),
        url = "https://app.zoya.finance",
        availability = LocalizedFinanceText(
            "يتطلب الربط البرمجي المفتاح والترخيص المناسبين من المزود.",
            "Programmatic integration requires a provider key and the appropriate licence.",
        ),
    ),
    Musaffa(
        label = LocalizedFinanceText("Musaffa", "Musaffa"),
        url = "https://musaffa.com/stock-screener/",
        availability = LocalizedFinanceText(
            "يتطلب الربط البرمجي اعتمادًا تجاريًا من المزود.",
            "Programmatic integration requires commercial provider onboarding.",
        ),
    ),
}

@Serializable
enum class DebtDirection {
    /** A debt another party owes to the user. */
    Receivable,

    /** A debt the user owes to another party. */
    Payable,
}

@Serializable
data class DebtEntry(
    val id: String,
    val partyName: String,
    val direction: DebtDirection,
    val amount: Double,
    val currency: String,
    val dueDate: String? = null,
    val reminderEnabled: Boolean = false,
    val notes: String = "",
    val createdAt: String,
)

object IslamicFinanceContent {
    val transactionGuides = listOf(
        TransactionsGuide(
            id = "trade",
            title = LocalizedFinanceText("البيع والشراء", "Buying and selling"),
            summary = LocalizedFinanceText(
                "الأصل في البيع الإباحة مع الصدق والوضوح والتراضي، ولا يحل الغش أو أكل المال بالباطل.",
                "Trade is generally permitted when it is honest, transparent, and mutually agreed; deception and unjust enrichment are not permitted.",
            ),
            points = listOf(
                LocalizedFinanceText("حدّد المبيع والثمن والكمية وطريقة التسليم بوضوح قبل إتمام العقد.", "Identify the item, price, quantity, and delivery method clearly before concluding the contract."),
                LocalizedFinanceText("أفصح عن العيب المؤثر وعن الشروط والرسوم؛ لا تعرض ما لا تملك أو لا تقدر على تسليمه.", "Disclose material defects, conditions, and fees; do not offer what you do not own or cannot deliver."),
                LocalizedFinanceText("احتفظ بالفاتورة أو الاتفاق؛ فالتوثيق يحفظ الحقوق ويقلل النزاع.", "Keep the invoice or agreement; documentation preserves rights and reduces disputes."),
            ),
            reference = LocalizedFinanceText("مرجع موجز: البقرة 2:275؛ مع مراجعة فقيه مختص في صور العقود التفصيلية.", "Brief reference: Qur’an 2:275; consult a qualified scholar for detailed contract structures."),
        ),
        TransactionsGuide(
            id = "loans",
            title = LocalizedFinanceText("القروض والديون", "Loans and debts"),
            summary = LocalizedFinanceText(
                "توثيق الدين وأجله وبيانات الأطراف يرفع الالتباس ويعين على الوفاء بالحق.",
                "Recording the debt, its due date, and the parties’ details reduces uncertainty and supports fulfilment of rights.",
            ),
            points = listOf(
                LocalizedFinanceText("اكتب المبلغ والعملة والأجل والشروط المعلومة، واحفظ ما يثبت السداد عند وقوعه.", "Record the amount, currency, due date, and known terms, and retain proof when repayment occurs."),
                LocalizedFinanceText("لا تجعل القرض وسيلة لزيادة مشروطة على أصل الدين؛ اسأل أهل الاختصاص عن الصور المركبة أو التمويل المصرفي.", "Do not turn a loan into a stipulated increase on the principal; ask qualified advisers about complex forms or bank financing."),
                LocalizedFinanceText("تعامل بالإنصاف والرفق عند العسر، وراجع القانون المحلي والعقد القائم قبل أي إجراء.", "Act fairly and compassionately in hardship, and review local law and the existing agreement before taking action."),
            ),
            reference = LocalizedFinanceText("مرجع موجز: البقرة 2:282؛ السجل أداة تنظيمية وليس عقدًا قانونيًا نافذًا.", "Brief reference: Qur’an 2:282; the ledger is an organisational aid, not an enforceable legal contract."),
        ),
        TransactionsGuide(
            id = "ecommerce",
            title = LocalizedFinanceText("التجارة الإلكترونية", "E-commerce"),
            summary = LocalizedFinanceText(
                "تطبق أصول المعاملة على البيع الرقمي: وضوح المنتج والسعر والتسليم والإرجاع وحماية بيانات العميل.",
                "The same transaction principles apply online: clarity about the product, price, delivery, returns, and customer data protection.",
            ),
            points = listOf(
                LocalizedFinanceText("أظهر وصفًا صحيحًا للمنتج وتكلفته النهائية ومدة التسليم وسياسة الإرجاع قبل الدفع.", "Show an accurate product description, final cost, delivery time, and return policy before payment."),
                LocalizedFinanceText("تجنب الرسوم الخفية والادعاءات التسويقية المضللة، واحفظ خصوصية العملاء وبيانات الدفع.", "Avoid hidden fees and misleading claims, and protect customer privacy and payment data."),
                LocalizedFinanceText("تحقق من آلية الدفع والعقد؛ فالتقسيط والعمولات والخدمات الرقمية قد تحتاج مراجعة فقهية وقانونية خاصة.", "Verify the payment method and contract; instalments, commissions, and digital services may need specialised religious and legal review."),
            ),
            reference = LocalizedFinanceText("ملاحظة: المحتوى تعليمي عام ولا يحسم صور التجارة الإلكترونية المستجدة.", "Note: this is general education and does not settle emerging e-commerce structures."),
        ),
    )

    val stockDisclosure = LocalizedFinanceText(
        "نتيجة الفلترة الشرعية تتغير مع البيانات والمنهجية والمزود. لا يعرض التطبيق حكمًا مستقلًا ولا توصية بالشراء أو البيع؛ راجع تقرير المزود ومختصًا موثوقًا قبل القرار.",
        "Shariah-screening status can change with data, methodology, and provider. The app does not issue an independent ruling or a buy/sell recommendation; review the provider report and a qualified adviser before deciding.",
    )
}
