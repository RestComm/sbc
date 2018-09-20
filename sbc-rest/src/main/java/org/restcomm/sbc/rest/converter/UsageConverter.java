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
import org.apache.commons.configuration.Configuration;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;


import org.joda.time.DateTime;
import org.restcomm.sbc.bo.Usage;

import java.lang.reflect.Type;
import java.math.BigDecimal;


/**
 * @author brainslog@gmail.com (Alexandre Mendonca)
 */
public final class UsageConverter extends AbstractConverter implements JsonSerializer<Usage> {
  public UsageConverter(final Configuration configuration) {
    super(configuration);
  }

  @SuppressWarnings("rawtypes")
  @Override
  public boolean canConvert(final Class klass) {
    return Usage.class.equals(klass);
  }

  @Override
  public void marshal(final Object object, final HierarchicalStreamWriter writer, final MarshallingContext context) {
    final Usage number = (Usage) object;
    writer.startNode("UsageRecord");
    writeCategory(number.getCategory(), writer);
    writeDescription(number.getDescription(), writer);
    writeStartDate(number.getStartDate(), writer);
    writeEndDate(number.getEndDate(), writer);
    writeUsage(number.getUsage(), writer);
    writeUsageUnit(number.getUsageUnit(), writer);
    writeCount(number.getCount(), writer);
    writeCountUnit(number.getCountUnit(), writer);
    writeRate(number.getRate(), writer);
    writeRateUnit(number.getRateUnit(), writer);
   
    writer.endNode();
  }

  private void writeCategory(final Usage.Category category, final HierarchicalStreamWriter writer) {
    writer.startNode("Category");
    if (category != null) {
      writer.setValue(category.toString());
    }
    writer.endNode();
  }

  private void writeCategory(final Usage.Category category, final JsonObject object) {
    object.addProperty("category", category.toString());
  }

  private void writeDescription(final String description, final HierarchicalStreamWriter writer) {
    writer.startNode("Description");
    if (description != null) {
      writer.setValue(description.toString());
    }
    writer.endNode();
  }

  private void writeDescription(final String description, final JsonObject object) {
    object.addProperty("description", description);
  }

  private void writeStartDate(final DateTime startDate, final HierarchicalStreamWriter writer) {
    writer.startNode("StartDate");
    if (startDate != null) {
      writer.setValue(startDate.toString("yyyy-MM-dd"));
    }
    writer.endNode();
  }

  private void writeStartDate(final DateTime startDate, final JsonObject object) {
    object.addProperty("start_date", startDate.toString("yyyy-MM-dd"));
  }

  private void writeEndDate(final DateTime endDate, final HierarchicalStreamWriter writer) {
    writer.startNode("EndDate");
    if (endDate != null) {
      writer.setValue(endDate.toString("yyyy-MM-dd"));
    }
    writer.endNode();
  }

  private void writeEndDate(final DateTime endDate, final JsonObject object) {
    object.addProperty("end_date", endDate.toString("yyyy-MM-dd"));
  }

  private void writeUsage(final BigDecimal usage, final HierarchicalStreamWriter writer) {
    writer.startNode("Usage");
    if (usage != null) {
      writer.setValue(usage.toPlainString());
    }
    writer.endNode();
  }

  private void writeUsage(final BigDecimal usage, final JsonObject object) {
    object.addProperty("usage", usage.toPlainString());
  }

  private void writeUsageUnit(final String usageUnit, final HierarchicalStreamWriter writer) {
    writer.startNode("UsageUnit");
    if (usageUnit != null) {
      writer.setValue(usageUnit);
    }
    writer.endNode();
  }

  private void writeUsageUnit(final String usageUnit, final JsonObject object) {
    object.addProperty("usage_unit", usageUnit);
  }

  private void writeCount(final Long count, final HierarchicalStreamWriter writer) {
    writer.startNode("Count");
    if (count != null) {
      writer.setValue(count.toString());
    }
    writer.endNode();
  }

  private void writeCount(final Long count, final JsonObject object) {
    object.addProperty("count", count.toString());
  }

  private void writeCountUnit(final String countUnit, final HierarchicalStreamWriter writer) {
    writer.startNode("CountUnit");
    if (countUnit != null) {
      writer.setValue(countUnit.toString());
    }
    writer.endNode();
  }

  private void writeCountUnit(final String countUnit, final JsonObject object) {
    object.addProperty("count_unit", countUnit);
  }

  private void writeRate(final BigDecimal rate, final HierarchicalStreamWriter writer) {
    writer.startNode("Rate");
    if (rate != null) {
      writer.setValue(rate.toPlainString());
    }
    writer.endNode();
  }

  private void writeRate(final BigDecimal rate, final JsonObject object) {
    object.addProperty("rate", rate.toPlainString());
  }
  
  private void writeRateUnit(final String rateUnit, final HierarchicalStreamWriter writer) {
	    writer.startNode("RateUnit");
	    if (rateUnit != null) {
	      writer.setValue(rateUnit);
	    }
	    writer.endNode();
  }

  private void writeRateUnit(final String rateUnit, final JsonObject object) {
	 object.addProperty("rate_unit", rateUnit);
  }

 
  @Override
  public JsonElement serialize(Usage usage, Type type, JsonSerializationContext jsonSerializationContext) {
    final JsonObject object = new JsonObject();
    writeCategory(usage.getCategory(), object);
    writeDescription(usage.getDescription(), object);
    writeStartDate(usage.getStartDate(), object);
    writeEndDate(usage.getEndDate(), object);
    writeUsage(usage.getUsage(), object);
    writeUsageUnit(usage.getUsageUnit(), object);
    writeCount(usage.getCount(), object);
    writeCountUnit(usage.getCountUnit(), object);
    writeRate(usage.getRate(), object);
    writeRateUnit(usage.getRateUnit(), object);
   
    return object;
  }
}
