/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.le.farcek.jbw.code.servlet;

import java.io.IOException;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import mn.le.farcek.common.utils.FClassUtils;
import mn.le.farcek.common.utils.FStringUtils;
import mn.le.farcek.jbw.api.IConfig;
import mn.le.farcek.jbw.api.action.IActionMethod;
import mn.le.farcek.jbw.api.action.IActionRequest;
import mn.le.farcek.jbw.api.action.IActionRequestPart;

/**
 *
 * @author Farcek
 */
public class ServletActionRequest implements IActionRequest {

    private final HttpServletRequest request;
    private final IConfig config;
    private final String extension;

    public ServletActionRequest(HttpServletRequest request, IConfig config, String extension) {
        this.request = request;
        this.config = config;
        this.extension = extension;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return request.getParameterMap();
    }

    @Override
    public String[] getParameterValues(String name) {
        return request.getParameterValues(name);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return request.getParameterNames();
    }

    @Override
    public String getParameter(String name) {
        return request.getParameter(name);
    }

    public String get(String name) {
        return request.getParameter(name);
    }

    @Override
    public Integer getIntParam(String name) {
        return FClassUtils.fromString(Integer.class, getParameter(name));
    }

    @Override
    public Boolean getBooleanParam(String name) {
        try {
            return FClassUtils.fromString(Boolean.class, getParameter(name));
        } catch (Exception e) {
            return false;
        }

    }

    @Override
    public Float getFloatParam(String name) {
        return FClassUtils.fromString(Float.class, getParameter(name));
    }

    @Override
    public String getContentType() {
        return request.getContentType();
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return request.getAttributeNames();
    }

    @Override
    public Object getAttribute(String name) {
        return request.getAttribute(name);
    }

    @Override
    public IActionMethod getMethod() {
        String method = request.getMethod();
        if (IActionMethod.Get.name().equalsIgnoreCase(method))
            return IActionMethod.Get;
        if (IActionMethod.Post.name().equalsIgnoreCase(method))
            return IActionMethod.Post;
        if (IActionMethod.Put.name().equalsIgnoreCase(method))
            return IActionMethod.Put;

        return null;
    }

    @Override
    public Locale getLocale() {
        Object o = request.getSession().getAttribute("_locale");
        if (o instanceof Locale)
            return (Locale) o;
        else {
            Locale l = config.getDefaultLocale();
            request.getSession().setAttribute("_locale", l);
            return l;
        }
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    @Override
    public String getExtension() {
        return extension;
    }

    @Override
    public IActionRequestPart getPart(String name) {
        try {
            Part part = request.getPart(name);

            return new IActionRequestPartImpl(part);

        } catch (IOException | ServletException ex) {
            return null;
        }
    }

    @Override
    public String getParameter(String name, String defaultValue) {
        String v = getParameter(name);
        if (v == null) return defaultValue;
        return v;
    }

    @Override
    public Integer getIntParam(String name, int defaultValue) {
        Integer v = getIntParam(name);
        if (v == null) return defaultValue;
        return v;
    }

    @Override
    public Long getLongParam(String name) {
        return FClassUtils.fromString(Long.class, getParameter(name));
    }

    @Override
    public Long getLongParam(String name, long defaultValue) {
        Long v = getLongParam(name);
        if (v == null) return defaultValue;
        return v;
    }

    @Override
    public Boolean getBooleanParam(String name, boolean defaultValue) {
        Boolean v = getBooleanParam(name);
        if (v == null) return defaultValue;
        return v;
    }

    @Override
    public Float getFloatParam(String name, float defaultValue) {
        Float v = getFloatParam(name);
        if (v == null) return defaultValue;
        return v;
    }

    @Override
    public Double getDoubleParam(String name) {
        return FClassUtils.fromString(Double.class, getParameter(name));
    }

    @Override
    public Double getDoubleParam(String name, double defaultValue) {
        Double v = getDoubleParam(name);
        if (v == null) return defaultValue;
        return v;
    }

    @Override
    public Date getDateParam(String name) {
        String d = getParameter(name);
        if (FStringUtils.isEmpty(d)) return null;
        try {
            return DateFormat.getDateInstance().parse(d);
        } catch (ParseException ex) {
            return null;
        }

    }

    @Override
    public Date getDateParam(String name, String format) {
        String d = getParameter(name);
        if (FStringUtils.isEmpty(d)) return null;

        SimpleDateFormat ft = new SimpleDateFormat(format);
        try {
            return ft.parse(d);
        } catch (ParseException ex) {
            return null;
        }
    }

    @Override
    public Date getDateParam(String name, Date defaultValue) {
        Date v = getDateParam(name);
        if (v == null) return defaultValue;
        return v;
    }

    @Override
    public Date getDateParam(String name, String format, Date defaultValue) {
        Date v = getDateParam(name, format);
        if (v == null) return defaultValue;
        return v;
    }

}
