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

import org.mobicents.servlet.sip.restcomm.Configurable;
import org.mobicents.servlet.sip.restcomm.LifeCycle;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    1 ago. 2016 19:31:13
 * @class   DaoManager.java
 *
 */
public interface DaoManager extends Configurable, LifeCycle {
    
	CallDetailRecordsDao getCallDetailRecordsDao();

	AccountsDao getAccountsDao();

	WhiteListDao getWhiteListDao();
	
	BlackListDao getBlackListDao();
	
	NetworkPointsDao getNetworkPointDao();
	
	ConnectorsDao getConnectorsDao();
	
	RoutesDao getRoutesDao();
	
	StatisticsDao getStatisticsDao();

	UsageDao getUsageDao();
    
}
