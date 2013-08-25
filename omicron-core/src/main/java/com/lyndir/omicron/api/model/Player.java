package com.lyndir.omicron.api.model;

import static com.lyndir.omicron.api.model.CoreUtils.*;

import com.google.common.base.*;
import com.lyndir.lhunath.opal.system.util.*;
import com.lyndir.omicron.api.Authenticated;
import com.lyndir.omicron.api.ChangeInt;
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
    private final Map<Integer, GameObject> objects = new HashMap<>();

    @ObjectMeta(ignoreFor = ObjectMeta.For.all)
    private int score;
    @ObjectMeta(ignoreFor = ObjectMeta.For.all)
    private int nextObjectID;

    public Player(final int playerID, @Nullable final PlayerKey key, final String name, final Color primaryColor,
                  final Color secondaryColor) {
        this.playerID = playerID;
        this.key = key;
        this.name = name;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
    }

    @Override
    @Nonnull
    public PlayerController getController() {
        return controller;
    }

    @Authenticated
    @Override
    public Maybool canObserve(@Nonnull final ITile location)
            throws Security.NotAuthenticatedException {
        return getController().canObserve( location );
    }

    @Authenticated
    @Nonnull
    @Override
    public Iterable<Tile> listObservableTiles() {
        return getController().listObservableTiles();
    }

    @Nonnull
    @Override
    public Optional<Player> getOwner() {
        return Optional.of( this );
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

    Collection<GameObject> getObjects() {
        return objects.values();
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
        return nextObjectID++;
    }

    @Nonnull
    Optional<GameObject> getObject(final int objectId) {
        return Optional.fromNullable( objects.get( objectId ) );
    }

    void removeObject(final IGameObject gameObject) {
        IGameObject lostObject = objects.remove( gameObject.getObjectID() );
        Preconditions.checkState( lostObject == null || lostObject == gameObject );

        if (lostObject != null)
            getController().getGameController().fireIfPlayer( new PredicateNN<IPlayer>() {
                @Override
                public boolean apply(@Nonnull final IPlayer input) {
                    return ObjectUtils.isEqual( Player.this, input );
                }
            } ).onPlayerLostObject( this, gameObject );
    }

    void addObject(final IGameObject gameObject) {
        GameObject previousObject = objects.put( gameObject.getObjectID(), coreGO( gameObject ) );
        Preconditions.checkState( previousObject == null || previousObject == gameObject );

        //noinspection VariableNotUsedInsideIf
        if (previousObject == null)
            getController().getGameController().fireIfPlayer( new PredicateNN<IPlayer>() {
                @Override
                public boolean apply(@Nonnull final IPlayer input) {
                    return ObjectUtils.isEqual( Player.this, input );
                }
            } ).onPlayerGainedObject( this, gameObject );
    }
}
