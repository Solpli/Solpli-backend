package com.ilta.solepli.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {

  // 에러코드 예시: 샘플 에러
  SAMPLE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "예시: 샘플 에러가 발생했습니다."),

  // 인증 관련 에러
  INCORRECT_ACCOUNT(HttpStatus.BAD_REQUEST, "해당 계정이 존재하지 않습니다."),
  INCORRECT_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 틀렸습니다."),
  INCORRECT_LOGIN_TYPE(HttpStatus.BAD_REQUEST, "올바르지 않은 로그인 타입입니다."),

  // 유저 관련 에러
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자 정보가 존재하지 않습니다."),
  USER_EXISTS(HttpStatus.BAD_REQUEST, "이미 존재하는 계정입니다."),

  // OAuth 관련 에러
  GET_KAKAO_ACCESS_TOKEN_FAILED(HttpStatus.BAD_GATEWAY, "카카오 엑세스 토큰 발급에 실패했습니다."),
  GET_KAKAO_UNIQUE_ID_FAILED(HttpStatus.BAD_GATEWAY, "카카오 유저 정보 흭득에 실패했습니다."),
  GET_NAVER_ACCESS_TOKEN_FAILED(HttpStatus.BAD_GATEWAY, "네이버 엑세스 토큰 발급에 실패했습니다."),
  GET_NAVER_UNIQUE_ID_FAILED(HttpStatus.BAD_GATEWAY, "네이버 유저 정보 흭득에 실패했습니다."),
  GET_GOOGLE_ACCESS_TOKEN_FAILED(HttpStatus.BAD_GATEWAY, "구글 엑세스 토큰 발급에 실패했습니다."),
  GET_GOOGLE_UNIQUE_ID_FAILED(HttpStatus.BAD_GATEWAY, "구글 유저 정보 흭득에 실패했습니다."),

  // validation
  LOGIN_ID_REQUIRED(HttpStatus.BAD_REQUEST, "아이디는 필수입니다."),
  PASSWORD_REQUIRED(HttpStatus.BAD_REQUEST, "비밀번호는 필수입니다."),

  // 태그 관련 에러
  TAG_NOT_EXISTS(HttpStatus.NOT_FOUND, "존재하지 않는 태그입니다."),

  // 장소 관련 에러
  PLACE_NOT_EXISTS(HttpStatus.NOT_FOUND, "존재하지 않는 장소입니다."),

  // S3 관련 에러
  EMPTY_IMAGE(HttpStatus.BAD_REQUEST, "이미지 파일이 비어있습니다."),
  UNSUPPORTED_IMAGE_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "지원하지 않는 이미지 파일 확장자입니다."),
  IMAGE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "이미지 파일 크기가 너무 큽니다."),
  S3_UPLOAD_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, "S3 버킷에 파일을 업로드하는 중 에러가 발생했습니다."),
  S3_DOWNLOAD_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, "S3 버킷에서 파일을 다운로드하는 중 에러가 발생했습니다."),
  S3_DELETE_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, "S3 버킷에서 파일을 삭제하는 중 에러가 발생했습니다."),
  MALFORMED_URL_EXCEPTION(HttpStatus.BAD_REQUEST, "잘못된 URL 형식입니다."),
  FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다."),

  // 리뷰 등록 관련 에러
  REVIEW_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "해당 장소에 이미 리뷰를 작성하셨습니다."),
  TOO_MANY_REVIEW_IMAGES(HttpStatus.BAD_REQUEST, "리뷰 사진은 최대 5장까지 가능합니다."),
  INVALID_RECOMMENDATION(HttpStatus.BAD_REQUEST, "1인 추천 여부는 필수입니다."),
  INVALID_PLACE_ID_NULL(HttpStatus.BAD_REQUEST, "장소 ID는 필수입니다."),
  INVALID_RATING_NULL(HttpStatus.BAD_REQUEST, "평점은 필수입니다."),
  INVALID_RATING_RANGE(HttpStatus.BAD_REQUEST, "평점은 1 이상 5 이하로 입력해야 합니다."),
  EMPTY_MOOD_TAG(HttpStatus.BAD_REQUEST, "분위기 태그는 최소 1개 이상 선택해야 합니다."),
  BLANK_MOOD_TAG_VALUE(HttpStatus.BAD_REQUEST, "분위기 태그 값은 비어 있을 수 없습니다."),
  EMPTY_SOLO_TAG(HttpStatus.BAD_REQUEST, "1인 이용 태그는 최소 1개 이상 선택해야 합니다."),
  BLANK_SOLO_TAG_VALUE(HttpStatus.BAD_REQUEST, "1인 이용 태그 값은 비어 있을 수 없습니다."),
  INVALID_CONTENT_LENGTH(HttpStatus.BAD_REQUEST, "리뷰 내용은 최대 500자까지 입력할 수 있습니다."),

  // 쏠맵 기능 관련 에러
  INVALID_VIEWPORT_COORDINATES(HttpStatus.BAD_REQUEST, "SW 좌표는 NE 좌표보다 작아야 합니다."),
  CATEGORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 카테고리입니다."),
  UNCATEGORIZED(HttpStatus.OK, "카테고리가 지정되지 않은 장소입니다."),

  // 쏠렉트 관련 에러
  SOLELECT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 쏠렉트입니다."),
  SOLELECT_FORBIDDEN(HttpStatus.FORBIDDEN, "쏠렉트의 소유자가 아닙니다."),
  CONTENT_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 콘텐츠 이미지를 찾을 수 없습니다.");

  private final HttpStatus httpStatus;
  private final String message;

  ErrorCode(HttpStatus httpStatus, String message) {

    this.httpStatus = httpStatus;
    this.message = message;
  }
}
