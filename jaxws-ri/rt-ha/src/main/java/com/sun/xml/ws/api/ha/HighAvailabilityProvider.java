/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.ws.api.ha;

import com.sun.istack.logging.Logger;
import org.glassfish.ha.store.api.BackingStore;
import org.glassfish.ha.store.api.BackingStoreConfiguration;
import org.glassfish.ha.store.api.BackingStoreException;
import org.glassfish.ha.store.api.BackingStoreFactory;
import org.glassfish.ha.store.spi.BackingStoreFactoryRegistry;

import java.io.Serializable;

/**
 * Singleton high-availability provider for Metro
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public enum HighAvailabilityProvider {
    INSTANCE;

    private static final Logger LOGGER = Logger.getLogger(HighAvailabilityProvider.class);

    /**
     * Enumeration of supported backing store factory types
     */
    public static enum StoreType {
        /**
         * In-memory replicated {@link BackingStoreFactory} implementation
         */
        IN_MEMORY("replicated"), // FIXME replace with a constant reference when available
        /**
         * NOOP implementation of {@link BackingStoreFactory} interface
         */
        NOOP(BackingStoreConfiguration.NO_OP_PERSISTENCE_TYPE);

        private final String storeTypeId;

        private StoreType(String storeTypeId) {
            this.storeTypeId = storeTypeId;
        }
    }

    private static class HaEnvironment {
        public static final HaEnvironment NO_HA_ENVIRONMENT = new HaEnvironment(null, null, false);

        private final String clusterName;
        private final String instanceName;
        private final boolean disableJreplica;

        private HaEnvironment(final String clusterName, final String instanceName, final boolean disableJreplica) {
            this.clusterName = clusterName;
            this.instanceName = instanceName;
            this.disableJreplica = disableJreplica;
        }

        public static HaEnvironment getInstance(final String clusterName, final String instanceName, final boolean disableJreplica) {
            if (clusterName == null && instanceName == null) {
                return NO_HA_ENVIRONMENT;
            }

            return new HaEnvironment(clusterName, instanceName, disableJreplica);
        }

        public String getClusterName() {
            return clusterName;
        }

        public String getInstanceName() {
            return instanceName;
        }

        public boolean isDisabledJreplica() {
            return disableJreplica;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final HaEnvironment other = (HaEnvironment) obj;
            if ((this.clusterName == null) ? (other.clusterName != null) : !this.clusterName.equals(other.clusterName)) {
                return false;
            }
            if ((this.instanceName == null) ? (other.instanceName != null) : !this.instanceName.equals(other.instanceName)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 89 * hash + (this.clusterName != null ? this.clusterName.hashCode() : 0);
            hash = 89 * hash + (this.instanceName != null ? this.instanceName.hashCode() : 0);
            return hash;
        }
    }

    private volatile HaEnvironment haEnvironment = HaEnvironment.NO_HA_ENVIRONMENT;

    /**
     * This method is not meant to be used directly by the user of the Metro
     * {@link HighAvailabilityProvider} class.
     *
     * It is primarily used by a container to inject the proper cluster name and
     * instance name values that are later used to initialize all {@link BackingStoreConfiguration}
     * instances created via {@link #initBackingStoreConfiguration(String, Class, Class)}
     * method
     *
     * @param clusterName name of the cluster
     * @param instanceName name of the cluster instance
     */
    public void initHaEnvironment(final String clusterName, final String instanceName) {
        initHaEnvironment(clusterName, instanceName, false);
    }

    public void initHaEnvironment(final String clusterName, final String instanceName, final boolean disableJreplica) {
        System.out.println("initHaEnvironment is called: "+clusterName+" "+instanceName);
        this.haEnvironment = HaEnvironment.getInstance(clusterName, instanceName, disableJreplica);
    }

    public boolean isDisabledJreplica() {
        return haEnvironment.isDisabledJreplica();
    }

    /**
     * Creates {@link BackingStoreConfiguration} instance initialized  with
     * all mandatory fields. This instance can be used to create {@link BackingStore}
     * instance.
     *
     * @param <K> backing store key type
     * @param <V> backing store value type
     * @param storeName name of the backing store
     * @param keyClass backing store key class
     * @param valueClass backing store value class
     *
     * @return initialized {@link BackingStoreConfiguration} instance
     */
    public <K extends Serializable, V extends Serializable> BackingStoreConfiguration<K, V> initBackingStoreConfiguration(
            final String storeName,
            final Class<K> keyClass,
            final Class<V> valueClass) {

        final HaEnvironment env = this.haEnvironment; // prevents synchronization issues with concurrent invocation of initEnvironment(...)

        return new BackingStoreConfiguration<K, V>()
                .setClusterName(env.clusterName)
                .setInstanceName(env.getInstanceName())
                .setStoreName(storeName)
                .setKeyClazz(keyClass)
                .setValueClazz(valueClass);
    }

    /**
     * Retrieves {@link BackingStoreFactory} implementation of the requested type.
     * In case this method is executed outside an HA environment (e.g. standalone mode),
     * {@link StoreType#NOOP} implementation is returned.
     *
     * @param type type of the {@link BackingStoreFactory} implementation to be retrieved
     *
     * @return {@link BackingStoreFactory} implementation of the requested type.
     * When executed outside HA environment, {@link StoreType#NOOP} implementation
     * is returned.
     *
     * @throws HighAvailabilityProviderException in case the method is executed inside
     * HA environment and the requested {@link BackingStoreFactory} implementation is not
     * available.
     */
    public BackingStoreFactory getBackingStoreFactory(final StoreType type) throws HighAvailabilityProviderException {
        if (!isHaEnvironmentConfigured()) {
            return getSafeBackingStoreFactory(StoreType.NOOP);
        }

        return getSafeBackingStoreFactory(type);
    }

    private BackingStoreFactory getSafeBackingStoreFactory(final StoreType type) throws HighAvailabilityProviderException {
        try {
            return BackingStoreFactoryRegistry.getFactoryInstance(type.storeTypeId);
        } catch (BackingStoreException ex) {
            throw LOGGER.logSevereException(new HighAvailabilityProviderException("", ex)); // TODO message
        }
    }

    /**
     * Provides information on whether there is a HA service available in the
     * current JVM or not.
     *
     * @return {@code true} in case there is a HA service available in the current
     *         JVM, {@code false} otherwise
     */
    public boolean isHaEnvironmentConfigured() {
        return !HaEnvironment.NO_HA_ENVIRONMENT.equals(this.haEnvironment);
    }

    /**
     * Helper method that avoids the need for exception handling boilerplate code
     * when creating a new {@link BackingStore} instance.
     * The original checked {@link BackingStoreException} is wrapped into a new
     * unchecked {@link HighAvailabilityProviderException}.
     *
     * @param <K> backing store key parameter type
     * @param <V> backing store value parameter type
     * @param factory {@link BackingStoreFactory} instance
     * @param backingStoreName name of the backing store to be created
     * @param keyClass backing store key class
     * @param valueClass backing store value class
     *
     * @return newly created {@link BackingStore} instance.
     */
    public <K extends Serializable, V extends Serializable> BackingStore<K, V> createBackingStore(
            BackingStoreFactory factory,
            String backingStoreName,
            Class<K> keyClass,
            Class<V> valueClass) {

        final BackingStoreConfiguration<K, V> bsConfig = initBackingStoreConfiguration(
                backingStoreName,
                keyClass,
                valueClass);
        try {
            return factory.createBackingStore(bsConfig);
        } catch (BackingStoreException ex) {
           throw LOGGER.logSevereException(new HighAvailabilityProviderException("", ex)); // TODO exception message
        }
    }

    /**
     * Helper method that avoids the need for exception handling boilerplate code
     * when loading data from a {@link BackingStore} instance.
     * The original checked {@link BackingStoreException} is wrapped into a new
     * unchecked {@link HighAvailabilityProviderException}.
     *
     * @param <K> backing store key parameter type
     * @param <V> backing store data parameter type
     * @param backingStore {@link BackingStore} instance
     * @param key stored data identifier
     * @param version stored data version
     *
     * @return stored data as specified by {@link BackingStore#load(java.io.Serializable, String)}
     */
    public static <K extends Serializable, V extends Serializable> V loadFrom(BackingStore<K, V> backingStore, K key, String version) {
        try {
            return backingStore.load(key, version);
        } catch (BackingStoreException ex) {
            throw LOGGER.logSevereException(new HighAvailabilityProviderException("", ex)); // TODO exception message
        }
    }

    /**
     * Helper method that avoids the need for exception handling boilerplate code
     * when storing data into a {@link BackingStore} instance.
     * The original checked {@link BackingStoreException} is wrapped into a new
     * unchecked {@link HighAvailabilityProviderException}.
     *
     * @param <K> backing store key parameter type
     * @param <V> backing store value parameter type
     * @param backingStore {@link BackingStore} instance
     * @param key stored data identifier
     * @param value data to be stored
     * @param isNew See {@link BackingStore#save(java.io.Serializable, java.io.Serializable, boolean)}
     *
     * @return See {@link BackingStore#save(java.io.Serializable, java.io.Serializable, boolean)}
     */
    public static <K extends Serializable, V extends Serializable> String saveTo(BackingStore<K, V> backingStore, K key, V value, boolean isNew) {
        try {
            return backingStore.save(key, value, isNew);
        } catch (BackingStoreException ex) {
            throw LOGGER.logSevereException(new HighAvailabilityProviderException("", ex)); // TODO exception message
        }
    }

    /**
     * Helper method that avoids the need for exception handling boilerplate code
     * when removing data from a {@link BackingStore} instance.
     * The original checked {@link BackingStoreException} is wrapped into a new
     * unchecked {@link HighAvailabilityProviderException}.
     *
     * @param <K> backing store key parameter type
     * @param <V> backing store data parameter type
     * @param backingStore {@link BackingStore} instance
     * @param key stored data identifier
     */
    public static <K extends Serializable, V extends Serializable> void removeFrom(BackingStore<K, V> backingStore, K key) {
        try {
            backingStore.remove(key);
        } catch (BackingStoreException ex) {
            throw LOGGER.logSevereException(new HighAvailabilityProviderException("", ex)); // TODO exception message
        }
    }


    /**
     * Helper method that avoids the need for exception handling boilerplate code
     * when closing a {@link BackingStore} instance.
     * The original checked {@link BackingStoreException} is wrapped into a new
     * unchecked {@link HighAvailabilityProviderException}.
     *
     * @param backingStore {@link BackingStore} instance
     */
    public static void close(BackingStore<?, ?> backingStore) {
        try {
            backingStore.close();
        } catch (BackingStoreException ex) {
            throw LOGGER.logSevereException(new HighAvailabilityProviderException("", ex)); // TODO exception message
        }
    }

    /**
     * Helper method that avoids the need for exception handling boilerplate code
     * when destroying a {@link BackingStore} instance.
     * The original checked {@link BackingStoreException} is wrapped into a new
     * unchecked {@link HighAvailabilityProviderException}.
     *
     * @param backingStore {@link BackingStore} instance
     */
    public static void destroy(BackingStore<?, ?> backingStore) {
        try {
            backingStore.destroy();
        } catch (BackingStoreException ex) {
            throw LOGGER.logSevereException(new HighAvailabilityProviderException("", ex)); // TODO exception message
        }
    }

    /**
     * Helper method that avoids the need for exception handling boilerplate code
     * when destroying a {@link BackingStore} instance.
     * The original checked {@link BackingStoreException} is wrapped into a new
     * unchecked {@link HighAvailabilityProviderException}.
     *
     * @param backingStore {@link BackingStore} instance
     */
    public static <K extends Serializable> void removeExpired(BackingStore<K, ?> backingStore) {
        try {
            backingStore.removeExpired();
        } catch (BackingStoreException ex) {
            throw LOGGER.logSevereException(new HighAvailabilityProviderException("", ex)); // TODO exception message
        }
    }
}
