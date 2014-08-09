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

package com.lyndir.omicron.api;

import com.google.common.collect.*;
import com.lyndir.lhunath.opal.system.error.TodoException;
import com.lyndir.omicron.api.core.*;
import com.lyndir.omicron.api.view.PlayerGameInfo;


public class GameController implements IGameController {

    private final Game game;

    public GameController(final Game game) {
        this.game = game;
    }

    @Override
    public IGame getGame() {
        return game;
    }

    @Override
    public void addGameListener(final GameListener gameListener)
            throws Security.NotAuthenticatedException {
        throw new TodoException();
    }

    @Override
    public PlayerGameInfo getPlayerGameInfo(final IPlayer player)
            throws Security.NotAuthenticatedException {
        throw new TodoException();
    }

    @Override
    public ImmutableCollection<PlayerGameInfo> listPlayerGameInfo()
            throws Security.NotAuthenticatedException {
        throw new TodoException();
    }

    @Override
    public Iterable<? extends IPlayer> listPlayers() {
        throw new TodoException();
    }

    @Override
    public boolean setReady()
            throws Security.NotAuthenticatedException {
        throw new TodoException();
    }

    @Override
    public ImmutableList<? extends ILevel> listLevels() {
        throw new TodoException();
    }

    @Override
    public ImmutableSet<? extends IPlayer> listReadyPlayers() {
        throw new TodoException();
    }
}
