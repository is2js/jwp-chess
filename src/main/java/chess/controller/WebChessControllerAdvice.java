package chess.controller;

import chess.dto.response.ErrorResponseDto;
import chess.exception.RoomNotFoundException;
import java.util.NoSuchElementException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(annotations = RestController.class)
public class WebChessControllerAdvice {

    @ExceptionHandler(value = {IllegalArgumentException.class, IllegalStateException.class,
        NullPointerException.class, NoSuchElementException.class})
    public ResponseEntity<ErrorResponseDto> handleIllegalException(RuntimeException e) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponseDto(e.getMessage()));
    }

    @ExceptionHandler(value = RoomNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNotFoundException(RoomNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponseDto("[ERROR] 방 정보를 찾을 수 없습니다."));
    }

    @ExceptionHandler(value = DuplicateKeyException.class)
    public ResponseEntity<ErrorResponseDto> handleDuplicateRoomNameException(DuplicateKeyException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponseDto("[ERROR] 방 이름이 중복일 수 없습니다."));
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponseDto> handleInternalServerException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponseDto("[ERROR] 예기치 못한 에러가 발생했습니다."));
    }
}
