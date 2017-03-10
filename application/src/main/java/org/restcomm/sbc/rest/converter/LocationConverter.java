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
import org.joda.time.DateTime;
import org.mobicents.servlet.sip.restcomm.annotations.concurrency.ThreadSafe;
import org.restcomm.sbc.bo.Location;






/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 */
@ThreadSafe
public final class LocationConverter extends AbstractConverter implements JsonSerializer<Location> {
   // private final String apiVersion;
   // private final String rootUri;

    public LocationConverter(final Configuration configuration) {
        super(configuration);
        //apiVersion = configuration.getString("api-version");
        //rootUri = StringUtils.addSuffixIfNotPresent(configuration.getString("root-uri"), "/");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean canConvert(final Class klass) {
        return Location.class.equals(klass);
    }

    @Override
    public void marshal(final Object object, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        final Location location = (Location) object;
        writer.startNode("Location");
        writeHost(location.getHost(), writer);
        writeUser(location.getUser(), writer);
        writeDomain(location.getDomain(), writer);
        writeTransport(location.getTransport(), writer);
        writeExpires(location.getExpires(), writer);
        writeUserAgent(location.getUserAgent(), writer);
        writePort(location.getPort(), writer);
        writeConnector(location.getSourceConnectorSid().toString(), writer);
        writer.endNode();
    }


    @Override
    public JsonElement serialize(final Location location, Type type, final JsonSerializationContext context) {
        final JsonObject object = new JsonObject();
        writeUser(location.getUser(), object);
        writeHost(location.getHost(), object);
        writeDomain(location.getDomain(), object);
        writeTransport(location.getTransport(), object);
        writePort(location.getPort(), object);
        writeExpires(location.getExpires(), object);
        writeUserAgent(location.getUserAgent(), object);
        writeConnector(location.getSourceConnectorSid().toString(), object);
        return object;
    }

    private void writeHost(final String host, final HierarchicalStreamWriter writer) {
        writer.startNode("Host");
        if (host != null) {
            writer.setValue(host);
        }
        writer.endNode();
    }

    private void writeHost(final String host, final JsonObject object) {
        object.addProperty("host", host);
    }
    
    private void writeDomain(final String domain, final HierarchicalStreamWriter writer) {
        writer.startNode("Domain");
        if (domain != null) {
            writer.setValue(domain);
        }
        writer.endNode();
    }

    private void writeConnector(final String connector, final JsonObject object) {
        object.addProperty("connectorSid", connector);
    }
    
    private void writeConnector(final String connector, final HierarchicalStreamWriter writer) {
        writer.startNode("ConnectorSid");
        if (connector != null) {
            writer.setValue(connector);
        }
        writer.endNode();
    }

    private void writeDomain(final String domain, final JsonObject object) {
        object.addProperty("domain", domain);
    }

    private void writeUserAgent(final String userAgent, final HierarchicalStreamWriter writer) {
        writer.startNode("UserAgent");
        if (userAgent != null) {
            writer.setValue(userAgent);
        }
        writer.endNode();
    }

    private void writeUserAgent(final String userAgent, final JsonObject object) {
        object.addProperty("userAgent", userAgent);
    }

  
    private void writePort(final Integer port, final HierarchicalStreamWriter writer) {
        writer.startNode("Port");
        if (port != null) {
            writer.setValue(port.toString());
        }
        writer.endNode();
    }

    private void writePort(final Integer port, final JsonObject object) {
        object.addProperty("port", port);
    }

    private void writeUser(final String user, final HierarchicalStreamWriter writer) {
        writer.startNode("User");
        if (user != null) {
            writer.setValue(user);
        }
        writer.endNode();
    }

    private void writeUser(final String user, final JsonObject object) {
        object.addProperty("user", user);
    }

    private void writeTransport(final String transport, final HierarchicalStreamWriter writer) {
        writer.startNode("Transport");
        if (transport != null) {
            writer.setValue(transport);
        }
        writer.endNode();
    }

    private void writeTransport(final String transport, final JsonObject object) {
        if (transport != null) {
            object.addProperty("transport", transport);
        }
    }


    private void writeExpires(final DateTime time, final HierarchicalStreamWriter writer) {
        writer.startNode("Expires");
        if (time != null) {
            writer.setValue(time.toString());
        }
        writer.endNode();
    }

    private void writeExpires(final DateTime time, final JsonObject object) {
        if (time != null) {
            object.addProperty("expires", time.toString());
        }
    }
    
   

}
