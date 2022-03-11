import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

final class InitialGameState {

    private static final FlipableGameState ADVANTAGE_PLAYER1 = new AdvantagePlayer1();

    private static final FlipableGameState ADVANTAGE_PLAYER2 = new AdvantagePlayer2();

    private static final FlipableGameState WIN_PLAYER1 = new WinPlayer1();

    private static final FlipableGameState WIN_PLAYER2 = new WinPlayer2();

    private static final FlipableGameState DEUCE = new Deuce();

    private static final GameState INITIAL_STATE = computeInitialState();

    private interface FlipableGameState extends GameState {

        FlipableGameState flip();

    }

    private static GameState computeInitialState() {

        Map<ScoreLookupKey, FlipableGameState> computed = new HashMap<>();

        computed.put(new ScoreLookupKey(3, 3), DEUCE);
        computed.put(new ScoreLookupKey(4, 4), DEUCE);

        computed.put(new ScoreLookupKey(4, 3), ADVANTAGE_PLAYER1);
        computed.put(new ScoreLookupKey(3, 4), ADVANTAGE_PLAYER2);

        // all wins that weren't proceeded with "Advantage player X"
        for (int i = 0; i <= 2; i++) {
            ScoreLookupKey key = new ScoreLookupKey(4, i);
            computed.put(key, WIN_PLAYER1);
            computed.put(key.flipPlayerScores(), WIN_PLAYER2);
        }

        // No need to start with 4, because all 4 based states are already in the map.
        // They are either wins or "Advantage player X".
        for (int i = 3; i > 0; i--) {
            for (int j = i; j >= 0; j--) {
                ScoreLookupKey key = new ScoreLookupKey(i, j);
                if (!computed.containsKey(key)) {
                    FlipableGameState player1Wins = computed.get(key.incrementPlayer1());
                    FlipableGameState player2Wins = computed.get(key.incrementPlayer2());
                    if (i == j) {
                        FlipableGameState state = new All(i, player1Wins, player2Wins);
                        computed.put(key, state);
                    } else {
                        FlipableGameState state = new GenericGameState(i, j, player1Wins, player2Wins);
                        computed.put(key, state);
                        // if i-j wasn't in the map then j-i will also not be in the map, no need to check
                        computed.put(key.flipPlayerScores(), state.flip());
                    }

                }
            }

        }
        return new All(0, computed.get(new ScoreLookupKey(1, 0)), computed.get(new ScoreLookupKey(0, 1)));
    }

    static GameState getInitialState() {
        return INITIAL_STATE;
    }

    static final class ScoreLookupKey {
        // REVIEW no need to use 2 ints, fits into 1 byte

        private final int scorePlayer1;

        private final int scorePlayer2;

        ScoreLookupKey(int scorePlayer1, int scorePlayer2) {
            this.scorePlayer1 = scorePlayer1;
            this.scorePlayer2 = scorePlayer2;
        }

        ScoreLookupKey incrementPlayer1() {
            return new ScoreLookupKey(this.scorePlayer1 + 1, this.scorePlayer2);
        }

        ScoreLookupKey incrementPlayer2() {
            return new ScoreLookupKey(this.scorePlayer1, this.scorePlayer2 + 1);
        }

        ScoreLookupKey flipPlayerScores() {
            return new ScoreLookupKey(this.scorePlayer2, this.scorePlayer1);
        }

        @Override
        public int hashCode() {
            // scorePlayer1 and scorePlayer2 don't exceed 4
            // this gives us a unique hashCode for all used values
            // by only using the lower 6 bits
            return Integer.rotateLeft(scorePlayer1, 3)  ^  scorePlayer2;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ScoreLookupKey)) {
                return false;
            }
            ScoreLookupKey other = (ScoreLookupKey) obj;
            return scorePlayer1 == other.scorePlayer1
                    && scorePlayer2 == other.scorePlayer2;
        }

        @Override
        public String toString() {
            return "(" + this.scorePlayer1 + ", " + this.scorePlayer2 + ")";
        }

    }

    private static String translate(int score) {
        switch (score) {
        case 0:
            return "Love";
        case 1:
            return "Fifteen";
        case 2:
            return "Thirty";
        case 3:
            return "Forty";
        default:
            throw new IllegalArgumentException("unsupported score: " + score);
        }
    }

    private static final class GenericGameState implements FlipableGameState {

        private final int scorePlayer1;

        private final int scorePlayer2;

        private final FlipableGameState player1Wins;

        private final FlipableGameState player2Wins;

        GenericGameState(int scorePlayer1, int scorePlayer2, FlipableGameState player1Wins, FlipableGameState player2Wins) {
            if (scorePlayer1 >= 4) {
                throw new IllegalArgumentException(AdvantagePlayer1.class + " shold be used");
            }
            if (scorePlayer2 >= 4) {
                throw new IllegalArgumentException(AdvantagePlayer2.class + " shold be used");
            }
            this.scorePlayer1 = scorePlayer1;
            this.scorePlayer2 = scorePlayer2;
            Objects.requireNonNull(player1Wins, "player1Wins");
            Objects.requireNonNull(player2Wins, "player2Wins");
            this.player1Wins = player1Wins;
            this.player2Wins = player2Wins;
        }

        @Override
        public GameState player1WonPoint(GameState.GameDisplayContext context) {
            Objects.requireNonNull(context);
            return this.player1Wins;
        }
        @Override
        public GameState player2WonPoint(GameState.GameDisplayContext context) {
            Objects.requireNonNull(context);
            return this.player2Wins;
        }

        @Override
        public String getScore(GameState.GameDisplayContext context) {
            return getScore();
        }

        private String getScore() {
            return translate(this.scorePlayer1) + "-" + translate(this.scorePlayer2);
        }

        @Override
        public String toString() {
            return this.getScore();
        }

        @Override
        public FlipableGameState flip() {
            // 3-0 (player1wins, (3,1)) -> 0-3((1-3), player2win)
            // REVIEW, recursive, ends up creating equal copies
            return new GenericGameState(this.scorePlayer2, this.scorePlayer1, this.player2Wins.flip(), this.player1Wins.flip());
        }

    }

    /**
     * Represents a state when both players have equal points but we haven't reached deuce yet.
     */
    private static final class All implements FlipableGameState {

        private final GameState player1Wins;

        private final GameState player2Wins;

        private int score;

        All(int score, GameState player1Wins, GameState player2Wins) {
            if (score > 2) {
                throw new IllegalArgumentException("deuce should be used");
            }
            if (score < 0) {
                throw new IllegalArgumentException("negative score not allowed");
            }
            Objects.requireNonNull(player1Wins, "player1Wins");
            Objects.requireNonNull(player2Wins, "player2Wins");
            this.player1Wins = player1Wins;
            this.player2Wins = player2Wins;
            this.score = score;
        }

        @Override
        public GameState player1WonPoint(GameState.GameDisplayContext context) {
            Objects.requireNonNull(context);
            return this.player1Wins;
        }

        @Override
        public GameState player2WonPoint(GameState.GameDisplayContext context) {
            Objects.requireNonNull(context);
            return this.player2Wins;
        }

        @Override
        public String getScore(GameState.GameDisplayContext context) {
            return getScore();
        }

        private String getScore() {
            return translate(this.score) + "-All";
        }

        @Override
        public String toString() {
            return this.getScore();
        }

        @Override
        public FlipableGameState flip() {
            return this;
        }

    }

    private static final class Deuce implements FlipableGameState {

        @Override
        public GameState player1WonPoint(GameState.GameDisplayContext context) {
            Objects.requireNonNull(context);
            return ADVANTAGE_PLAYER1;
        }

        @Override
        public GameState player2WonPoint(GameState.GameDisplayContext context) {
            Objects.requireNonNull(context);
            return ADVANTAGE_PLAYER2;
        }

        @Override
        public String getScore(GameState.GameDisplayContext context) {
            return this.getScore();
        }

        private String getScore() {
            return "Deuce";
        }

        @Override
        public FlipableGameState flip() {
            return this;
        }

        @Override
        public String toString() {
            return this.getScore();
        }

    }

    private static abstract class Advantage implements FlipableGameState {

        @Override
        public String getScore(GameState.GameDisplayContext context) {
            return "Advantage " + getPlayer(context);
        }

        protected abstract String getPlayer(GameState.GameDisplayContext context);

    }

    private static final class AdvantagePlayer1 extends Advantage {

        @Override
        public GameState player1WonPoint(GameState.GameDisplayContext context) {
            Objects.requireNonNull(context);
            return WIN_PLAYER1;
        }

        @Override
        public GameState player2WonPoint(GameState.GameDisplayContext context) {
            Objects.requireNonNull(context);
            return DEUCE;
        }

        @Override
        protected String getPlayer(GameState.GameDisplayContext context) {
            return context.getPlayer1();
        }

        @Override
        public FlipableGameState flip() {
            return ADVANTAGE_PLAYER2;
        }

        @Override
        public String toString() {
            return "Advantage player1";
        }

    }

    private static final class AdvantagePlayer2 extends Advantage {

        @Override
        public GameState player1WonPoint(GameState.GameDisplayContext context) {
            Objects.requireNonNull(context);
            return DEUCE;
        }

        @Override
        public GameState player2WonPoint(GameState.GameDisplayContext context) {
            Objects.requireNonNull(context);
            return WIN_PLAYER2;
        }

        @Override
        protected String getPlayer(GameState.GameDisplayContext context) {
            return context.getPlayer2();
        }

        @Override
        public FlipableGameState flip() {
            return ADVANTAGE_PLAYER1;
        }

        @Override
        public String toString() {
            return "Advantage player2";
        }

    }

    private static abstract class WonGame implements FlipableGameState {

        @Override
        public String getScore(GameState.GameDisplayContext context) {
            return "Win for " + getPlayer(context);
        }

        @Override
        public GameState player1WonPoint(GameDisplayContext context) {
            throw alreadyWon(context);
        }

        private IllegalStateException alreadyWon(GameDisplayContext context) {
            return new IllegalStateException("game is already won by " + getPlayer(context));
        }

        @Override
        public GameState player2WonPoint(GameDisplayContext context) {
            throw alreadyWon(context);
        }

        protected abstract String getPlayer(GameState.GameDisplayContext context);

    }

    private static final class WinPlayer1 extends WonGame {

        @Override
        protected String getPlayer(GameState.GameDisplayContext context) {
            return context.getPlayer1();
        }

        @Override
        public FlipableGameState flip() {
            return WIN_PLAYER2;
        }

        @Override
        public String toString() {
            return "Win for player1";
        }

    }

    private static final class WinPlayer2 extends WonGame {

        @Override
        protected String getPlayer(GameState.GameDisplayContext context) {
            return context.getPlayer2();
        }

        @Override
        public FlipableGameState flip() {
            return WIN_PLAYER1;
        }

        @Override
        public String toString() {
            return "Win for player2";
        }

    }

}
