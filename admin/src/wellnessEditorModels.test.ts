import { describe, expect, it } from "vitest";
import { updateProgramStep, type ProgramSessionDraft } from "./wellnessEditorModels";

describe("wellnessEditorModels", () => {
  it("updates program steps immutably", () => {
    const sessions: ProgramSessionDraft[] = [
      {
        id: "s1",
        title: "جلسه",
        order: 1,
        steps: [
          {
            instruction: "قبل",
            durationSeconds: "10",
            reps: "",
            restSeconds: "",
            mediaAssetId: "",
          },
        ],
      },
    ];
    const next = updateProgramStep(sessions, 0, 0, { instruction: "بعد" });
    expect(sessions[0].steps[0].instruction).toBe("قبل");
    expect(next[0].steps[0].instruction).toBe("بعد");
    expect(next).not.toBe(sessions);
    expect(next[0].steps).not.toBe(sessions[0].steps);
  });
});
