/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.le.farcek.jbw.code.extension;

import com.google.common.base.Strings;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Filter;
import com.mitchellbosecke.pebble.extension.Function;
import com.mitchellbosecke.pebble.extension.LocaleAware;
import com.mitchellbosecke.pebble.extension.Test;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import mn.le.farcek.common.bean.BeanProperty;
import mn.le.farcek.common.bean.BeanPropertyFactory;
import mn.le.farcek.common.bean.NoSuchPropertyException;
import mn.le.farcek.common.utils.FCollectionUtils;
import mn.le.farcek.common.utils.FStringUtils;
import mn.le.farcek.jbw.api.IConfig;
import mn.le.farcek.jbw.api.action.view.IJsonParser;
import mn.le.farcek.jbw.api.managers.IUrlBuilder;

/**
 *
 * @author Farcek
 */
public class JBWExtension extends AbstractExtension {

    private final Injector injector;

    public JBWExtension(Injector injector) {
        this.injector = injector;
    }

    private IConfig config() {
        return injector.getInstance(IConfig.class);
    }

    @Override
    public Map<String, Filter> getFilters() {
        return new FCollectionUtils.HashMapBuilder<String, Filter>()
                .put("message", new MessageFilter())
                .put("fromInjector", new FilterFromInjector())
                .put("href", new FilterHref())
                .put("imageHref", new ImageHref())
                .put("assetHref", new FilterAssetHref())
                .put("invoke", new FilterInvoke())
                .put("property", new FilterProperty())
                .put("json", new FilterJson())
                .put("debug", new FilterDebug())
                .build();
    }

    @Override
    public Map<String, Function> getFunctions() {
        return new FCollectionUtils.HashMapBuilder<String, Function>()
                .put("args", new FunctionArgs())
                .build();
    }

    @Override
    public Map<String, Test> getTests() {
        StringMatchesTest stringMatchesTest = new StringMatchesTest();
        return new FCollectionUtils.HashMapBuilder<String, Test>()
                .put("stringMatches", stringMatchesTest)
                .put("regexTest", stringMatchesTest)
                .build();
    }

    public class StringMatchesTest implements Test {

        @Override
        public boolean apply(Object o, Map<String, Object> map) {
            if (o == null)
                return false;
            String regex = (String) map.get("regex");

            if (regex == null)
                return false;

            //System.out.println(String.format("%s.matches(%s) = %s", o, regex, o.toString().matches(regex)));
            return o.toString().matches(regex);
        }

        @Override
        public List<String> getArgumentNames() {
            return Arrays.asList("regex");
        }

    }

    class FunctionArgs implements Function {

        @Override
        public Object execute(Map<String, Object> args) {
            return args.values().toArray();
        }

        @Override
        public List<String> getArgumentNames() {
            return null;
        }

    }

    class FilterDebug implements Filter {

        @Override
        public Object apply(Object o, Map<String, Object> map) {
            if (config().isDebug()) {

                Object level = map.get("level");
                Level l = Level.INFO;
                if (level != null)
                    l = Level.parse(level.toString());

                Object label = map.get("label");
                StringBuilder sb = new StringBuilder();
                if (label != null && !label.toString().isEmpty())
                    sb.append(label).append(": ");

                sb.append(o);

                injector.getInstance(Logger.class).log(l, sb.toString());

            }

            return null;
        }

        @Override
        public List<String> getArgumentNames() {
            return Arrays.asList("label", "level");
        }

    }

    class FilterJson implements Filter {

        @Override
        public Object apply(Object o, Map<String, Object> map) {
            IJsonParser jsonParser = injector.getInstance(IJsonParser.class);

            try {
                return jsonParser.toJson(o);
            } catch (IOException ex) {
                if (config().isDebug())
                    ex.printStackTrace();
            }
            return "{}";
        }

        @Override
        public List<String> getArgumentNames() {
            return null;
        }

    }

    class FilterFromInjector implements Filter {

        @Override
        public Object apply(Object input, Map<String, Object> args) {
            if (input == null)
                return null;

            Class cls;
            try {
                cls = Class.forName(input.toString());
            } catch (ClassNotFoundException ex) {
                if (config().isDebug())
                    ex.printStackTrace();
                return null;
            }

            Object name = args.get("name");

            try {
                if (name == null)
                    return injector.getInstance(cls);
                else
                    return injector.getInstance(Key.get(cls, Names.named(name.toString())));
            } catch (Exception e) {
                if (config().isDebug())
                    e.printStackTrace();
                return null;
            }

        }

        @Override
        public List<String> getArgumentNames() {
            return Arrays.asList("name");
        }

    }

    class FilterProperty implements Filter {

        @Override
        public Object apply(Object input, Map<String, Object> args) {
            if (input == null)
                return null;
            String name = (String) args.get("name");
            if (name == null) return null;
            try {
                BeanProperty factoryProperty = BeanPropertyFactory.factoryProperty(input.getClass(), name);
                return factoryProperty.getValue(input);
            } catch (NoSuchPropertyException ex) {
                if (config().isDebug())
                    ex.printStackTrace();
                return null;
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                if (config().isDebug())
                    ex.printStackTrace();
                return null;
            }
        }

        @Override
        public List<String> getArgumentNames() {
            return Arrays.asList("name");
        }

    }

    class FilterInvoke implements Filter {

        @Override
        public Object apply(Object input, Map<String, Object> args) {
            if (input == null)
                return null;

            String method = (String) args.get("method");
            Object[] params = (Object[]) args.get("args");

            for (Method m : input.getClass().getMethods())
                if (m.getName().equals(method) && m.getParameterTypes().length == params.length)
                    try {
                        return m.invoke(input, params);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        ex.printStackTrace();
                        return null;
                    }
            return null;
        }

        @Override
        public List<String> getArgumentNames() {
            return Arrays.asList("method", "args");
        }

    }

    class FilterHref implements Filter {

        IUrlBuilder builder = injector.getInstance(IUrlBuilder.class);

        @Override
        public Object apply(Object o, Map<String, Object> args) {
            if (o == null)
                return "";
            String href;
            if (o instanceof String)
                href = (String) o;
            else
                href = o.toString();
            IUrlBuilder.Mode mode = "full".equals(args.get("mode")) ? IUrlBuilder.Mode.FULL : IUrlBuilder.Mode.SHORT;
            return builder.buildBundle(href, mode);
        }

        @Override
        public List<String> getArgumentNames() {
            return Arrays.asList("mode");
        }

    }

    class FilterAssetHref implements Filter {

        IUrlBuilder builder = injector.getInstance(IUrlBuilder.class);

        @Override
        public Object apply(Object o, Map<String, Object> args) {
            if (o == null)
                return "";
            String href;
            if (o instanceof String)
                href = (String) o;
            else
                href = o.toString();

            Object asset = args.get("asset");
            if (asset == null) return "#not found asset arg";
            IUrlBuilder.Mode mode = "full".equals(args.get("mode")) ? IUrlBuilder.Mode.FULL : IUrlBuilder.Mode.SHORT;

            return builder.buildAsset(asset.toString(), href, mode);
        }

        @Override
        public List<String> getArgumentNames() {
            return Arrays.asList("asset", "mode");
        }

    }

    class ImageHref implements Filter {

        IUrlBuilder builder = injector.getInstance(IUrlBuilder.class);

        @Override
        public Object apply(Object o, Map<String, Object> args) {
            if (o == null)
                return "";
            String href;
            if (o instanceof String)
                href = (String) o;
            else
                href = o.toString();

            Object w = args.get("w");
            Object h = args.get("h");
            IUrlBuilder.Mode mode = "full".equals(args.get("mode")) ? IUrlBuilder.Mode.FULL : IUrlBuilder.Mode.SHORT;
            if (w instanceof Number && h instanceof Number)
                return builder.buildResourceImage(href, mode, ((Number) w).intValue(), ((Number) h).intValue());

            return builder.buildResource(href, mode);
        }

        @Override
        public List<String> getArgumentNames() {
            return Arrays.asList("mode", "w", "h");
        }

    }

    public class MessageFilter implements Filter, LocaleAware {

        @Override
        public Object apply(Object input, Map<String, Object> args) {

            if (input == null)
                return "";

            Locale localeObject;
            Object locale = args.get("locale");
            if (locale instanceof Locale)
                localeObject = (Locale) locale;
            else if (locale instanceof String)
                localeObject = new Locale((String) locale);
            else
                localeObject = this.locale;

            String s;
            if (input instanceof String)
                s = (String) input;
            else if (input instanceof Enum)
                s = input.getClass().getName() + "." + ((Enum) input).name();
            else
                s = input.toString();

            try {
                ResourceBundle bundle = ResourceBundle.getBundle("messages", localeObject);

                return bundle.getString(s);
            } catch (MissingResourceException e) {
                return "@" + s;
            }
        }

        @Override
        public List<String> getArgumentNames() {
            return Arrays.asList("locale");
        }

        Locale locale;

        @Override
        public void setLocale(Locale locale) {
            this.locale = locale;
        }
    }
}
