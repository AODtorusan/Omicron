/*
 * Copyright 2010, Maarten Billemont
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.lyndir.omicron.api.model;

import static com.lyndir.omicron.api.model.error.ExceptionUtils.*;

import com.google.common.base.Preconditions;
import com.lyndir.lhunath.opal.system.util.ObjectUtils;
import com.lyndir.omicron.api.model.error.OmicronSecurityException;
import com.lyndir.omicron.api.util.Maybe;


/**
 * @author lhunath, 2013-08-10
 */
public final class Security {

    private static final ThreadLocal<IPlayer> currentPlayerTL = new ThreadLocal<>();

    public static void authenticate(final IPlayer currentPlayer, final PlayerKey playerKey) {
        Preconditions.checkArgument( currentPlayer.hasKey( playerKey ), "Cannot authenticate, key does not match player: ", currentPlayer );

        currentPlayerTL.set( currentPlayer );
    }

    static boolean isAuthenticated() {
        return currentPlayerTL.get() != null;
    }

    static IPlayer currentPlayer()
            throws NotAuthenticatedException {
        IPlayer currentPlayer = currentPlayerTL.get();
        assertSecure( currentPlayer != null, NotAuthenticatedException.class );
        assert currentPlayer != null;

        return currentPlayer;
    }

    public static void assertOwned(final GameObserver observer)
            throws NotAuthenticatedException, NotOwnedException {
        assertSecure( observer.getOwner().isPresent() && ObjectUtils.equals( observer.getOwner().get(), currentPlayer() ), //
                      NotOwnedException.class, observer );
    }

    public static void assertObservable(final ITile location)
            throws NotAuthenticatedException, NotObservableException {
        assertSecure( currentPlayer().canObserve( location ).isTrue(), //
                      NotObservableException.class, location );
    }

    public static void assertObservable(final IGameObject gameObject)
            throws NotAuthenticatedException, NotObservableException {
        assertSecure( gameObject.checkLocation().presence() == Maybe.Presence.PRESENT, //
                      NotObservableException.class, gameObject );
    }

    public static class NotAuthenticatedException extends OmicronSecurityException {

        NotAuthenticatedException() {
            super( "Not authenticated.  To perform this action, first authenticate using Security#authenticate." );
        }
    }


    public static class NotOwnedException extends OmicronSecurityException {

        NotOwnedException(final GameObserver observer) {
            super( "Not owned by current player: %s", observer );
        }
    }


    public static class NotObservableException extends OmicronSecurityException {

        NotObservableException(final ITile location) {
            super( "Not observable by current player: %s", location );
        }

        NotObservableException(final IGameObject gameObject) {
            super( "Not observable by current player: %s", gameObject );
        }
    }
}
