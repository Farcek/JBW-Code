/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.le.farcek.jbw.code.servlet;

import java.util.Collection;
import javax.servlet.http.HttpServletResponse;
import mn.le.farcek.jbw.api.action.IResponseHeader;

public class IResponseHeaderImpl implements IResponseHeader {

    private final HttpServletResponse response;

    public IResponseHeaderImpl(HttpServletResponse response) {
        this.response = response;
    }

    @Override
    public Collection<String> getHeaderNames() {
        return response.getHeaderNames();
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return response.getHeaders(name);
    }

    @Override
    public String getHeader(String name) {
        return response.getHeader(name);
    }

    @Override
    public void addIntHeader(String name, int value) {
        response.addIntHeader(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        response.setIntHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        response.addHeader(name, value);
    }

    @Override
    public void setHeader(String name, String value) {
        response.setHeader(name, value);
    }

    @Override
    public void addDateHeader(String name, long date) {
        response.addDateHeader(name, date);
    }

    @Override
    public boolean containsHeader(String name) {
        return response.containsHeader(name);
    }

    @Override
    public void setContentType(String type) {
        response.setContentType(type);
    }

    @Override
    public String getContentType() {
        return response.getContentType();
    }

    @Override
    public void setStatus(int code) {
        response.setStatus(code);
    }
    
    @Override
    public void setCharacterEncoding(String charset){
        response.setCharacterEncoding(charset);
    }

}
