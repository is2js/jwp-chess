package chess.webdto.dao;

import chess.domain.Position;
import chess.domain.piece.Piece;

import java.util.Map;

public class TeamInfoDto {
    private String team;
    private String position;
    private String piece;
    private boolean isFirstMoved;
    private long roomId;

    public TeamInfoDto(String team, Map.Entry<Position, Piece> infos, long roomId) {
        this.team = team;
        this.position = infos.getKey().getPositionInitial();
        this.piece = convert(infos.getValue());
        this.isFirstMoved = infos.getValue().isFirstMove();
        this.roomId = roomId;
    }

    private String convert(Piece value) {
        return PieceDto.convert(value);
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getPiece() {
        return piece;
    }

    public void setPiece(String piece) {
        this.piece = piece;
    }

    public boolean getIsFirstMoved() {
        return isFirstMoved;
    }

    public void setIsFirstMoved(boolean firstMoved) {
        isFirstMoved = firstMoved;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }


}
