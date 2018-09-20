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
import org.restcomm.sbc.bo.Statistics;
import org.restcomm.sbc.bo.StatisticsList;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    12 oct. 2016 16:50:59
 * @class   StatisticsListConverter.java
 *
 */
@ThreadSafe
public final class StatisticsListConverter extends AbstractConverter implements JsonSerializer<StatisticsList> {


    public StatisticsListConverter(final Configuration configuration) {
        super(configuration);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean canConvert(final Class klass) {
        return StatisticsList.class.equals(klass);
    }

    @Override
    public void marshal(final Object object, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        final StatisticsList list = (StatisticsList) object;

        writer.startNode("Statistics");
        

        for (final Statistics route : list.getStatisticsList()) {
            context.convertAnother(route);
        }
        writer.endNode();
    }

   
    @Override
    public JsonObject serialize(StatisticsList routeList, Type type, JsonSerializationContext context) {

        JsonObject result = new JsonObject();

        JsonArray array = new JsonArray();
        for (Statistics route : routeList.getStatisticsList()) {
            array.add(context.serialize(route));
        }

        

        result.add("entries", array);

        return result;
    }

   

}
