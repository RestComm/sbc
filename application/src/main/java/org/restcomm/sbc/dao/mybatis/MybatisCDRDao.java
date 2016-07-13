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
import static org.restcomm.sbc.dao.DaoUtils.*;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.joda.time.DateTime;
import org.restcomm.sbc.annotations.concurrency.ThreadSafe;
import org.restcomm.sbc.bo.CDR;
import org.restcomm.sbc.bo.CDRFilter;
import org.restcomm.sbc.dao.CDRDao;



 /**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    1/7/2016 19:01:34
 * @class   MybatisCDRDao.java
 *
 */
@ThreadSafe
public final class MybatisCDRDao implements CDRDao {
    private static final String namespace = "org.restcomm.sbc.dao.CDRDao.";
    private final SqlSessionFactory sessions;

    public MybatisCDRDao(final SqlSessionFactory sessions) {
        super();
        this.sessions = sessions;
    }

    @Override
    public void addCDR(final CDR cdr) {
        final SqlSession session = sessions.openSession();
        try {
            session.insert(namespace + "addCDR", toMap(cdr));
            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public CDR getCDR(final int sid) {
        final SqlSession session = sessions.openSession();
        try {
            final Map<String, Object> result = session.selectOne(namespace + "getCDR", sid);
            if (result != null) {
                return toCDR(result);
            } else {
                return null;
            }
        } finally {
            session.close();
        }
    }

    // Issue 110
    @Override
    public Integer getTotalCDRs(CDRFilter filter) {

        final SqlSession session = sessions.openSession();
        try {
            final Integer total = session.selectOne(namespace + "getTotalCDRByUsingFilters", filter);
            return total;
        } finally {
            session.close();
        }

    }

    @Override
    public List<CDR> getCDRs(CDRFilter filter) {

        final SqlSession session = sessions.openSession();

        try {
            final List<Map<String, Object>> results = session.selectList(namespace + "getCDRByUsingFilters",
                    filter);
            final List<CDR> cdrs = new ArrayList<CDR>();

            if (results != null && !results.isEmpty()) {
                for (final Map<String, Object> result : results) {
                    cdrs.add(toCDR(result));
                }
            }
            return cdrs;
        } finally {
            session.close();
        }
    }


    @Override
    public List<CDR> getCDRsByRecipient(final String recipient) {
        return getCDRs(namespace + "getCDRsByRecipient", recipient);
    }

    @Override
    public List<CDR> getCDRsBySender(final String sender) {
        return getCDRs(namespace + "getCDRsBySender", sender);
    }

    @Override
    public List<CDR> getCDRsByStatus(final int status) {
        return getCDRs(namespace + "getCDRsByStatus", status);
    }

    @Override
    public List<CDR> getCDRsByStartTime(final DateTime startTime) {
        return getCDRs(namespace + "getCDRsByStartTime", startTime);
    }

    @Override
	public List<CDR> getCDRs() {
    	final SqlSession session = sessions.openSession();

        try {
            final List<Map<String, Object>> results = session.selectList(namespace + "getCDRs");
            final List<CDR> cdrs = new ArrayList<CDR>();

            if (results != null && !results.isEmpty()) {
                for (final Map<String, Object> result : results) {
                    cdrs.add(toCDR(result));
                }
            }
            return cdrs;
        } finally {
            session.close();
        }

	}

    private List<CDR> getCDRs(final String selector, Object input) {
        final SqlSession session = sessions.openSession();
        try {
            final List<Map<String, Object>> results = session.selectList(selector, input);
            final List<CDR> cdrs = new ArrayList<CDR>();
            if (results != null && !results.isEmpty()) {
                for (final Map<String, Object> result : results) {
                    cdrs.add(toCDR(result));
                }
            }
            return cdrs;
        } finally {
            session.close();
        }
    }
    private void removeCDR(final String selector, final int uid) {
        final SqlSession session = sessions.openSession();
        try {
            session.delete(selector, uid);
            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void removeCDR(final int uid) {
        removeCDR(namespace + "removeCDR", uid);
    }

    

    @Override
    public void updateCDR(final CDR cdr) {
        final SqlSession session = sessions.openSession();
        try {
            session.update(namespace + "updateCDR", toMap(cdr));
            session.commit();
        } finally {
            session.close();
        }
    }

    private CDR toCDR(final Map<String, Object> map) {
    	/*
    	 * 
    	this.uid = uid;
		this.fromUser = fromUser;
		this.toUser = toUser;
		this.fromIp = fromIp;
		this.startTime = startTime;
		this.endTime = endTime;
		this.duration = duration;
		this.status = status;
    	 */
        final int uid = readInteger(map.get("uid"));
        final DateTime startTime = readDateTime(map.get("startTime"));
        final DateTime endTime   = readDateTime(map.get("endTime"));
       
        final String fromUser = readString(map.get("fromUser"));
        final String fromIP   = readString(map.get("fromIP"));
        final String toUser   = readString(map.get("toUser"));

        final Integer duration = readInteger(map.get("duration"));
        final Integer status   = readInteger(map.get("status"));
      
        return new CDR(uid, fromUser, toUser, fromIP, startTime, endTime, duration, status);
    }

    private Map<String, Object> toMap(final CDR cdr) {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("uid", cdr.getUid());
        map.put("startTime", writeDateTime(cdr.getStartTime()));
        map.put("endTime  ", writeDateTime(cdr.getStartTime()));
        map.put("fromIP", cdr.getFromIp());
        map.put("fromUser", cdr.getFromUser());
        map.put("toUser", cdr.getToUser());
        map.put("status", cdr.getStatus());
   
        map.put("duration", cdr.getDuration());
      
        return map;
    }

	
}
