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
import com.google.inject.Injector;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.inject.Inject;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mn.le.farcek.common.utils.FClassUtils;
import mn.le.farcek.jbw.api.IConfig;
import mn.le.farcek.jbw.api.bundle.IDefaultBundle;
import mn.le.farcek.jbw.api.exception.CannotExecutAction;
import mn.le.farcek.jbw.api.exception.JBWPermissionsDenied;
import mn.le.farcek.jbw.api.exception.JBWSecurityExeption;
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
public class JBWBundleHandlerServlet extends HttpServlet {

    public JBWBundleHandlerServlet() {

    }

    @Inject
    IManager manager;

    @Inject
    Injector injector;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setCharacterEncoding("utf-8");
        PrintWriter writer = response.getWriter();

        String pt = request.getServletPath();

        List<String> paths = Splitter.on('/').omitEmptyStrings().trimResults().splitToList(pt);

        String bName, cName, aName, extension;
        int n = paths.size();

        if (n == 1) {
            bName = null;
            cName = null;
            String a[] = paths.get(0).split("\\.");
            aName = a[0];
            extension = a[1];

        } else if (n == 2) {
            bName = paths.get(0);
            cName = null;
            String a[] = paths.get(1).split("\\.");
            aName = a[0];
            extension = a[1];
        } else if (n == 3) {
            bName = paths.get(0);
            cName = paths.get(1);
            String a[] = paths.get(2).split("\\.");
            aName = a[0];
            extension = a[1];
        } else
            throw new ServletException("ca not parse path; path=" + pt);

        try {
            IBundleManager mBundle;
            if (bName == null) {
                IDefaultBundle defaultBundle = manager.getDefaultBundle();
                mBundle = manager.getBundleManager(BundleUtil.getBundleName(defaultBundle.getClass()));
            } else
                mBundle = manager.getBundleManager(bName);

            IControllerManager controllerManager;
            if (cName == null)
                controllerManager = mBundle.getDefaultControllerManager();
            else
                controllerManager = mBundle.getControllerManager(cName);

            IActionManager actionManager = controllerManager.getActionManager(aName);
//            IActionRequest servletActionRequest = new ServletActionRequest(request, config, prefix);
//            IRequestHeader requestHeader = new IRequestHeaderImpl(request);
//            IResponseHeader responseHeader = new IResponseHeaderImpl(response);
            actionManager.executAction(writer, new IActionParamFactoryImpl(injector, request, response, extension));

        } catch (MissingBundle | MissingController | MissingAction | CannotExecutAction ex) {
            IConfig conf = injector.getInstance(IConfig.class);
            if (conf.isDebug())
                ex.printStackTrace();
            if ("json".equals(extension)) {

                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(FClassUtils.getAllMessage(ex));
            } else
                throw new ServletException(ex);
        } catch (JBWSecurityExeption | JBWPermissionsDenied ex) {
            IConfig conf = injector.getInstance(IConfig.class);
            if (conf.isDebug())
                ex.printStackTrace();
            if ("json".equals(extension)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(FClassUtils.getAllMessage(ex));
            } else {

                String url = URLEncoder.encode(getFullURL(request), "utf8");
                String msg = URLEncoder.encode(ex.getMessage(), "utf8");
                response.sendRedirect(conf.getContextPath() + conf.getLoginPage() + "?return=" + url + "&msg=" + msg);

            }

        } catch (Exception ex) {
            IConfig conf = injector.getInstance(IConfig.class);
            if (conf.isDebug())
                ex.printStackTrace();
            if ("json".equals(extension)) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write(FClassUtils.getAllMessage(ex));
            } else
                throw new ServletException(ex);

        }
    }

    public static String getFullURL(HttpServletRequest request) {
        StringBuffer requestURL = request.getRequestURL();
        String queryString = request.getQueryString();

        if (queryString == null)
            return requestURL.toString();
        else
            return requestURL.append('?').append(queryString).toString();
    }

    class UrlParser {

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
