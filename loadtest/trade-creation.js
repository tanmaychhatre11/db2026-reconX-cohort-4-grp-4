import http from 'k6/http';
import { check, fail, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

const tradePostLatency = new Trend('trade_post_latency_ms');
const tradePostErrors = new Rate('trade_post_errors');

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080/api';
const LOGIN_EMAIL = __ENV.LOGIN_EMAIL || 'trader@db.com';
const LOGIN_PASSWORD = __ENV.LOGIN_PASSWORD || 'trader123';

export const options = {
  scenarios: {
    constant_load: {
      executor: 'constant-vus',
      vus: 200,
      duration: '2m',
      gracefulStop: '10s',
    },
  },
  thresholds: {
    trade_post_latency_ms: ['p(95)<800', 'p(99)<2000'],
    trade_post_errors: ['rate<0.02'],
    http_req_failed: ['rate<0.02'],
  },
};

export function setup() {
  const response = http.post(
    `${BASE_URL}/v1/auth/login`,
    JSON.stringify({
      email: LOGIN_EMAIL,
      password: LOGIN_PASSWORD,
    }),
    {
      headers: {
        'Content-Type': 'application/json',
      },
    },
  );

  const ok = check(response, {
    'login returns 200': (res) => res.status === 200,
    'login returns token': (res) => !!res.json('token'),
  });

  if (!ok) {
    fail(`Login failed during setup with status ${response.status}`);
  }

  return {
    token: response.json('token'),
  };
}

export default function runTradeCreationLoad(data) {
  const tradeRef = `K6-${String(__VU).padStart(3, '0')}${String(__ITER % 10).padStart(1, '0')}-${new Date()
    .toISOString()
    .slice(0, 10)
    .replace(/-/g, '')}-${String(((__ITER % 1000) * 10) + (__VU % 10)).padStart(4, '0')}`;

  const payload = JSON.stringify({
    tradeRef,
    instrumentId: 1,
    counterpartyId: 1,
    assetClass: 'EQUITY',
    side: __ITER % 2 === 0 ? 'BUY' : 'SELL',
    quantity: 100 + (__VU % 25),
    price: 245.5 + ((__ITER % 20) * 0.01),
    tradeDate: '2026-08-04',
  });

  const response = http.post(`${BASE_URL}/v1/trades`, payload, {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${data.token}`,
    },
  });

  tradePostLatency.add(response.timings.duration);

  const ok = check(response, {
    'trade create returns 201': (res) => res.status === 201,
    'trade create returns id': (res) => !!res.json('id'),
  });

  tradePostErrors.add(!ok);

  sleep(0.5);
}
