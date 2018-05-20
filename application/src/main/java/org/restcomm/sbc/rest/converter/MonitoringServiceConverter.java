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
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.restcomm.sbc.call.Call;
import org.restcomm.sbc.managers.utils.MonitoringServiceResponse;

import com.google.gson.JsonArray;
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
public class MonitoringServiceConverter extends AbstractConverter implements JsonSerializer<MonitoringServiceResponse>{

    public MonitoringServiceConverter(Configuration configuration) {
        super(configuration);
    }

    @Override
    public boolean canConvert(final Class klass) {
        return MonitoringServiceResponse.class.equals(klass);
    }

    @Override
    public JsonElement serialize(MonitoringServiceResponse monitoringServiceResponse, Type typeOfSrc, JsonSerializationContext context) {
        Map<String, Integer> countersMap = monitoringServiceResponse.getCountersMap();
        JsonObject result = new JsonObject();
        JsonObject metrics = new JsonObject();
        JsonArray callsArray = new JsonArray();

        //First add InstanceId and Version details
        result.addProperty("InstanceId", monitoringServiceResponse.getInstanceId().getId().toString());
        result.addProperty("Version", org.restcomm.sbc.Version.getVersion());
        result.addProperty("Revision", org.restcomm.sbc.Version.getRevision());

        Iterator<String> counterIterator = countersMap.keySet().iterator();
        while (counterIterator.hasNext()) {
            String counter = counterIterator.next();
            metrics.addProperty(counter, countersMap.get(counter));
        }
        result.add("Metrics", metrics);

        if (monitoringServiceResponse.getCallDetailsList().size() > 0)
            for (Call callInfo: monitoringServiceResponse.getCallDetailsList()) {
                callsArray.add(context.serialize(callInfo));
            }
            result.add("LiveCallDetails", callsArray);
        return result;
    }

    @Override
    public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext context) {
        final MonitoringServiceResponse monitoringServiceResponse = (MonitoringServiceResponse) object;
        int size = monitoringServiceResponse.getCallDetailsList().size();
        final Map<String, Integer> countersMap = monitoringServiceResponse.getCountersMap();
        Iterator<String> counterIterator = countersMap.keySet().iterator();

        writer.startNode("InstanceId");
        writer.setValue(monitoringServiceResponse.getInstanceId().getId().toString());
        writer.endNode();

        writer.startNode("Version");
        writer.setValue(org.restcomm.sbc.Version.getVersion());
        writer.endNode();

        writer.startNode("Revision");
        writer.setValue(org.restcomm.sbc.Version.getRevision());
        writer.endNode();

        writer.startNode("Metrics");
        while (counterIterator.hasNext()) {
            String counter = counterIterator.next();
            writer.startNode(counter);
            writer.setValue(String.valueOf(countersMap.get(counter)));
            writer.endNode();
        }
        writer.endNode();

        if (size > 0) {
            writer.startNode("LiveCallDetails");

            for (final Call callInfo : monitoringServiceResponse.getCallDetailsList()) {
                context.convertAnother(callInfo);
            }
            writer.endNode();
        }
    }
}
