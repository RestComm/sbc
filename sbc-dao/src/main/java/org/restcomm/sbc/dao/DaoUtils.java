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
package org.restcomm.sbc.dao;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Currency;
import java.util.Date;

import org.joda.time.DateTime;
import org.mobicents.servlet.sip.restcomm.annotations.concurrency.ThreadSafe;
import org.restcomm.sbc.bo.Account;
import org.restcomm.sbc.bo.BanList;
import org.restcomm.sbc.bo.Connector;
import org.restcomm.sbc.bo.NetworkPoint;
import org.restcomm.sbc.bo.Sid;






/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 */
@ThreadSafe
public final class DaoUtils {
    private DaoUtils() {
        super();
    }

    public static Account.Status readAccountStatus(final Object object) {
        if (object != null) {
            return Account.Status.getValueOf((String) object);
        } else {
            return null;
        }
    }

    public static Account.Type readAccountType(final Object object) {
        if (object != null) {
            return Account.Type.getValueOf((String) object);
        } else {
            return null;
        }
    }
    
    public static BanList.Reason readBanListReason(final Object object) {
        if (object != null) {
            return BanList.Reason.getValueOf((String) object);
        } else {
            return null;
        }
    }
    
    public static BanList.Type readBanListType(final Object object) {
        if (object != null) {
            return BanList.Type.getValueOf((String) object);
        } else {
            return null;
        }
    }

    public static BanList.Action readMonitorAction(final Object object) {
        if (object != null) {
            return BanList.Action.getValueOf((String) object);
        } else {
            return null;
        }
    }
    
    public static NetworkPoint.Tag readTag(final Object object) {
        if (object != null) {
            return NetworkPoint.Tag.getValueOf((String) object);
        } else {
            return null;
        }
    }
    
    public static Connector.Transport readTransport(final Object object) {
        if (object != null) {
            return Connector.Transport.getValueOf((String) object);
        } else {
            return null;
        }
    }
    public static Connector.State readState(final Object object) {
        if (object != null) {
            return Connector.State.getValueOf((String) object);
        } else {
            return null;
        }
    }
    public static BigDecimal readBigDecimal(final Object object) {
        if (object != null) {
            return new BigDecimal((String) object);
        } else {
            return null;
        }
    }

    public static Boolean readBoolean(final Object object) {
        if (object != null) {
            return (Boolean) object;
        } else {
            return null;
        }
    }

    public static DateTime readDateTime(final Object object) {
        if (object != null) {
            return new DateTime((Date) object);
        } else {
            return null;
        }
    }

    public static Double readDouble(final Object object) {
        if (object != null) {
            return (Double) object;
        } else {
            return null;
        }
    }

    public static Integer readInteger(final Object object) {
        if (object != null) {
            return (Integer) object;
        } else {
            return null;
        }
    }

    public static Long readLong(final Object object) {
        if (object != null) {
            return (Long) object;
        } else {
            return null;
        }
    }

    public static Sid readSid(final Object object) {
        if (object != null) {
            return new Sid((String) object);
        } else {
            return null;
        }
    }
    
    public static NetworkPoint readPoint(final Object object) {
        if (object != null) {
            return new NetworkPoint((String) object);
        } else {
            return null;
        }
    }

    public static String readString(final Object object) {
        if (object != null) {
            return (String) object;
        } else {
            return null;
        }
    }

    public static URI readUri(final Object object) {
        if (object != null) {
            return URI.create((String) object);
        } else {
            return null;
        }
    }

    public static Currency readCurrency(final Object object) {
        if (object != null) {
            return Currency.getInstance((String) object);
        } else {
            return null;
        }
    }


    public static String writeAccountStatus(final Account.Status status) {
        return status.toString();
    }

    public static String writeAccountType(final Account.Type type) {
        return type.toString();
    }
    
    public static String writeBanListReason(final BanList.Reason reason) {
        return reason.toString();
    }
    
    public static String writeBanListType(final BanList.Type color) {
        return color.toString();
    }
    
    public static String writeMonitorAction(final BanList.Action action) {
        return action.toString();
    }
    
    public static String writeTag(final NetworkPoint.Tag tag) {
        return tag.toString();
    }
    
    public static String writeTransport(final Connector.Transport transport) {
        return transport.toString();
    }
    
    public static String writeState(final Connector.State state) {
        return state.toString();
    }
    
    public static String writePoint(final NetworkPoint point) {
        return point.toString();
    }

    public static String writeBigDecimal(final BigDecimal bigDecimal) {
        if (bigDecimal != null) {
            return bigDecimal.toString();
        } else {
            return null;
        }
    }

    public static Date writeDateTime(final DateTime dateTime) {
        if (dateTime != null) {
            return dateTime.toDate();
        } else {
            return null;
        }
    }

    public static String writeSid(final Sid sid) {
        if (sid != null) {
            return sid.toString();
        } else {
            return null;
        }
    }

    public static String writeUri(final URI uri) {
        if (uri != null) {
            return uri.toString();
        } else {
            return null;
        }
    }

    public static String writeCurrency(final Currency currency) {
        if (currency != null) {
            return currency.getCurrencyCode();
        } else {
            return null;
        }
    }


}
