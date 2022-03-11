/**
 * Represents the state of a game.
 * <p>
 * Offers methods for transitioning to a next state ({@link #player1WonPoint(GameContext)}
 * and {@link #player2WonPoint(GameContext)}) as well as querying the current state
 * ({@link #getScore(GameContext)}).
 * <p>
 * Instances are stateless due to {@link GameContext} and therefore thread safe.
 * <p>
 * Instances may implement {@link #toString()} for debug purposes.
 */
interface GameState {

    /**
     * Returns the next state when player1 won a point.
     * 
     * @param context the context, used for generating exception messages,
     *                not {@code null}
     * @return the next valid game state
     * @throws IllegalStateException if the game is already won by a player
     */
    GameState player1WonPoint(GameDisplayContext context);
    
    /**
     * Returns the next state when player2 won a point.
     * 
     * @param context the context, used for generating exception messages,
     *                not {@code null}
     * @return the next valid game state
     * @throws IllegalStateException if the game is already won by a player
     */
    GameState player2WonPoint(GameDisplayContext context);
    String getScore(GameDisplayContext context);

    interface GameDisplayContext {

        String getPlayer1();

        String getPlayer2();

    }

}
