import { useCallback, useState } from "react";
import {
  adminLogin,
  createFixtureDocument,
  fetchFreshness,
  workflow,
  type AdminAuth,
} from "./cmsApi";

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
      <p className="muted">پنل CMS — M6. بدون متن پزشکی ساختگی در رابط.</p>
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
}: {
  auth: AdminAuth | null;
  loginEmail: string;
  loginPassword: string;
  onEmailChange: (v: string) => void;
  onPasswordChange: (v: string) => void;
  onLogin: () => void;
  onPublishFixture: () => void;
}) {
  return (
    <section className="card">
      <h2>CMS</h2>
      <p>ورود پرسنل، انتشار نمونهٔ غیرپزشکی، و بازنشستگی از طریق API.</p>
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
            انتشار نمونه (غیرپزشکی)
          </button>
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
      <p>مسیر: CMS → ذخیرهٔ شیء → CDN → کش اندروید.</p>
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
