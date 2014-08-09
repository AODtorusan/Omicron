package com.lyndir.omicron.api.core;

import com.lyndir.omicron.api.util.Maybool;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;


/**
 * <i>10 16, 2012</i>
 *
 * @author lhunath
 */
public interface GameObserver {

    /**
     * Check whether the current object can observe given observable.
     *
     * @param observable The observable that this observer is trying to see.
     *
     * @return true if the current player is able and allowed to observe the target.
     */
    @SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
    Maybool canObserve(@Nonnull GameObservable observable);

    /**
     * Enumerate the tiles this observer can observe.
     *
     * @return All the tiles observable both by this observer and the current player.
     */
    @NotNull
    @Nonnull
    Iterable<? extends ITile> iterateObservableTiles();
}
