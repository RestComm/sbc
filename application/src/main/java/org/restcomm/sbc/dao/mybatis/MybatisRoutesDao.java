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
import org.mobicents.servlet.sip.restcomm.annotations.concurrency.ThreadSafe;

import static org.restcomm.sbc.dao.DaoUtils.*;
import org.restcomm.sbc.dao.RoutesDao;
import org.restcomm.sbc.bo.Route;
import org.restcomm.sbc.bo.Sid;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    27 jul. 2016 19:06:21
 * @class   MybatisRoutingPoliciesDao.java
 *
 */
@ThreadSafe
public final class MybatisRoutesDao implements RoutesDao {  
private final static String namespace = "org.restcomm.sbc.dao.RoutesDao.";
    private final SqlSessionFactory sessions;
    private static transient Logger LOG = Logger.getLogger(MybatisRoutesDao.class);
	
    public MybatisRoutesDao(final SqlSessionFactory sessions) {
        super();
        this.sessions = sessions;
    }

    @Override
    public void addRoute(final Route entry) {
        final SqlSession session = sessions.openSession();
        try {
            session.insert(namespace + "addEntry", toMap(entry));
            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public Route getRoute(final Sid sid) {
    	Route entry = null;
    	Map<String, Object> parms = new HashMap<String, Object>();
    	parms.put("sid", sid.toString());
        entry = getRoutingPolicy(namespace + "getEntry", parms);
        
        return entry;
    }

    private Route getRoutingPolicy(final String selector, final Object parameters) {
        final SqlSession session = sessions.openSession();
        try {
            final Map<String, Object> result = session.selectOne(selector, parameters);
            if (result != null) {
                return toRoutingPolicy(result);
            } else {
                return null;
            }
        } finally {
            session.close();
        }
    }

    @Override
    public List<Route> getRoutes() {
        final SqlSession session = sessions.openSession();
        try {
            final List<Map<String, Object>> results = session.selectList(namespace + "getEntries");
            final List<Route> entrys = new ArrayList<Route>();
            if (results != null && !results.isEmpty()) {
                for (final Map<String, Object> result : results) {
                    entrys.add(toRoutingPolicy(result));
                }
            }
            return entrys;
        } finally {
            session.close();
        }
    }

   
    
    @Override
    public void removeRoute(final Sid sid) {
        removeRoutingPolicy(namespace + "removeEntry", sid.toString());
    }

    private void removeRoutingPolicy(final String selector, final Object parameters) {
        final SqlSession session = sessions.openSession();
        try {
            session.delete(selector, parameters);
            session.commit();
        } finally {
            session.close();
        }
    }

    private Route toRoutingPolicy(final Map<String, Object> map) {
    	final Sid sid = readSid(map.get("sid"));
    	final Sid sourceConnector = readSid(map.get("source_connector_sid"));
    	final Sid targetConnector = readSid(map.get("target_connector_sid"));
        final Sid entrySid = readSid(map.get("account_sid"));
        return new Route(sid, entrySid, sourceConnector, targetConnector);
    }
   
    private Map<String, Object> toMap(final Route entry) {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("sid", writeSid(entry.getSid()));
        map.put("source_connector_sid", writeSid(entry.getSourceConnector()));
        map.put("target_connector_sid", writeSid(entry.getTargetConnector()));
        map.put("account_sid", writeSid(entry.getAccountSid()));
        return map;
    }
    
}
