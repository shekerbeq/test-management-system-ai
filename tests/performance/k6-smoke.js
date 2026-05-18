import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 20,
  duration: '30s',
  thresholds: {
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.05'],
  },
};

export default function () {
  const response = http.get(`${__ENV.BASE_URL || 'http://localhost:8080'}/actuator/health`);
  check(response, {
    'health is reachable': (res) => res.status === 200 || res.status === 401,
  });
  sleep(1);
}
