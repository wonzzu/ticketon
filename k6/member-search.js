// k6 부하 테스트 — 관리자 회원 검색(상태별) 응답시간 측정
// 목적: member (member_status, created_at) 인덱스 도입 전/후 p95 비교
//
// 실행(도커):
//   docker run --rm -i -v C:\Users\wonzz\Desktop\ticketing\k6:/scripts grafana/k6 run /scripts/member-search.js
//
// 주의: 앱이 8080에 떠 있어야 하고, 컨테이너에서 호스트는 host.docker.internal 로 부른다.

import http from 'k6/http';
import { check } from 'k6';

const BASE = 'http://host.docker.internal:8080';

export const options = {
  vus: 10,
  duration: '3m',
  thresholds: {
    http_req_duration: ['p(95)<500'],
  },
};

// setup(): 관리자(admin@test.com)로 1번만 로그인 → 토큰 확보(Rate Limit 회피)
export function setup() {
  const res = http.post(
    `${BASE}/auth/login`,
    JSON.stringify({ email: 'admin@test.com', password: 'test1234' }),
    { headers: { 'Content-Type': 'application/json' } }
  );
  check(res, { '로그인 200': (r) => r.status === 200 });

  const token = res.json('data.accessToken');
  return { token };
}

// default(): SUSPENDED(정지) 회원 검색 — 인덱스 없으면 30만 풀스캔, 있으면 range
export default function (data) {
  const res = http.get(`${BASE}/admin/members?memberStatus=SUSPENDED&size=20`, {
    headers: { Authorization: `Bearer ${data.token}` },
  });
  check(res, { 'member 검색 200': (r) => r.status === 200 });
}
