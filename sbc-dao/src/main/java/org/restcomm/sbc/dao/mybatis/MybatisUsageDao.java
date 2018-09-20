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
package org.restcomm.sbc.dao.mybatis;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.mobicents.servlet.sip.restcomm.annotations.concurrency.ThreadSafe;

import org.restcomm.sbc.bo.Usage;
import org.restcomm.sbc.dao.DaoUtils;
import org.restcomm.sbc.dao.UsageDao;

import java.math.BigDecimal;
import java.sql.Date;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;

import static org.restcomm.sbc.dao.DaoUtils.*;


/**
 * @author brainslog@gmail.com (Alexandre Mendonca)
 */
@ThreadSafe
public final class MybatisUsageDao implements UsageDao {

  private static final String namespace = "org.restcomm.sbc.dao.UsageDao.";
  private final SqlSessionFactory sessions;
  private static final Logger LOG = Logger.getLogger(MybatisUsageDao.class);

  public MybatisUsageDao(final SqlSessionFactory sessions) {
    super();
    this.sessions = sessions;
  }

  @Override
  public List<Usage> getUsage() {
    return getUsageRecords(null, null, null, "getAllTimeRecords");
  }

  @Override
  public List<Usage> getUsageDaily(Usage.Category category, DateTime startDate, DateTime endDate) {
    return getUsageRecords(category, startDate, endDate, "getDailyRecords");
  }
  
  @Override
  public List<Usage> getUsageWeekly(Usage.Category category, DateTime startDate, DateTime endDate) {
    return getUsageRecords(category, startDate, endDate, "getWeeklyRecords");
  }

  @Override
  public List<Usage> getUsageMonthly(Usage.Category category, DateTime startDate, DateTime endDate) {
    return getUsageRecords(category, startDate, endDate, "getMonthlyRecords");
  }

  @Override
  public List<Usage> getUsageYearly(Usage.Category category, DateTime startDate, DateTime endDate) {
    return getUsageRecords(category, startDate, endDate, "getYearlyRecords");
  }

  @Override
  public List<Usage> getUsageAllTime(Usage.Category category, DateTime startDate, DateTime endDate) {
    return getUsageRecords(category, startDate, endDate, "getAllTimeRecords");
  }

  private List<Usage> getUsageRecords(Usage.Category category, DateTime startDate, DateTime endDate, final String queryName) {
    final SqlSession session = sessions.openSession();
    Map<String, Object> params = new HashMap<String, Object>();
    
    params.put("startDate", new Date(startDate.getMillis()));
    params.put("endDate", new Date(endDate.getMillis()));
    
    fillParametersByCategory(category, params);
    try {
      final List<Map<String, Object>> results = session.selectList(namespace + queryName, params);
      final List<Usage> usageRecords = new ArrayList<Usage>();
      if (results != null && !results.isEmpty()) {
        for (final Map<String, Object> result : results) {
          usageRecords.add(toUsageRecord(result));
        }
      }
      return usageRecords;
    } finally {
      session.close();
    }
  }

  private Usage toUsageRecord(final Map<String, Object> map) {
    final Usage.Category category = Usage.Category.LIVECALLS;
    final String description = "Total Calls";
    final DateTime startDate = DateTimeFormat.forPattern("yyyyy-MM-dd").parseDateTime(map.get("start_date").toString());
    final DateTime endDate = DateTimeFormat.forPattern("yyyyy-MM-dd").parseDateTime(map.get("end_date").toString());

    //final Long xusage = DaoUtils.readLong(map.get("xusage"));
    final BigDecimal usage = DaoUtils.readBigDecimal(map.get("usage").toString());
    
    final String usageUnit = "seconds";

    final Long count = readLong(map.get("count"));
    final String countUnit = "records";

    /* FIXME: readBigDecimal should take Double instead of String ? */
    final BigDecimal rate = DaoUtils.readBigDecimal(map.get("rate").toString());
   
    final String rateUnit = "Call";

   
    return new Usage(category, description, startDate, endDate, usage, usageUnit, count, countUnit, rate, rateUnit);
  }
  
  
  private Map<String, Object> fillParametersByCategory(Usage.Category category, Map<String, Object> params) {
    // FIXME: handle no category, meaning all
    if (category == null) category = Usage.Category.LIVECALLS;
    switch (category) {
      case LIVECALLS:
      
        params.put("tableName", "restcomm_statistics");
        params.put("usageExprPre", "");
        params.put("usageExprCol", "live_call_count");
        params.put("usageExprSuf", "");
 
        break;
      case CPU:
    	  params.put("tableName", "restcomm_statistics");
          params.put("usageExprPre", "");
          params.put("usageExprCol", "cpu_usage");
          params.put("usageExprSuf", "");
        break;
      case MEMORY:
    	  params.put("tableName", "restcomm_statistics");
          params.put("usageExprPre", "");
          params.put("usageExprCol", "mem_usage");
          params.put("usageExprSuf", "");
        break;
      case THREAT:
    	  params.put("tableName", "restcomm_statistics");
          params.put("usageExprPre", "COALESCE( CEIL(AVG(");
          params.put("usageExprCol", "threat_count");
          params.put("usageExprSuf", ") /60),0) ");
        break;
      case REJECTED:
    	  params.put("tableName", "restcomm_statistics");
          params.put("usageExprPre", "COALESCE( CEIL(AVG(");
          params.put("usageExprCol", "rejected_count");
          params.put("usageExprSuf", ") /60),0) ");
        break;
      default:

    	  params.put("tableName", "restcomm_statistics");
          params.put("usageExprPre", "COALESCE( CEIL(SUM(");
          params.put("usageExprCol", "live_call_count");
          params.put("usageExprSuf", ") /60),0) ");
        break;
    }
    return params;
  }

}
