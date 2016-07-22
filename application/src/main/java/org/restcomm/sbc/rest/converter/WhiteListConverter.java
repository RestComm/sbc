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
import org.mobicents.servlet.sip.restcomm.util.StringUtils;
import org.restcomm.sbc.bo.WhiteList;


/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 */
@ThreadSafe
public final class WhiteListConverter extends AbstractConverter implements JsonSerializer<WhiteList> {
    private final String apiVersion;
    private final String rootUri;

    public WhiteListConverter(final Configuration configuration) {
        super(configuration);
        this.apiVersion = configuration.getString("api-version");
        rootUri = StringUtils.addSuffixIfNotPresent(configuration.getString("root-uri"), "/");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean canConvert(final Class klass) {
        return WhiteList.class.equals(klass);
    }

    @Override
    public void marshal(final Object object, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        final WhiteList entry = (WhiteList) object;
        writer.startNode("WhiteList");
        writeSid(entry.getSid(), writer);
        writeIpAddress(entry, writer);
        writeReason(entry.getReason().toString(), writer);
        writeDateCreated(entry.getDateCreated(), writer);
        writeDateExpires(entry.getDateExpires(), writer);
        writer.endNode();
    }

    @Override
    public JsonElement serialize(final WhiteList entry, final Type type, final JsonSerializationContext context) {
        final JsonObject object = new JsonObject();
        writeSid(entry.getSid(), object);
        writeIpAddress(entry, object);
        writeReason(entry.getReason().toString(), object);
        writeDateCreated(entry.getDateCreated(), object);
        writeDateExpires(entry.getDateExpires(), object);
        return object;
    }

   

    private void writeIpAddress(final WhiteList entry, final HierarchicalStreamWriter writer) {
        writer.startNode("EmailAddress");
        writer.setValue(entry.getIpAddress());
        writer.endNode();
        writer.close();
    }

    private void writeIpAddress(final WhiteList entry, final JsonObject object) {
        object.addProperty("email_address", entry.getIpAddress());
    }

   
}
