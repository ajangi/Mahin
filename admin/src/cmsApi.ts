const apiBase = import.meta.env.VITE_API_BASE ?? "http://localhost:8080";

export type AdminAuth = {
  accessToken: string;
  roles: string[];
};

export async function adminLogin(email: string, password: string): Promise<AdminAuth> {
  const response = await fetch(`${apiBase}/v1/admin/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password }),
  });
  if (!response.ok) {
    throw new Error("login_failed");
  }
  const json = await response.json();
  return { accessToken: json.accessToken, roles: json.roles ?? [] };
}

export async function fetchFreshness(token: string) {
  const response = await fetch(`${apiBase}/v1/admin/content/freshness`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!response.ok) {
    throw new Error("freshness_failed");
  }
  return response.json();
}

export async function createFixtureDocument(token: string, slug: string) {
  const response = await fetch(`${apiBase}/v1/admin/content/documents`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      slug,
      locale: "fa-IR",
      title: "نمونه غیرپزشکی",
      summary: "فقط برای آزمون گردش کار CMS.",
      bodyRichtext: "این متن پزشکی نیست.",
      contentType: "article",
      lifeStage: "cycle",
      medicalRiskLevel: "general_education",
      tags: ["fixture"],
    }),
  });
  if (!response.ok) {
    throw new Error("create_failed");
  }
  return response.json();
}

export async function workflow(
  token: string,
  versionId: string,
  action: string,
  body: Record<string, string> = {},
) {
  const response = await fetch(`${apiBase}/v1/admin/content/versions/${versionId}/workflow/${action}`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  });
  if (!response.ok) {
    throw new Error(`workflow_${action}_failed`);
  }
  return response.json();
}
