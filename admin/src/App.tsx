import { useState } from "react";

type Section = "foundation" | "content" | "media";

export function App() {
  const [section, setSection] = useState<Section>("foundation");
  return (
    <div className="shell">
      <h1 className="brand">ماهین</h1>
      <p className="muted">پنل مدیریت — اسکلت مهندسی M0. محتوای پزشکی در این نسخه وجود ندارد.</p>
      <nav aria-label="بخش‌های مدیریت">
        <NavButton current={section} id="foundation" onSelect={setSection}>
          زیرساخت
        </NavButton>
        <NavButton current={section} id="content" onSelect={setSection}>
          محتوا
        </NavButton>
        <NavButton current={section} id="media" onSelect={setSection}>
          رسانه
        </NavButton>
      </nav>
      {section === "foundation" && <FoundationPanel />}
      {section === "content" && <ContentPanel />}
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
      <p className="muted">رنگ‌ها از design/tokens.json می‌آیند و نباید در ویژگی‌ها تکرار شوند.</p>
      <TokenRow name="brand.primary" cssVar="--brand-primary" />
      <TokenRow name="surface.background" cssVar="--surface-background" />
      <TokenRow name="health.period" cssVar="--health-period" />
      <TokenRow name="health.fertility" cssVar="--health-fertility" />
      <TokenRow name="health.pregnancy" cssVar="--health-pregnancy" />
    </section>
  );
}

function ContentPanel() {
  return (
    <section className="card">
      <h2>CMS</h2>
      <p>
        گردش کار محتوا در M6 پیاده می‌شود: پیش‌نویس، مرور پزشکی، انتشار و بازنشستگی. این صفحه
        بدنهٔ پزشکی ندارد.
      </p>
    </section>
  );
}

function MediaPanel() {
  return (
    <section className="card">
      <h2>رسانه</h2>
      <p>
        مسیر تحویل: CMS ← ذخیرهٔ شیء ← CDN ← لایهٔ تصویر اندروید. آدرس CDN تولیدی در UI قفل
        نمی‌شود. تصویر پزشکی نهایی در M0 تولید نمی‌شود.
      </p>
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
