/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.le.farcek.jbw.code.template;

import com.google.inject.Inject;
import com.mitchellbosecke.pebble.error.LoaderException;
import com.mitchellbosecke.pebble.loader.FileLoader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import mn.le.farcek.jbw.api.exception.MissingAsset;
import mn.le.farcek.jbw.api.exception.MissingBundle;
import mn.le.farcek.jbw.api.managers.IBundleManager;
import mn.le.farcek.jbw.api.managers.IManager;
import mn.le.farcek.jbw.api.template.ITemplateLoader;

/**
 *
 * @author Farcek
 */
public class ITamplateLoaderImpl extends FileLoader implements ITemplateLoader {

    @Inject
    IManager manager;

    @Override
    public Reader getReader(String templateName) throws LoaderException {
        return getTemplate(templateName);
    }

    synchronized private Reader getTemplate(String templateName) throws LoaderException {
        if (templateName == null) {
            throw new LoaderException(null, "Can not load template. Template name is `null`");
        }

        String[] template = parseTemplateName(templateName);

        if (template == null) {
            throw new LoaderException(null, "Can not load template. Can not parse the `" + templateName + "` templateName. ");
        }

        String location = template[1].replaceAll("\\.", "/") + (getSuffix() == null ? "" : getSuffix());

        if (template[0] == null) {

            try {
                InputStream assetReaderStream = manager.getDefaultBundle().getAssetReaderStream(location);
                InputStreamReader isr = new InputStreamReader(assetReaderStream, getCharset());
                return new BufferedReader(isr);
            } catch (UnsupportedEncodingException | MissingBundle ex) {
                throw new LoaderException(ex, "Can not load template in default bundle. load request `" + templateName + "` templateName.");
            }
        } else {
            String bundleName = template[0];

            InputStream is;

            try {
                is = manager.getDefaultBundle().getAssetOverrideReader(bundleName + "/" + location);
            } catch (MissingAsset | MissingBundle ex) {

                IBundleManager bundle;

                try {
                    bundle = manager.getBundleManager(bundleName);
                } catch (MissingBundle e1) {
                    throw new LoaderException(e1, "Could not find template `" + templateName + "`. Could not find bundle `" + template[0] + "` ");
                }

                try {
                    is = bundle.getAssetReaderStream(location);

                } catch (MissingAsset e) {
                    throw new LoaderException(e, "Can not load template in `" + bundleName + "` bundle. load request `" + templateName + "` templateName. location=" + location);
                }
            }

            try {
                InputStreamReader isr = new InputStreamReader(is, getCharset());
                return new BufferedReader(isr);
            } catch (UnsupportedEncodingException ex) {
                throw new LoaderException(ex, "Can not load template in `" + bundleName + "` bundle. load request `" + templateName + "` templateName.");
            }

        }
    }

    String[] parseTemplateName(String templateName) {
        String[] s;
        if (templateName.startsWith("bundle://")) {
            templateName = templateName.substring(9);
            s = templateName.split("/", 2);

            if (s.length == 2) {
                return s;
            } else {
                return null;
            }

        } else {
            s = new String[]{null, templateName};
        }

        if (s[1].startsWith("/")) {
            s[1] = s[1].substring(1);
        }

        return s;
    }

}
