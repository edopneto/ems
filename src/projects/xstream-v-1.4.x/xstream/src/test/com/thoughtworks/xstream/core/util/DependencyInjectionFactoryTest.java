/*
 * Copyright (C) 2007, 2009, 2010, 2011, 2012, 2016 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 30. March 2007 by Joerg Schaible
 */
package com.thoughtworks.xstream.core.util;

import com.thoughtworks.xstream.converters.ErrorWriter;
import com.thoughtworks.xstream.converters.reflection.ObjectAccessException;

import junit.framework.TestCase;

import java.util.BitSet;


public class DependencyInjectionFactoryTest extends TestCase {
    public void testDependencyInjectionWithMatchingParameterSequence() {
        final BitSet used = new BitSet();
        final Exception exception = (Exception)DependencyInjectionFactory.newInstance(
            ObjectAccessException.class, new Object[]{
                "The message", this, new RuntimeException("JUnit")}, used);
        assertTrue(exception instanceof ObjectAccessException);
        assertEquals("The message", ((ErrorWriter)exception).get("message"));
        assertEquals("JUnit", ((ErrorWriter)exception).get("cause-message"));
        assertTrue(used.get(0));
        assertFalse(used.get(1));
        assertTrue(used.get(2));
    }

    public void testWillUseDefaultConstructor() {
        final BitSet used = new BitSet();
        final String string = (String)DependencyInjectionFactory.newInstance(
            String.class, new Object[]{this}, used);
        assertEquals("", string);
        assertFalse(used.get(0));
    }

    public void testWillMatchNullValue() {
        final BitSet used = new BitSet();
        final Exception exception = (Exception)DependencyInjectionFactory.newInstance(
            ObjectAccessException.class, new Object[]{
                new TypedNull(String.class), this, new RuntimeException("JUnit")}, used);
        assertTrue(exception instanceof ObjectAccessException);
        assertNull(((ErrorWriter)exception).get("message"));
        assertEquals("JUnit", ((ErrorWriter)exception).get("cause-message"));
        assertTrue(used.get(0));
        assertFalse(used.get(1));
        assertTrue(used.get(2));
    }

    public void testWillMatchPrimitives() {
        final BitSet used = new BitSet();
        final String string = (String)DependencyInjectionFactory.newInstance(
            String.class,
            new Object[]{"JUnit".getBytes(), new Integer(1), this, new Integer(4)}, used);
        assertEquals("Unit", string);
        assertTrue(used.get(0));
        assertTrue(used.get(1));
        assertFalse(used.get(2));
        assertTrue(used.get(3));
    }

    public void testWillUseArbitraryOrder() {
        final BitSet used = new BitSet();
        final Exception exception = (Exception)DependencyInjectionFactory.newInstance(
            ObjectAccessException.class, new Object[]{
                new RuntimeException("JUnit"), this, "The message"}, used);
        assertTrue(exception instanceof ObjectAccessException);
        assertEquals("The message", ((ErrorWriter)exception).get("message"));
        assertEquals("JUnit", ((ErrorWriter)exception).get("cause-message"));
        assertTrue(used.get(0));
        assertFalse(used.get(1));
        assertTrue(used.get(2));
    }

    public void testWillMatchMostSpecificDependency() {
        final BitSet used = new BitSet();
        final Exception exception = (Exception)DependencyInjectionFactory.newInstance(
            ObjectAccessException.class, new Object[]{
                new RuntimeException("JUnit"), new IllegalArgumentException("foo"), this,
                "The message"}, used);
        assertTrue(exception instanceof ObjectAccessException);
        assertEquals("The message", ((ErrorWriter)exception).get("message"));
        assertEquals("foo", ((ErrorWriter)exception).get("cause-message"));
        assertFalse(used.get(0));
        assertTrue(used.get(1));
        assertFalse(used.get(2));
        assertTrue(used.get(3));
    }

    public void testWillMatchFirstMatchingDependency() {
        final BitSet used = new BitSet();
        final Exception exception = (Exception)DependencyInjectionFactory.newInstance(
            ObjectAccessException.class, new Object[]{
                new RuntimeException("JUnit"), "The message", "bar",
                new IllegalArgumentException("foo"), this}, used);
        assertTrue(exception instanceof ObjectAccessException);
        assertEquals("The message", ((ErrorWriter)exception).get("message"));
        assertEquals("foo", ((ErrorWriter)exception).get("cause-message"));
        assertFalse(used.get(0));
        assertTrue(used.get(1));
        assertFalse(used.get(2));
        assertTrue(used.get(3));
        assertFalse(used.get(4));
    }

    static class Thing {
        final TestCase testCase;
        final int first;
        final int second;

        public Thing() {
            this(1, 2, null);
        }

        public Thing(Number num) {
            this(num.intValue(), 8 * num.intValue(), null);
        }

        public Thing(String str, TestCase testCase) {
            this(str.length(), 4 * str.length(), testCase);
        }

        public Thing(Number num, TestCase testCase) {
            this(num.intValue(), 4 * num.intValue(), testCase);
        }

        public Thing(int first, int second, TestCase testCase) {
            this.first = first;
            this.second = second;
            this.testCase = testCase;
        }

        TestCase getTestCase() {
            return testCase;
        }

        int getFirst() {
            return first;
        }

        int getSecond() {
            return second;
        }
    }

    public void testWillMatchArbitraryOrderForOneAvailableConstructorOnly() {
        final BitSet used = new BitSet();
        final Thing thing = (Thing)DependencyInjectionFactory.newInstance(
            Thing.class, new Object[]{this, new Integer(1), new Integer(2)}, used);
        assertSame(this, thing.getTestCase());
        assertEquals(1, thing.getFirst());
        assertEquals(2, thing.getSecond());
        assertTrue(used.get(0));
        assertTrue(used.get(1));
        assertTrue(used.get(2));
    }
    
    public void testWillSelectMatchingConstructor() {
        BitSet used = new BitSet();
        Thing thing = (Thing)DependencyInjectionFactory.newInstance(
            Thing.class, new Object[]{this, new Integer(1)}, used);
        assertSame(this, thing.getTestCase());
        assertEquals(1, thing.getFirst());
        assertEquals(4, thing.getSecond());
        assertTrue(used.get(0));
        assertTrue(used.get(1));
        
        used = new BitSet();
        thing = (Thing)DependencyInjectionFactory.newInstance(
            Thing.class, new Object[]{this, "a"}, used);
        assertSame(this, thing.getTestCase());
        assertEquals(1, thing.getFirst());
        assertEquals(4, thing.getSecond());
        assertTrue(used.get(0));
        assertTrue(used.get(1));
    }
    
    public void testWillSelectMatchingConstructorForFirstMatchingArguments() {
        BitSet used = new BitSet();
        Thing thing = (Thing)DependencyInjectionFactory.newInstance(
            Thing.class, new Object[]{this, new Integer(1), "foo"}, used);
        assertSame(this, thing.getTestCase());
        assertEquals(1, thing.getFirst());
        assertEquals(4, thing.getSecond());
        assertTrue(used.get(0));
        assertTrue(used.get(1));
        assertFalse(used.get(2));
        
        used = new BitSet();
        thing = (Thing)DependencyInjectionFactory.newInstance(
            Thing.class, new Object[]{this, "foo", new Integer(1)}, used);
        assertSame(this, thing.getTestCase());
        assertEquals(3, thing.getFirst());
        assertEquals(12, thing.getSecond());
        assertTrue(used.get(0));
        assertTrue(used.get(1));
        assertFalse(used.get(2));
    }
}
