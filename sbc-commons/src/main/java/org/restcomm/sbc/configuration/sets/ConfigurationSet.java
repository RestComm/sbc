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
package org.restcomm.sbc.configuration.sets;

import org.restcomm.sbc.configuration.sources.ConfigurationSource;

/**
 * A logical group of configuration options. It encapsulates storage, initialization
 * and validation operations. Extend it to add new groups.
 *
 * @author orestis.tsakiridis@telestax.com (Orestis Tsakiridis)
 *
 */
public class ConfigurationSet {
    private final ConfigurationSource source;

    protected ConfigurationSet(ConfigurationSource source) {
        super();
        this.source = source;
    }

    public ConfigurationSource getSource() {
        return source;
    }
}
