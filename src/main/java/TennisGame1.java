import java.util.Objects;

public class TennisGame1 implements TennisGame {

    private final String server;
    private final String receiver;
    private GameState gameState;
    private final GameState.GameDisplayContext gameContext;

    public TennisGame1(String player1Name, String player2Name) {
        Objects.requireNonNull(player1Name, "player1Name");
        Objects.requireNonNull(player2Name, "player2Name");
        if (player1Name.equals(player2Name)) {
            throw new IllegalArgumentException("player names must be distint but where: " + player1Name);
        }
        this.server = player1Name;
        this.receiver = player2Name;
        this.gameState = InitialGameState.getInitialState();
        this.gameContext = new GameState.GameDisplayContext() {

            @Override
            public String getServer() {
                return TennisGame1.this.server;
            }

            @Override
            public String getReceiver() {
                return TennisGame1.this.receiver;
            }
        };
    }

    @Override
    public void wonPoint(String playerName) {
        if (this.server.equals(playerName)) {
            this.gameState = this.gameState.serverWonPoint(this.gameContext);
        } else if (this.receiver.equals(playerName)) {
            this.gameState = this.gameState.receiverWonPoint(this.gameContext);
        } else {
            throw new IllegalArgumentException("unknown player name: " + playerName);
        }
    }

    @Override
    public String getScore() {
        return this.gameState.getScore(this.gameContext);
    }
}
