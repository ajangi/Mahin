import http from 'k6/http';
import { check, sleep } from 'k6';

const baseUrl = __ENV.K6_BASE_URL;
if (!baseUrl) {
  throw new Error('Set K6_BASE_URL to staging origin (https://host, no trailing slash)');
}

export const options = {
  vus: Number(__ENV.K6_VUS || 10),
  duration: __ENV.K6_DURATION || '2m',
  thresholds: {
    http_req_failed: ['rate<0.01'],
    'http_req_duration{path:/v1/meta}': ['p(95)<800'],
  },
};

export default function () {
  const meta = http.get(`${baseUrl}/v1/meta`);
  check(meta, { 'meta 200': (r) => r.status === 200 });

  const content = http.get(`${baseUrl}/v1/content/articles?page=0&size=5`);
  check(content, { 'content 200': (r) => r.status === 200 });

  sleep(1);
}
