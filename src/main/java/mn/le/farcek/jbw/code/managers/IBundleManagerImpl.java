/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.le.farcek.jbw.code.managers;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Injector;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import mn.le.farcek.common.utils.FStringUtils;
import mn.le.farcek.jbw.api.Bundle;
import mn.le.farcek.jbw.api.Controller;
import mn.le.farcek.jbw.api.IBundle;
import mn.le.farcek.jbw.api.bundle.BundleSetter;
import mn.le.farcek.jbw.api.exception.MissingAsset;
import mn.le.farcek.jbw.api.exception.MissingController;
import mn.le.farcek.jbw.api.managers.IBundleManager;
import mn.le.farcek.jbw.api.managers.IControllerManager;
import mn.le.farcek.jbw.api.utils.BundleUtil;

public class IBundleManagerImpl implements IBundleManager {

    private final Injector injector;
    private final IBundle bundle;
    private final String name;
    private String defaultControllerName;

    private final HashMap<String, IControllerManager> controllers = new HashMap<>();
    private final LoadingCache<String, IControllerManager> cacher = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build(new CacheLoader<String, IControllerManager>() {

                @Override
                public IControllerManager load(String key) throws Exception {
                    return createControllerManager(key);
                }
            });

    ;

    public IBundleManagerImpl(Injector injector, IBundle bundle, String name) {
        this.injector = injector;
        this.bundle = bundle;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public IBundle getBundle() {
        return bundle;
    }

    public IControllerManager createControllerManager(String name) throws MissingController {
        for (Class<?> cls : bundle.getControllers()) {
            IControllerManager c = loockup(cls);
            if (c != null) return c;
        }

        throw new MissingController(String.format("`%s` the controller not found in `%s` bundle", name, getName()));
    }

    private IControllerManager loockup(Class<?> cls) {
        if (cls == null) return null;
        Controller cAnnotation = cls.getAnnotation(Controller.class);
        if (cAnnotation != null) {
            String cName = cAnnotation.name();
            if (FStringUtils.isEmptyOrNull(cName))
                cName = FStringUtils.firstLower(cls.getSimpleName());
            if (name.equals(cName)) {
                Object contollerObject = injector.getInstance(cls);
                //injector.injectMembers(contollerObject);

                if (contollerObject instanceof BundleSetter)
                    ((BundleSetter) contollerObject).setBundle(bundle);

                return new IControllerManagerImpl(contollerObject, injector, name, this);
            }

        }
        return null;
    }

    @Override
    public IControllerManager getControllerManager(String name) throws MissingController {
        try {
            return cacher.get(name);
        } catch (ExecutionException ex) {
            throw new MissingController(ex);
        }
    }

    public IControllerManager getControllerManager1212(String name) throws MissingController {
        IControllerManager controller = controllers.get(name);
        if (controller instanceof IControllerManager)
            return controller;

        for (Class<?> cls : bundle.getControllers())
            if (cls != null) {
                Controller cAnnotation = cls.getAnnotation(Controller.class);
                if (cAnnotation != null) {
                    String cName = cAnnotation.name();
                    if (FStringUtils.isEmpty(cName))
                        cName = FStringUtils.firstLower(cls.getSimpleName());
                    if (name.equals(cName))
                        try {
                            Object contollerObject = cls.newInstance();
                            injector.injectMembers(contollerObject);

                            if (contollerObject instanceof BundleSetter)
                                ((BundleSetter) contollerObject).setBundle(bundle);

                            controller = new IControllerManagerImpl(contollerObject, injector, cName, this);
                            controllers.put(name, controller);
                            return controller;
                        } catch (InstantiationException | IllegalAccessException ex) {
                            throw new MissingController(String.format("`%s` the controller not found in `%s` bundle", name, getName()), ex);
                        }
                }
            }

        throw new MissingController(String.format("`%s` controller is not found in `%s` bundle", name, getName()));
    }

    @Override
    public InputStream getAssetReaderStream(String assetPath) throws MissingAsset {
        return bundle.getAssetReaderStream(assetPath);
    }

    @Override
    public IControllerManager getDefaultControllerManager() throws MissingController {
        if (defaultControllerName == null) {
            Bundle bAnnotation = bundle.getClass().getAnnotation(Bundle.class);
            if (bAnnotation == null)
                throw new MissingController("default controller not defined. bundle=" + getName());

            Class<?>[] defaultControllers = bAnnotation.defaultController();
            if (defaultControllers != null && defaultControllers.length > 0)
                defaultControllerName = BundleUtil.getControllerName(defaultControllers[0]);
            else
                throw new MissingController("not defined default controller");

        }
        try {
            return cacher.get(defaultControllerName);
        } catch (ExecutionException ex) {
            throw new MissingController(ex);
        }
    }

}
