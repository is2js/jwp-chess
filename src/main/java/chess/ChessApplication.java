package chess;

import chess.controller.ConsoleController;

public class ChessApplication {
    public static void main(String[] args) {
        ConsoleController consoleController = new ConsoleController();
        consoleController.run();
    }
}