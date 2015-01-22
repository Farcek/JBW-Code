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
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import mn.le.farcek.jbw.api.exception.MissingResource;
import mn.le.farcek.jbw.api.managers.IActionManager;
import mn.le.farcek.jbw.api.managers.IBundleManager;
import mn.le.farcek.jbw.api.managers.IControllerManager;
import mn.le.farcek.jbw.api.managers.IManager;
import mn.le.farcek.jbw.api.managers.IResourceManager;
import mn.le.farcek.jbw.api.utils.BundleUtil;

/**
 *
 * @author Farcek
 */
public class JBWImageResourceHandlerServlet extends HttpServlet {

    public JBWImageResourceHandlerServlet() {

    }

    @Inject
    IConfig config;

    @Inject
    IResourceManager resourceManager;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (ServletOutputStream out = response.getOutputStream()) {
            String resourcePath = request.getPathInfo();
            if (resourcePath == null) {
                throw new ServletException("path is null");
            }

            Pattern p = Pattern.compile("^/(\\d+)x(\\d+)-(.*)");

            Matcher m = p.matcher(resourcePath);
            if (m.matches()) {

                try {
                    int w = Integer.parseInt(m.group(1));
                    int h = Integer.parseInt(m.group(2));
                    String r = m.group(3);

                    File thumbnailFile = resourceManager.getThumbnailFile(r, w, h);

                    try (FileInputStream fis = new FileInputStream(thumbnailFile)) {

                        String extension = FFileUtils.getExtension(r);
                        if ("css".equals(extension)) {
                            response.setContentType("text/css");
                        } else if ("js".equals(extension)) {
                            response.setContentType("text/javascript");
                        } else if ("png".equals(extension)) {
                            response.setContentType("image/png");
                        } else if ("jpg".equals(extension)) {
                            response.setContentType("image/jpeg");
                        } else if ("gif".equals(extension)) {
                            response.setContentType("image/gif");
                        }

                        response.addHeader("Cache-Control", "public, max-age=90000");
                        response.setDateHeader("Expires", System.currentTimeMillis() + 5 * 24 * 60 * 60 * 1000);

                        ByteStreams.copy(fis, out);
                    }

                } catch (NumberFormatException e) {
                    throw new ServletException(e);
                } catch (MissingResource ex) {
                    throw new ServletException(ex);
                }
            } else {
                throw new ServletException("not define page");
            }

//            
//
//            if (rs.isFile()) {
//                try (FileInputStream fis = new FileInputStream(rs)) {
//                    if (resourcePath.endsWith(".css")) {
//                        response.setContentType("text/css");
//                    } else if (resourcePath.endsWith(".js")) {
//                        response.setContentType("text/javascript");
//                    } else if (resourcePath.endsWith(".png")) {
//                        response.setContentType("image/png");
//                    } else if (resourcePath.endsWith(".jpg")) {
//                        response.setContentType("image/jpeg");
//                    } else if (resourcePath.endsWith(".gif")) {
//                        response.setContentType("image/gif");
//                    }
//
//                    response.addHeader("Cache-Control", "public, max-age=90000");
//                    response.setDateHeader("Expires", System.currentTimeMillis() + 5 * 24 * 60 * 60 * 1000);
//
//                    ByteStreams.copy(fis, out);
//                }
//
//            } else {
//                throw new ServletException("not found resource. resourcePath = " + resourcePath);
//            }
        }
    }

}
