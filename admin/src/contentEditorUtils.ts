export type WellnessContentKind =
  | "daily_tip"
  | "program"
  | "collection"
  | "care_schedule"
  | "checklist_template";

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

export const CARE_SCHEDULE_CATEGORIES = [
  "test",
  "ultrasound",
  "visit",
  "screening",
  "vaccine",
  "other",
] as const;

export const CARE_SCHEDULE_REGIONS = ["iran"] as const;

export const CHECKLIST_KINDS = ["hospital_bag"] as const;

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

export function buildCareSchedulePayload(regionContext: string = "iran") {
  return {
    placeholder: true,
    regionContext,
    items: [
      {
        id: "item-1",
        title: "آزمایش نمونه",
        category: "test",
        weekStart: 8,
        weekEnd: 10,
        description: "نمونه — غیرپزشکی",
      },
    ],
  };
}

export function buildChecklistTemplatePayload(checklistKind: string = "hospital_bag") {
  return {
    placeholder: true,
    checklistKind,
    sections: ["مدارک"],
    items: [
      {
        id: "c1",
        label: "لیست نمونه",
        section: "مدارک",
        order: 1,
      },
    ],
  };
}

export function validateWellnessDraft(kind: WellnessContentKind, title: string): string | null {
  if (!title.trim()) {
    return "عنوان الزامی است.";
  }
  if (kind === "collection" && title.length < 2) {
    return "عنوان مجموعه خیلی کوتاه است.";
  }
  if ((kind === "care_schedule" || kind === "checklist_template") && title.length < 2) {
    return "عنوان خیلی کوتاه است.";
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

export function visibleWorkflowActions(roles: string[]): string[] {
  const isEditor = roles.includes("EDITOR") || roles.includes("SUPER_ADMIN");
  const isMedical = roles.includes("MEDICAL_REVIEWER") || roles.includes("SUPER_ADMIN");
  return WORKFLOW_REVIEW_ACTIONS.filter((action) => {
    if (action === "approve_medical") {
      return isMedical;
    }
    return isEditor;
  });
}
