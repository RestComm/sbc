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
package org.restcomm.sbc.bo;

import org.joda.time.DateTime;
import org.mobicents.servlet.sip.restcomm.annotations.concurrency.Immutable;

import java.io.Serializable;
import java.math.BigDecimal;



/**
 * @author brainslog@gmail.com (Alexandre Mendonca)
 */
@Immutable
public final class Usage implements Serializable {
  private static final long serialVersionUID = 1L;

  private final Category category;
  private final String description;
  private final DateTime startDate;
  private final DateTime endDate;
  private final BigDecimal usage;
  private final String usageUnit;
  private final Long count;
  private final String countUnit;
  private final BigDecimal rate;
  private final String rateUnit;
  

  public Usage(final Category category, final String description, final DateTime startDate, final DateTime endDate,
               final BigDecimal usage, final String usageUnit, final Long count, final String countUnit, final BigDecimal rate,
               final String rateUnit) {
    super();
    this.category = category;
    this.description = description;
    this.startDate = startDate;
    this.endDate = endDate;
    this.usage = usage;
    this.usageUnit = usageUnit;
    this.count = count;
    this.countUnit = countUnit;
    this.rate = rate;
    this.rateUnit = rateUnit;
    
  }

  public Category getCategory() {
    return category;
  }

  public String getDescription() {
    return description;
  }


  public DateTime getStartDate() {
    return startDate;
  }

  public DateTime getEndDate() {
    return endDate;
  }

  public BigDecimal getUsage() {
    return usage;
  }

  public String getUsageUnit() {
    return usageUnit;
  }

  public Long getCount() {
    return count;
  }

  public String getCountUnit() {
    return countUnit;
  }

  public BigDecimal getRate() {
    return rate;
  }

  public String getRateUnit() {
    return rateUnit;
  }

 
  public enum Category {
    LIVECALLS("live-calls"), CPU("cpu"), MEMORY("memory"), REJECTED("rejected"), THREAT("threat");

    private final String text;

    private Category(final String text) {
      this.text = text;
    }

    public static Category getCategoryValue(final String text) {
      final Category[] values = values();
      for (final Category value : values) {
        if (value.toString().equals(text)) {
          return value;
        }
      }
      throw new IllegalArgumentException(text + " is not a valid category.");
    }

    @Override
    public String toString() {
      return text;
    }
  }
}
