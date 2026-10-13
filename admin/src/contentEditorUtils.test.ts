import { describe, expect, it } from "vitest";
import {
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

  it("lists governed workflow actions for review screens", () => {
    expect(WORKFLOW_REVIEW_ACTIONS).toContain("approve_medical");
    expect(WORKFLOW_REVIEW_ACTIONS).toContain("publish");
  });

  it("exposes approve_medical only to medical reviewers", () => {
    expect(visibleWorkflowActions(["EDITOR"])).not.toContain("approve_medical");
    expect(visibleWorkflowActions(["MEDICAL_REVIEWER"])).toContain("approve_medical");
  });
});
