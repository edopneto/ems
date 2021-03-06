/*
 * Copyright (C) 2007, 2009 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 04. May 2007 by Joerg Schaible
 */
package com.thoughtworks.xstream.tools.benchmark.strings.products;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.thoughtworks.xstream.tools.benchmark.Product;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.WeakHashMap;


/**
 * Uses WeakHashMap for StringConverter.
 * 
 * @author J&ouml;rg Schaible
 * @see com.thoughtworks.xstream.tools.benchmark.Harness
 * @see Product
 */
public class StringWithSynchronizedWeakHashMapConverter implements Product {

    private final XStream xstream;

    public StringWithSynchronizedWeakHashMapConverter() {
        xstream = new XStream(new XppDriver());
        xstream.registerConverter(new StringWithWeakHashMapConverter.StringConverter(Collections.synchronizedMap(new WeakHashMap())));
    }

    public void serialize(Object object, OutputStream output) throws Exception {
        xstream.toXML(object, output);
    }

    public Object deserialize(InputStream input) throws Exception {
        return xstream.fromXML(input);
    }

    public String toString() {
        return "StringConverter using synchronized WeakHashMap";
    }
}
