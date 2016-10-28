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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.mobicents.servlet.sip.restcomm.annotations.concurrency.ThreadSafe;

import static org.restcomm.sbc.dao.DaoUtils.*;
import org.restcomm.sbc.dao.StatisticsDao;
import org.restcomm.sbc.bo.Sid;
import org.restcomm.sbc.bo.Statistics;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    12 oct. 2016 14:49:42
 * @class   MybatisStatisticsDao.java
 *
 */
@ThreadSafe
public final class MybatisStatisticsDao implements StatisticsDao {  
private final static String namespace = "org.restcomm.sbc.dao.StatisticsDao.";
    private final SqlSessionFactory sessions;
    private static transient Logger LOG = Logger.getLogger(MybatisStatisticsDao.class);
	
    public MybatisStatisticsDao(final SqlSessionFactory sessions) {
        super();
        this.sessions = sessions;
    }

    @Override
    public void addRecord(final Statistics entry) {
        final SqlSession session = sessions.openSession();
        try {
            session.insert(namespace + "addEntry", toMap(entry));
            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public Statistics getRecord(final Sid sid) {
    	Statistics entry = null;
    	Map<String, Object> parms = new HashMap<String, Object>();
    	parms.put("sid", sid);
        entry = getRecord(namespace + "getEntry", parms);
        
        return entry;
    }

    private Statistics getRecord(final String selector, final Object parameters) {
        final SqlSession session = sessions.openSession();
        try {
            final Map<String, Object> result = session.selectOne(selector, parameters);
            if (result != null) {
                return toRecord(result);
            } else {
                return null;
            }
        } finally {
            session.close();
        }
    }

    @Override
    public List<Statistics> getRecords() {
        final SqlSession session = sessions.openSession();
        try {
            final List<Map<String, Object>> results = session.selectList(namespace + "getEntries");
            final List<Statistics> entrys = new ArrayList<Statistics>();
            if (results != null && !results.isEmpty()) {
                for (final Map<String, Object> result : results) {
                    entrys.add(toRecord(result));
                }
            }
            return entrys;
        } finally {
            session.close();
        }
    }

   
    
    @Override
    public void removeRecord(final Sid sid) {
        removeRecord(namespace + "removeEntry", ""+sid);
    }

    private void removeRecord(final String selector, final Object parameters) {
        final SqlSession session = sessions.openSession();
        try {
            session.delete(selector, parameters);
            session.commit();
        } finally {
            session.close();
        }
    }

    private Statistics toRecord(final Map<String, Object> map) {
    	final Sid sid = readSid(map.get("sid"));
    	final int mem = readInteger(map.get("mem_usage"));
    	final int cpu = readInteger(map.get("cpu_usage"));
    	final int liveCalls = readInteger(map.get("live_call_count"));
    	final int rejectedCount = readInteger(map.get("rejected_count"));
    	final int threatCount = readInteger(map.get("threat_count"));
    	final double callRate = readDouble(map.get("call_rate"));
    	final DateTime dateCreated = readDateTime(map.get("date_created"));
    	
        return new Statistics(sid, mem, cpu, liveCalls, callRate, rejectedCount, threatCount, dateCreated);
    }
   
    private Map<String, Object> toMap(final Statistics entry) {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("sid", entry.getSid());
        map.put("mem_usage", entry.getMemoryUsage());
        map.put("cpu_usage", entry.getCpuUsage());
        map.put("live_call_count", entry.getLiveCallsCount());
        map.put("rejected_count", entry.getCallRejectedCount());
        map.put("threat_count", entry.getThreatCount());
        map.put("call_rate", entry.getCallRate());
        map.put("date_created", writeDateTime(entry.getDateCreated()));
       
        return map;
    }
    
}
