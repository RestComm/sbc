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
import org.restcomm.sbc.bo.Statistics;




/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    12 oct. 2016 16:25:07
 * @class   StatisticsConverter.java
 *
 */
@ThreadSafe
public final class StatisticsConverter extends AbstractConverter implements JsonSerializer<Statistics> {
    
    public StatisticsConverter(final Configuration configuration) {
        super(configuration);
       
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean canConvert(final Class klass) {
        return Statistics.class.equals(klass);
    }

    @Override
    public void marshal(final Object object, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        final Statistics stat = (Statistics) object;
        writer.startNode("Statistics");
        writeSid(stat.getSid(), writer);
        this.writeInteger(stat.getCpuUsage(), "CpuUsage", writer);
        this.writeInteger(stat.getCallRejectedCount(), "CallsRejected", writer);
        this.writeInteger(stat.getLiveCallsCount(), "LiveCalls", writer);
        this.writeInteger(stat.getMemoryUsage(), "MemoryUsage", writer);
        this.writeInteger(stat.getThreatCount(), "Threats", writer);
        this.writeDouble(stat.getCallRate(), "CallRate", writer);
        writeDateCreated(stat.getDateCreated(), writer);
        
        
        writer.endNode();
    }


    @Override
    public JsonElement serialize(final Statistics stat, Type type, final JsonSerializationContext context) {
        final JsonObject object = new JsonObject();
        writeSid(stat.getSid(), object);
       
        this.writeInteger(stat.getCpuUsage(), "cpu_usage", object);
        this.writeInteger(stat.getCallRejectedCount(), "calls_rejected", object);
        this.writeInteger(stat.getLiveCallsCount(), "live_calls", object);
        this.writeInteger(stat.getMemoryUsage(), "memory_usage", object);
        this.writeInteger(stat.getThreatCount(), "threats", object);
        this.writeDouble(stat.getCallRate(), "call_rate", object);
        writeDateCreated(stat.getDateCreated(), object);
       
        return object;
    }
    
    protected void writeInteger(final int value, String name, final HierarchicalStreamWriter writer) {
        writer.startNode(name);
        writer.setValue(""+value);
        writer.endNode();
    }
    
   
    
    protected void writeDouble(final double value, String name, final HierarchicalStreamWriter writer) {
        writer.startNode(name);
        writer.setValue(""+value);
        writer.endNode();
    }

    protected void writeInteger(final int value, String name, final JsonObject object) {
        object.addProperty(name, ""+value);
    }
    
    protected void writeDouble(final double value, String name, final JsonObject object) {
        object.addProperty(name, ""+value);
    }

}
