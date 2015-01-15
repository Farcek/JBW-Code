/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.le.farcek.jbw.code.servlet;

import com.google.inject.Injector;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import mn.le.farcek.common.utils.FCollectionUtils;
import mn.le.farcek.http.request.HttpRequest;
import mn.le.farcek.http.request.HttpRequestBuilder;
import mn.le.farcek.jbw.api.IConfig;
import mn.le.farcek.jbw.api.action.IActionParamFactory;
import mn.le.farcek.jbw.api.action.IActionRequest;
import mn.le.farcek.jbw.api.action.IActionRequestPart;
import mn.le.farcek.jbw.api.action.IActionSession;
import mn.le.farcek.jbw.api.action.IRequestHeader;
import mn.le.farcek.jbw.api.action.IResponseHeader;
import mn.le.farcek.jbw.api.action.IActionSecurity;
import mn.le.farcek.jbw.api.action.view.IJsonParser;
import mn.le.farcek.jbw.api.security.ISecurityManager;
import mn.le.farcek.jbw.code.managers.IActionSecurityImpl;

public class IActionParamFactoryImpl implements IActionParamFactory {

    private final Injector injector;
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final String reqExtension;

    public IActionParamFactoryImpl(Injector injector, HttpServletRequest request, HttpServletResponse response, String reqExtension) {
        this.injector = injector;
        this.request = request;
        this.response = response;
        this.reqExtension = reqExtension;
    }

    IActionRequest actionRequest;

    @Override
    public IActionRequest factoryActionRequest() {
        if (actionRequest == null)
            actionRequest = new ServletActionRequest(request, injector.getInstance(IConfig.class), reqExtension);
        return actionRequest;
    }

    IActionSession actionSession;

    @Override
    public IActionSession factoryActionSession() {
        if (actionSession == null)
            actionSession = new IActionSessionImpl(request.getSession());
        return actionSession;
    }

    IRequestHeader requestHeader;

    @Override
    public IRequestHeader factoryRequestHeader() {
        if (requestHeader == null)
            requestHeader = new IRequestHeaderImpl(request);
        return requestHeader;
    }

    IResponseHeader responseHeader;

    @Override
    public IResponseHeader factoryResponseHeader() {
        if (responseHeader == null)
            responseHeader = new IResponseHeaderImpl(response);
        return responseHeader;
    }

    IActionSecurity actionSecurity;

    @Override
    public IActionSecurity factorySecurity() {
        if (actionSecurity == null)
            actionSecurity = new IActionSecurityImpl(injector.getInstance(ISecurityManager.class), factoryActionSession());
        return actionSecurity;
    }

    @Override
    public IActionRequestPart factoryActionRequestPart(String partName) {
        return factoryActionRequest().getPart(partName);
    }

    HttpRequest httpRequest;

    @Override
    public HttpRequest factoryHttpRequest() {
        if (httpRequest == null) {
            HashMap<String, String> map = new HashMap<>();
            for (Map.Entry<String, String[]> entrySet : request.getParameterMap().entrySet()) {
                String key = entrySet.getKey();
                String[] values = entrySet.getValue();

                String value = null;
                if (values != null && values.length > 0)
                    value = values[0];

                map.put(key, value);

            }
            httpRequest = HttpRequestBuilder.build(map);
        }
        return httpRequest;
    }

    @Override
    public <T> T factoryObjectFromJsonRequest(Class<T> cls) {
        IJsonParser jp = injector.getInstance(IJsonParser.class);
        try {
            return jp.fromJson(request.getReader(), cls);
        } catch (IOException ex) {
            if (injector.getInstance(IConfig.class).isDebug())
                injector.getInstance(Logger.class).log(Level.SEVERE, "cannot json parse", ex);
            return null;
        }
    }

}
