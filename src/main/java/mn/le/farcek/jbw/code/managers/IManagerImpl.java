/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.le.farcek.jbw.code.managers;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import mn.le.farcek.jbw.api.Bundle;
import mn.le.farcek.jbw.api.IConfig;
import mn.le.farcek.jbw.api.IBundle;
import mn.le.farcek.jbw.api.bundle.IDefaultBundle;
import mn.le.farcek.jbw.api.exception.MissingBundle;
import mn.le.farcek.jbw.api.managers.IBundleManager;

import mn.le.farcek.jbw.api.managers.IManager;
import mn.le.farcek.jbw.api.utils.BundleUtil;

@Singleton
public class IManagerImpl implements IManager {

    public IManagerImpl() {
        System.out.println("create IManagerImpl IManagerImpl" + this);
    }

    @Inject
    private IConfig config;

    @Inject
    Injector injector;

    private Map<String, IBundleManager> bundles;

    @Override
    public IBundleManager getBundleManager(String name1) throws MissingBundle {

        if (bundles == null) {
            bundles = new HashMap<>();
            Logger log = injector.getInstance(Logger.class);
            for (Class<? extends IBundle> cls : config.getBundles())
                if (cls != null) {
                    Bundle bAnnotation = cls.getAnnotation(Bundle.class);
                    if (bAnnotation != null) {
                        String bName = BundleUtil.getBundleName(cls);
                        try {
                            IBundle bungleObject = cls.newInstance();
                            injector.injectMembers(bungleObject);

                            IBundleManager bundleM = new IBundleManagerImpl(injector, bungleObject, bName);
                            //injector.injectMembers(bundleM);

                            IBundleManager put = bundles.put(bName, bundleM);
                            if (put != null)
                                log.warning(String.format("`%s` the bundle daplicated Bundle. `%s` vs `%s`", bName, put.getBundle(), bundleM));

                            log.info(String.format("`%s` -> `%s`", bName, bungleObject.getClass()));
                        } catch (InstantiationException | IllegalAccessException ex) {
                            log.log(Level.WARNING, String.format("egnore the `%s`(%s). ", bName, cls), ex);
                        }
                    } else
                        log.warning(String.format("egnore the class `%s` not defined @Bundle annotation", cls));
                }

            System.out.println("***********************************************");
        }
        IBundleManager bundle = bundles.get(name1);
        if (bundle instanceof IBundleManager)
            return bundle;

        

        throw new MissingBundle(String.format("`%s` bundle is not fount", name1));
    }

    private IDefaultBundle defaultBundle;

    @Override
    public IDefaultBundle getDefaultBundle() throws MissingBundle {
        if (defaultBundle == null) {
            Class<? extends IDefaultBundle> cls = config.getDefaultBundle();
            String bundleName = BundleUtil.getBundleName(cls);

            IBundle bundle = getBundleManager(bundleName).getBundle();
            if (bundle instanceof IDefaultBundle)
                defaultBundle = (IDefaultBundle) bundle;
            else
                throw new MissingBundle(String.format("`%s`(%s) bundle ni default bundle bish bn", bundleName, cls));
        }

        return defaultBundle;
    }

//    public String buildUrl(String bundleName,String controllerName,String actionName, String extension){
//        try {
//            IActionManager actionManager = getBundleManager(bundleName).getControllerManager(controllerName).getActionManager(actionName);
//            
//            
//        } catch (MissingBundle ex) {
//            Logger.getLogger(IManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (MissingController ex) {
//            Logger.getLogger(IManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (MissingAction ex) {
//            Logger.getLogger(IManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
}
