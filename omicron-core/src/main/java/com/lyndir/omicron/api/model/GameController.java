package com.lyndir.omicron.api.model;

import static com.lyndir.omicron.api.model.CoreUtils.*;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import com.lyndir.lhunath.opal.system.util.*;
import com.lyndir.omicron.api.Authenticated;
import com.lyndir.omicron.api.GameListener;
import com.lyndir.omicron.api.view.PlayerGameInfo;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class GameController implements IGameController {

    private final Game game;
    private final Map<GameListener, IPlayer> gameListeners = Collections.synchronizedMap( Maps.<GameListener, IPlayer>newHashMap() );

    GameController(final Game game) {
        this.game = game;

        for (final Player player : game.getPlayers())
            player.getController().setGameController( this );
    }

    @Override
    public Game getGame() {
        return game;
    }

    void addInternalGameListener(final GameListener gameListener) {
        gameListeners.put( gameListener, null );
    }

    @Override
    @Authenticated
    public void addGameListener(final GameListener gameListener)
            throws Security.NotAuthenticatedException {
        gameListeners.put( gameListener, Security.currentPlayer() );
    }

    /**
     * Retrieve information on a given player.
     *
     * @param player The player whose information is being requested.
     *
     * @return Information visible to the current player about the given player.
     */
    @Override
    @Authenticated
    public PlayerGameInfo getPlayerGameInfo(final IPlayer player)
            throws Security.NotAuthenticatedException {
        if (player.listObservableTiles().iterator().hasNext())
            return PlayerGameInfo.discovered( player, player.getScore() );

        return PlayerGameInfo.undiscovered( player );
    }

    @Override
    @Authenticated
    public ImmutableCollection<PlayerGameInfo> listPlayerGameInfo()
            throws Security.NotAuthenticatedException {
        ImmutableList.Builder<PlayerGameInfo> playerGameInfoBuilder = ImmutableList.builder();
        for (final IPlayer player : game.getPlayers())
            playerGameInfoBuilder.add( getPlayerGameInfo( player ) );

        return playerGameInfoBuilder.build();
    }

    @Override
    public Iterable<Player> listPlayers() {
        return game.getPlayers();
    }

    /**
     * Indicate that the current player is ready with his turn.
     *
     * @return true if this action has caused a new turn to begin.
     */
    @Override
    @Authenticated
    public boolean setReady()
            throws Security.NotAuthenticatedException {
        return setReady( coreP( Security.currentPlayer() ) );
    }

    /**
     * Indicate that the given player is ready with his turn.
     *
     * NOTE: The player must be key-less or be the currently authenticated player.
     *
     * @return true if this action has caused a new turn to begin.
     */
    boolean setReady(final Player player)
            throws Security.NotAuthenticatedException {
        if (!player.isKeyLess())
            Preconditions.checkState( ObjectUtils.isEqual( player, Security.currentPlayer() ),
                                      "Cannot set protected player ready: not authenticated.  First authenticate using Security.authenticate()." );

        Set<Player> readyPlayers = game.getReadyPlayers();
        synchronized (readyPlayers) {
            readyPlayers.add( player );
            for (final GameListener gameListener : gameListeners.keySet())
                gameListener.onPlayerReady( player );

            if (readyPlayers.containsAll( game.getPlayers() )) {
                readyPlayers.clear();
                fireNewTurn();
                return true;
            }
        }

        return false;
    }

    private void start() {
        Preconditions.checkState( !game.isRunning(), "The game cannot be started: It is already running." );

        game.setRunning( true );
        fire().onGameStarted( game );
    }

    void end(final VictoryConditionType victoryCondition, @Nullable final IPlayer victor) {
        Preconditions.checkState( game.isRunning(), "The game cannot end: It isn't running yet." );

        game.setRunning( false );
        fire().onGameEnded( game, publicVCT( victoryCondition ), victor );
    }

    private void fireNewTurn() {
        onNewTurn();

        synchronized (gameListeners) {
            for (final GameListener gameListener : gameListeners.keySet())
                gameListener.onNewTurn( game.getCurrentTurn() );
        }
    }

    protected void onNewTurn() {
        game.setCurrentTurn( new Turn( game.getCurrentTurn() ) );
        if (!game.isRunning())
            start();

        for (final Player player : game.getPlayers()) {
            player.getController().fireReset();
            player.getController().fireNewTurn();
        }
    }

    @Override
    public ImmutableList<Level> listLevels() {
        return game.listLevels();
    }

    @Override
    public ImmutableSet<Player> listReadyPlayers() {
        return ImmutableSet.copyOf( game.getReadyPlayers() );
    }

    /**
     * Get a game listener proxy to call an event on that should be fired for all game listeners.
     */
    GameListener fire() {
        return TypeUtils.newProxyInstance( GameListener.class, new InvocationHandler() {
            @Override
            @Nullable
            @SuppressWarnings("ProhibitedExceptionDeclared")
            public Object invoke(final Object proxy, final Method method, final Object[] args)
                    throws Throwable {
                synchronized (gameListeners) {
                    for (final Map.Entry<GameListener, IPlayer> gameListenerEntry : gameListeners.entrySet())
                        method.invoke( gameListenerEntry.getKey(), args );
                }

                return null;
            }
        } );
    }

    /**
     * Get a game listener proxy to call an event on that should be fired for all game listeners that are either internal or registered by
     * players that pass the playerCondition.
     *
     * @param playerCondition The predicate that should hold true for all players eligible to receive the notification.
     */
    GameListener fireIfPlayer(@Nonnull final PredicateNN<IPlayer> playerCondition) {
        return TypeUtils.newProxyInstance( GameListener.class, new InvocationHandler() {
            @Override
            @Nullable
            @SuppressWarnings("ProhibitedExceptionDeclared")
            public Object invoke(final Object proxy, final Method method, final Object[] args)
                    throws Throwable {
                synchronized (gameListeners) {
                    for (final Map.Entry<GameListener, IPlayer> gameListenerEntry : gameListeners.entrySet()) {
                        IPlayer gameListenerOwner = gameListenerEntry.getValue();
                        if (gameListenerOwner == null || playerCondition.apply( gameListenerOwner ))
                            method.invoke( gameListenerEntry.getKey(), args );
                    }
                }

                return null;
            }
        } );
    }

    /**
     * Get a game listener proxy to call an event on that should be fired for all game listeners that are either internal or registered by
     * players that can observe the given location.
     *
     * @param location The location that should be observable.
     */
    GameListener fireIfObservable(@Nonnull final Tile location) {
        return fireIfPlayer( new PredicateNN<IPlayer>() {
            @Override
            public boolean apply(@Nonnull final IPlayer input) {
                return input.canObserve( location ).isTrue();
            }
        } );
    }
}
