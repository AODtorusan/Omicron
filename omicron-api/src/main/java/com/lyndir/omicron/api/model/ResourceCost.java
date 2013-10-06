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

import static com.lyndir.lhunath.opal.system.util.ObjectUtils.*;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.lyndir.lhunath.opal.system.util.*;
import java.util.Map;
import javax.annotation.Nonnull;


/**
 * @author lhunath, 2013-08-04
 */
public abstract class ResourceCost extends MetaObject {

    protected abstract ImmutableMap<ResourceType, Integer> getResourceQuantities();

    /**
     * Create a new zero resource cost instance.
     *
     * @return A new immutable resource cost instance.
     */
    public static ImmutableResourceCost immutable() {
        return new ImmutableResourceCost();
    }

    /**
     * Create a new resource cost initialized with the given resource costs.
     *
     * @param resourceCost The type of resources to initialize a cost with.
     *
     * @return A new immutable resource cost instance.
     */
    public static ImmutableResourceCost immutable(final ResourceCost resourceCost) {
        return new ImmutableResourceCost( resourceCost.getResourceQuantities() );
    }

    /**
     * Create a new resource cost initialized by the given amount of resources of the given type.
     *
     * @param resourceType The type of resources to initialize a cost with.
     * @param amount       The amount of resources of the given type.
     *
     * @return A new immutable resource cost instance.
     */
    public static ImmutableResourceCost immutableOf(final ResourceType resourceType, final int amount) {
        return new ImmutableResourceCost( ImmutableMap.of( resourceType, amount ) );
    }

    /**
     * Create a new zero resource cost instance.
     *
     * @return A new mutable resource cost instance.
     */
    public static MutableResourceCost mutable() {
        return new MutableResourceCost();
    }

    /**
     * Create a new resource cost initialized with the given resource costs.
     *
     * @param resourceCost The type of resources to initialize a cost with.
     *
     * @return A new mutable resource cost instance.
     */
    public static MutableResourceCost mutable(final ResourceCost resourceCost) {
        return new MutableResourceCost( resourceCost.getResourceQuantities() );
    }

    /**
     * Create a new resource cost initialized by the given amount of resources of the given type.
     *
     * @param resourceType The type of resources to initialize a cost with.
     * @param amount       The amount of resources of the given type.
     *
     * @return A new mutable resource cost instance.
     */
    public static MutableResourceCost mutableOf(final ResourceType resourceType, final int amount) {
        return new MutableResourceCost( ImmutableMap.of( resourceType, amount ) );
    }

    /**
     * Get the resource cost for the resources of the given type.
     *
     * @param resourceType The type of resources to get the cost of.
     *
     * @return The amount of resources of the given type that are required by this resource cost.
     */
    public int get(final ResourceType resourceType) {
        return ifNotNullElse( getResourceQuantities().get( resourceType ), 0 );
    }

    /**
     * Reduce the resource cost of the given type by the given amount.
     *
     * @param resourceType The type of resources to reduce the cost of.
     * @param term         The amount with which to reduce the resource cost of the given type.
     */
    public abstract ResourceCost reduce(final ResourceType resourceType, final int term);

    /**
     * Add the given term to the resource cost of the given type.
     *
     * @param resourceType The type of resources to increase the cost of.
     * @param term         The amount with which to increase the resource cost of the given type.
     */
    public abstract ResourceCost add(final ResourceType resourceType, final int term);

    /**
     * Add the cost of all resources given to this resource cost.
     *
     * @param resourceCost The resource cost to add to this resource cost.
     */
    public abstract ResourceCost add(final ResourceCost resourceCost);

    /**
     * Multiply the cost of the resources of the given type by the given factor.
     *
     * @param factor The factor to multiply with.
     */
    public abstract ResourceCost multiply(final ResourceType resourceType, final int factor);

    /**
     * Multiply the cost of all resources by the given factor.
     *
     * @param factor The factor to multiply with.
     */
    public abstract ResourceCost multiply(final int factor);

    public boolean isZero() {
        return FluentIterable.from( getResourceQuantities().values() ).filter( new PredicateNN<Integer>() {
            @Override
            public boolean apply(@Nonnull final Integer input) {
                return input > 0;
            }
        } ).isEmpty();
    }

    @Override
    public String toString() {
        return String.format( "{%s: %s}", getClass().getSimpleName(), ObjectUtils.describe( getResourceQuantities() ) );
    }
}
