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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.Properties;

import org.apache.commons.configuration.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.mobicents.servlet.sip.restcomm.annotations.concurrency.ThreadSafe;
import org.restcomm.sbc.dao.AccountsDao;
import org.restcomm.sbc.dao.CallDetailRecordsDao;
import org.restcomm.sbc.dao.DaoManager;
import org.restcomm.sbc.dao.WhiteListDao;
import org.restcomm.sbc.dao.BlackListDao;


/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 */
@ThreadSafe
public final class MybatisDaoManager implements DaoManager {
    private Configuration configuration;
    private AccountsDao accountsDao;
    private CallDetailRecordsDao callDetailRecordsDao;
    private WhiteListDao whiteListDao;
    private BlackListDao blackListDao;
    

    public MybatisDaoManager() {
        super();
    }

    @Override
    public void configure(final Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public AccountsDao getAccountsDao() {
        return accountsDao;
    }
    @Override
	public CallDetailRecordsDao getCallDetailRecordsDao() {
		return callDetailRecordsDao;
	}
    
    @Override
	public WhiteListDao getWhiteListDao() {	
		return whiteListDao;
	}
    
    @Override
	public BlackListDao getBlackListDao() {	
		return blackListDao;
	}
    
    @Override
    public void shutdown() {
        // Nothing to do.
    }

    @Override
    public void start() throws RuntimeException {
        // This must be called before any other MyBatis methods.
        //org.apache.ibatis.logging.LogFactory.useSlf4jLogging();
        org.apache.ibatis.logging.LogFactory.useLog4JLogging();
        // Load the configuration file.
        final SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
        final String path = configuration.getString("configuration-file");
        Reader reader = null;
        try {
            reader = new FileReader(path);
        } catch (final FileNotFoundException exception) {
            throw new RuntimeException(exception);
        }
        final Properties properties = new Properties();
        final String dataFiles = configuration.getString("data-files");
        final String sqlFiles = configuration.getString("sql-files");
        properties.setProperty("data", dataFiles);
        properties.setProperty("sql", sqlFiles);
        final SqlSessionFactory sessions = builder.build(reader, properties);
        start(sessions);
    }

    public void start(final SqlSessionFactory sessions) {
        // Instantiate the DAO objects.
        accountsDao = new MybatisAccountsDao(sessions);
        callDetailRecordsDao = new MybatisCallDetailRecordsDao(sessions);
        whiteListDao = new MybatisWhiteListDao(sessions);
        blackListDao = new MybatisBlackListDao(sessions);
        
      
    }

	
}
