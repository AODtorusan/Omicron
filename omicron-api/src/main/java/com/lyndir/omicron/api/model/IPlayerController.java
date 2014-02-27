package com.lyndir.omicron.api.model;

import static com.lyndir.omicron.api.model.Security.*;

import com.lyndir.omicron.api.util.Maybe;


public interface IPlayerController extends GameObserver {

    IPlayer getPlayer();

    IGameController getGameController();

    /**
     * List the objects of this player observable by the current player.
     *
     * @return A list of game objects owned by this controller's player filtered by visibility by the current player.
     */
    Iterable<? extends IGameObject> iterateObservableObjects()
            throws NotAuthenticatedException;

    /**
     * Iterate the objects of this player that the given observer (and the current player) can observe.
     *
     *
     * @param observer The observer that we want to observe the player's objects with.
     *
     * @return An iterable of game objects owned by this controller's player.
     */
    Iterable<? extends IGameObject> iterateObservableObjects(GameObserver observer);

    Maybe<? extends IGameObject> getObject(int objectId)
            throws NotAuthenticatedException;
}
