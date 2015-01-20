/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.le.farcek.jbw.code.managers;

import com.google.inject.Injector;
import java.lang.reflect.Method;
import java.util.HashMap;

import mn.le.farcek.common.utils.FStringUtils;
import mn.le.farcek.jbw.api.Action;
import mn.le.farcek.jbw.api.exception.MissingAction;
import mn.le.farcek.jbw.api.managers.IActionManager;
import mn.le.farcek.jbw.api.managers.IBundleManager;
import mn.le.farcek.jbw.api.managers.IControllerManager;

public class IControllerManagerImpl implements IControllerManager {

    private final HashMap<String, IActionManager> actions = new HashMap<>();
    private final Object controllerObject;
    private final Injector injector;
    private final String name;
    private final IBundleManager bundleManager;

    public IControllerManagerImpl(Object controllerObject, Injector injector, String name, IBundleManager bundleManager) {
        this.controllerObject = controllerObject;
        this.injector = injector;
        this.name = name;
        this.bundleManager = bundleManager;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getControllerObject() {
        return controllerObject;
    }

    @Override
    public IBundleManager getBundleManager() {
        return bundleManager;
    }

    @Override
    public IActionManager getActionManager(String name) throws MissingAction {
        if (name == null)
            throw new NullPointerException();

        IActionManager action = actions.get(name);
        if (action instanceof IActionManager)
            return action;

        for (Method m : controllerObject.getClass().getMethods()) {
            Action aAnnotation = m.getAnnotation(Action.class);
            if (aAnnotation != null) {
                String aName = aAnnotation.name();
                if (FStringUtils.isEmpty(aName))
                    aName = m.getName();

                if (name.equals(aName)) {
                    action = new IActionManagerImpl(controllerObject, m, aName, this, injector);
                    injector.injectMembers(action);
                    actions.put(name, action);
                    return action;
                }
            }

        }

        throw new MissingAction(String.format("not found action `%s` in controller `%s` ", name, this.name));
    }

}
