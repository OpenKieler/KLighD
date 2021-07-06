/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 *
 * Copyright 2013 by
 * + Kiel University
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 *
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klighd.test;

import static de.cau.cs.kieler.klighd.util.ModelingUtil.eAllContainers;
import static de.cau.cs.kieler.klighd.util.ModelingUtil.eAllContentsOfType;
import static de.cau.cs.kieler.klighd.util.ModelingUtil.eAllContentsOfType2;
import static de.cau.cs.kieler.klighd.util.ModelingUtil.selfAndEAllContainers;
import static de.cau.cs.kieler.klighd.util.ModelingUtil.selfAndEAllContentsOfType;
import static de.cau.cs.kieler.klighd.util.ModelingUtil.selfAndEAllContentsOfType2;

import java.util.Iterator;

import org.eclipse.emf.ecore.EObject;
import org.hamcrest.Factory;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.cau.cs.kieler.klighd.kgraph.KGraphElement;
import de.cau.cs.kieler.klighd.kgraph.KLabel;
import de.cau.cs.kieler.klighd.kgraph.KNode;
import de.cau.cs.kieler.klighd.kgraph.util.KGraphUtil;
import de.cau.cs.kieler.klighd.krendering.KContainerRendering;
import de.cau.cs.kieler.klighd.krendering.KRendering;
import de.cau.cs.kieler.klighd.krendering.KRenderingFactory;
import de.cau.cs.kieler.klighd.krendering.KText;



/**
 * Test class testing certain methods in ModelingUtil.
 *
 * @author chsch
 */
public class ModelingUtilTest {

    private static KNode eAllContainerTestObject;
    private static KNode eAllContentsOfType2Object;

    // CHECKSTYLEOFF Javadoc|MagicNumber|Name

    @BeforeClass
    public static void initialize() {
        final KRenderingFactory rFac = KRenderingFactory.eINSTANCE;


        final KNode a = KGraphUtil.createInitializedNode();
        final KNode b = KGraphUtil.createInitializedNode();
        final KNode c = KGraphUtil.createInitializedNode();

        a.getChildren().add(b);
        b.getChildren().add(c);

        KGraphUtil.createInitializedLabel(a).getData().add(rFac.createKText());

        final KContainerRendering bRect = rFac.createKRectangle();
        bRect.getChildren().add(rFac.createKText());

        a.getData().add(rFac.createKText());
        b.getData().add(bRect);
        c.getData().add(rFac.createKText());

        eAllContainerTestObject = c;

        eAllContentsOfType2Object = a;

    }

    // Begin of the eContainer iterator tests

    @Test
    public void eAllContainerTest() {
        MatcherAssert.assertThat("",
                eAllContainers(eAllContainerTestObject),
                IsIteratorWithSize.iteratorWithSize(2));
    }

    @Test
    public void selfAndEAllContainerTest() {
        MatcherAssert.assertThat("",
                selfAndEAllContainers(eAllContainerTestObject),
                IsIteratorWithSize.iteratorWithSize(3)
        );
    }

    // Begin of the "non-pruned children iterators" tests

    // ModelingUtil.eAllContentsOfType(...) NO varArgs!

    @Test
    public void eAllContentsOfType_1() {
        MatcherAssert.assertThat("",
                eAllContentsOfType(eAllContentsOfType2Object, KNode.class),
                IsIteratorWithSize.iteratorWithSize(2)
        );

        try {
            for (final Iterator<KNode> it =
                    eAllContentsOfType(eAllContentsOfType2Object, KNode.class);
                    it.hasNext();) {

                final KNode node = it.next();
                Assert.assertNotNull(node);
            }
        } catch (final ClassCastException e) {
            Assert.fail("Caught a ClassCastException.");
        }
    }

    @Test
    public void eAllContentsOfType_2() {
        MatcherAssert.assertThat("",
                eAllContentsOfType(eAllContentsOfType2Object, KRendering.class),
                IsIteratorWithSize.iteratorWithSize(5)
        );
    }

    // ModelingUtil.eAllContentsOfType(...) with varArgs!

    @Test
    public void eAllContentsOfType_va1() {
        MatcherAssert.assertThat("",
                eAllContentsOfType(eAllContentsOfType2Object, KNode.class, KLabel.class),
                IsIteratorWithSize.iteratorWithSize(3)
        );

        try {
            for (final Iterator<KGraphElement> it =
                    eAllContentsOfType(eAllContentsOfType2Object, KNode.class, KLabel.class);
                    it.hasNext();) {

                final KGraphElement kge = it.next();
                Assert.assertNotNull(kge);
            }
        } catch (final ClassCastException e) {
            Assert.fail("Caught a ClassCastException.");
        }
    }

    @Test
    public void eAllContentsOfType_va2() {
        MatcherAssert.assertThat("",
                eAllContentsOfType(eAllContentsOfType2Object, KLabel.class, KRendering.class),
                IsIteratorWithSize.iteratorWithSize(6)
        );
    }

    // ModelingUtil.selfAndEAllContentsOfType(...) NO varArgs!

    @Test
    public void selfAndEAllContentsOfType_1() {
        MatcherAssert.assertThat("",
                selfAndEAllContentsOfType(eAllContentsOfType2Object, KNode.class),
                IsIteratorWithSize.iteratorWithSize(3)
        );
    }

    @Test
    public void selfAndEAllContentsOfType_2() {
        MatcherAssert.assertThat("",
                selfAndEAllContentsOfType(eAllContentsOfType2Object, KRendering.class),
                IsIteratorWithSize.iteratorWithSize(6)
        );

        try {
            for (final Iterator<EObject> it =
                    selfAndEAllContentsOfType(eAllContentsOfType2Object, KRendering.class);
                    it.hasNext();) {

                final EObject kge = it.next();
                Assert.assertNotNull(kge);
            }
        } catch (final ClassCastException e) {
            Assert.fail("Caught a ClassCastException.");
        }
    }

    @Test(expected = ClassCastException.class)
    public void selfAndEAllContentsOfType_3() {
        // an example of the wrong type parameter usage:
        //  expects Iterator of KRendering(s) instead of a common super type of KNode & KRendering

        for (final Iterator<KRendering> it =
                selfAndEAllContentsOfType(eAllContentsOfType2Object, KRendering.class);
                it.hasNext();) {

            final KRendering kge = it.next();
            Assert.assertNotNull(kge);
        }
    }

    // ModelingUtil.selfAndEAllContentsOfType(...) with varArgs!

    @Test
    public void selfAndEAllContentsOfType_va1() {
        MatcherAssert.assertThat("",
                selfAndEAllContentsOfType(eAllContentsOfType2Object, KNode.class, KLabel.class),
                IsIteratorWithSize.iteratorWithSize(4)
        );

        try {
            for (final Iterator<KGraphElement> it =
                    selfAndEAllContentsOfType(eAllContentsOfType2Object, KNode.class, KLabel.class);
                    it.hasNext();) {

                final KGraphElement kge = it.next();
                Assert.assertNotNull(kge);
            }
        } catch (final ClassCastException e) {
            Assert.fail("Caught a ClassCastException.");
        }
    }

    @Test
    public void selfAndEAllContentsOfType_va2() {
        MatcherAssert.assertThat("",
                selfAndEAllContentsOfType(eAllContentsOfType2Object, KLabel.class, KRendering.class),
                IsIteratorWithSize.iteratorWithSize(7)
        );
    }

    // Begin of the "pruned children iterators" tests

    // ModelingUtil.eAllContentsOfType2(...) NO varArgs!

    @Test
    public void eAllContentsOfType2_1() {
        MatcherAssert.assertThat("",
                eAllContentsOfType2(eAllContentsOfType2Object, KNode.class),
                IsIteratorWithSize.iteratorWithSize(2)
        );

        try {
            for (final Iterator<KNode> it =
                    eAllContentsOfType2(eAllContentsOfType2Object, KNode.class);
                    it.hasNext();) {

                final KNode node = it.next();
                Assert.assertNotNull(node);
            }
        } catch (final ClassCastException e) {
            Assert.fail("Caught a ClassCastException.");
        }
    }

    @Test
    public void eAllContentsOfType2_2() {
        MatcherAssert.assertThat("",
                eAllContentsOfType2(eAllContentsOfType2Object, KRendering.class),
                IsIteratorWithSize.iteratorWithSize(1)
        );
    }

    // ModelingUtil.eAllContentsOfType2(...) WITH varArgs!

    @Test
    public void eAllContentsOfType2_va1() {
        MatcherAssert.assertThat("",
                eAllContentsOfType2(eAllContentsOfType2Object, KNode.class, KLabel.class),
                IsIteratorWithSize.iteratorWithSize(3)
        );

        try {
            for (final Iterator<KGraphElement> it =
                    eAllContentsOfType2(eAllContentsOfType2Object, KNode.class, KLabel.class);
                    it.hasNext();) {

                final KGraphElement kge = it.next();
                Assert.assertNotNull(kge);
            }
        } catch (final ClassCastException e) {
            Assert.fail("Caught a ClassCastException.");
        }
    }

    @Test
    public void eAllContentsOfType2_va2() {
        MatcherAssert.assertThat("",
                eAllContentsOfType2(eAllContentsOfType2Object, KGraphElement.class, KRendering.class),
                IsIteratorWithSize.iteratorWithSize(8)
        );
    }

    // ModelingUtil.selfAndEAllContentsOfType2(...) NO varArgs!

    @Test
    public void selfAndEAllContentsOfType2_1() {
        MatcherAssert.assertThat("",
                selfAndEAllContentsOfType2(eAllContentsOfType2Object, KNode.class),
                IsIteratorWithSize.iteratorWithSize(3)
        );
    }

    @Test
    public void selfAndEAllContentsOfType2_2() {
        MatcherAssert.assertThat("",
                selfAndEAllContentsOfType2(eAllContentsOfType2Object, KRendering.class),
                IsIteratorWithSize.iteratorWithSize(2)
        );

        try {
            for (final Iterator<EObject> it =
                    selfAndEAllContentsOfType(eAllContentsOfType2Object, KRendering.class);
                    it.hasNext();) {

                final EObject kge = it.next();
                Assert.assertNotNull(kge);
            }
        } catch (final ClassCastException e) {
            Assert.fail("Caught a ClassCastException.");
        }
    }

    @Test(expected = ClassCastException.class)
    public void selfAndEAllContentsOfType2_3() {
        // an example of the wrong type parameter usage:
        //  expects Iterator of KRendering(s) instead of a common super type of KNode & KRendering

        for (final Iterator<KRendering> it =
                selfAndEAllContentsOfType2(eAllContentsOfType2Object, KRendering.class);
                it.hasNext();) {

            final KRendering kge = it.next();
            Assert.assertNotNull(kge);
        }
    }

    // ModelingUtil.selfAndEAllContentsOfType2(...) WITH varArgs!

    @Test
    public void selfAndEAllContentsOfType2_va1() {
        MatcherAssert.assertThat("",
                selfAndEAllContentsOfType2(eAllContentsOfType2Object, KNode.class, KText.class),
                IsIteratorWithSize.iteratorWithSize(5)
        );

        try {
            for (final Iterator<EObject> it =
                    selfAndEAllContentsOfType2(eAllContentsOfType2Object, KNode.class, KText.class);
                    it.hasNext();) {

                final EObject kge = it.next();
                Assert.assertNotNull(kge);
            }
        } catch (final ClassCastException e) {
            Assert.fail("Caught a ClassCastException.");
        }
    }

    @Test
    public void selfAndEAllContentsOfType2_va2() {
        MatcherAssert.assertThat("",
                selfAndEAllContentsOfType2(eAllContentsOfType2Object, KGraphElement.class, KText.class),
                IsIteratorWithSize.iteratorWithSize(7)
        );
    }


    /**
     * Analogously to {@link org.hamcrest.collection.IsIterableWithSize IsIterableWithSize}.
     */
    public static class IsIteratorWithSize<E> extends FeatureMatcher<Iterator<? extends E>, Integer> {

        /**
         * Creates a matcher for {@link Iterator}s that matches when a single pass over the examined
         * {@link Iterator} yields an item count that is equal to the specified <code>size</code>
         * argument.
         * <p/>
         * For example:
         *
         * <pre>
         * assertThat(Arrays.asList(&quot;foo&quot;, &quot;bar&quot;), iterableWithSize(2))
         * </pre>
         *
         * @param size
         *            the number of items that should be yielded by an examined {@link Iterable}
         */
        @Factory
        public static <E> Matcher<Iterator<? extends E>> iteratorWithSize(final int size) {
            return new IsIteratorWithSize<E>(IsEqual.equalTo(size));
        }

        public IsIteratorWithSize(final Matcher<? super Integer> sizeMatcher) {
            super(sizeMatcher, "an iterator with size", "iterator size");
        }

        /** measured size */
        private int size = -1;

        @Override
        protected Integer featureValueOf(final Iterator<? extends E> iterator) {
            // since iterators can be iterated only once we need keep the result in mind
            //  as this method is recalled while composing the failure description!
            if (size != -1) {
                return size;

            } else {
                size = 0;
                for (; iterator.hasNext(); iterator.next()) {
                    size++;
                }
                return size;
            }
        }
    }
}