package ru.ithub.chess;

public final class Main {
    public static void main(String[] args) {
        if (args.length != 6) {
            System.err.println("Usage: xk yk xr yr xb yb");
            System.exit(1);
        }
        try {
            int[] v = new int[6];
            for (int i = 0; i < v.length; i++) v[i] = Integer.parseInt(args[i]);
            ChessMath math = new ChessMath();
            ChessMath.AttackResult result = math.calculate(
                new ChessMath.Position(v[0], v[1]),
                new ChessMath.Position(v[2], v[3]),
                new ChessMath.Position(v[4], v[5]));
            System.out.println("Rook attacks: " + result.rook());
            System.out.println("Bishop attacks: " + result.bishop());
            System.out.println("King is under attack: " + result.any());
        } catch (IllegalArgumentException ex) {
            System.err.println("Error: " + ex.getMessage());
            System.exit(1);
        }
    }
}
