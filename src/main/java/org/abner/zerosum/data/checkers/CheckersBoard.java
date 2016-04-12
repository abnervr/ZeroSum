/* Copyright (c) 2013 G.I.C Consultoria e Comunicação Ltda */
package org.abner.zerosum.data.checkers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.abner.zerosum.data.Board;
import org.abner.zerosum.data.Move;
import org.abner.zerosum.data.Piece;
import org.abner.zerosum.data.Position;

public class CheckersBoard extends Board<CheckersPiece> implements Serializable {

	private static final long serialVersionUID = -2404998973178871446L;

	private static int MAX_VALUE = 1000;
	private static int NORMAL_PIECE_VALUE = 30;
	private static int QUEEN_PIECE_VALUE = 40;
	private static int CAPTURE_PENALTY = 20;

	private static int NORMAL_PIECE_POSITION = 1;

	private List<Move> availableMoves;

	public CheckersBoard() {
		for (short x = 0; x < BOARD_SIZE; x++) {
            for (short y = 0; y < BOARD_SIZE; y++) {
                if (x % 2 != y % 2) {
                    if (y < BOARD_SIZE / 2 - 1) {
                        pieces.put(new Position(x, y), new CheckersPiece(true));
                    } else if (y > BOARD_SIZE / 2) {
                        pieces.put(new Position(x, y), new CheckersPiece(false));
                    }
                }
            }
        }
	}

	public static CheckersBoard loadFromFile(String filename) throws FileNotFoundException, IOException, ClassNotFoundException {
		File file = new File("./" + filename + ".game");
		if (file.exists()) {
			ObjectInputStream input = new ObjectInputStream(new FileInputStream(file));
			try {
				Object object = input.readObject();
				if (object instanceof CheckersBoard) {
                    return (CheckersBoard)object;
                }
			} finally {
				input.close();
			}
		}
		return null;
	}

	@Override
	public void save() throws IOException {
		File file = new File("./" + hash() + ".game");
		if (!file.exists()) {
			file.createNewFile();
			ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file));
			output.writeObject(this);
			output.flush();
			output.close();
		}
	}

	@Override
	public String hash() {
		StringBuilder hash = new StringBuilder(50);
		hash.append(isBlackMove() ? "y" : "n");
		List<Position> keys = new ArrayList<Position>(pieces.keySet());
		Collections.sort(keys, new Comparator<Position>() {

			public int compare(Position o1, Position o2) {
				int c = o1.getX() - o2.getX();
				if (c != 0) {
                    return c;
                }
				return o1.getY() - o2.getY();
			}
		});
		for (Position position: keys) {
			CheckersPiece piece = pieces.get(position);
			int pieceHash = (short)(position.getX() + 1);
			pieceHash |= (position.getY() + 1) << 4;
			if (piece.isBlack()) {
                pieceHash |= 0x100;
            }
			if (piece.isQueen()) {
                pieceHash |= 0x200;
            }
			hash.append(Integer.toString(pieceHash, Character.MAX_RADIX));
		}
		return hash.toString();
	}

	public static CheckersBoard buildProblemBoard() throws IOException {
		CheckersBoard board = new CheckersBoard();
		board.pieces.clear();
		board.isBlackMove = true;
		board.pieces.put(new Position(0, 3), new CheckersPiece(true));
		board.pieces.put(new Position(3, 2), new CheckersPiece(true));
		board.pieces.put(new Position(5, 2), new CheckersPiece(true));
		board.pieces.put(new Position(6, 1), new CheckersPiece(true));
		board.pieces.put(new Position(7, 2), new CheckersPiece(true));
		board.pieces.put(new Position(6, 5), new CheckersPiece(true));

		board.pieces.put(new Position(1, 4), new CheckersPiece(false));
		board.pieces.put(new Position(1, 6), new CheckersPiece(false));
		board.pieces.put(new Position(2, 5), new CheckersPiece(false));
		board.pieces.put(new Position(3, 4), new CheckersPiece(false));
		board.pieces.put(new Position(5, 4), new CheckersPiece(false));
		board.pieces.put(new Position(7, 6), new CheckersPiece(false));
		return board;
	}

	@Override
	public long evaluate() {
		List<Move> availableMoves = getAvailableMoves();
		int value = 0;
		boolean blackCheckersPieces = false, whiteCheckersPieces = false;
		Set<Position> capturedPieces = new HashSet<Position>();
		if (!availableMoves.isEmpty() && availableMoves.get(0).getCaptured() > 0) {
            for (Move move: availableMoves) {
                capturedPieces.addAll(move.getCapturedPositions());
            }
        }
		for (Entry<Position, CheckersPiece> e: pieces.entrySet()) {
			CheckersPiece piece = e.getValue();
			if (piece.isBlack()) {
                blackCheckersPieces = true;
            } else {
                whiteCheckersPieces = true;
            }

			int y = e.getKey().getY();
			int pieceValue;
			if (piece.isQueen()) {
                pieceValue = QUEEN_PIECE_VALUE;
            } else {
				pieceValue = NORMAL_PIECE_VALUE;
				if (piece.isBlack()) {
					pieceValue += y * NORMAL_PIECE_POSITION;
				} else {
					pieceValue += (Board.BOARD_SIZE - 1 - y) * NORMAL_PIECE_POSITION;
				}
			}
			if (capturedPieces.contains(e.getKey())) {
                pieceValue -= CAPTURE_PENALTY;
            }
			if (!piece.isBlack()) {
                pieceValue = -pieceValue;
            }
			value += pieceValue;
		}
		//Acabaram as pecas, o jogo acabou
		if (!whiteCheckersPieces) {
            return MAX_VALUE + Math.max(1000 - getQtdMoves(), 0);
        }
		if (!blackCheckersPieces) {
            return -(MAX_VALUE + Math.max(1000 - getQtdMoves(), 0));
        }

		//Nao existem jogadas disponiveis, o jogo acabou
		if (availableMoves.isEmpty()) {
			if (isBlackMove()) {
                return -(MAX_VALUE + Math.max(1000 - getQtdMoves(), 0));
            } else {
                return MAX_VALUE + Math.max(1000 - getQtdMoves(), 0);
            }
		}
		return value;
	}

	@Override
	public List<Move> getAvailableMoves() {
		if (availableMoves == null) {
			List<Move> moves = new ArrayList<Move>();

			for (Entry<Position, CheckersPiece> e: pieces.entrySet()) {
				CheckersPiece piece = e.getValue();
				if (isBlackMove() == piece.isBlack()) {
                    moves.addAll(getAvailableMoves(e.getKey(), piece));
                }
			}
			Collections.sort(moves);
			if (!moves.isEmpty()) {
				int killed = moves.get(0).getCaptured();
				if (killed > 0) {
					int i = 1;
					while (i < moves.size()) {
						if (moves.get(i).getCaptured() < killed) {
                            moves.remove(i);
                        } else {
                            i++;
                        }
					}
				}
			}
			availableMoves = moves;
		}
		return availableMoves;
	}

	private List<Move> getAvailableMoves(Position position, CheckersPiece piece) {
		List<Move> moves = new ArrayList<Move>();
		int max = 1;
		if (piece.isQueen()) {
            max = BOARD_SIZE;
        }
		if (piece.isBlack() || piece.isQueen()) {
			moves.addAll(getAvailableMoves(position, piece, (short)1, (short)1, max));
			moves.addAll(getAvailableMoves(position, piece, (short)-1, (short)1, max));
		}
		if (!piece.isBlack() || piece.isQueen()) {
			moves.addAll(getAvailableMoves(position, piece, (short)1, (short)-1, max));
			moves.addAll(getAvailableMoves(position, piece, (short)-1, (short)-1, max));
		}
		return moves;
	}

	private List<Move> getAvailableMoves(Position position, CheckersPiece piece, short xSignal, short ySignal, int max) {
		return getAvailableMoves(position, piece, xSignal, ySignal, max, null);
	}

	private List<Move> getAvailableMoves(Position currentPosition, CheckersPiece piece, short xSignal, short ySignal, int max, Move parentMove) {
		List<Move> moves = new ArrayList<Move>();
		for (short i = 1; i <= max; i++) {
			Position position = new Position(currentPosition.getX() + i * xSignal, currentPosition.getY() + i * ySignal);
			if (!position.isValid() || (pieces.containsKey(position) && pieces.get(position).isBlack() == piece.isBlack())) {
                break;
            }
			if (parentMove == null && !pieces.containsKey(position)) {
                //Jogada comum
				moves.add(new Move(piece, currentPosition, position));
            } else if (pieces.containsKey(position) && pieces.get(position).isBlack() != piece.isBlack() && (parentMove == null || !parentMove.containsCapturedPosition(position))) {
				//Comendo uma peça
				i++;
				Position newPosition = new Position(currentPosition.getX() + i * xSignal, currentPosition.getY() + i * ySignal);
				if (newPosition.isValid() && (!pieces.containsKey(newPosition))) {
					//Checa se a peça já foi comida antes
					//Não sei se é válido, tem tantas variações de regras
					//|| (parentMove != null && parentMove.containsCapturedPosition(newPosition))) {
					Move move;
					if (parentMove == null) {
                        move = new Move(piece, currentPosition, newPosition);
                    } else {
						move = new Move(piece, parentMove, newPosition);
						move.addPosition(currentPosition);
					}
					move.addCapturedPosition(position);
					int size = moves.size();
					moves.addAll(getAvailableMoves(newPosition, piece, xSignal, ySignal, max, move));
					moves.addAll(getAvailableMoves(newPosition, piece, (short)-xSignal, ySignal, max, move));
					if (piece.isQueen()) {
						moves.addAll(getAvailableMoves(newPosition, piece, xSignal, (short)-ySignal, max, move));

						i++;
						while (i <= max) {
							newPosition = new Position(currentPosition.getX() + i * xSignal, currentPosition.getY() + i * ySignal);
							if (newPosition.isValid() && !pieces.containsKey(newPosition)) {
								Move queenMove = new Move(new CheckersPiece(piece.isBlack(), piece.isQueen()), move, newPosition);
								moves.addAll(getAvailableMoves(newPosition, piece, (short)-xSignal, ySignal, max, queenMove));
								moves.addAll(getAvailableMoves(newPosition, piece, xSignal, (short)-ySignal, max, queenMove));
								i++;
							} else {
								break;
							}
						}
					}
					if (moves.size() == size) {
                        moves.add(move);
                    }

				}
				break;
			}
		}
		return moves;
	}

	@Override
	public void doMove(Move move) {
		availableMoves = null;
		super.doMove(move);
		if (move.isQueenMove()) {
            pieces.get(move.getPosition()).setQueen(true);
        }
	}

	@Override
	public Move undoMove() {
		availableMoves = null;
		Move move = super.undoMove();
		if (move != null && move.isQueenMove()) {
            pieces.get(move.getStartPosition()).setQueen(false);
        }
		return move;
	}

	@Override
	public Board<? extends Piece> clone() {
		CheckersBoard board = new CheckersBoard();
		board.pieces = new HashMap<Position, CheckersPiece>();
		for (Entry<Position, CheckersPiece> e: pieces.entrySet()) {
            board.pieces.put(e.getKey(), (CheckersPiece)e.getValue().clone());
        }
		board.moves = new ArrayList<Move>();
		for (Move move: moves) {
            board.moves.add(move.clone());
        }
		board.isBlackMove = isBlackMove();
		board.qtdMoves = getQtdMoves();
		board.availableMoves = null;
		return board;
	}
}
