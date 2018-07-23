/*
 * Copyright (c) 2008-2018, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.config;

import com.hazelcast.internal.cluster.Versions;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.nio.serialization.impl.Versioned;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Config to be used by WanReplicationConsumer instances (EE only). This
 * allows creating a custom WAN consumer which is usually used in
 * combination with a custom WAN publisher.
 * The WAN consumer is in charge of consuming (processing) incoming WAN
 * events.
 *
 * @see WanReplicationConfig#setWanConsumerConfig(WanConsumerConfig)
 * @see WanPublisherConfig#setClassName(String)
 */
public class WanConsumerConfig implements IdentifiedDataSerializable, Versioned {

    private static final boolean DEFAULT_PERSIST_WAN_REPLICATED_DATA = true;

    private String className;
    private Object implementation;
    private Map<String, Comparable> properties = new HashMap<String, Comparable>();
    private boolean persistWanReplicatedData = DEFAULT_PERSIST_WAN_REPLICATED_DATA;

    /**
     * Returns the properties for this WAN consumer.
     */
    public Map<String, Comparable> getProperties() {
        return properties;
    }

    /**
     * Sets the properties for the WAN consumer. These properties are accessible
     * when initalizing the WAN consumer.
     *
     * @param properties the properties for the WAN consumer
     * @return this config
     */
    public WanConsumerConfig setProperties(Map<String, Comparable> properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Returns the fully qualified class name of the class implementing
     * WanReplicationConsumer.
     *
     * @return fully qualified class name
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets the name of the class implementing WanReplicationConsumer.
     *
     * @param className fully qualified class name
     * @return this config
     */
    public WanConsumerConfig setClassName(String className) {
        this.className = className;
        return this;
    }

    /**
     * Returns the implementation implementing WanReplicationConsumer.
     *
     * @return the implementation for this WAN consumer
     */
    public Object getImplementation() {
        return implementation;
    }

    /**
     * Sets the implementation for this WAN consumer. The object must implement
     * WanReplicationConsumer.
     *
     * @param implementation the object implementing WanReplicationConsumer
     * @return this config
     */
    public WanConsumerConfig setImplementation(Object implementation) {
        this.implementation = implementation;
        return this;
    }

    /**
     * @return {@code true} when persistence of replicated data into backing
     * store is enabled, otherwise returns {@code false}. By default this
     * method returns {@value #DEFAULT_PERSIST_WAN_REPLICATED_DATA}.
     */
    public boolean isPersistWanReplicatedData() {
        return persistWanReplicatedData;
    }

    /**
     * @param persistWanReplicatedData set {@code true} to enable
     *                                 persistence of replicated data into backing store, otherwise set
     *                                 {@code false} to disable it. Default value is {@value
     *                                 #DEFAULT_PERSIST_WAN_REPLICATED_DATA}. @return reference to this
     *                                 {@link WanReplicationRef} object
     */
    public WanConsumerConfig setPersistWanReplicatedData(boolean persistWanReplicatedData) {
        this.persistWanReplicatedData = persistWanReplicatedData;
        return this;
    }

    @Override
    public String toString() {
        return "WanConsumerConfig{"
                + "properties=" + properties
                + ", className='" + className + '\''
                + ", implementation=" + implementation
                + ", persistWanReplicatedData=" + persistWanReplicatedData
                + '}';
    }

    @Override
    public int getFactoryId() {
        return ConfigDataSerializerHook.F_ID;
    }

    @Override
    public int getId() {
        return ConfigDataSerializerHook.WAN_CONSUMER_CONFIG;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        int size = properties.size();
        out.writeInt(size);
        for (Map.Entry<String, Comparable> entry : properties.entrySet()) {
            out.writeUTF(entry.getKey());
            out.writeObject(entry.getValue());
        }
        out.writeUTF(className);
        out.writeObject(implementation);

        // RU_COMPAT_3_10
        if (out.getVersion().isGreaterOrEqual(Versions.V3_11)) {
            out.writeBoolean(persistWanReplicatedData);
        }
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            properties.put(in.readUTF(), (Comparable) in.readObject());
        }
        className = in.readUTF();
        implementation = in.readObject();

        // RU_COMPAT_3_10
        if (in.getVersion().isGreaterOrEqual(Versions.V3_11)) {
            persistWanReplicatedData = in.readBoolean();
        }
    }
}
