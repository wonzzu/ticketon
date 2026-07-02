// k6 부하 테스트 — findMine (내 예매 목록) 응답시간 측정
// 목적: reservation (member_id, created_at) 인덱스 도입 전/후 p95 비교
//
// 실행(도커):
//   docker run --rm -i -v C:\Users\wonzz\Desktop\ticketing\k6:/scripts grafana/k6 run /scripts/findmine.js
//
// 주의: 앱이 8080에 떠 있어야 하고, 컨테이너에서 호스트는 host.docker.internal 로 부른다.

import http from 'k6/http';
import { check } from 'k6';

const BASE = 'http://host.docker.internal:8080';

// 부하 프로파일: 가상유저(VU) 10명이 30초 동안 계속 요청
export const options = {
  vus: 10,
  duration: '3m',
  // 각 요청의 95%가 이 시간 안에 끝나야 통과(참고용 임계값)
  thresholds: {
    http_req_duration: ['p(95)<500'],
  },
};

// setup(): 부하 시작 전 딱 1번 실행 → 로그인해서 토큰만 확보(Rate Limit 회피)
// 헤비유저(perf0@test.com, 예매 512건)로 로그인해야 정렬 부하가 크게 걸린다.
export function setup() {
  const res = http.post(
    `${BASE}/auth/login`,
    JSON.stringify({ email: 'perf0@test.com', password: 'test1234' }),
    { headers: { 'Content-Type': 'application/json' } }
  );
  check(res, { '로그인 200': (r) => r.status === 200 });

  // 응답이 BaseResponse 래퍼라 실제 토큰은 data.accessToken 에 있다
  const token = res.json('data.accessToken');
  return { token };
}

// default(): VU들이 30초 동안 반복 실행하는 본체 → 여기서 findMine 호출
export default function (data) {
  const res = http.get(`${BASE}/reservations/me?size=10`, {
    headers: { Authorization: `Bearer ${data.token}` },
  });
  check(res, { 'findMine 200': (r) => r.status === 200 });
}
