type ReviewVersion = {
  versionId?: string;
  documentId?: string;
  title?: string;
  contentType?: string;
  status?: string;
  typePayload?: Record<string, unknown>;
  effectiveFrom?: string | null;
  effectiveTo?: string | null;
  clinicalReviewer?: string | null;
};

export function WellnessReviewPanel({ version }: { version: ReviewVersion | null }) {
  if (!version) {
    return <p>نسخه‌ای برای بازبینی بارگذاری نشده است.</p>;
  }
  const payload = version.typePayload ?? {};
  return (
    <section dir="rtl" aria-label="بازبینی محتوا">
      <h3>{version.title}</h3>
      <p>نوع: {version.contentType}</p>
      <p>وضعیت: {version.status}</p>
      <p>بازبین بالینی: {version.clinicalReviewer ?? "—"}</p>
      <p>از: {version.effectiveFrom ?? "—"}</p>
      <p>تا: {version.effectiveTo ?? "—"}</p>
      {version.contentType === "daily_tip" && (
        <div>
          <h4>هدف‌گیری</h4>
          <pre>{JSON.stringify((payload as { targeting?: unknown }).targeting ?? {}, null, 2)}</pre>
        </div>
      )}
      {version.contentType === "program" && (
        <div>
          <h4>ایمنی</h4>
          <p>{String((payload as { safetyNotice?: string }).safetyNotice ?? "")}</p>
          <p>{String((payload as { contraindications?: string }).contraindications ?? "")}</p>
          <h4>جلسات</h4>
          <pre>{JSON.stringify((payload as { sessions?: unknown }).sessions ?? [], null, 2)}</pre>
        </div>
      )}
      {version.contentType === "collection" && (
        <div>
          <h4>مقالات</h4>
          <pre>
            {JSON.stringify((payload as { articleDocumentIds?: unknown }).articleDocumentIds ?? [], null, 2)}
          </pre>
        </div>
      )}
    </section>
  );
}
