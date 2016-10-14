package org.restcomm.sbc.bo;

import org.joda.time.DateTime;
import org.restcomm.sbc.servlet.sip.SBCMonitorServlet.Action;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    22 jul. 2016 17:02:29
 * @class   BlackList.java
 *
 */
public final class BlackList extends BanList {

	public BlackList(DateTime dateCreated, DateTime dateExpires, String ipAddress, Sid accountSid,
			Reason reason, Action action) {
		super(dateCreated, dateExpires, ipAddress, accountSid, reason, action);
	}

}
