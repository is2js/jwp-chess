package chess.service;

import static chess.testutil.ControllerTestFixture.ROOM_REQUEST_DTO_ONLY_PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import chess.dto.request.MoveRequestDto;
import chess.dto.request.RoomRequestDto;
import chess.dto.response.GameResponseDto;
import chess.dto.response.RoomResponseDto;
import chess.dto.response.RoomsResponseDto;
import chess.dto.response.StatusResponseDto;
import chess.entity.BoardEntity;
import chess.repository.BoardRepository;
import chess.repository.BoardRepositoryImpl;
import chess.repository.RoomRepository;
import chess.repository.RoomRepositoryImpl;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@Transactional
@JdbcTest
class ChessServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    private BoardRepository boardRepository;

    private RoomRepository roomRepository;

    private ChessService chessService;

    @BeforeEach
    void setUp() {
        roomRepository = new RoomRepositoryImpl(jdbcTemplate, dataSource);
        boardRepository = new BoardRepositoryImpl(jdbcTemplate, dataSource);
        chessService = new ChessService(roomRepository, boardRepository);
    }

    @DisplayName("체스 초보만이라는 이름을 가진 방을 생성한다.")
    @Test
    void createRoom() {
        final RoomResponseDto room = createTestRoom("체스 초보만");

        assertAll(
            () -> assertThat(room.getName()).isEqualTo("체스 초보만"),
            () -> assertThat(boardRepository.findBoardByRoomId(room.getId())).hasSize(64)
        );
    }

    @DisplayName("중복된 이름의 방을 생성하면, DuplicateKeyException 예외가 발생한다.")
    @Test
    void createRoom_duplicate() {
        createTestRoom("체스 초보만1");

        assertThatThrownBy(
            () -> createTestRoom("체스 초보만1")
        ).isInstanceOf(DuplicateKeyException.class);
    }

    @DisplayName("생성한 체스 방을 모두 가져온다.")
    @Test
    void findRooms() {
        createTestRoom("체스 초보만1");
        createTestRoom("체스 초보만2");

        final RoomsResponseDto roomsResponseDto = chessService.findRooms();

        assertThat(roomsResponseDto.getRoomResponseDtos()).hasSize(2);
    }

    @DisplayName("체스 초보만 방의 정보를 가져온다.")
    @Test
    void enterRoom() {
        final Long id = createTestRoom("체스 초보만").getId();
        final GameResponseDto gameResponseDto = chessService.getCurrentBoards(id);

        assertAll(
            () -> assertThat(gameResponseDto.getName()).isEqualTo("체스 초보만"),
            () -> assertThat(gameResponseDto.getTeam()).isEqualTo("white"),
            () -> assertThat(gameResponseDto.getBoard().getBoards()).hasSize(64)
        );
    }

    @DisplayName("종료된 방에 입장을 요청하여 에러가 발생한다.")
    @Test
    void enterRoomException() {
        final Long id = createTestRoom("체스 초보만").getId();
        chessService.endRoom(id, ROOM_REQUEST_DTO_ONLY_PASSWORD);

        assertThatThrownBy(() -> chessService.getCurrentBoards(id))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("[ERROR] 이미 종료된 게임입니다.");
    }

    @DisplayName("a2의 기물을 a4로 이동한다.")
    @Test
    void move() {
        final Long id = createTestRoom("체스 초보만").getId();
        chessService.move(id, new MoveRequestDto("a2", "a4"));
        final BoardEntity sourceBoardEntity = boardRepository.findBoardByRoomIdAndPosition(id, "a2");
        final BoardEntity targetBoardEntity = boardRepository.findBoardByRoomIdAndPosition(id, "a4");

        assertAll(
            () -> assertThat(sourceBoardEntity.getPiece()).isEqualTo("blank"),
            () -> assertThat(targetBoardEntity.getPiece()).isEqualTo("white_pawn")
        );
    }

    @DisplayName("현재 방의 체스 게임을 종료한다.")
    @Test
    void end() {
        final Long id = createTestRoom("체스 초보만").getId();
        chessService.endRoom(id, ROOM_REQUEST_DTO_ONLY_PASSWORD);
        final RoomsResponseDto rooms = chessService.findRooms();
        assertThat(rooms.getRoomResponseDtos()).isEmpty();
    }

    @DisplayName("현재 방의 체스 점수를 계산한다.")
    @Test
    void createStatus() {
        final Long id = createTestRoom("체스 초보만").getId();
        final StatusResponseDto status = chessService.calculateStatus(id);

        assertThat(status.getBlackScore()).isEqualTo(38);
    }

    private RoomResponseDto createTestRoom(final String roomName) {
        final RoomRequestDto roomRequestDto = new RoomRequestDto(roomName, "1234");
        return chessService.createRoom(roomRequestDto);
    }
}
