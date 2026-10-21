package ru.ithub.chess;

public final class ChessMath {
    public record Position(int x, int y) {
        public Position {
            if (x < 1 || x > 8 || y < 1 || y > 8) {
                throw new IllegalArgumentException("Coordinates must be between 1 and 8");
            }
        }
    }

    public record AttackResult(boolean rook, boolean bishop, boolean any) {}

    public AttackResult calculate(Position k, Position r, Position b) {
        if (k.equals(r) || k.equals(b) || r.equals(b)) {
            throw new IllegalArgumentException("Pieces must occupy different squares");
        }
        boolean rookLine = k.x() == r.x() || k.y() == r.y();
        boolean bishopLine = Math.abs(k.x() - b.x()) == Math.abs(k.y() - b.y());
        boolean rookBlocked =
            (k.x() == r.x() && b.x() == k.x() && (b.y() - k.y()) * (b.y() - r.y()) < 0) ||
            (k.y() == r.y() && b.y() == k.y() && (b.x() - k.x()) * (b.x() - r.x()) < 0);
        boolean bishopBlocked = bishopLine &&
            Math.abs(r.x() - b.x()) == Math.abs(r.y() - b.y()) &&
            (r.x() - b.x()) * (r.x() - k.x()) < 0 &&
            (r.y() - b.y()) * (r.y() - k.y()) < 0;
        boolean rook = rookLine && !rookBlocked;
        boolean bishop = bishopLine && !bishopBlocked;
        return new AttackResult(rook, bishop, rook || bishop);
    }
}
