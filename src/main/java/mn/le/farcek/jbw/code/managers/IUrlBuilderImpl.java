/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.le.farcek.jbw.code.managers;

import com.google.inject.Inject;
import mn.le.farcek.jbw.api.IConfig;
import mn.le.farcek.jbw.api.managers.IManager;
import mn.le.farcek.jbw.api.managers.IUrlBuilder;

public class IUrlBuilderImpl implements IUrlBuilder {

    @Inject
    IConfig config;

    @Override
    public String buildBundle(String href, Mode mode) {
        href = href.trim();

        if (href.startsWith("http://") || href.startsWith("https://"))
            return href;

        if (href.startsWith("/")) {
            String url = config.getContextPath() + href;
            return url(url, mode);
        }

        String[] paths = href.split("/");
        if (paths.length == 3) {
            String url = config.getContextPath() + "/" + href;
            return url(url, mode);

        }

        return href;
    }

    private String url(String url, Mode mode) {
        return mode == Mode.FULL ? config.getWebDomain() + url : url;
    }

    @Override
    public String buildResource(String name, Mode mode) {
        String url = config.getContextPath() + config.getPathOfResourceHandler() + "/" + name;
        return url(url, mode);
    }

    @Override
    public String buildResourceImage(String name, Mode mode) {
        return buildResource(name, mode);
    }

    @Override
    public String buildResourceImage(String name, Mode mode, int w, int h) {
        String url = config.getContextPath() + config.getPathOfImageResourceHandler() + "/" + w + "x" + h + "-" + name;
        return url(url, mode);
    }

    @Override
    public String buildAsset(String asset, String path, Mode mode) {
        String url = config.getContextPath() + config.getPathOfAssetHandler() + "/" + asset + (path.startsWith("/") ? path : "/" + path);
        return url(url, mode);
    }

}
