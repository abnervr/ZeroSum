/* Copyright (c) 2013 G.I.C Consultoria e Comunicação Ltda */
package org.abner.zerosum;

import java.util.Map.Entry;

import org.abner.zerosum.data.Board;
import org.abner.zerosum.data.Move;
import org.abner.zerosum.data.Piece;
import org.abner.zerosum.data.Position;
import org.abner.zerosum.data.checkers.CheckersPiece;

public class UIBuilder {

	public static String buildBoard(Board<? extends Piece> board, boolean showScore) {
		StringBuilder sb = new StringBuilder();
		for (Entry<Position, ? extends Piece> e: board.getPieces().entrySet())
			sb.append(buildPiece(e.getValue(), e.getKey()));
		return sb.toString();
	}

	public static String buildPiece(Piece piece, Position position) {
		int color = piece.isBlack() ? 1 : 0;
		if (piece instanceof CheckersPiece && ((CheckersPiece)piece).isQueen())
			color += 2;
		return "buildPiece(" + position.getX() + "," + position.getY() + "," + color + ");\n";
	}

	public static String removePiece(Position position, int duration) {
		return "removePiece(" + position.getX() + "," + position.getY() + "," + duration + ");\n";
	}

	public static String movePiece(Move move) {
		String x = "[";
		String y = "[";
		x += move.getStartPosition().getX();
		y += move.getStartPosition().getY();
		if (move.getPositions() != null)
			for (Position position: move.getPositions()) {
				x += "," + position.getX();
				y += "," + position.getY();
			}
		x += "," + move.getPosition().getX();
		y += "," + move.getPosition().getY();
		x += "]";
		y += "]";
		return "movePiece(" + x + "," + y + ");\n";
	}

	public static String buildLine(Position startPosition, Position endPosition) {
		return "buildLine(" + startPosition.getX() + "," + startPosition.getY() + "," + endPosition.getX() + "," + endPosition.getY() + ");\n";
	}

	public static String buildScore(Move move, String score, String color) {
		Position endPosition = move.getPosition();
		if (move.getCaptured() > 0)
			endPosition = move.getCapturedPositions().get(0);
		return "buildScore(" + move.getStartPosition().getX() + "," + move.getStartPosition().getY() + ","
			+ endPosition.getX() + "," + endPosition.getY() + "," + score + ",'" + color + "');\n";
	}

	public static String buildScore(Position position, String score, String color) {
		return "buildScore(" + position.getX() + "," + position.getY() + "," + score + "," + color + ");\n";
	}

	public static String upgradePiece(Position position, int animationMultiplier) {
		return "upgradePiece(" + position.getX() + "," + position.getY() + ");\n";
	}

	public static String showScore(long score, int jogadas, String moves) {
		return "showScore(" + score + "," + jogadas + ",'" + moves.replace("\n", "<br>").replace("'", "\\'") + "');\n";
	}

	public static String nextMove(int timeout) {
		return "setTimeout('autoRequest();'," + timeout + ");\n";
	}

	public static String showError(String error) {
		return "document.getElementById('erros').innerHTML = '" + error + "';\n";
	}
}
