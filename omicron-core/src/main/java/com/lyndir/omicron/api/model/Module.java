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

import com.google.common.base.Preconditions;
import com.lyndir.lhunath.opal.system.logging.Logger;
import com.lyndir.lhunath.opal.system.util.MetaObject;
import com.lyndir.lhunath.opal.system.util.ObjectMeta;
import com.lyndir.omicron.api.util.PathUtils;


public abstract class Module extends MetaObject {

    @ObjectMeta(ignoreFor = ObjectMeta.For.all)
    protected final Logger logger = Logger.get( getClass() );

    private final ImmutableResourceCost resourceCost;

    private GameObject gameObject;

    protected Module(final ImmutableResourceCost resourceCost) {
        this.resourceCost = resourceCost;
        Preconditions.checkState( getType().getModuleType().isInstance( this ), "Invalid module type for module: %s", this );
    }

    public ImmutableResourceCost getResourceCost() {
        return resourceCost;
    }

    void setGameObject(final GameObject gameObject) {
        this.gameObject = gameObject;
    }

    public GameObject getGameObject() {
        return Preconditions.checkNotNull( gameObject, "This module has not yet been initialized by its game object." );
    }

    protected GameController getGameController() {
        return getGameObject().getGame().getController();
    }

    void assertOwned() {
        Security.assertOwned( getGameObject() );
    }

    void assertObservable() {
        Security.assertObservable( getGameObject().getLocation() );
    }

    protected abstract void onReset();

    protected abstract void onNewTurn();

    public abstract ModuleType<?> getType();


    public static class ImpossibleException extends IncompatibleStateException {

         ImpossibleException() {
            super( "Action not possible." );
        }
    }


    public static class InvalidatedException extends IncompatibleStateException {

         InvalidatedException() {
            super( "State change has invalidated this action." );
        }
    }


    public static class PathInvalidatedException extends InvalidatedException {

        private final PathUtils.Path<Tile> path;

         PathInvalidatedException(final PathUtils.Path<Tile> path) {
            this.path = path;
        }

        /**
         * @return The path to the point where it has become invalidated (target is the invalid tile).
         */
        public PathUtils.Path<Tile> getPath() {
            return path;
        }
    }
}
