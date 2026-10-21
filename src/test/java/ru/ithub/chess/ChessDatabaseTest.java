package ru.ithub.chess;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.sql.*;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
class ChessDatabaseTest {
    static Stream<Arguments> casesFromDatabase() throws Exception {
        String path = System.getProperty("chess.db", "database/chess_tests.db");
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + path);
             PreparedStatement q = c.prepareStatement("SELECT * FROM test_cases ORDER BY id");
             ResultSet r = q.executeQuery()) {
            Stream.Builder<Arguments> builder = Stream.builder();
            while (r.next()) {
                builder.add(Arguments.of(r.getInt("id"), r.getString("description"),
                    r.getInt("king_x"), r.getInt("king_y"), r.getInt("rook_x"), r.getInt("rook_y"),
                    r.getInt("bishop_x"), r.getInt("bishop_y"), r.getObject("expected_rook"),
                    r.getObject("expected_bishop"), r.getObject("expected_any"), r.getInt("expected_error")));
            }
            return builder.build();
        }
    }
    @ParameterizedTest(name = "{0}: {1}")
    @MethodSource("casesFromDatabase")
    void chessCase(int id, String description, int kx, int ky, int rx, int ry,
                   int bx, int by, Object er, Object eb, Object ea, int error) {
        ChessMath math = new ChessMath();
        if (error == 1) {
            assertThrows(IllegalArgumentException.class, () ->
                math.calculate(new ChessMath.Position(kx, ky),
                    new ChessMath.Position(rx, ry), new ChessMath.Position(bx, by)));
        } else {
            ChessMath.AttackResult actual = math.calculate(
                new ChessMath.Position(kx, ky), new ChessMath.Position(rx, ry),
                new ChessMath.Position(bx, by));
            assertEquals(((Number) er).intValue() == 1, actual.rook());
            assertEquals(((Number) eb).intValue() == 1, actual.bishop());
            assertEquals(((Number) ea).intValue() == 1, actual.any());
        }
    }
}
