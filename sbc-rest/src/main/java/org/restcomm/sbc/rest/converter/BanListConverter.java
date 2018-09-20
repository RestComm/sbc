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
import org.restcomm.sbc.bo.BanList;


/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 */
@ThreadSafe
public final class BanListConverter extends AbstractConverter implements JsonSerializer<BanList> {
    //private final String apiVersion;
    //private final String rootUri;
    private BanList.Type color;

    public BanListConverter(BanList.Type color, final Configuration configuration) {
        super(configuration);
        this.color = color;
      //  apiVersion = configuration.getString("api-version");
      //  rootUri = StringUtils.addSuffixIfNotPresent(configuration.getString("root-uri"), "/");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean canConvert(final Class klass) {
        return BanList.class.equals(klass);
    }

    @Override
    public void marshal(final Object object, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        final BanList banList = (BanList) object;
        writer.startNode(color.toString()+"List");
        writeDateCreated(banList.getDateCreated(), writer);
        writeDateExpires(banList.getDateExpires(), writer);
        writeAccountSid(banList.getAccountSid(), writer);
        writeIpAddress(banList.getIpAddress(), writer);
        writeAction(banList.getAction().toString(), writer);
        writeReason(banList.getReason().toString(), writer);
      
        writer.endNode();
    }


    @Override
    public JsonElement serialize(final BanList banList, Type type, final JsonSerializationContext context) {
        final JsonObject object = new JsonObject();
        writeDateCreated(banList.getDateCreated(), object);
        writeDateExpires(banList.getDateExpires(), object);
        writeAccountSid(banList.getAccountSid(), object);
        writeIpAddress(banList.getIpAddress(), object);
        writeReason(banList.getReason().toString(), object);
        writeAction(banList.getAction().toString(), object);
        //writeColor(color, object);
        return object;
    }

    private void writeIpAddress(final String ipAddress, final HierarchicalStreamWriter writer) {
        writer.startNode("IpAddress");
        if (ipAddress != null) {
            writer.setValue(ipAddress);
        }
        writer.endNode();
    }

    private void writeIpAddress(final String ipAddress, final JsonObject object) {
        object.addProperty("ip_address", ipAddress);
    }


    
}
