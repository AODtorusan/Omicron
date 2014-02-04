package com.lyndir.omicron.api.model;

import static com.lyndir.omicron.api.util.PathUtils.*;

import com.lyndir.lhunath.opal.system.util.MetaObject;
import com.lyndir.omicron.api.Authenticated;
import com.lyndir.omicron.api.model.IMobilityModule.ILeveling;
import com.lyndir.omicron.api.model.IMobilityModule.IMovement;


public class PublicMobilityModule extends PublicModule implements IMobilityModule {

    private final IMobilityModule coreModule;

    protected PublicMobilityModule(final IMobilityModule coreModule) {
        super( coreModule );

        this.coreModule = coreModule;
    }

    @Override
    public double getRemainingSpeed()
            throws Security.NotAuthenticatedException, Security.NotObservableException {
        assertObservable();

        return coreModule.getRemainingSpeed();
    }

    /**
     * Get the speed cost related to moving around in the given level.
     *
     * @param levelType The level to move around in.
     *
     * @return The speed cost.
     */
    @Override
    public double costForMovingInLevel(final LevelType levelType)
            throws Security.NotAuthenticatedException, Security.NotObservableException {
        assertObservable();

        return coreModule.costForMovingInLevel( levelType );
    }

    /**
     * Get the speed cost related to leveling from the current level type to the given level type.
     *
     * @param levelType The level to transition to.
     *
     * @return The speed cost.
     */
    @Override
    public double costForLevelingToLevel(final LevelType levelType)
            throws Security.NotAuthenticatedException, Security.NotObservableException {
        assertObservable();

        return coreModule.costForLevelingToLevel( levelType );
    }

    /**
     * Move the unit to the given level.
     *
     * @param levelType The side of the adjacent tile relative to the current.
     */
    @Override
    @Authenticated
    public ILeveling leveling(final LevelType levelType)
            throws Security.NotAuthenticatedException, Security.NotOwnedException, Security.NotObservableException {
        assertOwned();

        return coreModule.leveling( levelType );
    }

    /**
     * Move the unit to an adjacent tile.
     *
     * @param target The side of the adjacent tile relative to the current.
     */
    @Override
    @Authenticated
    public IMovement movement(final ITile target)
            throws Security.NotAuthenticatedException, Security.NotOwnedException, Security.NotObservableException {
        assertOwned();

        return coreModule.movement( target );
    }

    @Override
    public PublicModuleType<? extends IMobilityModule> getType() {
        return PublicModuleType.MOBILITY;
    }

    public class Leveling extends MetaObject implements ILeveling {

        private final ILeveling core;

        Leveling(final ILeveling core) {
            this.core = core;
        }

        @Override
        public boolean isPossible() {
            return core.isPossible();
        }

        /**
         * The cost for executing the leveling.  If not possible, the cost is either zero if unknown or the cost for the action that
         * exceeded the module's remaining speed (not the cost of getting to the target).
         *
         * @return An amount of speed.
         */
        @Override
        public double getCost() {
            return core.getCost();
        }

        /**
         * @return The target tile after leveling.
         *
         * @throws IllegalStateException if the leveling is not possible ({@link #isPossible()} returns {@code false})
         */
        @Override
        public ITile getTarget() {
            return core.getTarget();
        }

        @Override
        @Authenticated
        public void execute()
                throws Security.NotAuthenticatedException, Security.NotOwnedException, ImpossibleException, InvalidatedException,
                       Security.NotObservableException {
            assertOwned();

            core.execute();
        }
    }


    public class Movement extends MetaObject implements IMovement {

        private final IMovement core;

        Movement(final IMovement core) {
            this.core = core;
        }

        /**
         * The cost for executing the movement.  If not possible, the cost is either zero if unknown or the cost for the action that
         * exceeded the module's remaining speed (not the cost of getting to the target).
         *
         * @return An amount of speed.
         */
        @Override
        public double getCost() {
            return core.getCost();
        }

        /**
         * @return The target tile after leveling.
         *
         * @throws IllegalStateException if the leveling is not possible ({@link #isPossible()} returns {@code false})
         */
        @Override
        public Path<? extends ITile> getPath() {
            return core.getPath();
        }

        @Override
        public boolean isPossible() {
            return core.isPossible();
        }

        @Override
        @Authenticated
        public void execute()
                throws Security.NotAuthenticatedException, Security.NotOwnedException, ImpossibleException, InvalidatedException,
                       Security.NotObservableException {
            assertOwned();

            core.execute();
        }
    }
}
