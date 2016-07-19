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


import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.joda.time.DateTime;
import org.mobicents.servlet.sip.restcomm.annotations.concurrency.ThreadSafe;
import org.restcomm.sbc.bo.CallDetailRecord;
import org.restcomm.sbc.bo.CallDetailRecordFilter;
import org.restcomm.sbc.bo.Sid;
import org.restcomm.sbc.dao.CallDetailRecordsDao;

import static org.restcomm.sbc.dao.DaoUtils.readBigDecimal;
import static org.restcomm.sbc.dao.DaoUtils.readCurrency;
import static org.restcomm.sbc.dao.DaoUtils.readDateTime;
import static org.restcomm.sbc.dao.DaoUtils.readInteger;
import static org.restcomm.sbc.dao.DaoUtils.readSid;
import static org.restcomm.sbc.dao.DaoUtils.readString;
import static org.restcomm.sbc.dao.DaoUtils.readUri;
import static org.restcomm.sbc.dao.DaoUtils.readBoolean;
import static org.restcomm.sbc.dao.DaoUtils.writeBigDecimal;
import static org.restcomm.sbc.dao.DaoUtils.writeDateTime;
import static org.restcomm.sbc.dao.DaoUtils.writeSid;
import static org.restcomm.sbc.dao.DaoUtils.writeUri;


/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 */
@ThreadSafe
public final class MybatisCallDetailRecordsDao implements CallDetailRecordsDao {
    private static final String namespace = "org.restcomm.sbc.dao.CallDetailRecordsDao.";
    private final SqlSessionFactory sessions;

    public MybatisCallDetailRecordsDao(final SqlSessionFactory sessions) {
        super();
        this.sessions = sessions;
    }

    @Override
    public void addCallDetailRecord(final CallDetailRecord cdr) {
        final SqlSession session = sessions.openSession();
        try {
            session.insert(namespace + "addCallDetailRecord", toMap(cdr));
            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public CallDetailRecord getCallDetailRecord(final Sid sid) {
        final SqlSession session = sessions.openSession();
        try {
            final Map<String, Object> result = session.selectOne(namespace + "getCallDetailRecord", sid.toString());
            if (result != null) {
                return toCallDetailRecord(result);
            } else {
                return null;
            }
        } finally {
            session.close();
        }
    }

    // Issue 110
    @Override
    public Integer getTotalCallDetailRecords(CallDetailRecordFilter filter) {

        final SqlSession session = sessions.openSession();
        try {
            final Integer total = session.selectOne(namespace + "getTotalCallDetailRecordByUsingFilters", filter);
            return total;
        } finally {
            session.close();
        }

    }

    // Issue 153: https://bitbucket.org/telestax/telscale-restcomm/issue/153
    // Issue 110: https://bitbucket.org/telestax/telscale-restcomm/issue/110
    @Override
    public List<CallDetailRecord> getCallDetailRecords(CallDetailRecordFilter filter) {

        final SqlSession session = sessions.openSession();

        try {
            final List<Map<String, Object>> results = session.selectList(namespace + "getCallDetailRecordByUsingFilters",
                    filter);
            final List<CallDetailRecord> cdrs = new ArrayList<CallDetailRecord>();

            if (results != null && !results.isEmpty()) {
                for (final Map<String, Object> result : results) {
                    cdrs.add(toCallDetailRecord(result));
                }
            }
            return cdrs;
        } finally {
            session.close();
        }
    }

    @Override
    public List<CallDetailRecord> getCallDetailRecords(final Sid accountSid) {
        return getCallDetailRecords(namespace + "getCallDetailRecords", accountSid.toString());
    }

    @Override
    public List<CallDetailRecord> getCallDetailRecordsByRecipient(final String recipient) {
        return getCallDetailRecords(namespace + "getCallDetailRecordsByRecipient", recipient);
    }

    @Override
    public List<CallDetailRecord> getCallDetailRecordsBySender(final String sender) {
        return getCallDetailRecords(namespace + "getCallDetailRecordsBySender", sender);
    }

    @Override
    public List<CallDetailRecord> getCallDetailRecordsByStatus(final String status) {
        return getCallDetailRecords(namespace + "getCallDetailRecordsByStatus", status);
    }

    @Override
    public List<CallDetailRecord> getCallDetailRecordsByStartTime(final DateTime startTime) {
        return getCallDetailRecords(namespace + "getCallDetailRecordsByStartTime", startTime.toDate());
    }

    @Override
    public List<CallDetailRecord> getCallDetailRecordsByEndTime(final DateTime endTime) {
        return getCallDetailRecords(namespace + "getCallDetailRecordsByEndTime", endTime.toDate());
    }

    @Override
    public List<CallDetailRecord> getCallDetailRecordsByStarTimeAndEndTime(final DateTime endTime) {
        return getCallDetailRecords(namespace + "getCallDetailRecordsByStarTimeAndEndTime", endTime.toDate());
    }

    @Override
    public List<CallDetailRecord> getCallDetailRecordsByParentCall(final Sid parentCallSid) {
        return getCallDetailRecords(namespace + "getCallDetailRecordsByParentCall", parentCallSid.toString());
    }

    @Override
    public List<CallDetailRecord> getCallDetailRecordsByConferenceSid(final Sid conferenceSid) {
        return getCallDetailRecords(namespace + "getCallDetailRecordsByConferenceSid", conferenceSid.toString());
    }

    @Override
    public List<CallDetailRecord> getCallDetailRecordsByInstanceId(final Sid instanceId) {
        return getCallDetailRecords(namespace + "getCallDetailRecordsByInstanceId", instanceId.toString());
    }

    private List<CallDetailRecord> getCallDetailRecords(final String selector, Object input) {
        final SqlSession session = sessions.openSession();
        try {
            final List<Map<String, Object>> results = session.selectList(selector, input);
            final List<CallDetailRecord> cdrs = new ArrayList<CallDetailRecord>();
            if (results != null && !results.isEmpty()) {
                for (final Map<String, Object> result : results) {
                    cdrs.add(toCallDetailRecord(result));
                }
            }
            return cdrs;
        } finally {
            session.close();
        }
    }

    @Override
    public void removeCallDetailRecord(final Sid sid) {
        removeCallDetailRecords(namespace + "removeCallDetailRecord", sid);
    }

    @Override
    public void removeCallDetailRecords(final Sid accountSid) {
        removeCallDetailRecords(namespace + "removeCallDetailRecords", accountSid);
    }

    private void removeCallDetailRecords(final String selector, final Sid sid) {
        final SqlSession session = sessions.openSession();
        try {
            session.delete(selector, sid.toString());
            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void updateCallDetailRecord(final CallDetailRecord cdr) {
        final SqlSession session = sessions.openSession();
        try {
            session.update(namespace + "updateCallDetailRecord", toMap(cdr));
            session.commit();
        } finally {
            session.close();
        }
    }

    private CallDetailRecord toCallDetailRecord(final Map<String, Object> map) {
        final Sid sid = readSid(map.get("sid"));
        final String instanceId = readString(map.get("instanceid"));
        final Sid parentCallSid = readSid(map.get("parent_call_sid"));
        final Sid conferenceSid = readSid(map.get("conference_sid"));
        final DateTime dateCreated = readDateTime(map.get("date_created"));
        final DateTime dateUpdated = readDateTime(map.get("date_updated"));
        final Sid accountSid = readSid(map.get("account_sid"));
        final String to = readString(map.get("recipient"));
        final String from = readString(map.get("sender"));
        final Sid phoneNumberSid = readSid(map.get("phone_number_sid"));
        final String status = readString(map.get("status"));
        final DateTime startTime = readDateTime(map.get("start_time"));
        final DateTime endTime = readDateTime(map.get("end_time"));
        final Integer duration = readInteger(map.get("duration"));
        final Integer ringDuration = readInteger(map.get("ring_duration"));
        final BigDecimal price = readBigDecimal(map.get("price"));
        final Currency priceUnit = readCurrency(map.get("price_unit"));
        final String direction = readString(map.get("direction"));
        final String answeredBy = readString(map.get("answered_by"));
        final String apiVersion = readString(map.get("api_version"));
        final String forwardedFrom = readString(map.get("forwarded_from"));
        final String callerName = readString(map.get("caller_name"));
        final URI uri = readUri(map.get("uri"));
        final String callPath = readString(map.get("call_path"));
        final Boolean muted = readBoolean(map.get("muted"));
        final Boolean startConferenceOnEnter = readBoolean(map.get("start_conference_on_enter"));
        final Boolean endConferenceOnExit = readBoolean(map.get("end_conference_on_exit"));
        final Boolean onHold = readBoolean(map.get("on_hold"));
        return new CallDetailRecord(sid, instanceId, parentCallSid, conferenceSid, dateCreated, dateUpdated, accountSid, to, from, phoneNumberSid, status,
                startTime, endTime, duration, price, priceUnit, direction, answeredBy, apiVersion, forwardedFrom, callerName,
                uri, callPath, ringDuration, muted, startConferenceOnEnter, endConferenceOnExit, onHold);
    }

    private Map<String, Object> toMap(final CallDetailRecord cdr) {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("sid", writeSid(cdr.getSid()));
        map.put("instanceid", cdr.getInstanceId());
        map.put("parent_call_sid", writeSid(cdr.getParentCallSid()));
        map.put("conference_sid", writeSid(cdr.getConferenceSid()));
        map.put("date_created", writeDateTime(cdr.getDateCreated()));
        map.put("date_updated", writeDateTime(cdr.getDateUpdated()));
        map.put("account_sid", writeSid(cdr.getAccountSid()));
        map.put("to", cdr.getTo());
        map.put("from", cdr.getFrom());
        map.put("phone_number_sid", writeSid(cdr.getPhoneNumberSid()));
        map.put("status", cdr.getStatus());
        map.put("start_time", writeDateTime(cdr.getStartTime()));
        map.put("end_time", writeDateTime(cdr.getEndTime()));
        map.put("duration", cdr.getDuration());
        map.put("ring_duration", cdr.getRingDuration());
        map.put("price", writeBigDecimal(cdr.getPrice()));
        map.put("direction", cdr.getDirection());
        map.put("answered_by", cdr.getAnsweredBy());
        map.put("api_version", cdr.getApiVersion());
        map.put("forwarded_from", cdr.getForwardedFrom());
        map.put("caller_name", cdr.getCallerName());
        map.put("uri", writeUri(cdr.getUri()));
        map.put("call_path", cdr.getCallPath());
        map.put("muted", cdr.isMuted());
        map.put("start_conference_on_enter", cdr.isStartConferenceOnEnter());
        map.put("end_conference_on_exit", cdr.isEndConferenceOnExit());
        map.put("on_hold", cdr.isOnHold());
        return map;
    }
}
