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

import static com.lyndir.omicron.api.core.CoreUtils.*;
import static com.lyndir.omicron.api.core.Security.*;

import com.google.common.base.*;
import com.google.common.collect.ImmutableSet;
import com.google.common.hash.Hashing;
import com.lyndir.lhunath.opal.system.util.*;
import com.lyndir.omicron.api.Authenticated;
import com.lyndir.omicron.api.ChangeInt;
import com.lyndir.omicron.api.core.Color;
import com.lyndir.omicron.api.core.*;
import com.lyndir.omicron.api.util.Maybool;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * <i>10 15, 2012</i>
 *
 * @author lhunath
 */
public class Player extends MetaObject implements IPlayer {

    private static final String[] firstNames = { "Jack", "Daniel", "Derrick", "Yasmin", "Catherin", "Mary" };
    private static final String[] lastNames  = { "Taylor", "Smith", "Brown", "Wilson", "Jones", "Lee" };
    private static final Random   random     = new Random();

    @ObjectMeta(ignoreFor = ObjectMeta.For.all)
    private final PlayerController controller = new PlayerController( this );

    private final int       playerID;
    @ObjectMeta(ignoreFor = ObjectMeta.For.all)
    private final PlayerKey key;
    private final String    name;
    @ObjectMeta(ignoreFor = ObjectMeta.For.toString)
    private final Color     primaryColor;
    @ObjectMeta(ignoreFor = ObjectMeta.For.toString)
    private final Color     secondaryColor;
    @ObjectMeta(ignoreFor = ObjectMeta.For.all)
    private final Map<Integer, GameObject> objects = Collections.synchronizedMap( new HashMap<Integer, GameObject>() );

    @ObjectMeta(ignoreFor = ObjectMeta.For.all)
    private int score;
    @ObjectMeta(ignoreFor = ObjectMeta.For.all)
    private int nextObjectSeed;

    public Player(final int playerID, @Nullable final PlayerKey key, final String name, final Color primaryColor,
                  final Color secondaryColor) {
        this.playerID = playerID;
        this.key = key;
        this.name = name;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
    }

    @Override
    public int hashCode() {
        return playerID;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        return obj instanceof IPlayer && playerID == ((IPlayer) obj).getPlayerID();
    }

    @Override
    @Nonnull
    public PlayerController getController() {
        return controller;
    }

    @Override
    public int getPlayerID() {
        return playerID;
    }

    @Override
    public boolean hasKey(final PlayerKey playerKey) {
        return ObjectUtils.isEqual( key, playerKey );
    }

    boolean isKeyLess() {
        return key == null;
    }

    boolean isCurrentPlayer() {
        return ObjectUtils.isEqual( this, currentPlayer() );
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Color getPrimaryColor() {
        return primaryColor;
    }

    @Override
    public Color getSecondaryColor() {
        return secondaryColor;
    }

    ImmutableSet<GameObject> getObjects() {
        return ImmutableSet.copyOf( objects.values() );
    }

    public static String randomName() {
        return Joiner.on( ' ' ).join( firstNames[random.nextInt( firstNames.length )], lastNames[random.nextInt( lastNames.length )] );
    }

    @Override
    public int getScore() {
        return score;
    }

    void setScore(final int score) {
        ChangeInt.From scoreChange = ChangeInt.from( this.score );

        this.score = score;

        getController().getGameController().fire().onPlayerScore( this, scoreChange.to( this.score ) );
    }

    int nextObjectID() {
        return Hashing.murmur3_32().newHasher().putInt( playerID ).putInt( nextObjectSeed++ ).hash().asInt();
    }

    @Nonnull
    Optional<GameObject> getObject(final int objectId) {
        return Optional.fromNullable( objects.get( objectId ) );
    }

    void removeObject(final IGameObject gameObject) {
        IGameObject lostObject = objects.remove( gameObject.getObjectID() );
        Preconditions.checkState( lostObject == null || lostObject == gameObject );

        if (lostObject != null)
            getController().getGameController().fireIfPlayer( new PredicateNN<Player>() {
                @Override
                public boolean apply(@Nonnull final Player player) {
                    return ObjectUtils.isEqual( Player.this, player );
                }
            } ).onPlayerLostObject( this, gameObject );
    }

    void addObjects(final IGameObject gameObject) {
        GameObject previousObject = objects.put( gameObject.getObjectID(), coreGO( gameObject ) );
        Preconditions.checkState( previousObject == null || previousObject == gameObject );

        //noinspection VariableNotUsedInsideIf
        if (previousObject == null)
            getController().getGameController().fireIfPlayer( new PredicateNN<Player>() {
                @Override
                public boolean apply(@Nonnull final Player player) {
                    return ObjectUtils.isEqual( Player.this, player );
                }
            } ).onPlayerGainedObject( this, gameObject );
    }

    void addObjects(final IGameObject... gameObjects) {
        for (final IGameObject gameObject : gameObjects)
            addObjects(gameObject);
    }
}
