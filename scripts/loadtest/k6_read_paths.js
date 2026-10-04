import http from 'k6/http';
import { check, sleep } from 'k6';

const baseUrl = __ENV.K6_BASE_URL;
if (!baseUrl) {
  throw new Error('Set K6_BASE_URL to staging origin (https://host, no trailing slash)');
}

// Endpoints below are defined in openapi/openapi.yaml. All are anonymous (no Bearer).
// Registered-only routes (/v1/sync, /v1/identity, …) are intentionally omitted.
const searchQuery = __ENV.K6_SEARCH_Q || 'mahin';
const searchLocale = __ENV.K6_SEARCH_LOCALE || 'fa';

export const options = {
  vus: Number(__ENV.K6_VUS || 10),
  duration: __ENV.K6_DURATION || '2m',
  thresholds: {
    http_req_failed: ['rate<0.01'],
    'http_req_duration{name:meta}': ['p(95)<800'],
    'http_req_duration{name:content}': ['p(95)<800'],
  },
};

export default function () {
  const metaRes = http.get(`${baseUrl}/v1/meta`, { tags: { name: 'meta' } });
  check(metaRes, { 'meta 200': (r) => r.status === 200 });

  const catalogRes = http.get(`${baseUrl}/v1/content/catalog-status`, {
    tags: { name: 'content' },
  });
  check(catalogRes, { 'catalog-status 200': (r) => r.status === 200 });

  const searchRes = http.get(
    `${baseUrl}/v1/content/search?q=${encodeURIComponent(searchQuery)}&locale=${encodeURIComponent(searchLocale)}`,
    { tags: { name: 'content' } },
  );
  check(searchRes, { 'content search 200': (r) => r.status === 200 });

  sleep(1);
}
