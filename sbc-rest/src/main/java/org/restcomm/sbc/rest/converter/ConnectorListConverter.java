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

import java.lang.reflect.Type;

import org.apache.commons.configuration.Configuration;
import org.mobicents.servlet.sip.restcomm.annotations.concurrency.ThreadSafe;
import org.restcomm.sbc.bo.Connector;
import org.restcomm.sbc.bo.ConnectorList;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    27 jul. 2016 22:14:16
 * @class   ConnectorListConverter.java
 *
 */
@ThreadSafe
public final class ConnectorListConverter extends AbstractConverter implements JsonSerializer<ConnectorList> {


    public ConnectorListConverter(final Configuration configuration) {
        super(configuration);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean canConvert(final Class klass) {
        return ConnectorList.class.equals(klass);
    }

    @Override
    public void marshal(final Object object, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        final ConnectorList list = (ConnectorList) object;

        writer.startNode("Connectors");
        

        for (final Connector connector : list.getConnectorList()) {
            context.convertAnother(connector);
        }
        writer.endNode();
    }

   
    @Override
    public JsonObject serialize(ConnectorList connectorList, Type type, JsonSerializationContext context) {

        JsonObject result = new JsonObject();

        JsonArray array = new JsonArray();
        for (Connector connector : connectorList.getConnectorList()) {
            array.add(context.serialize(connector));
        }

        

        result.add("entries", array);

        return result;
    }

   

}
