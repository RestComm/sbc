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
import org.restcomm.sbc.bo.Route;
import org.restcomm.sbc.bo.Sid;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    1 ago. 2016 19:59:40
 * @class   RoutingPolicyConverter.java
 *
 */
@ThreadSafe
public final class RouteConverter extends AbstractConverter implements JsonSerializer<Route> {
    
    public RouteConverter(final Configuration configuration) {
        super(configuration);
       
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean canConvert(final Class klass) {
        return Route.class.equals(klass);
    }

    @Override
    public void marshal(final Object object, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        final Route route = (Route) object;
        writer.startNode("Route");
        writeSid(route.getSid(), writer);
        writeSid(route.getSourceConnector(), "SourceConnectorSid", writer);
        writeSid(route.getTargetConnector(), "TargetConnectorSid", writer);
        writeAccountSid(route.getAccountSid(), writer);
        
        writer.endNode();
    }


    @Override
    public JsonElement serialize(final Route route, Type type, final JsonSerializationContext context) {
        final JsonObject object = new JsonObject();
        writeSid(route.getSid(), object);
        writeSid(route.getSourceConnector(), "source_connector_sid", object);
        writeSid(route.getTargetConnector(), "target_connector_sid", object);
        writeAccountSid(route.getAccountSid(), object);
        
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

}
