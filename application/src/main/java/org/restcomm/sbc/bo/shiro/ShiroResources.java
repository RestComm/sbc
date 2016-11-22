/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2014, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */
package org.restcomm.sbc.bo.shiro;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.mobicents.servlet.sip.restcomm.annotations.concurrency.ThreadSafe;


/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 */
@ThreadSafe
public final class ShiroResources {
	
    private static final class SingletonHolder {
        private static final ShiroResources instance = new ShiroResources();
    }

    private final Map<Class<?>, Object> services;

    private ShiroResources() {
        super();
        this.services = new ConcurrentHashMap<Class<?>, Object>();
    }

    public <T> T get(final Class<T> klass) {
    	
        synchronized (klass) {
            final Object service = services.get(klass);
            if (service != null) {
                return klass.cast(service);
            } else {
                return null;
            }
        }
    }

    public static ShiroResources getInstance() {
        return SingletonHolder.instance;
    }

    public <T> void set(final Class<T> klass, final T instance) {
        synchronized (klass) {
            services.put(klass, instance);
        }
    }
}
