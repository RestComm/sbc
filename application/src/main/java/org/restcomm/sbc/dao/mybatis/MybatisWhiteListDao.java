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

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import org.joda.time.DateTime;
import org.mobicents.servlet.sip.restcomm.annotations.concurrency.ThreadSafe;

import static org.restcomm.sbc.dao.DaoUtils.*;
import org.restcomm.sbc.dao.WhiteListDao;
import org.restcomm.sbc.bo.WhiteList;
import org.restcomm.sbc.bo.Sid;


/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 */
@ThreadSafe
public final class MybatisWhiteListDao implements WhiteListDao {
    private static final String namespace = "org.restcomm.sbc.dao.WhiteListDao.";
    private final SqlSessionFactory sessions;

    public MybatisWhiteListDao(final SqlSessionFactory sessions) {
        super();
        this.sessions = sessions;
    }

    @Override
    public void addWhiteList(final WhiteList entry) {
        final SqlSession session = sessions.openSession();
        try {
            session.insert(namespace + "addEntry", toMap(entry));
            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public WhiteList getWhiteList(final Sid sid) {
        return getWhiteList(namespace + "getEntry", sid.toString());
    }

    @Override
    public WhiteList getWhiteList(final String name) {
        WhiteList entry = null;
        entry = getWhiteList(namespace + "getEntryByEmail", name);
        
        if (entry == null){
        	entry = getWhiteList(namespace + "getEntryByFriendlyName", name);
           
        }
        if (entry == null) {
            entry = getWhiteList(namespace + "getEntry", name);
        }

        return entry;
    }

    private WhiteList getWhiteList(final String selector, final Object parameters) {
        final SqlSession session = sessions.openSession();
        try {
            final Map<String, Object> result = session.selectOne(selector, parameters);
            if (result != null) {
                return toWhiteList(result);
            } else {
                return null;
            }
        } finally {
            session.close();
        }
    }

    @Override
    public List<WhiteList> getWhiteLists(final Sid entrySid) {
        final SqlSession session = sessions.openSession();
        try {
            final List<Map<String, Object>> results = session.selectList(namespace + "getEntries", entrySid.toString());
            final List<WhiteList> entrys = new ArrayList<WhiteList>();
            if (results != null && !results.isEmpty()) {
                for (final Map<String, Object> result : results) {
                    entrys.add(toWhiteList(result));
                }
            }
            return entrys;
        } finally {
            session.close();
        }
    }

    @Override
    public void removeWhiteList(final Sid sid) {
        removeWhiteList(namespace + "removeEntry", sid);
    }

    private void removeWhiteList(final String selector, final Sid sid) {
        final SqlSession session = sessions.openSession();
        try {
            session.delete(selector, sid.toString());
            session.commit();
        } finally {
            session.close();
        }
    }

    private WhiteList toWhiteList(final Map<String, Object> map) {
        final Sid sid = readSid(map.get("sid"));
        final DateTime dateCreated = readDateTime(map.get("date_created"));
        final DateTime dateExpires = readDateTime(map.get("date_expires"));
        final String ipAddress = readString(map.get("ip_address"));
        final Sid entrySid = readSid(map.get("entry_sid"));
        final WhiteList.Reason reason = readWhiteListReason(map.get("reason"));

        return new WhiteList(sid, dateCreated, dateExpires, ipAddress, entrySid, reason);
    }
   
    private Map<String, Object> toMap(final WhiteList entry) {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("sid", writeSid(entry.getSid()));
        map.put("date_created", writeDateTime(entry.getDateCreated()));
        map.put("date_expires", writeDateTime(entry.getDateExpires()));
        map.put("ip_address", entry.getIpAddress());
        map.put("entry_sid", writeSid(entry.getAccountSid()));
        map.put("reason", writeWhiteListReason(entry.getReason()));
      
        
        return map;
    }
}
