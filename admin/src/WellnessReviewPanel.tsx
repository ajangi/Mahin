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
      {version.contentType === "care_schedule" && (
        <div>
          <h4>زمان‌بندی مراقبت</h4>
          <p>منطقه: {String((payload as { regionContext?: string }).regionContext ?? "—")}</p>
          <pre>{JSON.stringify((payload as { items?: unknown }).items ?? [], null, 2)}</pre>
        </div>
      )}
      {version.contentType === "checklist_template" && (
        <div>
          <h4>چک‌لیست</h4>
          <p>نوع: {String((payload as { checklistKind?: string }).checklistKind ?? "—")}</p>
          <h5>بخش‌ها</h5>
          <pre>{JSON.stringify((payload as { sections?: unknown }).sections ?? [], null, 2)}</pre>
          <h5>ردیف‌ها</h5>
          <pre>{JSON.stringify((payload as { items?: unknown }).items ?? [], null, 2)}</pre>
        </div>
      )}
    </section>
  );
}
