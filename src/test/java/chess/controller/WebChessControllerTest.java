package chess.controller;

import static chess.testutil.ControllerTestFixture.REQUEST_MAPPING_URI;
import static chess.testutil.ControllerTestFixture.ROOM_A;
import static chess.testutil.ControllerTestFixture.ROOM_B;
import static chess.testutil.ControllerTestFixture.ROOM_REQUEST_DTO_ONLY_PASSWORD;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import chess.config.MockMvcConfig;
import chess.domain.board.BoardFactory;
import chess.domain.game.Score;
import chess.dto.BoardsDto;
import chess.dto.request.MoveRequestDto;
import chess.dto.request.RoomRequestDto;
import chess.dto.response.ErrorResponseDto;
import chess.dto.response.GameResponseDto;
import chess.dto.response.RoomResponseDto;
import chess.dto.response.RoomsResponseDto;
import chess.dto.response.StatusResponseDto;
import chess.entity.BoardEntity;
import chess.entity.RoomEntity;
import chess.exception.RoomNotFoundException;
import chess.service.ChessService;
import chess.util.PasswordSha256Encoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@Import(MockMvcConfig.class)
@ActiveProfiles("test")
@WebMvcTest(WebChessController.class)
class WebChessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChessService chessService;

    @DisplayName("방 생성 요청이 성공하면, 201 status 및 header 속 Location을 응답한다.")
    @Test
    void createRoom_success() throws Exception {
        final Long firstRoomId = 1L;
        final RoomRequestDto roomRequestDto = new RoomRequestDto("체스 초보만", "1234");
        final String requestBody = objectMapper.writeValueAsString(roomRequestDto);

        given(chessService.createRoom(any())).willReturn(
            RoomResponseDto.of(
                new RoomEntity(firstRoomId, PasswordSha256Encoder.encode("1234"), "체스 초보만", "white", false)));

        mockMvc.perform(post(REQUEST_MAPPING_URI).content(requestBody)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(
                header().string(HttpHeaders.LOCATION, REQUEST_MAPPING_URI + "/" + firstRoomId));
    }

    @DisplayName("진행 중인 모든 방을 조회 요청이 성공하면, 200 status 및 방 정보들 데이터를 반환한다")
    @Test
    void findRooms_success() throws Exception {
        final RoomsResponseDto roomsResponseDto = RoomsResponseDto.of(List.of(ROOM_A, ROOM_B));
        final String responseBody = objectMapper.writeValueAsString(roomsResponseDto);

        given(chessService.findRooms()).willReturn(roomsResponseDto);

        mockMvc.perform(get(REQUEST_MAPPING_URI)).andExpect(status().isOk())
            .andExpect(content().string(responseBody));
    }

    @DisplayName("방 입장 요청이 성공하면, 200 status 및 해당 방의 체스게임 데이터를 반환한다.")
    @Test
    void enterRoom_success() throws Exception {
        final Long firstRoomId = 1L;
        final GameResponseDto gameResponseDto = GameResponseDto.of(ROOM_A,
            BoardsDto.of(getBoardInRoom(1L)));
        final String responseBody = objectMapper.writeValueAsString(gameResponseDto);

        given(chessService.getCurrentBoards(any()))
            .willReturn(gameResponseDto);

        mockMvc.perform(get(REQUEST_MAPPING_URI + "/" + firstRoomId))
            .andExpect(status().isOk())
            .andExpect(content().string(responseBody));
    }

    @DisplayName("방 입장 요청이 실패하면, 404 status(NotFoundException) 및 예외 메세지를 응답한다.")
    @Test
    void enterRoom_fail() throws Exception {
        final Long invalidRoomId = -1L;

        final ErrorResponseDto errorResponseDto = new ErrorResponseDto("[ERROR] 방 정보를 찾을 수 없습니다.");
        final String responseBody = objectMapper.writeValueAsString(errorResponseDto);

        given(chessService.getCurrentBoards(invalidRoomId))
            .willThrow(new RoomNotFoundException(1));

        mockMvc.perform(get(REQUEST_MAPPING_URI + "/" + invalidRoomId))
            .andExpect(status().isNotFound())
            .andExpect(content().string(responseBody));
    }

    @DisplayName("기물 이동 요청이 성공하면, 200 status 및 방의 체스게임 데이터를 반환한다.")
    @Test
    void movePiece_success() throws Exception {
        final Long roomId = 1L;
        final MoveRequestDto moveRequestDto = new MoveRequestDto("a2", "a4");
        final String requestBody = objectMapper.writeValueAsString(moveRequestDto);
        final GameResponseDto gameResponseDto = GameResponseDto.of(ROOM_A,
            BoardsDto.of(getBoardInRoom(1L)));
        final String responseBody = objectMapper.writeValueAsString(gameResponseDto);

        given(chessService.move(anyLong(), any(MoveRequestDto.class)))
            .willReturn(gameResponseDto);

        mockMvc.perform(patch(REQUEST_MAPPING_URI + "/" + roomId + "/move")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(responseBody));
    }

    @DisplayName("기물 이동 요청이 실패하면, 400 status 및 예외 메세지를 응답한다.")
    @Test
    void movePiece_fail() throws Exception {
        final Long roomId = 1L;
        final MoveRequestDto moveRequestDto = new MoveRequestDto("a2", "e7");
        final String requestBody = objectMapper.writeValueAsString(moveRequestDto);

        final ErrorResponseDto errorResponseDto = new ErrorResponseDto("[ERROR] 이동할 수 없습니다.");
        final String responseBody = objectMapper.writeValueAsString(errorResponseDto);

        given(chessService.move(anyLong(), any(MoveRequestDto.class)))
            .willThrow(new IllegalStateException("[ERROR] 이동할 수 없습니다."));

        mockMvc.perform(patch(REQUEST_MAPPING_URI + "/" + roomId + "/move")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(responseBody));
    }


    @DisplayName("게임 종료 요청이 성공하면, 200 status를 응답한다.")
    @Test
    void finishGame_success() throws Exception {
        final Long roomId = 1L;
        final String requestBody = objectMapper.writeValueAsString(ROOM_REQUEST_DTO_ONLY_PASSWORD);

        doNothing().when(chessService)
            .endRoom(anyLong(), any(RoomRequestDto.class));

        mockMvc.perform(patch(REQUEST_MAPPING_URI + "/" + roomId + "/end")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @DisplayName("게임 종료한 상태에서 재요청하면, 400 status와 예외 메세지를 응답한다.")
    @Test
    void finishGame_fail() throws Exception {
        final Long roomId = 1L;
        final String requestBody = objectMapper.writeValueAsString(ROOM_REQUEST_DTO_ONLY_PASSWORD);
        final ErrorResponseDto errorResponseDto = new ErrorResponseDto("[ERROR] 이미 종료된 게임입니다.");
        final String responseBody = objectMapper.writeValueAsString(errorResponseDto);

        doThrow(new IllegalStateException("[ERROR] 이미 종료된 게임입니다.")).when(chessService)
            .endRoom(anyLong(), any(RoomRequestDto.class));

        mockMvc.perform(patch(REQUEST_MAPPING_URI + "/" + roomId + "/end")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(responseBody));
    }

    @DisplayName("점수 계산 요청이 성공하면, 200 status 및 점수 데이터를 응답한다.")
    @Test
    void calculateStatus() throws Exception {
        final Long roomId = 1L;
        final StatusResponseDto statusResponseDto = StatusResponseDto.of(new Score(BoardFactory.initialize()));
        final String responseBody = objectMapper.writeValueAsString(statusResponseDto);

        given(chessService.calculateStatus(anyLong()))
            .willReturn(statusResponseDto);

        mockMvc.perform(get(REQUEST_MAPPING_URI + "/" + roomId + "/status"))
            .andExpect(status().isOk())
            .andExpect(content().string(responseBody));
    }

    @DisplayName("방 제목 변경 요청이 성공하면, 200 status 및 변경된 방 데이터를 응답한다.")
    @Test
    void update_success() throws Exception {
        final Long roomId = 1L;
        final RoomRequestDto roomRequestDto = new RoomRequestDto("바뀐 제목", "1234");
        final String requestBody = objectMapper.writeValueAsString(roomRequestDto);
        final RoomResponseDto roomResponseDto = RoomResponseDto.of(
            new RoomEntity(roomId, PasswordSha256Encoder.encode("1234"), "바뀐 제목", "white", false));
        final String responseBody = objectMapper.writeValueAsString(roomResponseDto);

        given(chessService.updateRoom(anyLong(), any()))
            .willReturn(roomResponseDto);

        mockMvc.perform(patch(REQUEST_MAPPING_URI + "/" + roomId)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(responseBody));
    }

    @DisplayName("방 제목 변경 요청이 실패하면, 404 status(NotFoundException) 및 예외 메세지를 응답한다.")
    @Test
    void update_fail() throws Exception {
        final Long invalidRoomId = -1L;
        final RoomRequestDto roomRequestDto = new RoomRequestDto("바뀐 제목", "1234");
        final String requestBody = objectMapper.writeValueAsString(roomRequestDto);

        final ErrorResponseDto errorResponseDto = new ErrorResponseDto("[ERROR] 방 정보를 찾을 수 없습니다.");
        final String responseBody = objectMapper.writeValueAsString(errorResponseDto);

        given(chessService.updateRoom(anyLong(), any()))
            .willThrow(new RoomNotFoundException(1));

        mockMvc.perform(patch(REQUEST_MAPPING_URI + "/" + invalidRoomId)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().string(responseBody));
    }

    @DisplayName("방 생성 요청시 이미 존재하는 이름으로 생성 요청하면, 400 status 및 예외 메세지를 응답한다.")
    @Test
    void create_duplicate() throws Exception {

        final RoomRequestDto roomRequestDto = new RoomRequestDto("똑같은 제목", "1234");
        final String requestBody = objectMapper.writeValueAsString(roomRequestDto);

        chessService.createRoom(roomRequestDto);

        final ErrorResponseDto errorResponseDto = new ErrorResponseDto("[ERROR] 방 이름이 중복일 수 없습니다.");
        final String responseBody = objectMapper.writeValueAsString(errorResponseDto);

        given(chessService.createRoom(any(RoomRequestDto.class)))
            .willThrow(new DuplicateKeyException(""));

        mockMvc.perform(post(REQUEST_MAPPING_URI)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(responseBody));
    }

    private List<BoardEntity> getBoardInRoom(final Long roomId) {
        return BoardFactory.initialize()
            .entrySet()
            .stream()
            .map(entry -> new BoardEntity(
                roomId,
                entry.getKey().convertPositionToString(),
                entry.getValue().convertPieceToString())
            )
            .collect(Collectors.toList());
    }
}
