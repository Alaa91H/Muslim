package org.muslim.app.feature.learn.domain

import org.muslim.app.feature.learn.R

internal object FinanceLearnTopics {
    val topics: List<LearnTopic> = listOf(
        LearnTopic("finance_intro", LearnContent.CATEGORY_FINANCE, R.string.learn_topic_finance_intro, R.string.learn_topic_finance_intro_sub, listOf(LearnStep("مدخل إلى المعاملات", "العقد والشفافية والمخاطر والفرق بين الحكم الشرعي والوضع القانوني."))),
        LearnTopic("finance_sale_contracts", LearnContent.CATEGORY_FINANCE, R.string.learn_topic_finance_sale_contracts, R.string.learn_topic_finance_sale_contracts_sub, listOf(LearnStep("البيع والعقود", "وضوح المبيع والثمن والتسليم والعيوب والتوثيق."))),
        LearnTopic("finance_riba", LearnContent.CATEGORY_FINANCE, R.string.learn_topic_finance_riba, R.string.learn_topic_finance_riba_sub, listOf(LearnStep("الربا والتمويل", "الزيادة على القرض وكيف تقرأ المنتجات المصرفية دون الاكتفاء بالأسماء."))),
        LearnTopic("finance_debt_loans", LearnContent.CATEGORY_FINANCE, R.string.learn_topic_finance_debt_loans, R.string.learn_topic_finance_debt_loans_sub, listOf(LearnStep("الديون والقروض", "التوثيق والاستحقاق والسداد والتعامل مع العسر."))),
        LearnTopic("finance_ecommerce", LearnContent.CATEGORY_FINANCE, R.string.learn_topic_finance_ecommerce, R.string.learn_topic_finance_ecommerce_sub, listOf(LearnStep("التجارة الإلكترونية", "البيع الرقمي والتقسيط والاشتراكات والرسوم والاحتيال."))),
        LearnTopic("finance_business_investing", LearnContent.CATEGORY_FINANCE, R.string.learn_topic_finance_business_investing, R.string.learn_topic_finance_business_investing_sub, listOf(LearnStep("الأعمال والاستثمار", "مصدر الربح وفحص الأسهم والمخاطر وإشارات التحذير."))),
        LearnTopic("finance_tools_guide", LearnContent.CATEGORY_FINANCE, R.string.learn_topic_finance_tools_guide, R.string.learn_topic_finance_tools_guide_sub, listOf(LearnStep("أدوات المالية في التطبيق", "أدلة المعاملات ودفتر الديون والتنبيهات وروابط فحص الأسهم."))),
    )
}
