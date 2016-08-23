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
package org.restcomm.sbc.rest.converter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import java.lang.reflect.Type;

import org.apache.commons.configuration.Configuration;
import org.mobicents.servlet.sip.restcomm.annotations.concurrency.ThreadSafe;
import org.restcomm.sbc.bo.NetworkPoint;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    27 jul. 2016 21:38:01
 * @class   NetworkPointConverter.java
 *
 */
@ThreadSafe
public final class NetworkPointConverter extends AbstractConverter implements JsonSerializer<NetworkPoint> {
    
    public NetworkPointConverter(final Configuration configuration) {
        super(configuration);
       
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean canConvert(final Class klass) {
        return NetworkPoint.class.equals(klass);
    }

    @Override
    public void marshal(final Object object, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        final NetworkPoint point = (NetworkPoint) object;
        writer.startNode("NetworkPoint");
        
        writeId(point.getId(), writer);
        writeMAC(point.getMacAddress(), writer);
        writeDescription(point.getDescription(), writer);
        writeTag(point.getTag().toString(), writer);
        writeAccountSid(point.getAccountSid(), writer);
        
        writer.endNode();
    }


    @Override
    public JsonElement serialize(final NetworkPoint point, Type type, final JsonSerializationContext context) {
        final JsonObject object = new JsonObject();
       
        writeId(point.getId(), object);
        writeMAC(point.getMacAddress(), object);
        writeDescription(point.getDescription(), object);
        writeTag(point.getTag().toString(), object);
        writeAccountSid(point.getAccountSid(), object);
        
        return object;
    }

    private void writeId(final String id, final HierarchicalStreamWriter writer) {
        writer.startNode("Id");
        if (id != null) {
            writer.setValue(id);
        }
        writer.endNode();
    }

    private void writeId(final String id, final JsonObject object) {
        object.addProperty("id", id);
    }
    
    private void writeMAC(final String mac, final HierarchicalStreamWriter writer) {
        writer.startNode("MacAddress");
        if (mac != null) {
            writer.setValue(mac);
        }
        writer.endNode();
    }

    private void writeMAC(final String mac, final JsonObject object) {
        object.addProperty("mac_address", mac);
    }
    
    private void writeDescription(final String desc, final HierarchicalStreamWriter writer) {
        writer.startNode("Description");
        if (desc != null) {
            writer.setValue(desc);
        }
        writer.endNode();
    }

    private void writeDescription(final String desc, final JsonObject object) {
        object.addProperty("description", desc);
    }

    
}
