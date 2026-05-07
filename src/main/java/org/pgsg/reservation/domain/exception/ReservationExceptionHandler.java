package org.pgsg.reservation.domain.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pgsg.common.exception.CustomException;
import org.pgsg.common.exception.ErrorConfigProperties;
import org.pgsg.common.exception.GlobalExceptionAdvice;
import org.pgsg.common.response.ErrorResponse;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ReservationExceptionHandler implements GlobalExceptionAdvice {

    private final ErrorConfigProperties errorConfigProperties;

    /**
     * @Valid 검증 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();

        if (fieldError == null) {
            String globalErrorKey = e.getBindingResult().getGlobalError() != null
                    ? e.getBindingResult().getGlobalError().getDefaultMessage() : null;
            return buildResponse(globalErrorKey, null, e);
        }

        String errorKey = fieldError.getDefaultMessage();
        ReservationErrorCode errorCode = ReservationErrorCode.fromErrorKey(errorKey);

        if (errorCode != null) {
            return buildResponse(errorCode.getErrorKey(), errorCode.getField(), e);
        }

        return buildResponse(errorKey, fieldError.getField(), e);
    }

    /**
     * 예약 도메인 비즈니스 예외 처리
     */
    @ExceptionHandler(ReservationException.class)
    public ResponseEntity<ErrorResponse> handleReservationException(CustomException e) {
        String field = e.getField();
        if (field == null && e.getErrorCode() instanceof ReservationErrorCode rec) {
            field = rec.getField();
        }
        return buildResponse(e.getErrorCode().getErrorKey(), field, e);
    }

    /**
     * 공통 에러 응답 빌더
     */
    private ResponseEntity<ErrorResponse> buildResponse(String errorKey, String field, Exception e) {
        // errorKey가 null인 경우 시스템 에러 응답을 직접 생성하여 리턴합니다.
        if (errorKey == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "SYSTEM-500", field, "정의되지 않은 서버 에러가 발생했습니다."));
        }

        // 양 끝의 대괄호를 제거한 키를 생성합니다.
        String normalizedKey = (errorKey.startsWith("[") && errorKey.endsWith("]"))
                ? errorKey.substring(1, errorKey.length() - 1)
                : errorKey;

        // 반대로 대괄호가 포함된 형태의 키도 준비하여 바인딩 유연성을 확보합니다.
        String bracketKey = "[" + normalizedKey + "]";

        // 우선 대괄호가 없는 키로 조회를 시도하고, 결과가 없으면 대괄호가 포함된 키로 다시 조회합니다.
        ErrorConfigProperties.ErrorDetail detail = errorConfigProperties.getConfigs().entrySet().stream()
                .filter(entry -> entry.getKey().equals(normalizedKey) || ("[" + entry.getKey() + "]").equals(errorKey))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);

        // 두 가지 방식 모두 설정값 조회에 실패한 경우 KEY-NOT-FOUND 응답을 반환합니다.
        if (detail == null) {
            // 디버깅을 위해 현재 맵에 들어있는 키들을 로그로 찍어보세요.
            log.error("현재 로드된 설정 키 목록: {}", errorConfigProperties.getConfigs().keySet());
            log.error("[TraceID: {}] Undefined Error Key: {}", MDC.get("traceId"), normalizedKey);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "KEY-NOT-FOUND", field, "설정에서 찾을 수 없는 키입니다: " + normalizedKey));
        }

        log.error("[TraceID: {}] Exception: field={}, code={}, message={}",
                MDC.get("traceId"), field, detail.getCode(), detail.getMessage());

        // 설정된 status 값을 바탕으로 HttpStatus 객체를 생성하며 유효하지 않을 경우 500 에러를 기본값으로 사용합니다.
        HttpStatus status = HttpStatus.resolve(detail.getStatus());
        if (status == null) {
            log.warn("[TraceID: {}] Invalid HTTP Status Code in config: status={}, errorKey={}",
                    MDC.get("traceId"), detail.getStatus(), normalizedKey);
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return ResponseEntity
                .status(status)
                .body(ErrorResponse.of(status, detail.getCode(), field, detail.getMessage()));
    }
}