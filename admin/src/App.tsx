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
import { visibleWorkflowActions } from "./contentEditorUtils";
import { WellnessContentEditor } from "./WellnessContentEditor";
import { WellnessReviewPanel } from "./WellnessReviewPanel";

type Section = "foundation" | "content" | "media" | "freshness";

export async function publishFixtureDraft(auth: AdminAuth) {
  const slug = `fixture-${Date.now()}`;
  return createFixtureDocument(auth.accessToken, slug);
}

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
      const created = await publishFixtureDraft(auth);
      setStatusMessage(`پیش‌نویس نمونه ایجاد شد — شناسهٔ نسخه: ${String(created.versionId)}`);
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

export function ContentPanel({
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
  const [reviewVersionId, setReviewVersionId] = useState("");
  const [reviewSnapshot, setReviewSnapshot] = useState<Record<string, unknown> | null>(null);
  const [workflowError, setWorkflowError] = useState<string | null>(null);

  const onLoadReview = useCallback(async () => {
    if (!auth || !reviewVersionId.trim()) return;
    try {
      const data = await fetchVersion(auth.accessToken, reviewVersionId.trim());
      setReviewSnapshot(data as Record<string, unknown>);
      setWorkflowError(null);
    } catch {
      onStatus("بارگذاری نسخه برای بازبینی ناموفق بود.");
    }
  }, [auth, onStatus, reviewVersionId]);

  const onWorkflowAction = useCallback(
    async (action: string) => {
      if (!auth || !reviewVersionId.trim()) return;
      setWorkflowError(null);
      try {
        const updated = await workflow(auth.accessToken, reviewVersionId.trim(), action, {});
        setReviewSnapshot(updated as Record<string, unknown>);
        onStatus(`گردش کار: ${action}`);
      } catch (err) {
        const code = err instanceof Error ? err.message : "workflow_failed";
        setWorkflowError(code);
      }
    },
    [auth, onStatus, reviewVersionId],
  );

  return (
    <section className="card" dir="rtl">
      <h2>CMS — M17a</h2>
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
          <WellnessContentEditor
            kind={editorKind}
            onSubmit={async (body) => {
              const created = await createWellnessDocument(auth.accessToken, body as never);
              setReviewVersionId(String(created.versionId));
              setReviewSnapshot(created as Record<string, unknown>);
              onStatus("پیش‌نویس ذخیره شد.");
            }}
          />
          <h3>بازبینی</h3>
          <label>
            شناسهٔ نسخه
            <input
              value={reviewVersionId}
              onChange={(e) => setReviewVersionId(e.target.value)}
              placeholder="version UUID"
            />
          </label>
          <button type="button" onClick={onLoadReview}>بارگذاری نسخه</button>
          <div className="token-row">
            {visibleWorkflowActions(auth.roles).map((action) => (
              <button key={action} type="button" onClick={() => onWorkflowAction(action)}>
                {action}
              </button>
            ))}
          </div>
          {workflowError && <p role="alert">{workflowError}</p>}
          <WellnessReviewPanel version={reviewSnapshot} />
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
