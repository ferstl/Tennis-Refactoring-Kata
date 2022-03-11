import java.util.Objects;

public class TennisGame1 implements TennisGame {

    private final String player1Name;
    private final String player2Name;
    private GameState gameState;
    private final GameState.GameDisplayContext gameContext;

    public TennisGame1(String player1Name, String player2Name) {
        Objects.requireNonNull(player1Name, "player1Name");
        Objects.requireNonNull(player2Name, "player2Name");
        if (player1Name.equals(player2Name)) {
            throw new IllegalArgumentException("player names must be distint but where: " + player1Name);
        }
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.gameState = InitialGameState.getInitialState();
        this.gameContext = new GameState.GameDisplayContext() {
            
            @Override
            public String getPlayer1() {
                return TennisGame1.this.player1Name;
            }
            
            @Override
            public String getPlayer2() {
                return TennisGame1.this.player2Name;
            }
        };
    }

    public void wonPoint(String playerName) {
        if (this.player1Name.equals(playerName)) {
            this.gameState = this.gameState.player1WonPoint(this.gameContext);
        } else if (this.player2Name.equals(playerName)) {
            this.gameState = this.gameState.player2WonPoint(this.gameContext);
        } else {
            throw new IllegalArgumentException("unknown player name: " + playerName);
        }
    }

    public String getScore() {
        return this.gameState.getScore(this.gameContext);
    }
}
