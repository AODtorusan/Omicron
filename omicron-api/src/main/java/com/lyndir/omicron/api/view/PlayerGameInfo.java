package com.lyndir.omicron.api.view;

import com.lyndir.omicron.api.model.IPlayer;


/**
 * <i>10 16, 2012</i>
 *
 * @author lhunath
 */
public class PlayerGameInfo {

    private final IPlayer player;
    private final boolean discovered;
    private final int     score;

    public PlayerGameInfo(final IPlayer player, final boolean discovered, final int score) {

        this.player = player;
        this.discovered = discovered;
        this.score = score;
    }

    public IPlayer getPlayer() {

        return player;
    }

    public boolean isDiscovered() {

        return discovered;
    }

    public int getScore() {

        return score;
    }

    public static PlayerGameInfo discovered(final IPlayer player, final Integer score) {

        return new PlayerGameInfo( player, true, score );
    }

    public static PlayerGameInfo undiscovered(final IPlayer player) {

        return new PlayerGameInfo( player, false, 0 );
    }
}
