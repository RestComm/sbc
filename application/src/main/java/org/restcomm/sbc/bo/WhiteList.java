package org.restcomm.sbc.bo;

import org.joda.time.DateTime;
import org.restcomm.sbc.notification.impl.Monitor.Action;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    22 jul. 2016 17:00:44
 * @class   WhiteList.java
 *
 */
public final class WhiteList extends BanList {

	public WhiteList(DateTime dateCreated, DateTime dateExpires, String ipAddress, Sid accountSid,
			Reason reason, Action action) {
		super(dateCreated, dateExpires, ipAddress, accountSid, reason, action);
	}

}
