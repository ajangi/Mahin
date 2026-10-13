export type WellnessContentKind = "daily_tip" | "program" | "collection";

export const COLLECTION_CATEGORIES = [
  "cycle",
  "fertility",
  "pregnancy",
  "symptoms",
  "nutrition",
  "tests",
  "lifestyle",
] as const;

export const PROGRAM_TYPES = ["pelvic_floor", "movement", "breathing"] as const;

export function buildDailyTipPayload(targetingModes: string, cyclePhase: string) {
  return {
    placeholder: true,
    targeting: {
      modes: targetingModes ? [targetingModes] : [],
      cyclePhases: cyclePhase ? [cyclePhase] : [],
      pregnancyWeeks: [] as number[],
    },
  };
}

export function buildProgramPayload(programType: string) {
  return {
    placeholder: true,
    programType,
    safetyNotice: "نمونه — غیرپزشکی",
    contraindications: "نمونه — غیرپزشکی",
    sessions: [
      {
        id: "session-1",
        title: "جلسهٔ نمونه",
        order: 1,
        steps: [{ instruction: "نمونه — غیرپزشکی" }],
      },
    ],
  };
}

export function buildCollectionPayload(category: string, articleDocumentIds: string[]) {
  return {
    placeholder: true,
    category,
    articleDocumentIds,
    featured: false,
  };
}

export function validateWellnessDraft(kind: WellnessContentKind, title: string): string | null {
  if (!title.trim()) {
    return "عنوان الزامی است.";
  }
  if (kind === "collection" && title.length < 2) {
    return "عنوان مجموعه خیلی کوتاه است.";
  }
  return null;
}

export const WORKFLOW_REVIEW_ACTIONS = [
  "submit_medical_review",
  "approve_medical",
  "submit_editorial_review",
  "approve_editorial",
  "publish",
  "retire",
] as const;
