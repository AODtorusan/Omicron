package com.lyndir.omicron.api.util;

import com.google.common.collect.ImmutableSet;
import com.lyndir.lhunath.opal.system.logging.Logger;
import com.lyndir.lhunath.opal.system.util.NNFunctionNN;
import com.lyndir.lhunath.opal.system.util.PredicateNN;
import java.util.*;
import java.util.stream.Stream;
import javax.annotation.Nonnull;


public abstract class PathUtils {

    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = Logger.get( PathUtils.class );

    /**
     * A breath-first search from root.
     *
     * @param root               The object to start the search from.
     * @param foundFunction      The function that checks a neighbouring object to see if it's the object we're looking for.
     * @param costFunction       The function that determines the cost for navigating from a given object to a given neighbouring object.
     * @param maxCost            The maximum cost of a path.  Any paths that cost more than this amount are abandoned.
     * @param neighboursFunction The function that determines what an object's direct neighbours are.
     * @param <E>                The type of objects we're searching.
     *
     * @return An optional path to the found object, or empty if no path was found (no neighbours left or all paths too expensive).
     */
    public static <E, R extends E> Optional<Path<E>> find(final R root, final PredicateNN<E> foundFunction,
                                             final NNFunctionNN<Step<E>, Double> costFunction, final double maxCost,
                                             final NNFunctionNN<E, Stream<? extends E>> neighboursFunction) {

        // Test the root.
        if (foundFunction.apply( root )) {
            logger.trc( "found root: %s", root );
            return Optional.of( new Path<>( root, 0 ) );
        }

        // Initialize breath-first.
        Set<E> testedNodes = new HashSet<>();
        Deque<Path<E>> testPaths = new LinkedList<>();
        testPaths.addLast( new Path<>( root, 0 ) );
        testedNodes.add( root );

        // Search breath-first.
        while (!testPaths.isEmpty()) {
            Path<E> testPath = testPaths.removeFirst();

            // Check each neighbour.
            Iterator<? extends E> neighboursIt = neighboursFunction.apply( testPath.getTarget() ).iterator();
            while (neighboursIt.hasNext()) {
                E neighbour = neighboursIt.next();
                if (!testedNodes.add( neighbour ))
                    // Neighbour was already tested.
                    continue;

                double neighbourCost = testPath.getCost() + costFunction.apply( new Step<>( testPath.getTarget(), neighbour ) );
                if (neighbourCost > maxCost) {
                    // Stepping to neighbour from here would exceed maximum cost.
                    logger.trc( "neighbour exceeds maximum cost (%.2f > %.2f): %s", neighbourCost, maxCost, neighbour );
                    continue;
                }

                // Did we find the target?
                Path<E> neighbourPath = new Path<>( testPath, neighbour, neighbourCost );
                if (foundFunction.apply( neighbour )) {
                    logger.trc( "found neighbour at cost %.2f: %s", neighbourCost, neighbour );
                    return Optional.of( neighbourPath );
                }
                logger.trc( "intermediate neighbour at cost %.2f: %s", neighbourCost, neighbour );

                // Neighbour is not the target, add it for testing its neighbours later.
                testPaths.add( neighbourPath );
            }
        }

        return Optional.empty();
    }

    /**
     * A variation of the breath-first search from root which just enumerates all the objects around root.
     *
     * @param root               The object to start the search from.
     * @param radius             The maximum distance of an object.  Any objects farther removed from the root than the radius are
     *                           abandoned
     *                           and not included.
     * @param neighboursFunction The function that determines what an object's direct neighbours are.
     * @param <E>                The type of objects we're searching.
     *
     * @return A collection of the object's neighbours.
     */
    public static <E> Collection<E> neighbours(final E root, final int radius, final NNFunctionNN<E, Iterable<? extends E>> neighboursFunction) {

        if (radius == 0)
            return ImmutableSet.of( root );

        // Initialize breath-first.
        Set<E> neighbours = new HashSet<>();
        Deque<Path<E>> testPaths = new LinkedList<>();
        testPaths.addLast( new Path<>( root, 0 ) );
        neighbours.add( root );

        // Search breath-first.
        while (!testPaths.isEmpty()) {
            Path<E> testPath = testPaths.removeFirst();

            // Check each neighbour.
            for (final E neighbour : neighboursFunction.apply( testPath.getTarget() )) {
                if (!neighbours.add( neighbour ))
                    // Neighbour was already tested.
                    continue;

                double neighbourDistance = testPath.getCost() + 1;
                if (neighbourDistance > radius) {
                    // Stepping to neighbour from here would exceed maximum cost.
                    logger.trc( "neighbour exceeds radius (%.2f > %.2f): %s", neighbourDistance, radius, neighbour );
                    continue;
                }

                // Add it for testing its neighbours later.
                logger.trc( "neighbour at distance %.2f: %s", neighbourDistance, neighbour );
                testPaths.add( new Path<>( testPath, neighbour, neighbourDistance ) );
            }
        }

        return neighbours;
    }

    public static class Path<E> {

        private final Optional<Path<E>> parent;
        private final E                 target;
        private final double            cost;

        Path(final E target, final double cost) {
            parent = Optional.empty();
            this.target = target;
            this.cost = cost;
        }

        Path(@Nonnull final Path<E> parent, final E target, final double cost) {
            this.parent = Optional.of( parent );
            this.target = target;
            this.cost = cost;
        }

        public Optional<Path<E>> getParent() {
            return parent;
        }

        public double getCost() {
            return cost;
        }

        public E getTarget() {
            return target;
        }
    }


    public static class Step<E> {

        private final E from;
        private final E to;

        Step(final E from, final E to) {
            this.from = from;
            this.to = to;
        }

        public E getFrom() {
            return from;
        }

        public E getTo() {
            return to;
        }
    }
}
