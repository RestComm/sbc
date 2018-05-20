/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2013, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.restcomm.sbc.rest.converter;

import java.lang.reflect.Type;

import javax.servlet.sip.URI;

import org.apache.commons.configuration.Configuration;
import org.mobicents.servlet.sip.restcomm.util.StringUtils;
import org.restcomm.sbc.call.Call;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * @author <a href="mailto:gvagenas@gmail.com">gvagenas</a>
 *
 */
public class CallConverter extends AbstractConverter implements JsonSerializer<Call>{
    private final String apiVersion;
    private final String rootUri;

    @SuppressWarnings("rawtypes")
    @Override
    public boolean canConvert(final Class klass) {
        return Call.class.equals(klass);
    }

    /**
     * @param configuration
     */
    public CallConverter(Configuration configuration) {
        super(configuration);
        apiVersion = configuration.getString("api-version");
        rootUri = StringUtils.addSuffixIfNotPresent(configuration.getString("root-uri"), "/");
    }

    @Override
    public JsonElement serialize(Call call, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject object = new JsonObject();
        writeSid(call.getSid(), object);
        writeState(call.getStatus().name(), object);
        if (call.getType() != null)
            writeType(call.getType().name(), object);
        writeDirection(call.getDirection().name(), object);
        writeDateCreated(call.getDateCreated(), object);
        writeCallerName(call.getFromName(), object);
        writeFrom(call.getFrom(), object);
        writeTo(call.getTo(), object);
        
        return object;
    }

    @Override
    public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext context) {
        final Call call = (Call) object;
        writer.startNode("Call");
        writeSid(call.getSid(), writer);
        writeState(call.getStatus().name(), writer);
        if (call.getType() != null)
            writeType(call.getType().name(), writer);
        writeDirection(call.getDirection().name(), writer);
        writeDateCreated(call.getDateCreated(), writer);
        writeCallerName(call.getFromName(), writer);
        writeFrom(call.getFrom(), writer);
        writeTo(call.getTo(), writer);
       
        writer.endNode();
    }

    private void writeState(final String state, final HierarchicalStreamWriter writer) {
        writer.startNode("State");
        writer.setValue(state);
        writer.endNode();
    }

    private void writeState(final String state, final JsonObject object) {
        object.addProperty("State", state);
    }

    private void writeDirection(final String direction, final HierarchicalStreamWriter writer) {
        writer.startNode("Direction");
        writer.setValue(direction);
        writer.endNode();
    }

    private void writeDirection(final String direction, final JsonObject object) {
        object.addProperty("direction", direction);
    }

    private void writeForwardedFrom(final String forwardedFrom, final HierarchicalStreamWriter writer) {
        writer.startNode("ForwardedFrom");
        if (forwardedFrom != null) {
            writer.setValue(forwardedFrom);
        }
        writer.endNode();
    }

    private void writeForwardedFrom(final String forwardedFrom, final JsonObject object) {
        object.addProperty("ForwardedFrom", forwardedFrom);
    }

    private void writeCallerName(final String callerName, final HierarchicalStreamWriter writer) {
        writer.startNode("CallerName");
        if (callerName != null) {
            writer.setValue(callerName);
        }
        writer.endNode();
    }

    private void writeCallerName(final String callerName, final JsonObject object) {
        object.addProperty("CallerName",  callerName);
    }

    private void writeInviteUri(final URI requestUri, final HierarchicalStreamWriter writer) {
        writer.startNode("Initial Invite");
        if (requestUri != null) {
            writer.setValue(requestUri.toString());
        }
        writer.endNode();
    }

    private void writeInviteUri(final URI requestUri, final JsonObject object) {
        object.addProperty("Initial Invite", requestUri.toString());
    }

    private void writeLastResponseUri(final int responseCode, final HierarchicalStreamWriter writer) {
        writer.startNode("Last Response");
        if (responseCode > -1) {
            writer.setValue(String.valueOf(responseCode));
        }
        writer.endNode();
    }

    private void writeLastResponseUri(final int responseCode, final JsonObject object) {
        object.addProperty("Last Response", responseCode);
    }
}
