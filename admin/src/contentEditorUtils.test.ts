import { describe, expect, it } from "vitest";
import {
  buildCareSchedulePayload,
  buildChecklistTemplatePayload,
  buildCollectionPayload,
  buildDailyTipPayload,
  buildProgramPayload,
  validateWellnessDraft,
  visibleWorkflowActions,
  WORKFLOW_REVIEW_ACTIONS,
} from "./contentEditorUtils";

describe("content editor utils", () => {
  it("builds RTL-friendly placeholder daily tip payload", () => {
    const payload = buildDailyTipPayload("cycle", "follicular");
    expect(payload.placeholder).toBe(true);
    expect(payload.targeting.modes).toEqual(["cycle"]);
    expect(payload.targeting.cyclePhases).toEqual(["follicular"]);
  });

  it("builds program payload with safety fields for CMS review", () => {
    const payload = buildProgramPayload("breathing");
    expect(payload.programType).toBe("breathing");
    expect(payload.safetyNotice).toContain("نمونه");
    expect(payload.sessions[0].steps[0].instruction).toContain("غیرپزشکی");
  });

  it("validates Persian titles for wellness drafts", () => {
    expect(validateWellnessDraft("daily_tip", "نکتهٔ نمونه")).toBeNull();
    expect(validateWellnessDraft("daily_tip", "  ")).toMatch(/عنوان/);
  });

  it("builds collection payload with category", () => {
    const payload = buildCollectionPayload("pregnancy", []);
    expect(payload.category).toBe("pregnancy");
    expect(payload.placeholder).toBe(true);
  });

  it("builds care schedule placeholder with week window", () => {
    const payload = buildCareSchedulePayload();
    expect(payload.regionContext).toBe("iran");
    expect(payload.items[0].weekStart).toBe(8);
    expect(payload.items[0].category).toBe("test");
  });

  it("builds checklist template placeholder with sections and items", () => {
    const payload = buildChecklistTemplatePayload();
    expect(payload.checklistKind).toBe("hospital_bag");
    expect(payload.sections).toEqual(["مدارک"]);
    expect(payload.items[0].order).toBe(1);
  });

  it("validates care_schedule and checklist_template titles", () => {
    expect(validateWellnessDraft("care_schedule", "زمان‌بندی")).toBeNull();
    expect(validateWellnessDraft("checklist_template", " ")).toMatch(/عنوان/);
    expect(validateWellnessDraft("checklist_template", "ا")).toMatch(/کوتاه/);
  });

  it("lists governed workflow actions for review screens", () => {
    expect(WORKFLOW_REVIEW_ACTIONS).toContain("approve_medical");
    expect(WORKFLOW_REVIEW_ACTIONS).toContain("publish");
  });

  it("exposes approve_medical only to medical reviewers", () => {
    expect(visibleWorkflowActions(["EDITOR"])).not.toContain("approve_medical");
    expect(visibleWorkflowActions(["MEDICAL_REVIEWER"])).toContain("approve_medical");
  });
});
