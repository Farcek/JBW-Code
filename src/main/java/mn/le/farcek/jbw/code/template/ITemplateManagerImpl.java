/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.le.farcek.jbw.code.template;

import com.google.inject.Injector;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.extension.Extension;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mitchellbosecke.pebble.extension.debug.DebugExtension;
import mn.le.farcek.jbw.api.IConfig;
import mn.le.farcek.jbw.api.bundle.PebbleExtensionFactory;
import mn.le.farcek.jbw.api.managers.ITemplateManager;
import mn.le.farcek.jbw.api.template.ITemplate;
import mn.le.farcek.jbw.api.template.ITemplateLoader;
import mn.le.farcek.jbw.code.extension.JBWExtension;

@Singleton
public class ITemplateManagerImpl implements ITemplateManager {

    @Inject
    IConfig conf;

    @Inject
    ITemplateLoader templateLoader;

    @Inject
    PebbleExtensionFactory extensionFactory;

    @Inject
    Injector injector;

    private PebbleEngine engine;

    private PebbleEngine getEngine() {
        if (engine == null) {
            templateLoader.setSuffix(".html");
            engine = new PebbleEngine(templateLoader);
            engine.setDefaultLocale(conf.getDefaultLocale());
            if (conf.isDebug())
                engine.setTemplateCache(null);

            engine.addExtension(new JBWExtension(injector));

            for (Extension ex : extensionFactory.getExtensions())
                engine.addExtension(ex);
        }
        return engine;
    }

    @Override
    public ITemplate factoryRenderer(String template) {
        return new Template(template);
    }

    private class Template implements ITemplate {

        private final String name;

        public Template(String name) {
            this.name = name;
        }

        @Override
        public void render(Writer writer, Map<String, Object> params, Locale locale) throws IOException {

            try {
                Map<String, Object> attrs = new HashMap<>();
                if (params != null)
                    attrs.putAll(params);

                //System.out.println("request=="+attrs);
                PebbleTemplate t = getEngine().getTemplate(name);
                t.evaluate(writer, attrs, locale);

            } catch (PebbleException ex) {
                if (conf.isDebug()) {
                    writer.write(ex.getMessage());
                    ex.printStackTrace();
                }
            }

        }

        @Override
        public String toString() {
            return name;
        }
    }

}
