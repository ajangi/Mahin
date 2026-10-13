import type { WellnessContentKind } from "./cmsApi";

export type ProgramStepDraft = {
  instruction: string;
  durationSeconds: string;
  reps: string;
  restSeconds: string;
  mediaAssetId: string;
};

export type ProgramSessionDraft = {
  id: string;
  title: string;
  order: number;
  steps: ProgramStepDraft[];
};

export type DailyTipEditorState = {
  title: string;
  summary: string;
  bodyRichtext: string;
  locale: string;
  modes: string[];
  cyclePhases: string[];
  pregnancyWeeks: string;
  effectiveFrom: string;
  effectiveTo: string;
  placeholder: boolean;
};

export type ProgramEditorState = {
  title: string;
  summary: string;
  locale: string;
  programType: string;
  safetyNotice: string;
  contraindications: string;
  essentialSafetyInformation: string;
  sessions: ProgramSessionDraft[];
  effectiveFrom: string;
  effectiveTo: string;
  placeholder: boolean;
};

export type CollectionEditorState = {
  title: string;
  summary: string;
  locale: string;
  category: string;
  articleDocumentIds: string;
  featured: boolean;
  effectiveFrom: string;
  effectiveTo: string;
  placeholder: boolean;
};

export function buildCreateDocumentBody(
  kind: WellnessContentKind,
  slug: string,
  state: DailyTipEditorState | ProgramEditorState | CollectionEditorState,
): Record<string, unknown> {
  const base = {
    slug,
    locale: state.locale,
    title: state.title,
    summary: state.summary,
    contentType: kind,
    medicalRiskLevel: kind === "program" ? "general_education" : "general_education",
    effectiveFrom: state.effectiveFrom || undefined,
    effectiveTo: state.effectiveTo || undefined,
  };
  if (kind === "daily_tip") {
    const s = state as DailyTipEditorState;
    return {
      ...base,
      bodyRichtext: s.bodyRichtext,
      typePayload: {
        placeholder: s.placeholder,
        targeting: {
          modes: s.modes,
          cyclePhases: s.cyclePhases,
          pregnancyWeeks: s.pregnancyWeeks
            .split(",")
            .map((v) => v.trim())
            .filter(Boolean)
            .map((v) => Number.parseInt(v, 10))
            .filter((n) => !Number.isNaN(n)),
        },
      },
    };
  }
  if (kind === "program") {
    const s = state as ProgramEditorState;
    return {
      ...base,
      typePayload: {
        placeholder: s.placeholder,
        programType: s.programType,
        safetyNotice: s.safetyNotice,
        contraindications: s.contraindications,
        essentialSafetyInformation: s.essentialSafetyInformation || undefined,
        sessions: s.sessions.map((session) => ({
          id: session.id,
          title: session.title,
          order: session.order,
          steps: session.steps.map((step) => ({
            instruction: step.instruction,
            durationSeconds: parseOptionalInt(step.durationSeconds),
            reps: parseOptionalInt(step.reps),
            restSeconds: parseOptionalInt(step.restSeconds),
            mediaAssetId: step.mediaAssetId || undefined,
          })),
        })),
      },
    };
  }
  const s = state as CollectionEditorState;
  return {
    ...base,
    medicalRiskLevel: "none",
    typePayload: {
      placeholder: s.placeholder,
      category: s.category,
      featured: s.featured,
      articleDocumentIds: s.articleDocumentIds
        .split(",")
        .map((v) => v.trim())
        .filter(Boolean),
    },
  };
}

function parseOptionalInt(raw: string): number | undefined {
  if (!raw.trim()) return undefined;
  const n = Number.parseInt(raw, 10);
  return Number.isNaN(n) ? undefined : n;
}
