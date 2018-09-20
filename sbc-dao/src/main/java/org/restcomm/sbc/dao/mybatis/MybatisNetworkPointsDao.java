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
import org.mobicents.servlet.sip.restcomm.annotations.concurrency.ThreadSafe;

import static org.restcomm.sbc.dao.DaoUtils.*;
import org.restcomm.sbc.dao.NetworkPointsDao;
import org.restcomm.sbc.bo.NetworkPoint;
import org.restcomm.sbc.bo.Sid;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    27 jul. 2016 19:06:21
 * @class   MybatisNetworkPointsDao.java
 *
 */
@ThreadSafe
public final class MybatisNetworkPointsDao implements NetworkPointsDao {  
private final static String namespace = "org.restcomm.sbc.dao.NetworkPointsDao.";
    private final SqlSessionFactory sessions;
   
    public MybatisNetworkPointsDao(final SqlSessionFactory sessions) {
        super();
        this.sessions = sessions;
    }

    @Override
    public void addNetworkPoint(final NetworkPoint entry) {
        final SqlSession session = sessions.openSession();
        try {
            session.insert(namespace + "addEntry", toMap(entry));
            session.commit();
        } finally {
            session.close();
        }
    }


    @Override
    public NetworkPoint getNetworkPoint(final String name) {
    	NetworkPoint entry = null;
        entry = getNetworkPoint(namespace + "getEntryByTag", name);
        
        if (entry == null) {
            entry = getNetworkPoint(namespace + "getEntry", name);
        }

        return entry;
    }
    
   
    public boolean isTagged(final String point) {
    	NetworkPoint entry = null;
    	entry = getNetworkPoint(namespace + "getEntry", point);        
        
    	if (entry == null) {
    		return false;   
        }
    	
    	return entry.getTag().isTagged();

    }

    private NetworkPoint getNetworkPoint(final String selector, final Object parameters) {
        final SqlSession session = sessions.openSession();
        try {
            final Map<String, Object> result = session.selectOne(selector, parameters);
            if (result != null) {
                return toNetworkPoint(result);
            } else {
                return null;
            }
        } finally {
            session.close();
        }
    }

    @Override
    public List<NetworkPoint> getNetworkPoints() {
        final SqlSession session = sessions.openSession();
        try {
            final List<Map<String, Object>> results = session.selectList(namespace + "getEntries");
            final List<NetworkPoint> entrys = new ArrayList<NetworkPoint>();
            if (results != null && !results.isEmpty()) {
                for (final Map<String, Object> result : results) {
                    entrys.add(toNetworkPoint(result));
                }
            }
            return entrys;
        } finally {
            session.close();
        }
    }

    
    
    @Override
    public void updateNetworkPoint(final NetworkPoint point) {
        updateNetworkPoint(namespace + "updateEntry", point);
    }
    
    private void updateNetworkPoint(final String selector, final NetworkPoint point) {
        final SqlSession session = sessions.openSession();
        try {
            session.update(selector, toMap(point));
            session.commit();
        } finally {
            session.close();
        }
    }
    
    @Override
    public void removeNetworkPoint(final String uid) {
        removeNetworkPoint(namespace + "removeEntry", uid);
    }

    private void removeNetworkPoint(final String selector, final String uid) {
        final SqlSession session = sessions.openSession();
        try {
            session.delete(selector, uid);
            session.commit();
        } finally {
            session.close();
        }
    }

    private NetworkPoint toNetworkPoint(final Map<String, Object> map) {
        final String id = readString(map.get("id"));
        final Sid entrySid = readSid(map.get("account_sid"));
        final NetworkPoint.Tag tag = readTag(map.get("tag"));
        return new NetworkPoint(id, entrySid, tag);
    }
   
    private Map<String, Object> toMap(final NetworkPoint entry) {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", entry.getId());
        map.put("tag", writeTag(entry.getTag()));
        map.put("account_sid", writeSid(entry.getAccountSid()));
    
        return map;
    }
    
}
