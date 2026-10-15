import type { WellnessContentKind } from "./cmsApi";

export type ProgramStepDraft = {
  instruction: string;
  durationSeconds: string;
  reps: string;
  restSeconds: string;
  mediaAssetId: string;
};

export function updateProgramStep(
  sessions: ProgramSessionDraft[],
  sessionIndex: number,
  stepIndex: number,
  patch: Partial<ProgramStepDraft>,
): ProgramSessionDraft[] {
  return sessions.map((session, si) =>
    si !== sessionIndex
      ? session
      : {
          ...session,
          steps: session.steps.map((step, sti) =>
            sti !== stepIndex ? step : { ...step, ...patch },
          ),
        },
  );
}

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

export type CareScheduleItemDraft = {
  id: string;
  title: string;
  category: string;
  weekStart: string;
  weekEnd: string;
  description: string;
  appointmentTypeHint: string;
};

export function updateCareScheduleItem(
  items: CareScheduleItemDraft[],
  itemIndex: number,
  patch: Partial<CareScheduleItemDraft>,
): CareScheduleItemDraft[] {
  return items.map((item, i) => (i !== itemIndex ? item : { ...item, ...patch }));
}

export type CareScheduleEditorState = {
  title: string;
  summary: string;
  locale: string;
  regionContext: string;
  items: CareScheduleItemDraft[];
  effectiveFrom: string;
  effectiveTo: string;
  placeholder: boolean;
};

export type ChecklistItemTemplateDraft = {
  id: string;
  label: string;
  section: string;
  order: string;
};

export function updateChecklistItem(
  items: ChecklistItemTemplateDraft[],
  itemIndex: number,
  patch: Partial<ChecklistItemTemplateDraft>,
): ChecklistItemTemplateDraft[] {
  return items.map((item, i) => (i !== itemIndex ? item : { ...item, ...patch }));
}

export type ChecklistTemplateEditorState = {
  title: string;
  summary: string;
  locale: string;
  checklistKind: string;
  sections: string;
  items: ChecklistItemTemplateDraft[];
  effectiveFrom: string;
  effectiveTo: string;
  placeholder: boolean;
};

export type WellnessEditorState =
  | DailyTipEditorState
  | ProgramEditorState
  | CollectionEditorState
  | CareScheduleEditorState
  | ChecklistTemplateEditorState;

export function buildCreateDocumentBody(
  kind: WellnessContentKind,
  slug: string,
  state: WellnessEditorState,
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
  if (kind === "collection") {
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
  if (kind === "care_schedule") {
    const s = state as CareScheduleEditorState;
    return {
      ...base,
      typePayload: {
        placeholder: s.placeholder,
        regionContext: s.regionContext,
        items: s.items.map((item) => ({
          id: item.id,
          title: item.title,
          category: item.category,
          weekStart: parseRequiredInt(item.weekStart, 1),
          weekEnd: parseRequiredInt(item.weekEnd, 1),
          description: item.description || undefined,
          appointmentTypeHint: item.appointmentTypeHint || undefined,
        })),
      },
    };
  }
  const s = state as ChecklistTemplateEditorState;
  return {
    ...base,
    typePayload: {
      placeholder: s.placeholder,
      checklistKind: s.checklistKind,
      sections: s.sections
        .split(",")
        .map((v) => v.trim())
        .filter(Boolean),
      items: s.items.map((item) => ({
        id: item.id,
        label: item.label,
        section: item.section,
        order: parseRequiredInt(item.order, 1),
      })),
    },
  };
}

function parseRequiredInt(raw: string, fallback: number): number {
  const n = Number.parseInt(raw.trim(), 10);
  return Number.isNaN(n) ? fallback : n;
}

function parseOptionalInt(raw: string): number | undefined {
  if (!raw.trim()) return undefined;
  const n = Number.parseInt(raw, 10);
  return Number.isNaN(n) ? undefined : n;
}
