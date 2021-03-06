package com.lyndir.omicron.cli.command;

import static com.lyndir.lhunath.opal.system.util.ObjectUtils.ifNotNullElse;

import com.google.common.collect.ImmutableList;
import com.lyndir.omicron.api.*;
import com.lyndir.omicron.api.view.PlayerGameInfo;
import com.lyndir.omicron.cli.OmicronCLI;
import java.util.*;


/**
 * <i>10 07, 2012</i>
 *
 * @author lhunath
 */
@CommandGroup(name = "list", abbr = "ls", desc = "Enumerate certain types of game objects.")
public class ListCommand extends Command {

    public ListCommand(final OmicronCLI omicron) {
        super( omicron );
    }

    @SubCommand(abbr = "p", desc = "Enumerate all players in the game.")
    public void players(final Iterator<String> tokens) {

        Optional<IGameController> gameController = getOmicron().getGameController();
        if (!gameController.isPresent()) {
            err( "No game is running.  Create one with the 'create' command." );
            return;
        }

        List<PlayerGameInfo> playerGameInfos = new LinkedList<>( gameController.get().listPlayerGameInfo() );
        Collections.sort( playerGameInfos, (o1, o2) -> o1.getScore() > o2.getScore()? 1: o1.getScore() < o2.getScore()? -1: 0 );

        inf( "%20s | %s", "score", "name" );
        for (final PlayerGameInfo playerGameInfo : playerGameInfos)
            inf( "%20s | %s%s", playerGameInfo.getScore(), playerGameInfo.getPlayer().getName(),
                 playerGameInfo.isDiscovered()? "": " <undiscovered>" );
    }

    @SubCommand(abbr = "o", desc = "Enumerate all types of game objects the player can detect.")
    public void objects(final Iterator<String> tokens) {

        Optional<IGameController> gameController = getOmicron().getGameController();
        if (!gameController.isPresent()) {
            err( "No game is running.  Create one with the 'create' command." );
            return;
        }

        Optional<IPlayer> localPlayerOptional = getOmicron().getLocalPlayer();
        if (!localPlayerOptional.isPresent()) {
            err( "No local player in the game." );
            return;
        }
        IPlayer localPlayer = localPlayerOptional.get();

        ImmutableList.Builder<IGameObject> gameObjectBuilder = ImmutableList.builder();
        for (final IPlayer player : gameController.get().getGame().getPlayers())
            gameObjectBuilder.addAll( player.getController().playerObjectsObservableBy( localPlayer ).iterator() );

        inf( "%5s | %20s | (%7s: %3s, %3s) | %s", "ID", "player", "type", "x", "y", "type" );
        for (final IGameObject gameObject : gameObjectBuilder.build()) {
            ITile location = gameObject.getLocation().get();
            inf( "%5s | %20s | (%7s: %3d, %3d) | %s", //
                 gameObject.getObjectID(), ifNotNullElse( IPlayer.class, gameObject.getOwner().orElse( null ), "-" ).getName(),
                 location.getLevel().getType().getName(), location.getPosition().getX(), location.getPosition().getY(),
                 gameObject.getType().getTypeName() );
        }
    }
}
