/*
 * Copyright (C) 2014 Farcek
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package mn.le.farcek.jbw.code.servlet;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteStreams;
import com.google.inject.Injector;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mn.le.farcek.common.utils.FFileUtils;
import mn.le.farcek.jbw.api.IConfig;
import mn.le.farcek.jbw.api.bundle.IDefaultBundle;
import mn.le.farcek.jbw.api.exception.CannotExecutAction;
import mn.le.farcek.jbw.api.exception.MissingAction;
import mn.le.farcek.jbw.api.exception.MissingBundle;
import mn.le.farcek.jbw.api.exception.MissingController;
import mn.le.farcek.jbw.api.managers.IActionManager;
import mn.le.farcek.jbw.api.managers.IBundleManager;
import mn.le.farcek.jbw.api.managers.IControllerManager;
import mn.le.farcek.jbw.api.managers.IManager;
import mn.le.farcek.jbw.api.utils.BundleUtil;

/**
 *
 * @author Farcek
 */
public class JBWAssetHandlerServlet extends HttpServlet {

    public JBWAssetHandlerServlet() {

    }

    @Inject
    IManager manager;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (ServletOutputStream out = response.getOutputStream()) {
            String resourcePath = request.getPathInfo();
            if (resourcePath == null)
                throw new ServletException("path is null");

            String[] split = resourcePath.split("/", 3);
            if (split.length == 3) {
                String bundleName = split[1];
                String assetPath = split[2];

                String ext = FFileUtils.getExtension(assetPath);
                if (ext != null)
                    if (ext.equals("css"))
                        response.setContentType("text/css");
                    else if (ext.equals("js"))
                        response.setContentType("text/javascript");
                    else if (ext.equals("png"))
                        response.setContentType("image/png");
                    else if (ext.equals("jpg"))
                        response.setContentType("image/jpeg");
                    else if (ext.equals("gif"))
                        response.setContentType("image/gif");

                InputStream rs;
                try {
                    rs = manager.getBundleManager(bundleName).getAssetReaderStream(assetPath);

                    if (rs != null) {
                        response.addHeader("Cache-Control", "public, max-age=90000");
                        response.setDateHeader("Expires", System.currentTimeMillis() + 5 * 24 * 60 * 60 * 1000);

                        ByteStreams.copy(rs, out);
                        
                    } else
                        throw new ServletException("not found asset. request path `" + assetPath + "` in the `" + bundleName + "` bundle.");

                } catch (MissingBundle ex) {
                    throw new ServletException("not found asset. the `" + bundleName + "` bundle is not found");
                }

            } else
                throw new ServletException("not found asset. requared formar: `/{bundle name}/{asset path}`; called path:" + resourcePath);

        }
    }

}
