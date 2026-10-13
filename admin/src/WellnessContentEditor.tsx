import { useMemo, useState } from "react";
import type { WellnessContentKind } from "./cmsApi";
import { COLLECTION_CATEGORIES, PROGRAM_TYPES } from "./contentEditorUtils";
import {
  buildCreateDocumentBody,
  type CollectionEditorState,
  type DailyTipEditorState,
  type ProgramEditorState,
  type ProgramSessionDraft,
} from "./wellnessEditorModels";

export type WellnessContentEditorProps = {
  kind: WellnessContentKind;
  onSubmit: (body: Record<string, unknown>) => Promise<void>;
};

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
  const [error, setError] = useState<string | null>(null);

  const state = useMemo(() => {
    if (kind === "daily_tip") return daily;
    if (kind === "program") return program;
    return collection;
  }, [kind, daily, program, collection]);

  return (
    <section dir="rtl" aria-label={`ویرایشگر ${kind}`}>
      <label>
        عنوان
        <input
          aria-label="عنوان"
          value={state.title}
          onChange={(e) => {
            const title = e.target.value;
            if (kind === "daily_tip") setDaily((s) => ({ ...s, title }));
            else if (kind === "program") setProgram((s) => ({ ...s, title }));
            else setCollection((s) => ({ ...s, title }));
          }}
        />
      </label>
      <label>
        locale
        <input
          aria-label="locale"
          value={state.locale}
          onChange={(e) => {
            const locale = e.target.value;
            if (kind === "daily_tip") setDaily((s) => ({ ...s, locale }));
            else if (kind === "program") setProgram((s) => ({ ...s, locale }));
            else setCollection((s) => ({ ...s, locale }));
          }}
        />
      </label>
      <label>
        effectiveFrom (ISO)
        <input
          aria-label="effectiveFrom"
          value={state.effectiveFrom}
          onChange={(e) => {
            const v = e.target.value;
            if (kind === "daily_tip") setDaily((s) => ({ ...s, effectiveFrom: v }));
            else if (kind === "program") setProgram((s) => ({ ...s, effectiveFrom: v }));
            else setCollection((s) => ({ ...s, effectiveFrom: v }));
          }}
        />
      </label>
      <label>
        effectiveTo (ISO)
        <input
          aria-label="effectiveTo"
          value={state.effectiveTo}
          onChange={(e) => {
            const v = e.target.value;
            if (kind === "daily_tip") setDaily((s) => ({ ...s, effectiveTo: v }));
            else if (kind === "program") setProgram((s) => ({ ...s, effectiveTo: v }));
            else setCollection((s) => ({ ...s, effectiveTo: v }));
          }}
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
                      onChange={(e) => {
                        const next = [...program.sessions];
                        next[sessionIndex].steps[stepIndex].instruction = e.target.value;
                        setProgram((s) => ({ ...s, sessions: next }));
                      }}
                    />
                  </label>
                  <label>
                    مدت (ثانیه)
                    <input
                      aria-label="durationSeconds"
                      value={step.durationSeconds}
                      onChange={(e) => {
                        const next = [...program.sessions];
                        next[sessionIndex].steps[stepIndex].durationSeconds = e.target.value;
                        setProgram((s) => ({ ...s, sessions: next }));
                      }}
                    />
                  </label>
                  <label>
                    تکرار
                    <input
                      aria-label="reps"
                      value={step.reps}
                      onChange={(e) => {
                        const next = [...program.sessions];
                        next[sessionIndex].steps[stepIndex].reps = e.target.value;
                        setProgram((s) => ({ ...s, sessions: next }));
                      }}
                    />
                  </label>
                  <label>
                    استراحت (ثانیه)
                    <input
                      aria-label="restSeconds"
                      value={step.restSeconds}
                      onChange={(e) => {
                        const next = [...program.sessions];
                        next[sessionIndex].steps[stepIndex].restSeconds = e.target.value;
                        setProgram((s) => ({ ...s, sessions: next }));
                      }}
                    />
                  </label>
                  <label>
                    mediaAssetId
                    <input
                      aria-label="mediaAssetId"
                      value={step.mediaAssetId}
                      onChange={(e) => {
                        const next = [...program.sessions];
                        next[sessionIndex].steps[stepIndex].mediaAssetId = e.target.value;
                        setProgram((s) => ({ ...s, sessions: next }));
                      }}
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
      <label>
        <input
          type="checkbox"
          checked={state.placeholder}
          onChange={(e) => {
            const placeholder = e.target.checked;
            if (kind === "daily_tip") setDaily((s) => ({ ...s, placeholder }));
            else if (kind === "program") setProgram((s) => ({ ...s, placeholder }));
            else setCollection((s) => ({ ...s, placeholder }));
          }}
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
