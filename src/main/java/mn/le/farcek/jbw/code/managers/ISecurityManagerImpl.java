/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.le.farcek.jbw.code.managers;

import com.google.inject.Inject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mn.le.farcek.jbw.api.IConfig;
import mn.le.farcek.jbw.api.action.IActionSession;
import mn.le.farcek.jbw.api.security.ISecurityManager;
import mn.le.farcek.jbw.api.security.IUser;
import mn.le.farcek.jbw.api.security.SecurityRole;

public class ISecurityManagerImpl implements ISecurityManager {

    @Inject
    IConfig config;

    @Override
    public String encryptPassword(String pass, String salt) {
        String generatedPassword = null;
        String fullSalt = config.getSecureToken() + salt;
        try {

            // Create MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            //Add password bytes to digest
            md.update(fullSalt.getBytes());
            //Get the hash's bytes
            byte[] bytes = md.digest(pass.getBytes());
            //This bytes[] has bytes in decimal format;
            //Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            //Get complete hashed password in hex format
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            if (config.isDebug()) {
                e.printStackTrace();
            }

        }
        return generatedPassword;
    }

    @Override
    public SecurityRole getSecurityRole() {
        return config.getSecurityRole();
    }

    @Override
    public void setAuthentication(IUser user, IActionSession session) {
        if (user == null) {
            throw new NullPointerException();
        }
        
        
        
        session.setAttribute(JBW_CURRENT_USER_ATTR, user);

        // role
        List<String> userRoles = user.getRoles();
        Set<String> roles = new HashSet<>();
        if (userRoles != null) {
            for (String r : userRoles) {
                roles.add(r);
            }
        }
        roles.add(JBW_MEMBER_ROLE);
        
      
        
        session.setAttribute(JBW_CURRENT_ROLES_ATTR, roles);
    }

    @Override
    public void clearAuthentication(IActionSession session) {
        session.removeAttribute(JBW_CURRENT_USER_ATTR);
        session.removeAttribute(JBW_CURRENT_ROLES_ATTR);
    }

    @Override
    public IUser getAuthentication(IActionSession session) {
        Object o = session.getAttribute(JBW_CURRENT_USER_ATTR);
        if (o instanceof IUser) {
            return (IUser) o;
        }
        return null;
    }

    @Override
    public Set<String> getCurrentRoles(IActionSession session) {
        Object o = session.getAttribute(JBW_CURRENT_ROLES_ATTR);
        if (o instanceof Set) {
            return (Set<String>) o;
        }
        return null;
    }

    @Override
    public void addCurrentRole(String role, IActionSession session) {
        Set<String> currentRoles = getCurrentRoles(session);
        if (currentRoles == null) {
            currentRoles = new HashSet<>();
        }
        currentRoles.add(role);
        session.setAttribute(JBW_CURRENT_ROLES_ATTR, currentRoles);
    }

}
