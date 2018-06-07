/*******************************************************************************
 * Copyright (c) 20178 Microsoft Research. All rights reserved.
 *
 * The MIT License (MIT)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Contributors:
 *   Markus Alexander Kuppe - initial API and implementation
 ******************************************************************************/
package tlc2.value;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.BeforeClass;
import org.junit.Test;

import tlc2.util.FP64;
import tlc2.value.SubsetValue.CoinTossingSubsetEnumerator;
import tlc2.value.SubsetValue.SubsetEnumerator;
import util.Assert;

public class SubsetValueTest {

	private static final Value[] getValue(String... strs) {
		final List<Value> values = new ArrayList<>(strs.length);
		for (int i = 0; i < strs.length; i++) {
			values.add(new StringValue(strs[i]));
		}
		Collections.shuffle(values);
		return values.toArray(new Value[values.size()]);
	}

	@BeforeClass
	public static void setup() {
		// Needed to insert elements into java.util.Set (because of hashcode) later to
		// detect duplicates.
		FP64.Init();
	}
	
	@Test
	public void testCalcK() {
		assertEquals(0, SubsetValue.calculateK(-1.0, 0));
		assertEquals(0, SubsetValue.calculateK(-0.5, 0));
		assertEquals(0, SubsetValue.calculateK(-0.0, 0));
		assertEquals(0, SubsetValue.calculateK(-0, 0));
		assertEquals(0, SubsetValue.calculateK(0, 0));
		assertEquals(0, SubsetValue.calculateK(0.0, 0));
		assertEquals(0, SubsetValue.calculateK(1, 0));
		assertEquals(0, SubsetValue.calculateK(1.0, 0));
		assertEquals(0, SubsetValue.calculateK(1E-65, 0));
		
		assertEquals(0, SubsetValue.calculateK(-1.0, 1));
		assertEquals(0, SubsetValue.calculateK(-0.5, 1));
		assertEquals(0, SubsetValue.calculateK(-0.0, 1));
		assertEquals(0, SubsetValue.calculateK(-0, 1));
		assertEquals(0, SubsetValue.calculateK(0, 1));
		assertEquals(0, SubsetValue.calculateK(0.0, 1));
		assertEquals(1 << 1, SubsetValue.calculateK(1, 1));
		assertEquals(1 << 1, SubsetValue.calculateK(1.0, 1));
		assertEquals(1, SubsetValue.calculateK(1E-65, 1));

		assertEquals(0, SubsetValue.calculateK(-1.0, 30));
		assertEquals(0, SubsetValue.calculateK(-0.5, 30));
		assertEquals(0, SubsetValue.calculateK(-0.0, 30));
		assertEquals(0, SubsetValue.calculateK(-0, 30));
		assertEquals(0, SubsetValue.calculateK(0, 30));
		assertEquals(0, SubsetValue.calculateK(0.0, 30));
		assertEquals(1 << 30, SubsetValue.calculateK(1, 30));
		assertEquals(1 << 30, SubsetValue.calculateK(1.0, 30));
		assertEquals(1074, SubsetValue.calculateK(0.000001, 30));
		assertEquals(1, SubsetValue.calculateK(1E-65, 30));

		assertEquals(0, SubsetValue.calculateK(-1.0, 64));
		assertEquals(0, SubsetValue.calculateK(-0.5, 64));
		assertEquals(0, SubsetValue.calculateK(-0.0, 64));
		assertEquals(0, SubsetValue.calculateK(-0, 64));
		assertEquals(0, SubsetValue.calculateK(0, 64));
		assertEquals(0, SubsetValue.calculateK(0.0, 64));
		assertEquals(Integer.MAX_VALUE, SubsetValue.calculateK(1, 64));
		assertEquals(Integer.MAX_VALUE, SubsetValue.calculateK(1.0, 64));
		assertEquals(1, SubsetValue.calculateK(1E-19, 63));
		assertEquals(10, SubsetValue.calculateK(1E-18, 63));
		assertEquals(93, SubsetValue.calculateK(1E-17, 63));
		assertEquals(923, SubsetValue.calculateK(1E-16, 63));
		assertEquals(Integer.MAX_VALUE, SubsetValue.calculateK(0.5, 64));
		assertEquals(Integer.MAX_VALUE, SubsetValue.calculateK(0.25, 64));

		assertEquals(0, SubsetValue.calculateK(-1.0, Integer.MAX_VALUE));
		assertEquals(0, SubsetValue.calculateK(-0.5, Integer.MAX_VALUE));
		assertEquals(0, SubsetValue.calculateK(-0.0, Integer.MAX_VALUE));
		assertEquals(0, SubsetValue.calculateK(-0, Integer.MAX_VALUE));
		assertEquals(0, SubsetValue.calculateK(0, Integer.MAX_VALUE));
		assertEquals(0, SubsetValue.calculateK(0.0, Integer.MAX_VALUE));
		assertEquals(Integer.MAX_VALUE, SubsetValue.calculateK(1, Integer.MAX_VALUE));
		assertEquals(Integer.MAX_VALUE, SubsetValue.calculateK(1.0, Integer.MAX_VALUE));
		assertEquals(Integer.MAX_VALUE, SubsetValue.calculateK(1E-65, Integer.MAX_VALUE));
		assertEquals(Integer.MAX_VALUE, SubsetValue.calculateK(0.5, Integer.MAX_VALUE));
		assertEquals(Integer.MAX_VALUE, SubsetValue.calculateK(0.25, Integer.MAX_VALUE));
	}

	private void doTest(final int expectedSize, final EnumerableValue innerSet) {
		doTest(expectedSize, innerSet, 1d, expectedSize);
	}

	private void doTest(final int expectedSize, final EnumerableValue innerSet, final double fraction,
			int expectedElements) {
		final SubsetValue subsetValue = new SubsetValue(innerSet);
		assertEquals(expectedSize, subsetValue.size());

		final Set<Value> s = new TreeSet<>(new Comparator<Value>() {
			@Override
			public int compare(Value o1, Value o2) {
				// o1.normalize();
				// ((SetEnumValue) o1).elems.sort(true);
				//
				// o2.normalize();
				// ((SetEnumValue) o2).elems.sort(true);

				return o1.compareTo(o2);
			}
		});
		
		final ValueEnumeration elements = subsetValue.elements(fraction);
		assertTrue(elements instanceof SubsetEnumerator);
		
		SetEnumValue next = null;
		while ((next = (SetEnumValue) elements.nextElement()) != null) {
			final int size = next.elems.size();
			assertTrue(0 <= size && size <= innerSet.size());
			s.add(next);
		}
		assertEquals(expectedElements, s.size());
	}

	@Test
	public void testRandomSubsetE7F1() {
		final SetEnumValue innerSet = new SetEnumValue(getValue("a", "b", "c", "d", "e", "f", "g"), true);
		doTest(1 << innerSet.size(), innerSet);
	}

	@Test
	public void testRandomSubsetE7F05() {
		final SetEnumValue innerSet = new SetEnumValue(getValue("a", "b", "c", "d", "e", "f", "g"), true);
		doTest(1 << innerSet.size(), innerSet, 0.5d, 64);
	}

	@Test
	public void testRandomSubsetE6F1() {
		final SetEnumValue innerSet = new SetEnumValue(getValue("a", "b", "c", "d", "e", "f"), true);
		doTest(1 << innerSet.size(), innerSet);
	}

	@Test
	public void testRandomSubsetE5F01() {
		final SetEnumValue innerSet = new SetEnumValue(getValue("a", "b", "c", "d", "e"), true);
		doTest(1 << innerSet.size(), innerSet, .1d, 4);
	}

	@Test
	public void testRandomSubsetE5F025() {
		final SetEnumValue innerSet = new SetEnumValue(getValue("a", "b", "c", "d", "e"), true);
		doTest(1 << innerSet.size(), innerSet, .25d, 8);
	}

	@Test
	public void testRandomSubsetE5F05() {
		final SetEnumValue innerSet = new SetEnumValue(getValue("a", "b", "c", "d", "e"), true);
		doTest(1 << innerSet.size(), innerSet, .5d, 16);
	}

	@Test
	public void testRandomSubsetE5F075() {
		final SetEnumValue innerSet = new SetEnumValue(getValue("a", "b", "c", "d", "e"), true);
		doTest(1 << innerSet.size(), innerSet, .75d, 24);
	}

	@Test
	public void testRandomSubsetE5F1() {
		final SetEnumValue innerSet = new SetEnumValue(getValue("a", "b", "c", "d", "e"), true);
		doTest(1 << innerSet.size(), innerSet);
	}

	@Test
	public void testRandomSubsetE32F1ENeg6() {
		final IntervalValue innerSet = new IntervalValue(1, 32);
		final SubsetValue subsetValue = new SubsetValue(innerSet);

		ValueEnumeration elements = subsetValue.elements(1E-6);
		assertTrue(elements instanceof CoinTossingSubsetEnumerator);

		final Set<Value> s = new HashSet<>();
		SetEnumValue next = null;
		while ((next = (SetEnumValue) elements.nextElement()) != null) {
			final int size = next.elems.size();
			assertTrue(0 <= size && size <= innerSet.size());
			s.add(next);
		}

		CoinTossingSubsetEnumerator tossingEnumerator = (CoinTossingSubsetEnumerator) elements;
		assertTrue(tossingEnumerator.getNumOfPicks() - 100 <= s.size() && s.size() <= tossingEnumerator.getNumOfPicks());
	}

	@Test
	public void testRandomSubsetE17F1ENeg3() {
		final IntervalValue innerSet = new IntervalValue(1, 17);
		final SubsetValue subsetValue = new SubsetValue(innerSet);

		final ValueEnumeration elements = subsetValue.elements(1E-3);
		assertTrue(elements instanceof SubsetEnumerator);

		final Set<Value> s = new HashSet<>();
		SetEnumValue next = null;
		while ((next = (SetEnumValue) elements.nextElement()) != null) {
			final int size = next.elems.size();
			assertTrue(0 <= size && size <= innerSet.size());
			s.add(next);
		}

		final SubsetEnumerator enumerator = (SubsetEnumerator) elements;
		assertEquals(enumerator.k, s.size());
	}

	@Test
	public void testRandomSubsetSubset16() {
		final SetEnumValue innerSet = new SetEnumValue(getValue("a", "b"), true);
		final SubsetValue innerSubset = new SubsetValue(innerSet);
		final SubsetValue subsetValue = new SubsetValue(innerSubset);

		final int expectedSize = 1 << innerSubset.size();
		assertEquals(expectedSize, subsetValue.size());

		// No duplicates
		final Set<Value> s = new HashSet<>(expectedSize);
		final ValueEnumeration elements = subsetValue.elements(1d);
		Value next = null;
		while ((next = elements.nextElement()) != null) {
			s.add(next);
		}
		assertEquals(expectedSize, s.size());
	}

	@Test
	public void testRandomSubsetSubset256() {
		final SetEnumValue innerSet = new SetEnumValue(getValue("a", "b", "c"), true);
		final SubsetValue innerSubset = new SubsetValue(innerSet);
		final SubsetValue subsetValue = new SubsetValue(innerSubset);

		final int expectedSize = 1 << innerSubset.size();
		assertEquals(expectedSize, subsetValue.size());

		// No duplicates
		final Set<Value> s = new HashSet<>(expectedSize);
		final ValueEnumeration elements = subsetValue.elements(1d);
		Value next = null;
		while ((next = elements.nextElement()) != null) {
			s.add(next);
		}
		assertEquals(expectedSize, s.size());
	}

	@Test
	public void testRandomSubsetSubset65536() {
		final SetEnumValue innerSet = new SetEnumValue(getValue("a", "b", "c", "d"), true);
		final SubsetValue innerSubset = new SubsetValue(innerSet);
		final SubsetValue subsetValue = new SubsetValue(innerSubset);

		final int expectedSize = 1 << innerSubset.size();
		assertEquals(expectedSize, subsetValue.size());

		// No duplicates
		final Set<Value> s = new HashSet<>(expectedSize);
		final ValueEnumeration elements = subsetValue.elements(1d);
		Value next = null;
		while ((next = elements.nextElement()) != null) {
			s.add(next);
		}
		assertEquals(expectedSize, s.size());
	}

	@Test
	public void testRandomSubsetSubsetNoOverflow() {
		final SetEnumValue innerSet = new SetEnumValue(getValue("a", "b", "c", "d", "e"), true);
		final SubsetValue innerSubset = new SubsetValue(innerSet);
		final SubsetValue subsetValue = new SubsetValue(innerSubset);

		try {
			subsetValue.size();
		} catch (Assert.TLCRuntimeException e) {
			final Set<Value> s = new HashSet<>();

			final ValueEnumeration elements = subsetValue.elements(0.0000005d);
			assertTrue(elements instanceof CoinTossingSubsetEnumerator);
			Value next = null;
			while ((next = elements.nextElement()) != null) {
				s.add(next);
			}
			// No duplicates
			assertEquals(2148, s.size());
		}
	}
}