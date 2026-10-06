package dev.mahin.core.designsystem.icon

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import dev.mahin.core.designsystem.R

@Immutable
data class MahinIconSpec(
    val semanticId: String,
    @DrawableRes val drawableRes: Int,
    @StringRes val contentDescriptionRes: Int,
    val autoMirrored: Boolean = false,
)

/** Typed Mahin icon registry (M14b). Each entry has a stable semantic ID and fa content description. */
object MahinIcons {
    object Nav {
        val today =
            MahinIconSpec(
                semanticId = "nav/today/v1",
                drawableRes = R.drawable.mahin_ic_nav_today_v1,
                contentDescriptionRes = R.string.icon_desc_nav_today_v1,
                autoMirrored = false,
            )
        val calendar =
            MahinIconSpec(
                semanticId = "nav/calendar/v1",
                drawableRes = R.drawable.mahin_ic_nav_calendar_v1,
                contentDescriptionRes = R.string.icon_desc_nav_calendar_v1,
                autoMirrored = false,
            )
        val log =
            MahinIconSpec(
                semanticId = "nav/log/v1",
                drawableRes = R.drawable.mahin_ic_nav_log_v1,
                contentDescriptionRes = R.string.icon_desc_nav_log_v1,
                autoMirrored = false,
            )
        val insights =
            MahinIconSpec(
                semanticId = "nav/insights/v1",
                drawableRes = R.drawable.mahin_ic_nav_insights_v1,
                contentDescriptionRes = R.string.icon_desc_nav_insights_v1,
                autoMirrored = false,
            )
        val learn =
            MahinIconSpec(
                semanticId = "nav/learn/v1",
                drawableRes = R.drawable.mahin_ic_nav_learn_v1,
                contentDescriptionRes = R.string.icon_desc_nav_learn_v1,
                autoMirrored = false,
            )
        val pregnancy =
            MahinIconSpec(
                semanticId = "nav/pregnancy/v1",
                drawableRes = R.drawable.mahin_ic_nav_pregnancy_v1,
                contentDescriptionRes = R.string.icon_desc_nav_pregnancy_v1,
                autoMirrored = false,
            )
        val plan =
            MahinIconSpec(
                semanticId = "nav/plan/v1",
                drawableRes = R.drawable.mahin_ic_nav_plan_v1,
                contentDescriptionRes = R.string.icon_desc_nav_plan_v1,
                autoMirrored = false,
            )
        val history =
            MahinIconSpec(
                semanticId = "nav/history/v1",
                drawableRes = R.drawable.mahin_ic_nav_history_v1,
                contentDescriptionRes = R.string.icon_desc_nav_history_v1,
                autoMirrored = false,
            )
        val settings =
            MahinIconSpec(
                semanticId = "nav/settings/v1",
                drawableRes = R.drawable.mahin_ic_nav_settings_v1,
                contentDescriptionRes = R.string.icon_desc_nav_settings_v1,
                autoMirrored = false,
            )
    }

    object Action {
        val add =
            MahinIconSpec(
                semanticId = "action/add/v1",
                drawableRes = R.drawable.mahin_ic_action_add_v1,
                contentDescriptionRes = R.string.icon_desc_action_add_v1,
                autoMirrored = false,
            )
        val edit =
            MahinIconSpec(
                semanticId = "action/edit/v1",
                drawableRes = R.drawable.mahin_ic_action_edit_v1,
                contentDescriptionRes = R.string.icon_desc_action_edit_v1,
                autoMirrored = false,
            )
        val delete =
            MahinIconSpec(
                semanticId = "action/delete/v1",
                drawableRes = R.drawable.mahin_ic_action_delete_v1,
                contentDescriptionRes = R.string.icon_desc_action_delete_v1,
                autoMirrored = false,
            )
        val undo =
            MahinIconSpec(
                semanticId = "action/undo/v1",
                drawableRes = R.drawable.mahin_ic_action_undo_v1,
                contentDescriptionRes = R.string.icon_desc_action_undo_v1,
                autoMirrored = true,
            )
        val share =
            MahinIconSpec(
                semanticId = "action/share/v1",
                drawableRes = R.drawable.mahin_ic_action_share_v1,
                contentDescriptionRes = R.string.icon_desc_action_share_v1,
                autoMirrored = true,
            )
        val search =
            MahinIconSpec(
                semanticId = "action/search/v1",
                drawableRes = R.drawable.mahin_ic_action_search_v1,
                contentDescriptionRes = R.string.icon_desc_action_search_v1,
                autoMirrored = false,
            )
        val bookmark =
            MahinIconSpec(
                semanticId = "action/bookmark/v1",
                drawableRes = R.drawable.mahin_ic_action_bookmark_v1,
                contentDescriptionRes = R.string.icon_desc_action_bookmark_v1,
                autoMirrored = false,
            )
        val close =
            MahinIconSpec(
                semanticId = "action/close/v1",
                drawableRes = R.drawable.mahin_ic_action_close_v1,
                contentDescriptionRes = R.string.icon_desc_action_close_v1,
                autoMirrored = false,
            )
        val back =
            MahinIconSpec(
                semanticId = "action/back/v1",
                drawableRes = R.drawable.mahin_ic_action_back_v1,
                contentDescriptionRes = R.string.icon_desc_action_back_v1,
                autoMirrored = true,
            )
    }

    object Flow {
        val spotting =
            MahinIconSpec(
                semanticId = "flow/spotting/v1",
                drawableRes = R.drawable.mahin_ic_flow_spotting_v1,
                contentDescriptionRes = R.string.icon_desc_flow_spotting_v1,
                autoMirrored = false,
            )
        val light =
            MahinIconSpec(
                semanticId = "flow/light/v1",
                drawableRes = R.drawable.mahin_ic_flow_light_v1,
                contentDescriptionRes = R.string.icon_desc_flow_light_v1,
                autoMirrored = false,
            )
        val medium =
            MahinIconSpec(
                semanticId = "flow/medium/v1",
                drawableRes = R.drawable.mahin_ic_flow_medium_v1,
                contentDescriptionRes = R.string.icon_desc_flow_medium_v1,
                autoMirrored = false,
            )
        val heavy =
            MahinIconSpec(
                semanticId = "flow/heavy/v1",
                drawableRes = R.drawable.mahin_ic_flow_heavy_v1,
                contentDescriptionRes = R.string.icon_desc_flow_heavy_v1,
                autoMirrored = false,
            )
        val very_heavy =
            MahinIconSpec(
                semanticId = "flow/very_heavy/v1",
                drawableRes = R.drawable.mahin_ic_flow_very_heavy_v1,
                contentDescriptionRes = R.string.icon_desc_flow_very_heavy_v1,
                autoMirrored = false,
            )
    }

    object Symptom {
        val cramps =
            MahinIconSpec(
                semanticId = "symptom/cramps/v1",
                drawableRes = R.drawable.mahin_ic_symptom_cramps_v1,
                contentDescriptionRes = R.string.icon_desc_symptom_cramps_v1,
                autoMirrored = false,
            )
        val headache =
            MahinIconSpec(
                semanticId = "symptom/headache/v1",
                drawableRes = R.drawable.mahin_ic_symptom_headache_v1,
                contentDescriptionRes = R.string.icon_desc_symptom_headache_v1,
                autoMirrored = false,
            )
        val migraine =
            MahinIconSpec(
                semanticId = "symptom/migraine/v1",
                drawableRes = R.drawable.mahin_ic_symptom_migraine_v1,
                contentDescriptionRes = R.string.icon_desc_symptom_migraine_v1,
                autoMirrored = false,
            )
        val bloating =
            MahinIconSpec(
                semanticId = "symptom/bloating/v1",
                drawableRes = R.drawable.mahin_ic_symptom_bloating_v1,
                contentDescriptionRes = R.string.icon_desc_symptom_bloating_v1,
                autoMirrored = false,
            )
        val breast_tenderness =
            MahinIconSpec(
                semanticId = "symptom/breast_tenderness/v1",
                drawableRes = R.drawable.mahin_ic_symptom_breast_tenderness_v1,
                contentDescriptionRes = R.string.icon_desc_symptom_breast_tenderness_v1,
                autoMirrored = false,
            )
        val acne =
            MahinIconSpec(
                semanticId = "symptom/acne/v1",
                drawableRes = R.drawable.mahin_ic_symptom_acne_v1,
                contentDescriptionRes = R.string.icon_desc_symptom_acne_v1,
                autoMirrored = false,
            )
        val back_pain =
            MahinIconSpec(
                semanticId = "symptom/back_pain/v1",
                drawableRes = R.drawable.mahin_ic_symptom_back_pain_v1,
                contentDescriptionRes = R.string.icon_desc_symptom_back_pain_v1,
                autoMirrored = false,
            )
        val nausea =
            MahinIconSpec(
                semanticId = "symptom/nausea/v1",
                drawableRes = R.drawable.mahin_ic_symptom_nausea_v1,
                contentDescriptionRes = R.string.icon_desc_symptom_nausea_v1,
                autoMirrored = false,
            )
        val fatigue =
            MahinIconSpec(
                semanticId = "symptom/fatigue/v1",
                drawableRes = R.drawable.mahin_ic_symptom_fatigue_v1,
                contentDescriptionRes = R.string.icon_desc_symptom_fatigue_v1,
                autoMirrored = false,
            )
        val digestive =
            MahinIconSpec(
                semanticId = "symptom/digestive/v1",
                drawableRes = R.drawable.mahin_ic_symptom_digestive_v1,
                contentDescriptionRes = R.string.icon_desc_symptom_digestive_v1,
                autoMirrored = false,
            )
        val cravings =
            MahinIconSpec(
                semanticId = "symptom/cravings/v1",
                drawableRes = R.drawable.mahin_ic_symptom_cravings_v1,
                contentDescriptionRes = R.string.icon_desc_symptom_cravings_v1,
                autoMirrored = false,
            )
        val insomnia =
            MahinIconSpec(
                semanticId = "symptom/insomnia/v1",
                drawableRes = R.drawable.mahin_ic_symptom_insomnia_v1,
                contentDescriptionRes = R.string.icon_desc_symptom_insomnia_v1,
                autoMirrored = false,
            )
        val dizziness =
            MahinIconSpec(
                semanticId = "symptom/dizziness/v1",
                drawableRes = R.drawable.mahin_ic_symptom_dizziness_v1,
                contentDescriptionRes = R.string.icon_desc_symptom_dizziness_v1,
                autoMirrored = false,
            )
        val other =
            MahinIconSpec(
                semanticId = "symptom/other/v1",
                drawableRes = R.drawable.mahin_ic_symptom_other_v1,
                contentDescriptionRes = R.string.icon_desc_symptom_other_v1,
                autoMirrored = false,
            )
        val pain =
            MahinIconSpec(
                semanticId = "symptom/pain/v1",
                drawableRes = R.drawable.mahin_ic_symptom_pain_v1,
                contentDescriptionRes = R.string.icon_desc_symptom_pain_v1,
                autoMirrored = false,
            )
    }

    object Mood {
        val calm =
            MahinIconSpec(
                semanticId = "mood/calm/v1",
                drawableRes = R.drawable.mahin_ic_mood_calm_v1,
                contentDescriptionRes = R.string.icon_desc_mood_calm_v1,
                autoMirrored = false,
            )
        val happy =
            MahinIconSpec(
                semanticId = "mood/happy/v1",
                drawableRes = R.drawable.mahin_ic_mood_happy_v1,
                contentDescriptionRes = R.string.icon_desc_mood_happy_v1,
                autoMirrored = false,
            )
        val energetic =
            MahinIconSpec(
                semanticId = "mood/energetic/v1",
                drawableRes = R.drawable.mahin_ic_mood_energetic_v1,
                contentDescriptionRes = R.string.icon_desc_mood_energetic_v1,
                autoMirrored = false,
            )
        val sensitive =
            MahinIconSpec(
                semanticId = "mood/sensitive/v1",
                drawableRes = R.drawable.mahin_ic_mood_sensitive_v1,
                contentDescriptionRes = R.string.icon_desc_mood_sensitive_v1,
                autoMirrored = false,
            )
        val irritable =
            MahinIconSpec(
                semanticId = "mood/irritable/v1",
                drawableRes = R.drawable.mahin_ic_mood_irritable_v1,
                contentDescriptionRes = R.string.icon_desc_mood_irritable_v1,
                autoMirrored = false,
            )
        val anxious =
            MahinIconSpec(
                semanticId = "mood/anxious/v1",
                drawableRes = R.drawable.mahin_ic_mood_anxious_v1,
                contentDescriptionRes = R.string.icon_desc_mood_anxious_v1,
                autoMirrored = false,
            )
        val sad =
            MahinIconSpec(
                semanticId = "mood/sad/v1",
                drawableRes = R.drawable.mahin_ic_mood_sad_v1,
                contentDescriptionRes = R.string.icon_desc_mood_sad_v1,
                autoMirrored = false,
            )
        val stressed =
            MahinIconSpec(
                semanticId = "mood/stressed/v1",
                drawableRes = R.drawable.mahin_ic_mood_stressed_v1,
                contentDescriptionRes = R.string.icon_desc_mood_stressed_v1,
                autoMirrored = false,
            )
        val mood_swings =
            MahinIconSpec(
                semanticId = "mood/mood_swings/v1",
                drawableRes = R.drawable.mahin_ic_mood_mood_swings_v1,
                contentDescriptionRes = R.string.icon_desc_mood_mood_swings_v1,
                autoMirrored = false,
            )
        val note =
            MahinIconSpec(
                semanticId = "mood/note/v1",
                drawableRes = R.drawable.mahin_ic_mood_note_v1,
                contentDescriptionRes = R.string.icon_desc_mood_note_v1,
                autoMirrored = false,
            )
    }

    object Discharge {
        val dry =
            MahinIconSpec(
                semanticId = "discharge/dry/v1",
                drawableRes = R.drawable.mahin_ic_discharge_dry_v1,
                contentDescriptionRes = R.string.icon_desc_discharge_dry_v1,
                autoMirrored = false,
            )
        val sticky =
            MahinIconSpec(
                semanticId = "discharge/sticky/v1",
                drawableRes = R.drawable.mahin_ic_discharge_sticky_v1,
                contentDescriptionRes = R.string.icon_desc_discharge_sticky_v1,
                autoMirrored = false,
            )
        val creamy =
            MahinIconSpec(
                semanticId = "discharge/creamy/v1",
                drawableRes = R.drawable.mahin_ic_discharge_creamy_v1,
                contentDescriptionRes = R.string.icon_desc_discharge_creamy_v1,
                autoMirrored = false,
            )
        val watery =
            MahinIconSpec(
                semanticId = "discharge/watery/v1",
                drawableRes = R.drawable.mahin_ic_discharge_watery_v1,
                contentDescriptionRes = R.string.icon_desc_discharge_watery_v1,
                autoMirrored = false,
            )
        val egg_white =
            MahinIconSpec(
                semanticId = "discharge/egg_white/v1",
                drawableRes = R.drawable.mahin_ic_discharge_egg_white_v1,
                contentDescriptionRes = R.string.icon_desc_discharge_egg_white_v1,
                autoMirrored = false,
            )
        val unusual =
            MahinIconSpec(
                semanticId = "discharge/unusual/v1",
                drawableRes = R.drawable.mahin_ic_discharge_unusual_v1,
                contentDescriptionRes = R.string.icon_desc_discharge_unusual_v1,
                autoMirrored = false,
            )
    }

    object Tests {
        val opk =
            MahinIconSpec(
                semanticId = "tests/opk/v1",
                drawableRes = R.drawable.mahin_ic_tests_opk_v1,
                contentDescriptionRes = R.string.icon_desc_tests_opk_v1,
                autoMirrored = false,
            )
        val pregnancy_test =
            MahinIconSpec(
                semanticId = "tests/pregnancy_test/v1",
                drawableRes = R.drawable.mahin_ic_tests_pregnancy_test_v1,
                contentDescriptionRes = R.string.icon_desc_tests_pregnancy_test_v1,
                autoMirrored = false,
            )
        val bbt =
            MahinIconSpec(
                semanticId = "tests/bbt/v1",
                drawableRes = R.drawable.mahin_ic_tests_bbt_v1,
                contentDescriptionRes = R.string.icon_desc_tests_bbt_v1,
                autoMirrored = false,
            )
    }

    object Lifestyle {
        val sleep =
            MahinIconSpec(
                semanticId = "lifestyle/sleep/v1",
                drawableRes = R.drawable.mahin_ic_lifestyle_sleep_v1,
                contentDescriptionRes = R.string.icon_desc_lifestyle_sleep_v1,
                autoMirrored = false,
            )
        val exercise =
            MahinIconSpec(
                semanticId = "lifestyle/exercise/v1",
                drawableRes = R.drawable.mahin_ic_lifestyle_exercise_v1,
                contentDescriptionRes = R.string.icon_desc_lifestyle_exercise_v1,
                autoMirrored = false,
            )
        val water =
            MahinIconSpec(
                semanticId = "lifestyle/water/v1",
                drawableRes = R.drawable.mahin_ic_lifestyle_water_v1,
                contentDescriptionRes = R.string.icon_desc_lifestyle_water_v1,
                autoMirrored = false,
            )
        val stress =
            MahinIconSpec(
                semanticId = "lifestyle/stress/v1",
                drawableRes = R.drawable.mahin_ic_lifestyle_stress_v1,
                contentDescriptionRes = R.string.icon_desc_lifestyle_stress_v1,
                autoMirrored = false,
            )
        val weight =
            MahinIconSpec(
                semanticId = "lifestyle/weight/v1",
                drawableRes = R.drawable.mahin_ic_lifestyle_weight_v1,
                contentDescriptionRes = R.string.icon_desc_lifestyle_weight_v1,
                autoMirrored = false,
            )
        val medication =
            MahinIconSpec(
                semanticId = "lifestyle/medication/v1",
                drawableRes = R.drawable.mahin_ic_lifestyle_medication_v1,
                contentDescriptionRes = R.string.icon_desc_lifestyle_medication_v1,
                autoMirrored = false,
            )
    }

    val all: List<MahinIconSpec> =
        listOf(
            Nav.today,
            Nav.calendar,
            Nav.log,
            Nav.insights,
            Nav.learn,
            Nav.pregnancy,
            Nav.plan,
            Nav.history,
            Nav.settings,
            Action.add,
            Action.edit,
            Action.delete,
            Action.undo,
            Action.share,
            Action.search,
            Action.bookmark,
            Action.close,
            Action.back,
            Flow.spotting,
            Flow.light,
            Flow.medium,
            Flow.heavy,
            Flow.very_heavy,
            Symptom.cramps,
            Symptom.headache,
            Symptom.migraine,
            Symptom.bloating,
            Symptom.breast_tenderness,
            Symptom.acne,
            Symptom.back_pain,
            Symptom.nausea,
            Symptom.fatigue,
            Symptom.digestive,
            Symptom.cravings,
            Symptom.insomnia,
            Symptom.dizziness,
            Symptom.other,
            Symptom.pain,
            Mood.calm,
            Mood.happy,
            Mood.energetic,
            Mood.sensitive,
            Mood.irritable,
            Mood.anxious,
            Mood.sad,
            Mood.stressed,
            Mood.mood_swings,
            Mood.note,
            Discharge.dry,
            Discharge.sticky,
            Discharge.creamy,
            Discharge.watery,
            Discharge.egg_white,
            Discharge.unusual,
            Tests.opk,
            Tests.pregnancy_test,
            Tests.bbt,
            Lifestyle.sleep,
            Lifestyle.exercise,
            Lifestyle.water,
            Lifestyle.stress,
            Lifestyle.weight,
            Lifestyle.medication,
        )

    fun findBySemanticId(id: String): MahinIconSpec? = all.firstOrNull { it.semanticId == id }
}
