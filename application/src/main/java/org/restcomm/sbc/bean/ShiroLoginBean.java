package org.restcomm.sbc.bean;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.restcomm.sbc.bo.shiro.Realm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import java.io.IOException;
import java.io.Serializable;

@ManagedBean(name = "shiroLoginBean")
@ViewScoped
public class ShiroLoginBean implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 5533733415597724728L;

	private static final Logger log = LoggerFactory.getLogger(ShiroLoginBean.class);

    private String username;
    private String password;
    private Boolean rememberMe;

    public ShiroLoginBean() {
    }

    /**
     * Try and authenticate the user
     */
    public void doLogin() {
    	
    	Realm realm = new Realm();
    	CredentialsMatcher customMatcher = new org.restcomm.sbc.bo.shiro.CredentialsMatcher();
    	realm.setCredentialsMatcher(customMatcher);
    	
    	SecurityManager securityManager = new DefaultSecurityManager(realm);
    	//Make the SecurityManager instance available to the entire application:
    	SecurityUtils.setSecurityManager(securityManager);
    	
        Subject subject = SecurityUtils.getSubject();
        if(log.isDebugEnabled()) {
        	log.debug("Subject "+subject.toString()+":"+subject.getPrincipal());
        }
       
        UsernamePasswordToken token = new UsernamePasswordToken(getUsername(), getPassword(), getRememberMe());
        
        try {
            subject.login(token);

            if (subject.hasRole("Administrator")) {
                FacesContext.getCurrentInstance().getExternalContext().redirect("admin/index.xhtml");
            }
            else {
                FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml");
            }
        }
        catch (UnknownAccountException ex) {
            facesError("Unknown account");
            log.error(ex.getMessage(), ex);
        }
        catch (IncorrectCredentialsException ex) {
            facesError("Wrong password");
            log.error(ex.getMessage(), ex);
        }
        catch (LockedAccountException ex) {
            facesError("Locked account");
            log.error(ex.getMessage(), ex);
        }
        catch (AuthenticationException | IOException ex) {
            facesError("Unknown error: " + ex.getMessage());
            log.error(ex.getMessage(), ex);
        }
        finally {
            token.clear();
        }
    }

    /**
     * Adds a new SEVERITY_ERROR FacesMessage for the ui
     * @param message Error Message
     */
    private void facesError(String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String login) {
        this.username = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String senha) {
        this.password = senha;
    }

    public Boolean getRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(Boolean lembrar) {
        this.rememberMe = lembrar;
    }
}