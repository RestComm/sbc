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
import org.restcomm.sbc.bo.Connector;
import org.restcomm.sbc.bo.NetworkPoint;
import org.restcomm.sbc.bo.Sid;
import org.restcomm.sbc.managers.NetworkManager;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    27 jul. 2016 21:38:01
 * @class   ConnectorConverter.java
 *
 */
@ThreadSafe
public final class ConnectorConverter extends AbstractConverter implements JsonSerializer<Connector> {
    
    public ConnectorConverter(final Configuration configuration) {
        super(configuration);
       
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean canConvert(final Class klass) {
        return Connector.class.equals(klass);
    }

    @Override
    public void marshal(final Object object, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        final Connector connector = (Connector) object;
        writer.startNode("Connector");
        NetworkPoint point=NetworkManager.getNetworkPoint(connector.getPoint());
        if(point!=null)
        	writeID(point.getAddress().getHostAddress(), "IpAddress", writer);
        else
        	writeID("N/A", "IpAddress", writer);   	
        writeSid(connector.getSid(), writer);
        writePort(connector.getPort(), writer);
        writeID(connector.getPoint(), "NetworkPointId", writer);
        writeTransport(connector.getTransport().toString(), writer);
        writeState(connector.getState().toString(), writer);
        writeAccountSid(connector.getAccountSid(), writer);
        
        writer.endNode();
    }


    @Override
    public JsonElement serialize(final Connector connector, Type type, final JsonSerializationContext context) {
        final JsonObject object = new JsonObject();
        writeID(NetworkManager.getNetworkPoint(connector.getPoint()).getAddress().getHostAddress(), "ip_address", object);
        writeSid(connector.getSid(), object);
        writePort(connector.getPort(), object);
        writeID(connector.getPoint(), "n_point", object);
        writeTransport(connector.getTransport().toString(), object);
        writeState(connector.getState().toString(), object);
        writeAccountSid(connector.getAccountSid(), object);
        
        return object;
    }
    
    protected void writeSid(final Sid sid, String name, final HierarchicalStreamWriter writer) {
        writer.startNode(name);
        writer.setValue(sid.toString());
        writer.endNode();
    }

    protected void writeSid(final Sid sid, String name, final JsonObject object) {
        object.addProperty(name, sid.toString());
    }

    private void writePort(final int port, final HierarchicalStreamWriter writer) {
        writer.startNode("Port");
        if (port != 0) {
            writer.setValue(""+port);
        }
        writer.endNode();
    }

    private void writePort(final int port, final JsonObject object) {
        object.addProperty("port", port);
    }
    
    private void writeTransport(final String transport, final HierarchicalStreamWriter writer) {
        writer.startNode("Transport");
        if (transport != null) {
            writer.setValue(transport);
        }
        writer.endNode();
    }

    private void writeTransport(final String transport, final JsonObject object) {
        object.addProperty("transport", transport);
    }
    
    private void writeState(final String state, final HierarchicalStreamWriter writer) {
        writer.startNode("State");
        if (state != null) {
            writer.setValue(state);
        }
        writer.endNode();
    }

    private void writeState(final String state, final JsonObject object) {
        object.addProperty("state", state);
    }
    
    private void writeID(final String id, final String name, final HierarchicalStreamWriter writer) {
        writer.startNode(name);
        if (id != null) {
            writer.setValue(id);
        }
        writer.endNode();
    }

    private void writeID(final String id, final String name, final JsonObject object) {
        object.addProperty(name, id);
    }

    
}
