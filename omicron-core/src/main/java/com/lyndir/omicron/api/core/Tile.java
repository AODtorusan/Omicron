package com.lyndir.omicron.api.core;

import static com.lyndir.lhunath.opal.system.util.ObjectUtils.*;
import static com.lyndir.omicron.api.core.Security.*;

import com.google.common.base.*;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.lyndir.lhunath.opal.math.Side;
import com.lyndir.lhunath.opal.math.Vec2;
import com.lyndir.lhunath.opal.system.util.*;
import com.lyndir.omicron.api.*;
import com.lyndir.omicron.api.util.Maybe;
import java.util.*;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * <i>10 07, 2012</i>
 *
 * @author lhunath
 */
@ObjectMeta(useFor = { })
public class Tile extends MetaObject implements ITile {

    @Nullable
    private       GameObject contents;
    @ObjectMeta(useFor = ObjectMeta.For.all)
    private final Vec2       position;
    @ObjectMeta(useFor = ObjectMeta.For.all)
    private final Level      level;
    @ObjectMeta(useFor = ObjectMeta.For.all)
    private final Map<ResourceType, Integer> resourceQuantities = Collections.synchronizedMap(
            new EnumMap<ResourceType, Integer>( ResourceType.class ) );

    Tile(final Vec2 position, final Level level) {
        this.position = position;
        this.level = level;
    }

    Tile(final int x, final int y, final Level level) {
        this( Vec2.create( x, y ), level );
    }

    @Override
    public int hashCode() {
        return Objects.hash( position, level );
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof Tile))
            return false;

        Tile o = (Tile) obj;
        return isEqual( position, o.position ) && isEqual( level, o.level );
    }

    @Override
    public Maybe<? extends IPlayer> checkOwner() {
        if (!isGod() || !currentPlayer().canObserve( this ).isTrue())
            return Maybe.unknown();

        if (contents == null)
            return Maybe.absent();

        return contents.checkOwner();
    }

    @Override
    public Maybe<? extends ITile> checkLocation()
            throws NotAuthenticatedException {
        if (!isGod() && !currentPlayer().canObserve( this ).isTrue())
            return Maybe.unknown();

        return Maybe.of( this );
    }

    @Nonnull
    Optional<GameObject> getContents() {
        return Optional.fromNullable( contents );
    }

    @Override
    @Nonnull
    public Maybe<GameObject> checkContents()
    throws NotAuthenticatedException {
        if (!isGod() && !currentPlayer().canObserve( this ).isTrue())
            // Cannot observe tile.
            return Maybe.unknown();

        return Maybe.fromNullable( contents );
    }

    void setContents(@Nullable final GameObject contents) {
        if (contents != null)
            Preconditions.checkState( this.contents == null || this.contents.equals( contents ),
                                      "Cannot put object on tile that is not empty: %s", this );

        replaceContents( contents );
    }

    void replaceContents(@SuppressWarnings("ParameterHidesMemberVariable") @Nullable final GameObject contents) {
        Change.From<IGameObject> contentsChange = Change.<IGameObject>from( this.contents );

        this.contents = contents;
        if (contents != null)
            contents.setLocation( this );

        getLevel().getGame().getController().fireIfObservable( this ) //
                .onTileContents( this, contentsChange.to( this.contents ) );
    }

    @Override
    public Vec2 getPosition() {
        return position;
    }

    @Override
    public Level getLevel() {
        return level;
    }

    void setResourceQuantity(final ResourceType resourceType, final int resourceQuantity) {
        Preconditions.checkArgument( resourceQuantity >= 0, "Resource quantity cannot be less than zero: %s", resourceQuantity );
        ChangeInt.From quantityChange;
        if (resourceQuantity > 0)
            quantityChange = ChangeInt.from( resourceQuantities.put( resourceType, resourceQuantity ) );
        else
            quantityChange = ChangeInt.from( resourceQuantities.remove( resourceType ) );

        getLevel().getGame().getController().fireIfObservable( this ) //
                .onTileResources( this, resourceType, quantityChange.to( resourceQuantity ) );
    }

    void addResourceQuantity(final ResourceType resourceType, final int resourceQuantity) {
        setResourceQuantity( resourceType, ifNotNullElse( resourceQuantities.get( resourceType ), 0 ) + resourceQuantity );
    }

    @Override
    @Authenticated
    public Maybe<Integer> checkResourceQuantity(final ResourceType resourceType)
            throws NotAuthenticatedException {
        if (!isGod() && !currentPlayer().canObserve( this ).isTrue())
            // Cannot observe location.
            return Maybe.unknown();

        return Maybe.fromNullable( resourceQuantities.get( resourceType ) );
    }

    /**
     * @return true if this tile has no contents.
     */
    boolean isAccessible() {
        return !getContents().isPresent();
    }

    /**
     * @return true if this tile is visible to the current player and has no contents.
     */
    @Override
    @Authenticated
    public boolean checkAccessible()
            throws NotAuthenticatedException {
        return checkContents().presence() == Maybe.Presence.ABSENT;
    }
}