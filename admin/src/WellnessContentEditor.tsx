import { useMemo, useState } from "react";
import type { WellnessContentKind } from "./cmsApi";
import {
  CARE_SCHEDULE_CATEGORIES,
  CARE_SCHEDULE_REGIONS,
  CHECKLIST_KINDS,
  COLLECTION_CATEGORIES,
  PROGRAM_TYPES,
} from "./contentEditorUtils";
import {
  buildCreateDocumentBody,
  updateCareScheduleItem,
  updateChecklistItem,
  updateProgramStep,
  updateWarningSignItem,
  type CareScheduleEditorState,
  type CareScheduleItemDraft,
  type ChecklistItemTemplateDraft,
  type ChecklistTemplateEditorState,
  type CollectionEditorState,
  type DailyTipEditorState,
  type ProgramEditorState,
  type ProgramSessionDraft,
  type WarningSignItemDraft,
  type WarningSignsEditorState,
} from "./wellnessEditorModels";

export type WellnessContentEditorProps = {
  kind: WellnessContentKind;
  onSubmit: (body: Record<string, unknown>) => Promise<void>;
};

const defaultCareScheduleItem = (): CareScheduleItemDraft => ({
  id: `item-${Date.now()}`,
  title: "آزمایش نمونه",
  category: CARE_SCHEDULE_CATEGORIES[0],
  weekStart: "8",
  weekEnd: "10",
  description: "نمونه — غیرپزشکی",
  appointmentTypeHint: "",
});

const defaultChecklistItem = (): ChecklistItemTemplateDraft => ({
  id: `c-${Date.now()}`,
  label: "لیست نمونه",
  section: "مدارک",
  order: "1",
});

const defaultWarningSignItem = (): WarningSignItemDraft => ({
  id: `sign-${Date.now()}`,
  title: "علامت نمونه",
  summary: "توضیح نمونه — غیرپزشکی",
});

const defaultSession = (): ProgramSessionDraft => ({
  id: `session-${Date.now()}`,
  title: "جلسهٔ نمونه",
  order: 1,
  steps: [
    {
      instruction: "نمونه — غیرپزشکی",
      durationSeconds: "30",
      reps: "",
      restSeconds: "10",
      mediaAssetId: "",
    },
  ],
});

export function WellnessContentEditor({ kind, onSubmit }: WellnessContentEditorProps) {
  const [daily, setDaily] = useState<DailyTipEditorState>({
    title: "نکتهٔ نمونه",
    summary: "خلاصه",
    bodyRichtext: "متن نمونه",
    locale: "fa-IR",
    modes: ["cycle"],
    cyclePhases: ["follicular"],
    pregnancyWeeks: "",
    effectiveFrom: "",
    effectiveTo: "",
    placeholder: true,
  });
  const [program, setProgram] = useState<ProgramEditorState>({
    title: "برنامهٔ نمونه",
    summary: "خلاصه",
    locale: "fa-IR",
    programType: PROGRAM_TYPES[0],
    safetyNotice: "نمونه — غیرپزشکی",
    contraindications: "نمونه — غیرپزشکی",
    essentialSafetyInformation: "",
    sessions: [defaultSession()],
    effectiveFrom: "",
    effectiveTo: "",
    placeholder: true,
  });
  const [collection, setCollection] = useState<CollectionEditorState>({
    title: "مجموعهٔ نمونه",
    summary: "خلاصه",
    locale: "fa-IR",
    category: COLLECTION_CATEGORIES[0],
    articleDocumentIds: "",
    featured: false,
    effectiveFrom: "",
    effectiveTo: "",
    placeholder: true,
  });
  const [careSchedule, setCareSchedule] = useState<CareScheduleEditorState>({
    title: "زمان‌بندی مراقبت نمونه",
    summary: "خلاصه",
    locale: "fa-IR",
    regionContext: CARE_SCHEDULE_REGIONS[0],
    items: [defaultCareScheduleItem()],
    effectiveFrom: "",
    effectiveTo: "",
    placeholder: true,
  });
  const [checklistTemplate, setChecklistTemplate] = useState<ChecklistTemplateEditorState>({
    title: "چک‌لیست نمونه",
    summary: "خلاصه",
    locale: "fa-IR",
    checklistKind: CHECKLIST_KINDS[0],
    sections: "مدارک",
    items: [defaultChecklistItem()],
    effectiveFrom: "",
    effectiveTo: "",
    placeholder: true,
  });
  const [warningSigns, setWarningSigns] = useState<WarningSignsEditorState>({
    title: "هشدارهای بارداری نمونه",
    summary: "خلاصه",
    locale: "fa-IR",
    introTitle: "مقدمه نمونه",
    introBody: "متن آموزشی نمونه",
    escalationTitle: "ارتقا",
    escalationBody: "در صورت نگرانی با مراقب تماس بگیرید",
    contactClinicianLabel: "با پزشک یا ماما تماس بگیرید",
    urgentEscalationTitle: "مراقبت فوری",
    urgentEscalationBody: "در صورت علائم شدید فوراً اقدام کنید",
    urgentContactClinicianLabel: "فوراً با مراقب تماس بگیرید",
    elevatedSystolic: "140",
    elevatedDiastolic: "90",
    urgentSystolic: "160",
    urgentDiastolic: "110",
    signs: [defaultWarningSignItem()],
    effectiveFrom: "",
    effectiveTo: "",
    placeholder: true,
  });
  const [error, setError] = useState<string | null>(null);

  const patchShared = <K extends "title" | "locale" | "effectiveFrom" | "effectiveTo" | "placeholder">(
    key: K,
    value: string | boolean,
  ) => {
    if (kind === "daily_tip") setDaily((s) => ({ ...s, [key]: value }));
    else if (kind === "program") setProgram((s) => ({ ...s, [key]: value }));
    else if (kind === "collection") setCollection((s) => ({ ...s, [key]: value }));
    else if (kind === "care_schedule") setCareSchedule((s) => ({ ...s, [key]: value }));
    else if (kind === "warning_signs") setWarningSigns((s) => ({ ...s, [key]: value }));
    else setChecklistTemplate((s) => ({ ...s, [key]: value }));
  };

  const state = useMemo(() => {
    if (kind === "daily_tip") return daily;
    if (kind === "program") return program;
    if (kind === "collection") return collection;
    if (kind === "care_schedule") return careSchedule;
    if (kind === "warning_signs") return warningSigns;
    return checklistTemplate;
  }, [kind, daily, program, collection, careSchedule, checklistTemplate, warningSigns]);

  return (
    <section dir="rtl" aria-label={`ویرایشگر ${kind}`}>
      <label>
        عنوان
        <input
          aria-label="عنوان"
          value={state.title}
          onChange={(e) => patchShared("title", e.target.value)}
        />
      </label>
      <label>
        locale
        <input
          aria-label="locale"
          value={state.locale}
          onChange={(e) => patchShared("locale", e.target.value)}
        />
      </label>
      <label>
        effectiveFrom (ISO)
        <input
          aria-label="effectiveFrom"
          value={state.effectiveFrom}
          onChange={(e) => patchShared("effectiveFrom", e.target.value)}
        />
      </label>
      <label>
        effectiveTo (ISO)
        <input
          aria-label="effectiveTo"
          value={state.effectiveTo}
          onChange={(e) => patchShared("effectiveTo", e.target.value)}
        />
      </label>
      {kind === "daily_tip" && (
        <>
          <label>
            متن نکته
            <textarea
              aria-label="متن نکته"
              value={daily.bodyRichtext}
              onChange={(e) => setDaily((s) => ({ ...s, bodyRichtext: e.target.value }))}
            />
          </label>
          <fieldset>
            <legend>هدف‌گیری</legend>
            {["cycle", "ttc", "pregnancy"].map((mode) => (
              <label key={mode}>
                <input
                  type="checkbox"
                  checked={daily.modes.includes(mode)}
                  onChange={(e) =>
                    setDaily((s) => ({
                      ...s,
                      modes: e.target.checked
                        ? [...s.modes, mode]
                        : s.modes.filter((m) => m !== mode),
                    }))
                  }
                />
                {mode}
              </label>
            ))}
            {["menstrual", "follicular", "ovulation", "luteal"].map((phase) => (
              <label key={phase}>
                <input
                  type="checkbox"
                  checked={daily.cyclePhases.includes(phase)}
                  onChange={(e) =>
                    setDaily((s) => ({
                      ...s,
                      cyclePhases: e.target.checked
                        ? [...s.cyclePhases, phase]
                        : s.cyclePhases.filter((p) => p !== phase),
                    }))
                  }
                />
                {phase}
              </label>
            ))}
            <label>
              هفته‌های بارداری (با ویرگول)
              <input
                aria-label="pregnancyWeeks"
                value={daily.pregnancyWeeks}
                onChange={(e) => setDaily((s) => ({ ...s, pregnancyWeeks: e.target.value }))}
              />
            </label>
          </fieldset>
        </>
      )}
      {kind === "program" && (
        <>
          <label>
            نوع برنامه
            <select
              aria-label="programType"
              value={program.programType}
              onChange={(e) => setProgram((s) => ({ ...s, programType: e.target.value }))}
            >
              {PROGRAM_TYPES.map((t) => (
                <option key={t} value={t}>{t}</option>
              ))}
            </select>
          </label>
          <label>
            اطلاعات ایمنی
            <textarea
              aria-label="safetyNotice"
              value={program.safetyNotice}
              onChange={(e) => setProgram((s) => ({ ...s, safetyNotice: e.target.value }))}
            />
          </label>
          <label>
            موارد منع
            <textarea
              aria-label="contraindications"
              value={program.contraindications}
              onChange={(e) => setProgram((s) => ({ ...s, contraindications: e.target.value }))}
            />
          </label>
          {program.sessions.map((session, sessionIndex) => (
            <fieldset key={session.id}>
              <legend>جلسه {session.order}</legend>
              {session.steps.map((step, stepIndex) => (
                <div key={`${session.id}-${stepIndex}`}>
                  <label>
                    دستورالعمل
                    <textarea
                      aria-label="instruction"
                      value={step.instruction}
                      onChange={(e) =>
                        setProgram((s) => ({
                          ...s,
                          sessions: updateProgramStep(s.sessions, sessionIndex, stepIndex, {
                            instruction: e.target.value,
                          }),
                        }))
                      }
                    />
                  </label>
                  <label>
                    مدت (ثانیه)
                    <input
                      aria-label="durationSeconds"
                      value={step.durationSeconds}
                      onChange={(e) =>
                        setProgram((s) => ({
                          ...s,
                          sessions: updateProgramStep(s.sessions, sessionIndex, stepIndex, {
                            durationSeconds: e.target.value,
                          }),
                        }))
                      }
                    />
                  </label>
                  <label>
                    تکرار
                    <input
                      aria-label="reps"
                      value={step.reps}
                      onChange={(e) =>
                        setProgram((s) => ({
                          ...s,
                          sessions: updateProgramStep(s.sessions, sessionIndex, stepIndex, {
                            reps: e.target.value,
                          }),
                        }))
                      }
                    />
                  </label>
                  <label>
                    استراحت (ثانیه)
                    <input
                      aria-label="restSeconds"
                      value={step.restSeconds}
                      onChange={(e) =>
                        setProgram((s) => ({
                          ...s,
                          sessions: updateProgramStep(s.sessions, sessionIndex, stepIndex, {
                            restSeconds: e.target.value,
                          }),
                        }))
                      }
                    />
                  </label>
                  <label>
                    mediaAssetId
                    <input
                      aria-label="mediaAssetId"
                      value={step.mediaAssetId}
                      onChange={(e) =>
                        setProgram((s) => ({
                          ...s,
                          sessions: updateProgramStep(s.sessions, sessionIndex, stepIndex, {
                            mediaAssetId: e.target.value,
                          }),
                        }))
                      }
                    />
                  </label>
                </div>
              ))}
            </fieldset>
          ))}
        </>
      )}
      {kind === "collection" && (
        <>
          <label>
            دسته
            <select
              aria-label="category"
              value={collection.category}
              onChange={(e) => setCollection((s) => ({ ...s, category: e.target.value }))}
            >
              {COLLECTION_CATEGORIES.map((c) => (
                <option key={c} value={c}>{c}</option>
              ))}
            </select>
          </label>
          <label>
            شناسهٔ مقالات (با ویرگول)
            <textarea
              aria-label="articleDocumentIds"
              value={collection.articleDocumentIds}
              onChange={(e) => setCollection((s) => ({ ...s, articleDocumentIds: e.target.value }))}
            />
          </label>
        </>
      )}
      {kind === "care_schedule" && (
        <>
          <label>
            زمینهٔ منطقه
            <select
              aria-label="regionContext"
              value={careSchedule.regionContext}
              onChange={(e) => setCareSchedule((s) => ({ ...s, regionContext: e.target.value }))}
            >
              {CARE_SCHEDULE_REGIONS.map((r) => (
                <option key={r} value={r}>{r}</option>
              ))}
            </select>
          </label>
          {careSchedule.items.map((item, itemIndex) => (
            <fieldset key={item.id}>
              <legend>آیتم {itemIndex + 1}</legend>
              <label>
                شناسه
                <input
                  aria-label="careScheduleItemId"
                  value={item.id}
                  onChange={(e) =>
                    setCareSchedule((s) => ({
                      ...s,
                      items: updateCareScheduleItem(s.items, itemIndex, { id: e.target.value }),
                    }))
                  }
                />
              </label>
              <label>
                عنوان
                <input
                  aria-label="careScheduleItemTitle"
                  value={item.title}
                  onChange={(e) =>
                    setCareSchedule((s) => ({
                      ...s,
                      items: updateCareScheduleItem(s.items, itemIndex, { title: e.target.value }),
                    }))
                  }
                />
              </label>
              <label>
                دسته
                <select
                  aria-label="careScheduleItemCategory"
                  value={item.category}
                  onChange={(e) =>
                    setCareSchedule((s) => ({
                      ...s,
                      items: updateCareScheduleItem(s.items, itemIndex, { category: e.target.value }),
                    }))
                  }
                >
                  {CARE_SCHEDULE_CATEGORIES.map((c) => (
                    <option key={c} value={c}>{c}</option>
                  ))}
                </select>
              </label>
              <label>
                هفتهٔ شروع
                <input
                  aria-label="careScheduleWeekStart"
                  value={item.weekStart}
                  onChange={(e) =>
                    setCareSchedule((s) => ({
                      ...s,
                      items: updateCareScheduleItem(s.items, itemIndex, { weekStart: e.target.value }),
                    }))
                  }
                />
              </label>
              <label>
                هفتهٔ پایان
                <input
                  aria-label="careScheduleWeekEnd"
                  value={item.weekEnd}
                  onChange={(e) =>
                    setCareSchedule((s) => ({
                      ...s,
                      items: updateCareScheduleItem(s.items, itemIndex, { weekEnd: e.target.value }),
                    }))
                  }
                />
              </label>
              <label>
                توضیح
                <textarea
                  aria-label="careScheduleDescription"
                  value={item.description}
                  onChange={(e) =>
                    setCareSchedule((s) => ({
                      ...s,
                      items: updateCareScheduleItem(s.items, itemIndex, { description: e.target.value }),
                    }))
                  }
                />
              </label>
              <label>
                appointmentTypeHint
                <input
                  aria-label="appointmentTypeHint"
                  value={item.appointmentTypeHint}
                  onChange={(e) =>
                    setCareSchedule((s) => ({
                      ...s,
                      items: updateCareScheduleItem(s.items, itemIndex, {
                        appointmentTypeHint: e.target.value,
                      }),
                    }))
                  }
                />
              </label>
            </fieldset>
          ))}
        </>
      )}
      {kind === "warning_signs" && (
        <>
          <label>
            عنوان مقدمه
            <input
              aria-label="warningIntroTitle"
              value={warningSigns.introTitle}
              onChange={(e) => setWarningSigns((s) => ({ ...s, introTitle: e.target.value }))}
            />
          </label>
          <label>
            متن مقدمه
            <textarea
              aria-label="warningIntroBody"
              value={warningSigns.introBody}
              onChange={(e) => setWarningSigns((s) => ({ ...s, introBody: e.target.value }))}
            />
          </label>
          <fieldset>
            <legend>ارتقا (معمولی)</legend>
            <label>
              عنوان
              <input
                aria-label="warningEscalationTitle"
                value={warningSigns.escalationTitle}
                onChange={(e) => setWarningSigns((s) => ({ ...s, escalationTitle: e.target.value }))}
              />
            </label>
            <label>
              متن
              <textarea
                aria-label="warningEscalationBody"
                value={warningSigns.escalationBody}
                onChange={(e) => setWarningSigns((s) => ({ ...s, escalationBody: e.target.value }))}
              />
            </label>
            <label>
              برچسب تماس
              <input
                aria-label="warningContactClinicianLabel"
                value={warningSigns.contactClinicianLabel}
                onChange={(e) =>
                  setWarningSigns((s) => ({ ...s, contactClinicianLabel: e.target.value }))
                }
              />
            </label>
          </fieldset>
          <fieldset>
            <legend>ارتقا (فوری)</legend>
            <label>
              عنوان
              <input
                aria-label="warningUrgentEscalationTitle"
                value={warningSigns.urgentEscalationTitle}
                onChange={(e) =>
                  setWarningSigns((s) => ({ ...s, urgentEscalationTitle: e.target.value }))
                }
              />
            </label>
            <label>
              متن
              <textarea
                aria-label="warningUrgentEscalationBody"
                value={warningSigns.urgentEscalationBody}
                onChange={(e) =>
                  setWarningSigns((s) => ({ ...s, urgentEscalationBody: e.target.value }))
                }
              />
            </label>
            <label>
              برچسب تماس فوری
              <input
                aria-label="warningUrgentContactClinicianLabel"
                value={warningSigns.urgentContactClinicianLabel}
                onChange={(e) =>
                  setWarningSigns((s) => ({ ...s, urgentContactClinicianLabel: e.target.value }))
                }
              />
            </label>
          </fieldset>
          <fieldset>
            <legend>آستانه‌های فشار خون (CMS)</legend>
            <label>
              سیستولیک بالا
              <input
                aria-label="bpElevatedSystolic"
                value={warningSigns.elevatedSystolic}
                onChange={(e) => setWarningSigns((s) => ({ ...s, elevatedSystolic: e.target.value }))}
              />
            </label>
            <label>
              دیاستولیک بالا
              <input
                aria-label="bpElevatedDiastolic"
                value={warningSigns.elevatedDiastolic}
                onChange={(e) => setWarningSigns((s) => ({ ...s, elevatedDiastolic: e.target.value }))}
              />
            </label>
            <label>
              سیستولیک فوری
              <input
                aria-label="bpUrgentSystolic"
                value={warningSigns.urgentSystolic}
                onChange={(e) => setWarningSigns((s) => ({ ...s, urgentSystolic: e.target.value }))}
              />
            </label>
            <label>
              دیاستولیک فوری
              <input
                aria-label="bpUrgentDiastolic"
                value={warningSigns.urgentDiastolic}
                onChange={(e) => setWarningSigns((s) => ({ ...s, urgentDiastolic: e.target.value }))}
              />
            </label>
          </fieldset>
          {warningSigns.signs.map((sign, signIndex) => (
            <fieldset key={sign.id}>
              <legend>علامت {signIndex + 1}</legend>
              <label>
                شناسه
                <input
                  aria-label="warningSignId"
                  value={sign.id}
                  onChange={(e) =>
                    setWarningSigns((s) => ({
                      ...s,
                      signs: updateWarningSignItem(s.signs, signIndex, { id: e.target.value }),
                    }))
                  }
                />
              </label>
              <label>
                عنوان
                <input
                  aria-label="warningSignTitle"
                  value={sign.title}
                  onChange={(e) =>
                    setWarningSigns((s) => ({
                      ...s,
                      signs: updateWarningSignItem(s.signs, signIndex, { title: e.target.value }),
                    }))
                  }
                />
              </label>
              <label>
                خلاصه
                <textarea
                  aria-label="warningSignSummary"
                  value={sign.summary}
                  onChange={(e) =>
                    setWarningSigns((s) => ({
                      ...s,
                      signs: updateWarningSignItem(s.signs, signIndex, { summary: e.target.value }),
                    }))
                  }
                />
              </label>
            </fieldset>
          ))}
        </>
      )}
      {kind === "checklist_template" && (
        <>
          <label>
            نوع چک‌لیست
            <select
              aria-label="checklistKind"
              value={checklistTemplate.checklistKind}
              onChange={(e) => setChecklistTemplate((s) => ({ ...s, checklistKind: e.target.value }))}
            >
              {CHECKLIST_KINDS.map((k) => (
                <option key={k} value={k}>{k}</option>
              ))}
            </select>
          </label>
          <label>
            بخش‌ها (با ویرگول)
            <input
              aria-label="checklistSections"
              value={checklistTemplate.sections}
              onChange={(e) => setChecklistTemplate((s) => ({ ...s, sections: e.target.value }))}
            />
          </label>
          {checklistTemplate.items.map((item, itemIndex) => (
            <fieldset key={item.id}>
              <legend>ردیف {itemIndex + 1}</legend>
              <label>
                شناسه
                <input
                  aria-label="checklistItemId"
                  value={item.id}
                  onChange={(e) =>
                    setChecklistTemplate((s) => ({
                      ...s,
                      items: updateChecklistItem(s.items, itemIndex, { id: e.target.value }),
                    }))
                  }
                />
              </label>
              <label>
                برچسب
                <input
                  aria-label="checklistItemLabel"
                  value={item.label}
                  onChange={(e) =>
                    setChecklistTemplate((s) => ({
                      ...s,
                      items: updateChecklistItem(s.items, itemIndex, { label: e.target.value }),
                    }))
                  }
                />
              </label>
              <label>
                بخش
                <input
                  aria-label="checklistItemSection"
                  value={item.section}
                  onChange={(e) =>
                    setChecklistTemplate((s) => ({
                      ...s,
                      items: updateChecklistItem(s.items, itemIndex, { section: e.target.value }),
                    }))
                  }
                />
              </label>
              <label>
                ترتیب
                <input
                  aria-label="checklistItemOrder"
                  value={item.order}
                  onChange={(e) =>
                    setChecklistTemplate((s) => ({
                      ...s,
                      items: updateChecklistItem(s.items, itemIndex, { order: e.target.value }),
                    }))
                  }
                />
              </label>
            </fieldset>
          ))}
        </>
      )}
      <label>
        <input
          type="checkbox"
          checked={state.placeholder}
          onChange={(e) => patchShared("placeholder", e.target.checked)}
        />
        placeholder (نمونه)
      </label>
      {error && <p role="alert">{error}</p>}
      <button
        type="button"
        onClick={async () => {
          setError(null);
          try {
            const slug = `${kind}-${Date.now()}`;
            const body = buildCreateDocumentBody(kind, slug, state);
            await onSubmit(body);
          } catch {
            setError("ذخیره ناموفق بود.");
          }
        }}
      >
        ذخیرهٔ پیش‌نویس
      </button>
    </section>
  );
}
