/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.le.farcek.jbw.code;

import com.google.inject.Injector;
import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.MultipartConfigElement;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import mn.le.farcek.jbw.api.IConfig;
import mn.le.farcek.jbw.code.servlet.JBWAssetHandlerServlet;
import mn.le.farcek.jbw.code.servlet.JBWBundleHandlerServlet;
import mn.le.farcek.jbw.code.servlet.JBWImageResourceHandlerServlet;
import mn.le.farcek.jbw.code.servlet.JBWResourceHandlerServlet;

/**
 *
 * @author Farcek
 */
public abstract class JBWBootstartup implements ServletContextListener {

    protected abstract JBWInjector getJBWInjector(ServletContext servletContext);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        final ServletContext servletContext = sce.getServletContext();
        final Injector injector = getJBWInjector(servletContext).getInjector();
        IConfig config = injector.getInstance(IConfig.class);

        // bundle handler
        JBWBundleHandlerServlet srvBundle = new JBWBundleHandlerServlet();
        injector.injectMembers(srvBundle);

        ServletRegistration.Dynamic bh = servletContext.addServlet("__bh", srvBundle);
        bh.addMapping("*.html", "*.json", "*.xml", "*.txt");
        bh.setMultipartConfig(new MultipartConfigElement("c:/t"));

        // resource handler
        JBWResourceHandlerServlet srvResource = new JBWResourceHandlerServlet();
        injector.injectMembers(srvResource);

        ServletRegistration.Dynamic rh = servletContext.addServlet("__rh", srvResource);
        rh.addMapping(config.getPathOfResourceHandler() + "/*");
        // resource image handler
        JBWImageResourceHandlerServlet srvImage = new JBWImageResourceHandlerServlet();
        injector.injectMembers(srvImage);

        ServletRegistration.Dynamic irh = servletContext.addServlet("__irh", srvImage);
        irh.addMapping(config.getPathOfImageResourceHandler() + "/*");
        // asset handler
        
        JBWAssetHandlerServlet srvAsset = new JBWAssetHandlerServlet();
        injector.injectMembers(srvAsset);
        ServletRegistration.Dynamic ah = servletContext.addServlet("__ah", srvAsset);
        ah.addMapping(config.getPathOfAssetHandler()+ "/*");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
