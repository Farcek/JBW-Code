/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.le.farcek.jbw.code.managers;

import java.util.List;
import java.util.Set;
import mn.le.farcek.jbw.api.action.IActionSecurity;
import mn.le.farcek.jbw.api.action.IActionSession;
import mn.le.farcek.jbw.api.security.ISecurityManager;
import mn.le.farcek.jbw.api.security.IUser;
import mn.le.farcek.jbw.api.security.SecurityRole;

public class IActionSecurityImpl implements IActionSecurity {

    private final ISecurityManager securityManager;
    private final IActionSession actionSession;

    public IActionSecurityImpl(ISecurityManager securityManager, IActionSession actionSession) {
        this.securityManager = securityManager;
        this.actionSession = actionSession;
    }

    @Override
    public IUser getCurrentUser() {
        return securityManager.getAuthentication(actionSession);
    }

    @Override
    public boolean hasAccess(String role) {
        IUser u = getCurrentUser();
        if (u == null) {
            return false;
        }
        Set<String> roles = securityManager.getCurrentRoles(actionSession);
        if (roles == null) {
            return false;
        }
        for (String r : roles) {
            SecurityRole.Role sr = securityManager.getSecurityRole().get(r);

            if (sr != null && sr.hasAccess(role)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasRole(String role) {
        IUser u = getCurrentUser();
        if (u == null) {
            return false;
        }
        Set<String> roles = securityManager.getCurrentRoles(actionSession);
        if (roles == null) {
            return false;
        }

        for (String r : roles) {
            if (role.equalsIgnoreCase(r)) {
                return true;
            }
        }
        return false;
    }

}
