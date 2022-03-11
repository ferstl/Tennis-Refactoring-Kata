import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

interface GameState {

    static final GameState ADVANTAGE_PLAYER1 = new AdvantagePlayer1();

    static final GameState ADVANTAGE_PLAYER2 = new AdvantagePlayer2();

    static final GameState WIN_PLAYER1 = new WinPlayer1();

    static final GameState WIN_PLAYER2 = new WinPlayer2();

    static final GameState DEUCE = new Deuce();

    GameState player1WonPoint(GameContext context);
    GameState player2WonPoint(GameContext context);
    String getScore(GameContext context);
    GameState flip();

    interface GameContext {

        String getPlayer1();

        String getPlayer2();

    }

    static GameState getInitialState() {
        Map<ScoreLookupKey, GameState> computed = new HashMap<>();
//        computed.put(new ScoreLookupKey(2, 2), new ThirtyAll());

        computed.put(new ScoreLookupKey(3, 3), DEUCE);
        computed.put(new ScoreLookupKey(4, 4), DEUCE);

        computed.put(new ScoreLookupKey(4, 3), ADVANTAGE_PLAYER1);
        computed.put(new ScoreLookupKey(3, 4), ADVANTAGE_PLAYER2);


        for (int i = 0; i <= 2; i++) {
            ScoreLookupKey key = new ScoreLookupKey(4, i);
            computed.put(key, WIN_PLAYER1);
            computed.put(key.flipPlayerScores(), WIN_PLAYER2);
        }

        for (int i = 4; i > 0; i--) {
            for (int j = i; j >= 0; j--) {
                ScoreLookupKey key = new ScoreLookupKey(i, j);
                if (!computed.containsKey(key)) {
                    GameState player1Wins = i == 4 ? WIN_PLAYER1 : computed.get(key.incrementPlayer1());
                    GameState player2Wins = computed.get(key.incrementPlayer2());
                    GenericGameState state = new GenericGameState(i, j, player1Wins, player2Wins);
                    computed.put(key, state);
                    if (i != j) {
                        // if i-j wasn't in the map, j-i will also not be in it, no need to check
                        computed.put(key.flipPlayerScores(), state.flip());
                    }
                }
            }

        }
        return new GenericGameState(0, 0, computed.get(new ScoreLookupKey(1, 0)), computed.get(new ScoreLookupKey(0, 1)));
    }

    static final class ScoreLookupKey {

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
            return Objects.hash(scorePlayer1, scorePlayer2);
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
            return scorePlayer1 == other.scorePlayer1 && scorePlayer2 == other.scorePlayer2;
        }

        @Override
        public String toString() {
            return "(" + this.scorePlayer1 + ", " + this.scorePlayer2 + ")";
        }

    }

    static String translate(int score) {
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

    final class GenericGameState implements GameState {

        private final int scorePlayer1;

        private final int scorePlayer2;

        private final GameState player1Wins;

        private final GameState player2Wins;

        GenericGameState(int scorePlayer1, int scorePlayer2, GameState player1Wins, GameState player2Wins) {
            if (scorePlayer1 > 4 || scorePlayer2 > 4) {
                throw new IllegalArgumentException();
            }
            this.scorePlayer1 = scorePlayer1;
            this.scorePlayer2 = scorePlayer2;
            Objects.requireNonNull(player1Wins, "player1Wins");
            Objects.requireNonNull(player2Wins, "player2Wins");
            this.player1Wins = player1Wins;
            this.player2Wins = player2Wins;
        }

        @Override
        public GameState player1WonPoint(GameState.GameContext context) {
            return this.player1Wins;
        }
        @Override
        public GameState player2WonPoint(GameState.GameContext context) {
            return this.player2Wins;
        }

        @Override
        public String getScore(GameState.GameContext context) {
            return getScore();
        }

        private String getScore() {
            if (this.scorePlayer1 == this.scorePlayer2) {
                return translate(this.scorePlayer1) + "-All";
            } else {
                return translate(this.scorePlayer1) + "-" + translate(this.scorePlayer2);
            }
        }

        @Override
        public String toString() {
//            return "(" + this.scorePlayer1 + ", " + this.scorePlayer2 + ")";
            return this.getScore();
        }
        
        @Override
        public GameState flip() {
            // 3-0 (player1wins, (3,1)) -> 0-3(player2win, (1-3))
            return new GenericGameState(this.scorePlayer2, this.scorePlayer1, this.player2Wins.flip(), this.player1Wins.flip());
        }

    }

    abstract class All implements GameState {

        @Override
        public String getScore(GameState.GameContext context) {
            return getScore();
        }

        private String getScore() {
            return translate(getScoreInt()) + "-All";
        }

        protected abstract int getScoreInt();
        
        @Override
        public String toString() {
            return this.getScore();
        }

    }

    //    final class LoveAll extends All {
    //
    //        @Override
    //        protected int getScore() {
    //            return 0;
    //        }
    //
    //    }
    //
    //    final class FifteenAll extends All {
    //
    //        @Override
    //        protected int getScore() {
    //            return 1;
    //        }
    //
    //    }

    final class ThirtyAll extends All {

        @Override
        public GameState player1WonPoint(GameState.GameContext context) {
            return ADVANTAGE_PLAYER1;
        }

        @Override
        public GameState player2WonPoint(GameState.GameContext context) {
            return ADVANTAGE_PLAYER2;
        }

        @Override
        protected int getScoreInt() {
            return 2;
        }

        @Override
        public GameState flip() {
            return this;
        }

    }

    final class Deuce implements GameState {

        @Override
        public GameState player1WonPoint(GameState.GameContext context) {
            return ADVANTAGE_PLAYER1;
        }

        @Override
        public GameState player2WonPoint(GameState.GameContext context) {
            return ADVANTAGE_PLAYER2;
        }

        @Override
        public String getScore(GameState.GameContext context) {
            return this.getScore();
        }

        private String getScore() {
            return "Deuce";
        }

        @Override
        public GameState flip() {
            return this;
        }
        
        @Override
        public String toString() {
            return this.getScore();
        }

    }

    abstract class Advantage implements GameState {

        @Override
        public String getScore(GameState.GameContext context) {
            return "Advantage " + getPlayer(context);
        }

        protected abstract String getPlayer(GameState.GameContext context);

    }

    final class AdvantagePlayer1 extends Advantage {

        @Override
        public GameState player1WonPoint(GameState.GameContext context) {
            return WIN_PLAYER1;
        }

        @Override
        public GameState player2WonPoint(GameState.GameContext context) {
            return DEUCE;
        }

        @Override
        protected String getPlayer(GameState.GameContext context) {
            return context.getPlayer1();
        }

        @Override
        public GameState flip() {
            return ADVANTAGE_PLAYER2;
        }

    }

    final class AdvantagePlayer2 extends Advantage {

        @Override
        public GameState player1WonPoint(GameState.GameContext context) {
            return DEUCE;
        }

        @Override
        public GameState player2WonPoint(GameState.GameContext context) {
            return WIN_PLAYER2;
        }

        @Override
        protected String getPlayer(GameState.GameContext context) {
            return context.getPlayer2();
        }

        @Override
        public GameState flip() {
            return ADVANTAGE_PLAYER1;
        }

    }

    abstract class WonGame implements GameState {

        @Override
        public String getScore(GameState.GameContext context) {
            return "Win for " + getPlayer(context);
        }

        @Override
        public GameState player1WonPoint(GameContext context) {
            throw alreadyWon(context);
        }

        private IllegalStateException alreadyWon(GameContext context) {
            return new IllegalStateException("game is already won by " + getPlayer(context));
        }

        @Override
        public GameState player2WonPoint(GameContext context) {
            throw alreadyWon(context);
        }

        protected abstract String getPlayer(GameState.GameContext context);

    }

    final class WinPlayer1 extends WonGame {

        @Override
        protected String getPlayer(GameState.GameContext context) {
            return context.getPlayer1();
        }

        @Override
        public GameState flip() {
            return WIN_PLAYER2;
        }

    }

    final class WinPlayer2 extends WonGame {

        @Override
        protected String getPlayer(GameState.GameContext context) {
            return context.getPlayer2();
        }

        @Override
        public GameState flip() {
            return WIN_PLAYER1;
        }

    }

}
