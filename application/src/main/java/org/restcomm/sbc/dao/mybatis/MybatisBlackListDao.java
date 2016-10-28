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
import org.restcomm.sbc.dao.BlackListDao;
import org.restcomm.sbc.servlet.sip.SBCMonitorServlet;
import org.restcomm.sbc.bo.BanList;
import org.restcomm.sbc.bo.BanListFilter;
import org.restcomm.sbc.bo.Sid;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    22 jul. 2016 17:09:05
 * @class   MybatisBanListDao.java
 *
 */
@ThreadSafe
public final class MybatisBlackListDao implements BlackListDao {  
private final static String namespace = "org.restcomm.sbc.dao.BlackListDao.";
    private final SqlSessionFactory sessions;
    private static transient Logger LOG = Logger.getLogger(MybatisBlackListDao.class);
	
    public MybatisBlackListDao(final SqlSessionFactory sessions) {
        super();
        this.sessions = sessions;
    }

    @Override
    public void addBanList(final BanList entry) {
        final SqlSession session = sessions.openSession();
        try {
            session.insert(namespace + "addEntry", toMap(entry));
            session.commit();
        } finally {
            session.close();
        }
    }


    @Override
    public BanList getBanList(final String name) {
        BanList entry = null;
        entry = getBanList(namespace + "getEntryByIpAddress", name);
        
        if (entry == null){
        	entry = getBanList(namespace + "getEntryByReason", name);
           
        }
        if (entry == null) {
            entry = getBanList(namespace + "getEntry", name);
        }

        return entry;
    }

    private BanList getBanList(final String selector, final Object parameters) {
    	if(LOG.isDebugEnabled()) {
    		LOG.debug("Params="+parameters);
    	}
        final SqlSession session = sessions.openSession();
        try {
            final Map<String, Object> result = session.selectOne(selector, parameters);
            if (result != null) {
                return toBanList(result);
            } else {
                return null;
            }
        } finally {
            session.close();
        }
    }

    @Override
    public List<BanList> getBanLists() {
        final SqlSession session = sessions.openSession();
        try {
            final List<Map<String, Object>> results = session.selectList(namespace + "getEntries");
            final List<BanList> entrys = new ArrayList<BanList>();
            if (results != null && !results.isEmpty()) {
                for (final Map<String, Object> result : results) {
                    entrys.add(toBanList(result));
                }
            }
            return entrys;
        } finally {
            session.close();
        }
    }

    
    
    @Override
    public void updateBanList(final BanList banList) {
        updateBanList(namespace + "updateEntry", banList);
    }
    
    private void updateBanList(final String selector, final BanList banList) {
        final SqlSession session = sessions.openSession();
        try {
            session.update(selector, toMap(banList));
            session.commit();
        } finally {
            session.close();
        }
    }
    
    @Override
    public void removeBanList(final BanList banList) {
        removeBanList(namespace + "removeEntry", banList);
    }

    private void removeBanList(final String selector, final BanList banList) {
        final SqlSession session = sessions.openSession();
        try {
            session.delete(selector, toMap(banList));
            session.commit();
        } finally {
            session.close();
        }
    }

    private BanList toBanList(final Map<String, Object> map) {
        final DateTime dateCreated = readDateTime(map.get("date_created"));
        final DateTime dateExpires = readDateTime(map.get("date_expires"));
        final String ipAddress = readString(map.get("ip_address"));
        final Sid entrySid = readSid(map.get("account_sid"));
        final BanList.Reason reason = readBanListReason(map.get("reason"));
        final SBCMonitorServlet.Action action = readMonitorAction(map.get("monitor_action"));
        return new BanList(dateCreated, dateExpires, ipAddress, entrySid, reason, action);
    }
   
    private Map<String, Object> toMap(final BanList entry) {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("date_created", writeDateTime(entry.getDateCreated()));
        map.put("date_expires", writeDateTime(entry.getDateExpires()));
        map.put("ip_address", entry.getIpAddress());
        map.put("account_sid", writeSid(entry.getAccountSid()));
        map.put("reason", writeBanListReason(entry.getReason()));
        map.put("monitor_action", writeMonitorAction(entry.getAction()));
        
        
        return map;
    }
    
    
 // Issue 110
    @Override
    public Integer getTotalBanLists(BanListFilter filter) {

        final SqlSession session = sessions.openSession();
        try {
            final Integer total = session.selectOne(namespace + "getTotalBanListByUsingFilters", filter);
            return total;
        } finally {
            session.close();
        }

    }

    @Override
    public List<BanList> getBanLists(BanListFilter filter) {

        final SqlSession session = sessions.openSession();

        try {
            final List<Map<String, Object>> results = session.selectList(namespace + "getBanListByUsingFilters",
                    filter);
            final List<BanList> banLists = new ArrayList<BanList>();

            if (results != null && !results.isEmpty()) {
                for (final Map<String, Object> result : results) {
                    banLists.add(toBanList(result));
                }
            }
            return banLists;
        } finally {
            session.close();
        }
    }

}
