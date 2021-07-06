/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 *
 * Copyright 2015 by
 * + Kiel University
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 *
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klighd.test;

import java.util.Iterator;

import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsEmptyIterable;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;

import de.cau.cs.kieler.klighd.util.Iterables2;

/**
 * Tests of methods in {@link de.cau.cs.kieler.klighd.util.Iterables2 Iterables2}.
 *
 * @author chsch
 */
public class Iterables2Test {

    private static final ImmutableList<Integer> TEST_LIST =
            ImmutableList.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

    // CHECKSTYLEOFF Javadoc|MagicNumber

    public Iterables2Test() throws Exception {
        // the tests are faster than booting the platform causing some exceptions, so...
        Thread.sleep(5);

        // I'm not kidding!
    }

    @Test(expected = IllegalArgumentException.class)
    public void skipLastNegAmount() {
        Iterables2.skipLast(TEST_LIST, -1);
    }

    @Test
    public void skipLastZeroElements() {
        Assert.assertSame(TEST_LIST, Iterables2.skipLast(TEST_LIST, 0));
    }

    public void skipLastX(final int x) {
        // System.out.println(Iterables.toString(Iterables2.skipLast(TEST_LIST, x)));

        MatcherAssert.assertThat("",Iterables2.skipLast(TEST_LIST, x), x >= TEST_LIST.size()
                ? IsEmptyIterable.emptyIterable()
                : IsIterableContainingInOrder.contains(
                        TEST_LIST.subList(0, TEST_LIST.size() - x).toArray()));

        // check the lazy evaluation, a)
        final Iterator<Integer> it1 = TEST_LIST.iterator();
        Iterables2.skipLast(it1, x);

        // 'it1' should not be touched, so
        Assert.assertEquals(Math.max(0, TEST_LIST.size()), Iterators.size(it1));

        // check the lazy evaluation, b)
        final Iterator<Integer> it2 = TEST_LIST.iterator();
        Iterables2.skipLast(it2, x).hasNext();

        // 'it2' should be ask for x elements, so
        Assert.assertEquals(Math.max(0, TEST_LIST.size() - x), Iterators.size(it2));
    }


    @Test
    public void skipLast00() throws Exception {
        skipLastX(0);
    }

    @Test
    public void skipLast01() throws Exception {
        skipLastX(1);
    }

    @Test
    public void skipLast05() throws Exception {
        skipLastX(5);
    }

    @Test
    public void skipLast09() throws Exception {
        skipLastX(9);
    }

    @Test
    public void skipLast10() throws Exception {
        skipLastX(10);
    }

    @Test
    public void skipLast11() throws Exception {
        skipLastX(11);
    }

    @Test
    public void skipLast12() throws Exception {
        skipLastX(12);
    }

    @Test
    public void skipLast13() throws Exception {
        skipLastX(13);
    }

    @Test
    public void skipLast14() throws Exception {
        skipLastX(14);
    }

    @Test
    public void skipLast15() throws Exception {
        skipLastX(15);
    }
}
