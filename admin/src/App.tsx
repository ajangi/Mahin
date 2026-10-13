import { useCallback, useState } from "react";
import {
  adminLogin,
  createFixtureDocument,
  createWellnessDocument,
  fetchFreshness,
  fetchVersion,
  workflow,
  type AdminAuth,
  type WellnessContentKind,
} from "./cmsApi";
import {
  buildCollectionPayload,
  buildDailyTipPayload,
  buildProgramPayload,
  COLLECTION_CATEGORIES,
  PROGRAM_TYPES,
  validateWellnessDraft,
  WORKFLOW_REVIEW_ACTIONS,
} from "./contentEditorUtils";

type Section = "foundation" | "content" | "media" | "freshness";

export function App() {
  const [section, setSection] = useState<Section>("content");
  const [auth, setAuth] = useState<AdminAuth | null>(null);
  const [loginEmail, setLoginEmail] = useState("");
  const [loginPassword, setLoginPassword] = useState("");
  const [statusMessage, setStatusMessage] = useState<string | null>(null);
  const [freshness, setFreshness] = useState<unknown>(null);

  const onLogin = useCallback(async () => {
    setStatusMessage(null);
    try {
      const session = await adminLogin(loginEmail, loginPassword);
      setAuth(session);
      setStatusMessage("ورود موفق");
    } catch {
      setStatusMessage("ورود ناموفق — ایمیل یا رمز اشتباه است.");
    }
  }, [loginEmail, loginPassword]);

  const onPublishFixture = useCallback(async () => {
    if (!auth) return;
    setStatusMessage(null);
    try {
      const slug = `fixture-${Date.now()}`;
      const created = await createFixtureDocument(auth.accessToken, slug);
      const versionId = created.versionId as string;
      await workflow(auth.accessToken, versionId, "submit_medical_review");
      await workflow(auth.accessToken, versionId, "approve_medical", {
        clinicalReviewer: "reviewer@mahin.test",
      });
      await workflow(auth.accessToken, versionId, "approve_editorial");
      await workflow(auth.accessToken, versionId, "publish");
      setStatusMessage(`منتشر شد: ${slug}`);
    } catch {
      setStatusMessage("گردش کار ناموفق — نقش‌ها یا مراحل را بررسی کنید.");
    }
  }, [auth]);

  const onLoadFreshness = useCallback(async () => {
    if (!auth) return;
    try {
      const data = await fetchFreshness(auth.accessToken);
      setFreshness(data);
    } catch {
      setStatusMessage("بارگذاری تازگی محتوا ناموفق بود.");
    }
  }, [auth]);

  return (
    <div className="shell">
      <h1 className="brand">ماهین</h1>
      <p className="muted">پنل CMS — M17a. نمونه‌ها با برچسب placeholder؛ بدون راهنمای بالینی واقعی.</p>
      <nav aria-label="بخش‌های مدیریت">
        <NavButton current={section} id="foundation" onSelect={setSection}>
          زیرساخت
        </NavButton>
        <NavButton current={section} id="content" onSelect={setSection}>
          محتوا
        </NavButton>
        <NavButton current={section} id="freshness" onSelect={setSection}>
          تازگی
        </NavButton>
        <NavButton current={section} id="media" onSelect={setSection}>
          رسانه
        </NavButton>
      </nav>
      {statusMessage && <p role="status">{statusMessage}</p>}
      {section === "foundation" && <FoundationPanel />}
      {section === "content" && (
        <ContentPanel
          auth={auth}
          loginEmail={loginEmail}
          loginPassword={loginPassword}
          onEmailChange={setLoginEmail}
          onPasswordChange={setLoginPassword}
          onLogin={onLogin}
          onPublishFixture={onPublishFixture}
          onStatus={setStatusMessage}
        />
      )}
      {section === "freshness" && (
        <FreshnessPanel auth={auth} data={freshness} onRefresh={onLoadFreshness} />
      )}
      {section === "media" && <MediaPanel />}
    </div>
  );
}

function NavButton({
  current,
  id,
  onSelect,
  children,
}: {
  current: Section;
  id: Section;
  onSelect: (id: Section) => void;
  children: string;
}) {
  return (
    <button
      type="button"
      aria-current={current === id ? "page" : undefined}
      onClick={() => onSelect(id)}
    >
      {children}
    </button>
  );
}

function FoundationPanel() {
  return (
    <section className="card">
      <h2>توکن‌های ماهین</h2>
      <p className="muted">رنگ‌ها از design/tokens.json می‌آیند.</p>
      <TokenRow name="brand.primary" cssVar="--brand-primary" />
      <TokenRow name="surface.background" cssVar="--surface-background" />
    </section>
  );
}

function ContentPanel({
  auth,
  loginEmail,
  loginPassword,
  onEmailChange,
  onPasswordChange,
  onLogin,
  onPublishFixture,
  onStatus,
}: {
  auth: AdminAuth | null;
  loginEmail: string;
  loginPassword: string;
  onEmailChange: (v: string) => void;
  onPasswordChange: (v: string) => void;
  onLogin: () => void;
  onPublishFixture: () => void;
  onStatus: (message: string | null) => void;
}) {
  const [editorKind, setEditorKind] = useState<WellnessContentKind>("daily_tip");
  const [title, setTitle] = useState("نکتهٔ نمونه");
  const [targetMode, setTargetMode] = useState("cycle");
  const [targetPhase, setTargetPhase] = useState("follicular");
  const [programType, setProgramType] = useState<string>(PROGRAM_TYPES[0]);
  const [collectionCategory, setCollectionCategory] = useState<string>(COLLECTION_CATEGORIES[0]);
  const [reviewVersionId, setReviewVersionId] = useState("");
  const [reviewSnapshot, setReviewSnapshot] = useState<unknown>(null);

  const onCreateWellnessDraft = useCallback(async () => {
    if (!auth) return;
    const validationError = validateWellnessDraft(editorKind, title);
    if (validationError) {
      onStatus(validationError);
      return;
    }
    onStatus(null);
    try {
      const slug = `${editorKind}-${Date.now()}`;
      let typePayload: Record<string, unknown>;
      if (editorKind === "daily_tip") {
        typePayload = buildDailyTipPayload(targetMode, targetPhase);
      } else if (editorKind === "program") {
        typePayload = buildProgramPayload(programType);
      } else {
        typePayload = buildCollectionPayload(collectionCategory, []);
      }
      const created = await createWellnessDocument(auth.accessToken, {
        slug,
        title,
        summary: "نمونه — غیرپزشکی",
        bodyRichtext: editorKind === "daily_tip" ? "متن نمونه برای آزمون زیرساخت." : undefined,
        contentType: editorKind,
        typePayload,
        medicalRiskLevel: editorKind === "program" ? "general_education" : "none",
      });
      setReviewVersionId(created.versionId as string);
      onStatus(`پیش‌نویس ${editorKind} ذخیره شد.`);
    } catch {
      onStatus("ایجاد پیش‌نویس ناموفق بود.");
    }
  }, [
    auth,
    collectionCategory,
    editorKind,
    onStatus,
    programType,
    targetMode,
    targetPhase,
    title,
  ]);

  const onLoadReview = useCallback(async () => {
    if (!auth || !reviewVersionId.trim()) return;
    try {
      const data = await fetchVersion(auth.accessToken, reviewVersionId.trim());
      setReviewSnapshot(data);
    } catch {
      onStatus("بارگذاری نسخه برای بازبینی ناموفق بود.");
    }
  }, [auth, onStatus, reviewVersionId]);

  const onWorkflowAction = useCallback(
    async (action: string) => {
      if (!auth || !reviewVersionId.trim()) return;
      try {
        const extra: Record<string, string> =
          action === "approve_medical" ? { clinicalReviewer: "reviewer@mahin.test" } : {};
        const updated = await workflow(auth.accessToken, reviewVersionId.trim(), action, extra);
        setReviewSnapshot(updated);
        onStatus(`گردش کار: ${action}`);
      } catch {
        onStatus(`اجرای ${action} ناموفق بود.`);
      }
    },
    [auth, onStatus, reviewVersionId],
  );

  return (
    <section className="card" dir="rtl">
      <h2>CMS — M17a</h2>
      <p>ویرایشگرهای نکتهٔ روزانه، برنامهٔ هدایت‌شده و مجموعهٔ Learn؛ بازبینی از طریق گردش کار موجود.</p>
      {!auth ? (
        <form
          onSubmit={(event) => {
            event.preventDefault();
            onLogin();
          }}
        >
          <label>
            ایمیل
            <input value={loginEmail} onChange={(e) => onEmailChange(e.target.value)} />
          </label>
          <label>
            رمز
            <input
              type="password"
              value={loginPassword}
              onChange={(e) => onPasswordChange(e.target.value)}
            />
          </label>
          <button type="submit">ورود</button>
        </form>
      ) : (
        <>
          <p>نقش‌ها: {auth.roles.join(", ")}</p>
          <button type="button" onClick={onPublishFixture}>
            انتشار نمونهٔ مقاله (غیرپزشکی)
          </button>

          <h3>ویرایشگر نوع محتوا</h3>
          <div className="token-row">
            {(["daily_tip", "program", "collection"] as WellnessContentKind[]).map((kind) => (
              <button
                key={kind}
                type="button"
                aria-pressed={editorKind === kind}
                onClick={() => setEditorKind(kind)}
              >
                {kind}
              </button>
            ))}
          </div>
          <label>
            عنوان
            <input value={title} onChange={(e) => setTitle(e.target.value)} />
          </label>
          {editorKind === "daily_tip" && (
            <>
              <label>
                حالت
                <select value={targetMode} onChange={(e) => setTargetMode(e.target.value)}>
                  <option value="cycle">cycle</option>
                  <option value="ttc">ttc</option>
                  <option value="pregnancy">pregnancy</option>
                </select>
              </label>
              <label>
                فاز چرخه
                <select value={targetPhase} onChange={(e) => setTargetPhase(e.target.value)}>
                  <option value="menstrual">menstrual</option>
                  <option value="follicular">follicular</option>
                  <option value="ovulation">ovulation</option>
                  <option value="luteal">luteal</option>
                </select>
              </label>
            </>
          )}
          {editorKind === "program" && (
            <label>
              نوع برنامه
              <select value={programType} onChange={(e) => setProgramType(e.target.value)}>
                {PROGRAM_TYPES.map((t) => (
                  <option key={t} value={t}>{t}</option>
                ))}
              </select>
            </label>
          )}
          {editorKind === "collection" && (
            <label>
              دستهٔ Learn
              <select
                value={collectionCategory}
                onChange={(e) => setCollectionCategory(e.target.value)}
              >
                {COLLECTION_CATEGORIES.map((c) => (
                  <option key={c} value={c}>{c}</option>
                ))}
              </select>
            </label>
          )}
          <button type="button" onClick={onCreateWellnessDraft}>
            ذخیرهٔ پیش‌نویس نمونه (placeholder)
          </button>

          <h3>بازبینی و گردش کار</h3>
          <label>
            شناسهٔ نسخه
            <input
              value={reviewVersionId}
              onChange={(e) => setReviewVersionId(e.target.value)}
              placeholder="version UUID"
            />
          </label>
          <button type="button" onClick={onLoadReview}>
            بارگذاری نسخه
          </button>
          <div className="token-row">
            {WORKFLOW_REVIEW_ACTIONS.map((action) => (
              <button key={action} type="button" onClick={() => onWorkflowAction(action)}>
                {action}
              </button>
            ))}
          </div>
          <pre>{reviewSnapshot ? JSON.stringify(reviewSnapshot, null, 2) : "—"}</pre>
        </>
      )}
    </section>
  );
}

function FreshnessPanel({
  auth,
  data,
  onRefresh,
}: {
  auth: AdminAuth | null;
  data: unknown;
  onRefresh: () => void;
}) {
  return (
    <section className="card">
      <h2>تازگی محتوا</h2>
      <p className="muted">بازبینی معوق، منابع قدیمی، و برداشت‌های اخیر.</p>
      <button type="button" disabled={!auth} onClick={onRefresh}>
        بارگذاری داشبورد
      </button>
      <pre>{data ? JSON.stringify(data, null, 2) : "—"}</pre>
    </section>
  );
}

function MediaPanel() {
  return (
    <section className="card">
      <h2>رسانه</h2>
      <p>مسیر: CMS → ذخیرهٔ شیء → CDN → کش اندروید (M17b).</p>
    </section>
  );
}

function TokenRow({ name, cssVar }: { name: string; cssVar: string }) {
  return (
    <div className="token-row">
      <span className="swatch" style={{ background: `var(${cssVar})` }} />
      <code>{name}</code>
    </div>
  );
}
