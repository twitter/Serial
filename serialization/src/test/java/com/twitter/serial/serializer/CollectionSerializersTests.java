/*
 * Copyright 2017 Twitter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twitter.serial.serializer;

import com.twitter.serial.SerializationTestUtils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CollectionSerializersTests {
    @Test
    public void testNullCollections() throws Exception {
        SerializationTestUtils.checkSerialization(null,
                CollectionSerializers.getListSerializer(CoreSerializers.STRING));
        SerializationTestUtils.checkSerialization(null,
                CollectionSerializers.getListSerializer(CoreSerializers.STRING));
        SerializationTestUtils.checkSerialization(null,
                CollectionSerializers.getSetSerializer(CoreSerializers.STRING));
    }

    @Test
    public void testSerializeSimpleLists() throws Exception {
        final Serializer<List<Integer>> serializer = CollectionSerializers.getListSerializer(CoreSerializers.INTEGER);

        final List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        SerializationTestUtils.checkSerialization(list, serializer);

        SerializationTestUtils.checkSerialization(new ArrayList<Integer>(), serializer);
        SerializationTestUtils.checkSerialization(list.subList(0, 1), serializer);
    }

    @Test
    public void testSerializeSimpleSets() throws Exception {
        final Serializer<Set<Integer>> serializer = CollectionSerializers.getSetSerializer(CoreSerializers.INTEGER);

        final Set<Integer> emptySet = new HashSet<>();
        SerializationTestUtils.checkSerialization(emptySet, serializer);

        final Set<Integer> singletonSet = new HashSet<>(1);
        SerializationTestUtils.checkSerialization(singletonSet, serializer);

        final Set<Integer> set = new HashSet<>();
        set.add(1);
        set.add(2);
        set.add(3);
        set.add(4);
        SerializationTestUtils.checkSerialization(set, serializer);
    }

    @Test
    public void testSerializeObjectSet() throws Exception {
        final Set<SerializationTestUtils.TestObject> set = new HashSet<>();
        set.add(new SerializationTestUtils.TestObject("3", 30));
        set.add(new SerializationTestUtils.TestObject("1", 15));
        set.add(new SerializationTestUtils.TestObject("2", 10));
        SerializationTestUtils.checkSerialization(
                set, CollectionSerializers.getSetSerializer(SerializationTestUtils.TestObject.SERIALIZER));
    }
}
