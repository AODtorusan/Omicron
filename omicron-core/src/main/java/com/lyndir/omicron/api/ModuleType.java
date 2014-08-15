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

import com.lyndir.lhunath.opal.system.error.InternalInconsistencyException;
import com.lyndir.lhunath.opal.system.logging.Logger;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import javax.annotation.Nonnull;


/**
 * @author lhunath, 2013-08-02
 */
@SuppressWarnings("FieldNameHidesFieldInSuperclass")
public abstract class ModuleType<M extends IModule> extends PublicModuleType<M> {

    static final Logger logger = Logger.get( ModuleType.class );

    public static final ModuleType<ExtractorModule>   EXTRACTOR   = //
            new ModuleType<ExtractorModule>( ExtractorModule.class, PublicModuleType.EXTRACTOR.getStandardCost() ) {};
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    public static final ModuleType<ContainerModule>   CONTAINER   = //
            new ModuleType<ContainerModule>( ContainerModule.class, PublicModuleType.CONTAINER.getStandardCost() ) {};
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    public static final ModuleType<MobilityModule>    MOBILITY    = //
            new ModuleType<MobilityModule>( MobilityModule.class, PublicModuleType.MOBILITY.getStandardCost() ) {};
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    public static final ModuleType<ConstructorModule> CONSTRUCTOR = //
            new ModuleType<ConstructorModule>( ConstructorModule.class, PublicModuleType.CONSTRUCTOR.getStandardCost() ) {};
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    public static final ModuleType<BaseModule>        BASE        = //
            new ModuleType<BaseModule>( BaseModule.class, PublicModuleType.BASE.getStandardCost() ) {};
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    public static final ModuleType<WeaponModule>      WEAPON      = //
            new ModuleType<WeaponModule>( WeaponModule.class, PublicModuleType.WEAPON.getStandardCost() ) {};

    private ModuleType(@Nonnull final Class<M> moduleType, @Nonnull final ImmutableResourceCost standardCost) {
        super( moduleType, standardCost );
    }

    public static <M extends IModule> ModuleType<M> of(final PublicModuleType<M> moduleType) {
        if (moduleType instanceof ModuleType)
            return (ModuleType<M>) moduleType;

        for (final Field field : ModuleType.class.getFields())
            if (Modifier.isStatic( field.getModifiers() ))
                if (ModuleType.class.isAssignableFrom( field.getType() ))
                    try {
                        ModuleType<?> coreModuleType = (ModuleType<?>) field.get( null );
                        if (moduleType.getModuleType().isAssignableFrom( coreModuleType.getModuleType() ))
                            //noinspection unchecked
                            return (ModuleType<M>) coreModuleType;
                    }
                    catch (final IllegalAccessException e) {
                        throw new InternalInconsistencyException( "Expected field to contain a core module: " + field, e );
                    }

        throw new InternalInconsistencyException( "No core module field found for module type: " + moduleType );
    }
}
