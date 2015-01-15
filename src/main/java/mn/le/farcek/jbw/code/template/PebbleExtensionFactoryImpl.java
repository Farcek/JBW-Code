/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.le.farcek.jbw.code.template;

import com.google.inject.Injector;
import com.mitchellbosecke.pebble.extension.Extension;
import java.util.ArrayList;
import java.util.List;
import com.google.inject.Inject;
import mn.le.farcek.jbw.api.IBundle;
import mn.le.farcek.jbw.api.IConfig;
import mn.le.farcek.jbw.api.bundle.BundlePebbleExtension;

import mn.le.farcek.jbw.api.bundle.PebbleExtensionFactory;

public class PebbleExtensionFactoryImpl implements PebbleExtensionFactory {

    @Inject
    IConfig config;

    @Inject
    Injector injector;

    @Override
    public List<Extension> getExtensions() {
        List<Extension> list = new ArrayList<>();
        List<Class<? extends IBundle>> bundles = config.getBundles();
        for (Class<? extends IBundle> bundle : bundles) {
            BundlePebbleExtension ann = bundle.getAnnotation(BundlePebbleExtension.class);

            if (ann != null) {
                try {
                    Extension ex = ann.value().newInstance();
                    injector.injectMembers(ex);
                    list.add(ex);
                } catch (InstantiationException | IllegalAccessException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return list;
    }

}
