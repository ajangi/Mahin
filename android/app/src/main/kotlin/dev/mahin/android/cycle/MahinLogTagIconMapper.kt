package dev.mahin.android.cycle

import dev.mahin.core.designsystem.icon.MahinIconSpec
import dev.mahin.core.designsystem.icon.MahinIcons

/**
 * Maps persisted log tag labels (fa display strings from Log UI) to M14b icon specs.
 */
internal object MahinLogTagIconMapper {
    private val symptomByLabel =
        mapOf(
            "گرفتگی" to MahinIcons.Symptom.cramps,
            "سردرد" to MahinIcons.Symptom.headache,
            "نفخ" to MahinIcons.Symptom.bloating,
            "خستگی" to MahinIcons.Symptom.fatigue,
            "درد پستان" to MahinIcons.Symptom.breast_tenderness,
            "تهوع" to MahinIcons.Symptom.nausea,
            "درد کمر" to MahinIcons.Symptom.back_pain,
            "ورم" to MahinIcons.Symptom.bloating,
            "میگرن" to MahinIcons.Symptom.migraine,
            "جوش" to MahinIcons.Symptom.acne,
            "هضم" to MahinIcons.Symptom.digestive,
            "ولع" to MahinIcons.Symptom.cravings,
            "بی‌خوابی" to MahinIcons.Symptom.insomnia,
            "سرگیجه" to MahinIcons.Symptom.dizziness,
            "درد" to MahinIcons.Symptom.pain,
        )

    private val moodByLabel =
        mapOf(
            "آرام" to MahinIcons.Mood.calm,
            "شاد" to MahinIcons.Mood.happy,
            "پرانرژی" to MahinIcons.Mood.energetic,
            "حساس" to MahinIcons.Mood.sensitive,
            "تحریک‌پذیر" to MahinIcons.Mood.irritable,
            "مضطرب" to MahinIcons.Mood.anxious,
            "غمگین" to MahinIcons.Mood.sad,
            "استرس" to MahinIcons.Mood.stressed,
            "نوسان خلقی" to MahinIcons.Mood.mood_swings,
        )

    fun symptomIcon(label: String): MahinIconSpec = symptomByLabel[label.trim()] ?: MahinIcons.Symptom.other

    fun moodIcon(label: String): MahinIconSpec = moodByLabel[label.trim()] ?: MahinIcons.Mood.note
}
