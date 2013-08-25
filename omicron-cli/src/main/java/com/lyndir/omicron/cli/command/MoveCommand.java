package com.lyndir.omicron.cli.command;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.*;
import com.lyndir.lhunath.opal.system.util.ConversionUtils;
import com.lyndir.omicron.api.model.*;
import com.lyndir.omicron.api.util.Maybe;
import com.lyndir.omicron.cli.OmicronCLI;
import java.util.Iterator;


/**
 * <i>10 07, 2012</i>
 *
 * @author lhunath
 */
@CommandGroup(name = "move", abbr = "mv", desc = "Move game objects around in the level.")
public class MoveCommand extends Command {

    public MoveCommand(final OmicronCLI omicron) {
        super( omicron );
    }

    @Override
    public void evaluate(final Iterator<String> tokens) {

        final Optional<IPlayer> localPlayerOptional = getOmicron().getLocalPlayer();
        if (!localPlayerOptional.isPresent()) {
            err( "No local player in the game." );
            return;
        }
        final IPlayer localPlayer = localPlayerOptional.get();

        String objectIDArgument = Iterators.getNext( tokens, null );
        if (objectIDArgument == null) {
            err( "Missing objectID.  Syntax: objectID side/level" );
            return;
        }
        if ("help".equals( objectIDArgument )) {
            inf( "Usage: objectID side/level" );
            inf( "    objectID: The ID of the object to move (see list objects)." );
            inf( "  side/level: The side of the object's current tile or level to move to." );
            return;
        }

        int objectId = ConversionUtils.toIntegerNN( objectIDArgument );
        String sideArgument = Iterators.getNext( tokens, null );
        if (sideArgument == null) {
            err( "Missing side/level.  Syntax: objectID side/level" );
            return;
        }

        Optional<Coordinate.Side> side = Coordinate.Side.forName( sideArgument );
        if (!side.isPresent()) {
            err( "No such side/level: %s.  Valid sides are: %s, valid levels are: %s", side, //
                 FluentIterable.from( ImmutableList.copyOf( Coordinate.Side.values() ) )
                               .transform( new Function<Coordinate.Side, String>() {
                                   @Override
                                   public String apply(final Coordinate.Side input) {

                                       return input.name();
                                   }
                               } ), //
                 FluentIterable.from( ImmutableList.copyOf( LevelType.values() ) ).transform( new Function<LevelType, String>() {
                     @Override
                     public String apply(final LevelType input) {

                         return input.name();
                     }
                 } ) );
            return;
        }

        // Find the game object for the given ID.
        Maybe<? extends IGameObject> maybeObject = localPlayer.getController().getObject( objectId );
        if (maybeObject.presence() != Maybe.Presence.PRESENT) {
            err( "No observable object with ID: %s", objectId );
            return;
        }
        IGameObject gameObject = maybeObject.get();

        // Check to see if it's mobile by finding its mobility module.
        Optional<IMobilityModule> optionalMobility = gameObject.getModule( PublicModuleType.MOBILITY, 0 );
        if (!optionalMobility.isPresent()) {
            err( "Object is not mobile: %s", gameObject );
            return;
        }
        IMobilityModule mobilityModule = optionalMobility.get();

        Maybe<? extends ITile> maybeLocation = mobilityModule.getGameObject().checkLocation();
        if (maybeLocation.presence() != Maybe.Presence.PRESENT) {
            err( "Object's location is not known: %s", gameObject );
            return;
        }
        ITile location = maybeLocation.get();

        Coordinate targetPosition = side.get().delta( location.getPosition() );
        Optional<? extends ITile> targetLocation = location.getLevel().getTile( targetPosition );
        if (!targetLocation.isPresent()) {
            err( "No tile at that side for position: %s.", targetPosition );
            return;
        }

        // Move the object.
        mobilityModule.movement( targetLocation.get() );
        inf( "Object is now at: %s", gameObject.checkLocation() );
    }
}
