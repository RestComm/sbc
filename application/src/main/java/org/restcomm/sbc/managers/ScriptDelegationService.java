package org.restcomm.sbc.managers;
import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.log4j.Logger;


 /**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    28 jul. 2016 18:36:53
 * @class   Script.java
 *
 */
public class ScriptDelegationService {
    static int iExitValue=-1;
    static String sCommandString;
    
    public static final String SCRIPT_BAN = "sh /usr/local/bin/RestComm-sbc/ban.sh";
    
    
    private static transient Logger LOG = Logger.getLogger(ScriptDelegationService.class);

    public static int runScript(String command){
        sCommandString = command;
        CommandLine oCmdLine = CommandLine.parse(sCommandString);
        DefaultExecutor oDefaultExecutor = new DefaultExecutor();
        oDefaultExecutor.setExitValue(0);
        try {
            iExitValue = oDefaultExecutor.execute(oCmdLine);
        } catch (ExecuteException e) {
            LOG.error(command+" Execution failed.");
        } catch (IOException e) {
            LOG.error(command+" Permission denied.");
        }
        return iExitValue;
    }
    
    public static int runBanScript(String ipAddress) {
    	return runScript(SCRIPT_BAN+" add "+ipAddress);
    }

	public static int runUnBanScript(String ipAddress) {
		return runScript(SCRIPT_BAN+" remove "+ipAddress);
	}

	public static int runAllowScript(String ipAddress) {
		return runScript(SCRIPT_BAN+" allow "+ipAddress);
	}
	
	public static int runDisallowScript(String ipAddress) {
		return runScript(SCRIPT_BAN+" remove "+ipAddress);
	}

}