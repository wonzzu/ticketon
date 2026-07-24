/**
 * 파일 업로드 API.
 *
 * - POST /uploads/poster — 포스터 이미지 업로드 (SELLER), multipart/form-data
 *   → S3에 저장 후 이미지 URL(string)을 반환한다.
 *   응답은 http.js 인터셉터가 BaseResponse를 언래핑해서 data(=URL 문자열)만 반환.
 *
 * 로컬(백엔드 aws.s3.enabled=false)에서는 실제 업로드 없이
 * placeholder 경로('/images/poster-placeholder.jpg')를 돌려준다.
 */
import http from './http'

export const uploadApi = {
  // file: <input type="file">에서 얻은 File 객체
  poster: (file) => {
    const formData = new FormData()
    formData.append('file', file)   // 백엔드 @RequestParam("file")와 이름 일치

    return http.post('/uploads/poster', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
}
