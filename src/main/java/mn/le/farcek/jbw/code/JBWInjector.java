/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.le.farcek.jbw.code;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import java.util.ArrayList;
import java.util.List;

import mn.le.farcek.jbw.api.IBundle;
import mn.le.farcek.jbw.api.IConfig;
import mn.le.farcek.jbw.api.IValidator;
import mn.le.farcek.jbw.api.action.view.IJsonParser;
import mn.le.farcek.jbw.api.bundle.BundleInjector;
import mn.le.farcek.jbw.api.bundle.PebbleExtensionFactory;

import mn.le.farcek.jbw.api.managers.IManager;
import mn.le.farcek.jbw.api.managers.IResourceManager;
import mn.le.farcek.jbw.api.managers.ITemplateManager;
import mn.le.farcek.jbw.api.managers.IUrlBuilder;
import mn.le.farcek.jbw.api.validation.ValidationManager;
import mn.le.farcek.jbw.api.security.ISecurityManager;
import mn.le.farcek.jbw.api.template.ITemplateLoader;

import mn.le.farcek.jbw.code.managers.IManagerImpl;
import mn.le.farcek.jbw.code.managers.ISecurityManagerImpl;
import mn.le.farcek.jbw.code.template.ITamplateLoaderImpl;
import mn.le.farcek.jbw.code.template.ITemplateManagerImpl;
import mn.le.farcek.jbw.code.template.PebbleExtensionFactoryImpl;
import mn.le.farcek.jbw.code.action.view.IJsonParserImpl;
import mn.le.farcek.jbw.code.managers.IResourceManagerImpl;
import mn.le.farcek.jbw.code.managers.IUrlBuilderImpl;
import mn.le.farcek.jbw.code.validation.IValidatorImpl;
import mn.le.farcek.jbw.code.validation.IValidationManagerImpl;

/**
 *
 * @author Farcek
 */
public abstract class JBWInjector {

    private Injector injector;

    public Injector getInjector() {
        if (injector == null) {
            List<Module> injectModules = new ArrayList<>();
            injectModules.add(new Config());
            setupInjectModules(injectModules);
            injectModules.add(getUserInjectModule());

            injector = Guice.createInjector(injectModules);
        }
        return injector;
    }

    protected abstract Module getUserInjectModule();

    protected abstract IConfig getConfig();

    private class Config extends AbstractModule {

        @Override
        protected void configure() {
            bind(IConfig.class).toInstance(getConfig());
            bind(ITemplateManager.class).to(ITemplateManagerImpl.class);
            bind(ISecurityManager.class).to(ISecurityManagerImpl.class);
            bind(IResourceManager.class).to(IResourceManagerImpl.class);
            bind(IUrlBuilder.class).to(IUrlBuilderImpl.class);
            bind(ITemplateLoader.class).to(ITamplateLoaderImpl.class);
            bind(IManager.class).to(IManagerImpl.class);
            bind(IJsonParser.class).to(IJsonParserImpl.class);
            bind(PebbleExtensionFactory.class).to(PebbleExtensionFactoryImpl.class);
            bind(IValidator.class).to(IValidatorImpl.class);
            bind(ValidationManager.class).to(IValidationManagerImpl.class);
        }
    }

    private void setupInjectModules(List<Module> injectModules) {

        List<Class<? extends IBundle>> bundles = getConfig().getBundles();
        for (Class<? extends IBundle> bundle : bundles) {
            BundleInjector ann = bundle.getAnnotation(BundleInjector.class);

            if (ann != null) {
                try {
                    Module newInstance = ann.value().newInstance();
                    injectModules.add(newInstance);
                } catch (InstantiationException | IllegalAccessException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

}
