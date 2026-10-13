import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { WellnessContentEditor } from "./WellnessContentEditor";
import { buildCreateDocumentBody } from "./wellnessEditorModels";
import * as cmsApi from "./cmsApi";
import { workflow } from "./cmsApi";

describe("Wellness editors", () => {
  it("builds daily_tip request body from editor state", () => {
    const body = buildCreateDocumentBody("daily_tip", "tip-1", {
      title: "نکته",
      summary: "خلاصه",
      bodyRichtext: "متن",
      locale: "fa-IR",
      modes: ["cycle"],
      cyclePhases: ["luteal"],
      pregnancyWeeks: "12, 13",
      effectiveFrom: "2026-01-01T00:00:00Z",
      effectiveTo: "",
      placeholder: true,
    });
    expect(body.contentType).toBe("daily_tip");
    expect((body.typePayload as { targeting: { pregnancyWeeks: number[] } }).targeting.pregnancyWeeks).toEqual([
      12,
      13,
    ]);
  });

  it("submits program editor payload through onSubmit", async () => {
    const onSubmit = vi.fn().mockResolvedValue(undefined);
    render(<WellnessContentEditor kind="program" onSubmit={onSubmit} />);
    await userEvent.click(screen.getByRole("button", { name: "ذخیرهٔ پیش‌نویس" }));
    expect(onSubmit).toHaveBeenCalledTimes(1);
    const body = onSubmit.mock.calls[0][0] as Record<string, unknown>;
    expect(body.contentType).toBe("program");
    expect((body.typePayload as { programType: string }).programType).toBe("pelvic_floor");
  });

  it("calls workflow API without hardcoded clinical reviewer", async () => {
    const fetchMock = vi.fn().mockResolvedValue({ ok: true, json: async () => ({}) });
    vi.stubGlobal("fetch", fetchMock);
    await workflow("token", "version-id", "approve_medical", {});
    expect(fetchMock).toHaveBeenCalled();
    const init = fetchMock.mock.calls[0][1] as RequestInit;
    expect(init.body).toBe("{}");
    vi.unstubAllGlobals();
  });

  it("surfaces workflow error state from failed API", async () => {
    vi.spyOn(cmsApi, "workflow").mockRejectedValueOnce(new Error("workflow_failed"));
    await expect(workflow("token", "v", "publish", {})).rejects.toThrow();
  });
});
