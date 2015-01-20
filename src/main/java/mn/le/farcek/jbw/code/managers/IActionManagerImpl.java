/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.le.farcek.jbw.code.managers;

import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import java.util.Map;
import java.util.logging.Level;

import java.util.logging.Logger;
import javax.inject.Named;
import mn.le.farcek.common.utils.FClassUtils;

import mn.le.farcek.common.utils.FStringUtils;
import mn.le.farcek.http.request.HttpRequest;
import mn.le.farcek.jbw.api.Action;
import mn.le.farcek.jbw.api.IConfig;
import mn.le.farcek.jbw.api.action.IActionMethod;
import mn.le.farcek.jbw.api.action.IActionParamFactory;
import mn.le.farcek.jbw.api.action.IActionRequest;
import mn.le.farcek.jbw.api.action.IActionRequestPart;
import mn.le.farcek.jbw.api.action.IActionSecurity;
import mn.le.farcek.jbw.api.action.IActionSession;
import mn.le.farcek.jbw.api.action.IRequestHeader;
import mn.le.farcek.jbw.api.action.IResponseHeader;
import mn.le.farcek.jbw.api.action.JsonPostRequest;
import mn.le.farcek.jbw.api.action.SessionVar;
import mn.le.farcek.jbw.api.action.view.HtmlView;
import mn.le.farcek.jbw.api.action.view.IJsonParser;
import mn.le.farcek.jbw.api.action.view.JsonView;
import mn.le.farcek.jbw.api.action.view.RedirectView;
import mn.le.farcek.jbw.api.exception.BadRequestException;
import mn.le.farcek.jbw.api.exception.CannotExecutAction;
import mn.le.farcek.jbw.api.exception.JBWPermissionsDenied;
import mn.le.farcek.jbw.api.exception.JBWSecurityExeption;
import mn.le.farcek.jbw.api.managers.IActionManager;
import mn.le.farcek.jbw.api.managers.IBundleManager;
import mn.le.farcek.jbw.api.managers.ITemplateManager;
import mn.le.farcek.jbw.api.security.ISecurityManager;
import mn.le.farcek.jbw.api.security.IUser;
import mn.le.farcek.jbw.api.template.ITemplate;

public class IActionManagerImpl implements IActionManager {

    private final Object controllerObject;
    private final Method method;
    private final String name;
    private final IControllerManagerImpl controllerManager;
    private final Injector injector;

    public IActionManagerImpl(Object controllerObject, Method method, String name, IControllerManagerImpl controllerManager, Injector injector) {
        this.controllerObject = controllerObject;
        this.method = method;
        this.name = name;
        this.controllerManager = controllerManager;
        this.injector = injector;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public IControllerManagerImpl getControllerManager() {
        return controllerManager;
    }

    @Override
    public void executAction(Writer writer, IActionParamFactory actionParamFactory)
            throws CannotExecutAction, IOException {

        Logger logger = injector.getInstance(Logger.class);

        logger.info(String.format("calling action. contoller = %s; method=%s", controllerObject, method));

        if (actionParamFactory == null)
            throw new NullPointerException();

        Action aAnnt = method.getAnnotation(Action.class);
        if (aAnnt == null)
            throw new NullPointerException("`Action` annotation not defined the method " + method);

        boolean checkMember = aAnnt.member();
        String role = aAnnt.role();

        if (FStringUtils.notEmptyOrNull(role))
            checkMember = true;

        if (checkMember) {
            IActionSecurity actionSecurity = actionParamFactory.factorySecurity();
            IUser currentUser = actionSecurity.getCurrentUser();
            if (currentUser == null)
                throw new JBWSecurityExeption("login requared");

            if (FStringUtils.notEmptyOrNull(role))
                if (actionSecurity.hasAccess(role) == false)
                    throw new JBWPermissionsDenied(String.format("requared role `%s`", role));
        }
        Object actionResult;
        try {
            Factorys factorys = new Factorys(actionParamFactory);
            actionResult = method.invoke(controllerObject, factorys.factoryArguments());
        } catch (BadRequestException ex) {
            throw new CannotExecutAction(ex);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {

            throw new CannotExecutAction(ex);
        }

        // 
        if (actionResult instanceof RedirectView) {
            RedirectView redirectView = (RedirectView) actionResult;
            actionParamFactory.factoryResponseHeader().setStatus(((RedirectView) actionResult).getStatusCode());
            actionParamFactory.factoryResponseHeader().setHeader("Location", redirectView.getUrl());
            return;
        }

        // json
        if (actionResult instanceof JsonView) {
            JsonView jv = (JsonView) actionResult;
            new JsonResp(jv.getData(), jv.getStatusCode(), actionParamFactory).render(writer);
            return;
        }

        // html
        if (actionResult instanceof HtmlView) {
            HtmlView htmlView = (HtmlView) actionResult;
            String viewName = getViewName(htmlView.getName());
            new HtmlResp(viewName, htmlView.getStatusCode(), htmlView.getParams(), actionParamFactory).render(writer);
            return;
        }

        String resultType = aAnnt.result();

        // result is null
        if ("json".equals(resultType))
            new JsonResp(actionResult, 200, actionParamFactory).render(writer);
        else if ("html".equals(resultType)) {
            String viewName = getViewName(aAnnt.view());

            new HtmlResp(viewName, 200, actionResult, actionParamFactory).render(writer);
        } else
            throw new CannotExecutAction("not supported action result.the result=" + resultType);
    }

    private String getViewName(String viewName) {
        IBundleManager bundleManager = controllerManager.getBundleManager();
        if (FStringUtils.isEmpty(viewName))
            return String.format("bundle://%s/%s/%s", bundleManager.getName(), controllerManager.getName(), getName());

        if (viewName.startsWith("//")) {
            String v = viewName.substring(2);
            if (v.isEmpty())
                v = getName();
            return String.format("bundle://%s/%s/%s", bundleManager.getName(), controllerManager.getName(), v);
        }
        if (viewName.startsWith("/")) {

            String v = viewName.substring(1);
            if (v.isEmpty())
                v = getName();
            return String.format("bundle://%s/%s", bundleManager.getName(), v);
        }
        if (viewName.startsWith("bundle://"))
            return viewName;

        return String.format("bundle://%s/%s/%s", bundleManager.getName(), controllerManager.getName(), viewName);
    }

    private class HtmlResp {

        final int status;
        final String view;
        final Object param;
        final IActionParamFactory actionParamFactory;

        public HtmlResp(String view, int status, Object param, IActionParamFactory actionParamFactory) {
            this.view = view;
            this.status = status;
            this.param = param;
            this.actionParamFactory = actionParamFactory;
        }

        void render(Writer writer) throws IOException {

            //actionParamFactory.factoryResponseHeader().setCharacterEncoding("utf-8");
            actionParamFactory.factoryResponseHeader().setStatus(200);
            ITemplateManager templateManager = injector.getInstance(ITemplateManager.class);

            ITemplate template = templateManager.factoryRenderer(view);

            HashMap<String, Object> attrs = new HashMap<>();
            if (param instanceof Map)
                attrs.putAll((Map<? extends String, ? extends Object>) param);
            else if (param != null)
                attrs.put("result", param);

            attrs.put("request", actionParamFactory.factoryActionRequest());
            attrs.put("session", actionParamFactory.factoryActionSession());
            attrs.put("__config", injector.getInstance(IConfig.class));
            
            template.render(writer, attrs, actionParamFactory.factoryActionRequest().getLocale());
        }

    }

    private class JsonResp {

        final int status;
        final Object data;
        final IActionParamFactory actionParamFactory;

        public JsonResp(Object data, int status, IActionParamFactory actionParamFactory) {
            this.data = data;
            this.status = status;
            this.actionParamFactory = actionParamFactory;
        }

        void render(Writer writer) throws IOException {
            actionParamFactory.factoryResponseHeader().setContentType("application/json");
            actionParamFactory.factoryResponseHeader().setStatus(status);
            String jsonString = injector.getInstance(IJsonParser.class).toJson(data);
            writer.write(jsonString);
        }

    }

    private class Factorys {

        private final IActionParamFactory actionParamFactory;

        public Factorys(IActionParamFactory actionParamFactory) {
            this.actionParamFactory = actionParamFactory;
        }

        private Object[] factoryArguments() {
            Class<?>[] parameterTypes = method.getParameterTypes();
            int n = parameterTypes.length;

            Object[] params = new Object[n];

            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            for (int i = 0; i < n; i++)
                params[i] = factoryArgument(parameterTypes[i], parameterAnnotations[i]);
            return params;
        }

        private Object factoryArgument(Class<?> parameterType, Annotation[] parameterAnnotations) {
            IActionRequest request = actionParamFactory.factoryActionRequest();
            IActionSession session = actionParamFactory.factoryActionSession();
            if (parameterType.equals(IActionRequest.class))
                return request;
            else if (parameterType.equals(IRequestHeader.class))
                return actionParamFactory.factoryRequestHeader();
            else if (parameterType.equals(IResponseHeader.class))
                return actionParamFactory.factoryResponseHeader();
            else if (parameterType.equals(IActionSession.class))
                return session;
            else if (parameterType.equals(IActionSecurity.class))
                return actionParamFactory.factorySecurity();
            else if (parameterType.equals(IActionMethod.class))
                return request.getMethod();
            else if (parameterType.equals(HttpRequest.class))
                return actionParamFactory.factoryHttpRequest();
            //IUser
            else if (FClassUtils.instanceOf(IUser.class, parameterType))
                return actionParamFactory.factorySecurity().getCurrentUser();
            else if (parameterType.equals(IActionRequestPart.class)) {
                String partName = null;
                for (Annotation ann : parameterAnnotations)
                    if (Named.class.equals(ann.annotationType()))
                        partName = ((Named) ann).value();

                return actionParamFactory.factoryActionRequestPart(partName);

            } else {
                for (Annotation ann : parameterAnnotations) {
                    if (SessionVar.class.equals(ann.annotationType())) {
                        SessionVar an = (SessionVar) ann;
                        String n = an.value();
                        if (FStringUtils.isEmpty(n)) {
                            Named nAnn = parameterType.getAnnotation(Named.class);
                            if (nAnn != null)
                                n = nAnn.value();
                        }
                        if (FStringUtils.isEmpty(n))
                            n = parameterType.getName();

                        IActionSession s = actionParamFactory.factoryActionSession();
                        Object o = s.getAttribute(n);
                        if (parameterType.isInstance(o))
                            return o;

                        Object p;
                        try {
                            p = parameterType.newInstance();
                        } catch (InstantiationException | IllegalAccessException ex) {
                            ex.printStackTrace();
                            return null;
                        }

                        s.setAttribute(n, p);

                        return p;

                    }
                    if (JsonPostRequest.class.equals(ann.annotationType())) {
                        if (request.getMethod() == IActionMethod.Post)
                            return actionParamFactory.factoryObjectFromJsonRequest(parameterType);
                        return null;
                    }
                }

                try {
                    return injector.getInstance(parameterType);
                } catch (ConfigurationException | ProvisionException e) {
                    IConfig config = injector.getInstance(IConfig.class);
                    if (config.isDebug())
                        e.printStackTrace();
                    return null;
                }
            }
        }
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", getName(), method);
    }

}
