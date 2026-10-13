import { render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi, beforeEach, afterEach } from "vitest";
import { App, ContentPanel } from "./App";
import { WellnessContentEditor } from "./WellnessContentEditor";
import { WellnessReviewPanel } from "./WellnessReviewPanel";
import * as cmsApi from "./cmsApi";

const articleId = "11111111-1111-1111-1111-111111111111";

const contentPanelBaseProps = {
  loginEmail: "",
  loginPassword: "",
  onEmailChange: () => {},
  onPasswordChange: () => {},
  onLogin: () => {},
  onPublishFixture: () => {},
  onStatus: () => {},
};

describe("Wellness editors", () => {
  it("daily_tip editor submits exact payload after typing fields", async () => {
    const onSubmit = vi.fn().mockResolvedValue(undefined);
    render(<WellnessContentEditor kind="daily_tip" onSubmit={onSubmit} />);
    const section = screen.getByLabelText("ویرایشگر daily_tip");
    expect(section).toHaveAttribute("dir", "rtl");

    await userEvent.clear(screen.getByLabelText("عنوان"));
    await userEvent.type(screen.getByLabelText("عنوان"), "نکتهٔ آزمون");
    await userEvent.clear(screen.getByLabelText("متن نکته"));
    await userEvent.type(screen.getByLabelText("متن نکته"), "متن فارسی");
    await userEvent.clear(screen.getByLabelText("pregnancyWeeks"));
    await userEvent.type(screen.getByLabelText("pregnancyWeeks"), "12, 14");
    await userEvent.clear(screen.getByLabelText("effectiveFrom"));
    await userEvent.type(screen.getByLabelText("effectiveFrom"), "2026-06-01T00:00:00Z");

    await userEvent.click(screen.getByRole("button", { name: "ذخیرهٔ پیش‌نویس" }));
    expect(onSubmit).toHaveBeenCalledTimes(1);
    const body = onSubmit.mock.calls[0][0] as Record<string, unknown>;
    expect(body).toMatchObject({
      contentType: "daily_tip",
      title: "نکتهٔ آزمون",
      bodyRichtext: "متن فارسی",
      effectiveFrom: "2026-06-01T00:00:00Z",
    });
    const payload = body.typePayload as {
      targeting: { pregnancyWeeks: number[] };
    };
    expect(payload.targeting.pregnancyWeeks).toEqual([12, 14]);
  });

  it("program editor submits sessions, safety and edited step", async () => {
    const onSubmit = vi.fn().mockResolvedValue(undefined);
    render(<WellnessContentEditor kind="program" onSubmit={onSubmit} />);

    await userEvent.clear(screen.getByLabelText("safetyNotice"));
    await userEvent.type(screen.getByLabelText("safetyNotice"), "هشدار ایمنی");
    await userEvent.clear(screen.getByLabelText("contraindications"));
    await userEvent.type(screen.getByLabelText("contraindications"), "منع مصرف");
    await userEvent.clear(screen.getByLabelText("instruction"));
    await userEvent.type(screen.getByLabelText("instruction"), "قدم ویرایش‌شده");
    await userEvent.clear(screen.getByLabelText("durationSeconds"));
    await userEvent.type(screen.getByLabelText("durationSeconds"), "45");

    await userEvent.click(screen.getByRole("button", { name: "ذخیرهٔ پیش‌نویس" }));
    const body = onSubmit.mock.calls[0][0] as Record<string, unknown>;
    const payload = body.typePayload as {
      safetyNotice: string;
      contraindications: string;
      sessions: { steps: { instruction: string; durationSeconds?: number }[] }[];
    };
    expect(payload.safetyNotice).toBe("هشدار ایمنی");
    expect(payload.contraindications).toBe("منع مصرف");
    expect(payload.sessions[0].steps[0]).toMatchObject({
      instruction: "قدم ویرایش‌شده",
      durationSeconds: 45,
    });
  });

  it("collection editor submits category, window and article ids", async () => {
    const onSubmit = vi.fn().mockResolvedValue(undefined);
    render(<WellnessContentEditor kind="collection" onSubmit={onSubmit} />);

    await userEvent.selectOptions(screen.getByLabelText("category"), "pregnancy");
    await userEvent.clear(screen.getByLabelText("articleDocumentIds"));
    await userEvent.type(screen.getByLabelText("articleDocumentIds"), articleId);
    await userEvent.clear(screen.getByLabelText("effectiveTo"));
    await userEvent.type(screen.getByLabelText("effectiveTo"), "2026-12-31T23:59:59Z");

    await userEvent.click(screen.getByRole("button", { name: "ذخیرهٔ پیش‌نویس" }));
    const body = onSubmit.mock.calls[0][0] as Record<string, unknown>;
    expect(body.effectiveTo).toBe("2026-12-31T23:59:59Z");
    const payload = body.typePayload as {
      category: string;
      articleDocumentIds: string[];
    };
    expect(payload.category).toBe("pregnancy");
    expect(payload.articleDocumentIds).toEqual([articleId]);
  });

  it("renders structured WellnessReviewPanel fields", () => {
    render(
      <WellnessReviewPanel
        version={{
          title: "برنامه",
          contentType: "program",
          status: "draft",
          typePayload: {
            safetyNotice: "ایمنی",
            contraindications: "منع",
            sessions: [{ id: "s1", title: "جلسه", order: 1, steps: [{ instruction: "گام" }] }],
          },
        }}
      />,
    );
    const panel = screen.getByLabelText("بازبینی محتوا");
    expect(panel).toHaveAttribute("dir", "rtl");
    expect(within(panel).getByRole("heading", { name: "ایمنی" })).toBeInTheDocument();
    expect(within(panel).getByText("منع")).toBeInTheDocument();
    expect(within(panel).getByText(/گام/)).toBeInTheDocument();
  });
});

describe("ContentPanel workflow", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("hides approve_medical for editor-only roles and shows for medical reviewer", () => {
    const { rerender } = render(
      <ContentPanel
        {...contentPanelBaseProps}
        auth={{ accessToken: "t", roles: ["EDITOR"] }}
      />,
    );
    expect(screen.queryByRole("button", { name: "approve_medical" })).toBeNull();
    expect(screen.getByRole("button", { name: "submit_medical_review" })).toBeInTheDocument();

    rerender(
      <ContentPanel
        {...contentPanelBaseProps}
        auth={{ accessToken: "t", roles: ["MEDICAL_REVIEWER"] }}
      />,
    );
    expect(screen.getByRole("button", { name: "approve_medical" })).toBeInTheDocument();
  });

  it("runs a single workflow action with mocked fetch and surfaces errors", async () => {
    const fetchMock = vi.fn().mockResolvedValueOnce({
      ok: false,
      status: 409,
      json: async () => ({ code: "clinical_review_required" }),
    });
    vi.stubGlobal("fetch", fetchMock);

    render(
      <ContentPanel
        {...contentPanelBaseProps}
        auth={{ accessToken: "token", roles: ["EDITOR", "MEDICAL_REVIEWER"] }}
      />,
    );

    const section = screen.getByRole("heading", { name: "CMS — M17a" }).closest("section");
    expect(section).toHaveAttribute("dir", "rtl");

    await userEvent.type(screen.getByPlaceholderText("version UUID"), "version-1");
    await userEvent.click(screen.getByRole("button", { name: "publish" }));

    expect(fetchMock).toHaveBeenCalledTimes(1);
    const [url, init] = fetchMock.mock.calls[0] as [string, RequestInit];
    expect(url).toContain("/v1/admin/content/versions/version-1/workflow/publish");
    expect(init.method).toBe("POST");
    expect(init.body).toBe("{}");

    expect(await screen.findByRole("alert")).toHaveTextContent("clinical_review_required");
  });
});

describe("App fixture publish", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it("does not chain workflow steps when creating fixture draft", async () => {
    vi.spyOn(cmsApi, "adminLogin").mockResolvedValue({
      accessToken: "token",
      roles: ["EDITOR"],
    });
    const createSpy = vi.spyOn(cmsApi, "createFixtureDocument").mockResolvedValue({
      versionId: "v-fixture",
    });
    const workflowSpy = vi.spyOn(cmsApi, "workflow");
    const workflowFetchUrls: string[] = [];
    vi.stubGlobal(
      "fetch",
      vi.fn((url: string) => {
        if (String(url).includes("/workflow/")) {
          workflowFetchUrls.push(String(url));
        }
        return Promise.resolve({ ok: false, json: async () => ({}) });
      }),
    );

    render(<App />);
    await userEvent.type(screen.getByLabelText("ایمیل"), "editor@mahin.test");
    await userEvent.type(screen.getByLabelText("رمز"), "password-12-chars");
    await userEvent.click(screen.getByRole("button", { name: "ورود" }));
    await userEvent.click(screen.getByRole("button", { name: /نمونهٔ مقاله/ }));

    expect(createSpy).toHaveBeenCalledTimes(1);
    expect(workflowSpy).not.toHaveBeenCalled();
    expect(workflowFetchUrls).toHaveLength(0);
    vi.unstubAllGlobals();
  });
});
