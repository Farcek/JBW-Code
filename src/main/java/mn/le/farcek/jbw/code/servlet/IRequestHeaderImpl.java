/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.le.farcek.jbw.code.servlet;

import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import mn.le.farcek.jbw.api.action.IRequestHeader;

public class IRequestHeaderImpl implements IRequestHeader {

    private final HttpServletRequest request;

    public IRequestHeaderImpl(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public int getIntHeader(String name) {
        return request.getIntHeader(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return request.getHeaderNames();
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return request.getHeaders(name);
    }

    @Override
    public String getHeader(String name) {
        return request.getHeader(name);
    }

    @Override
    public long getDateHeader(String name) {
        return request.getDateHeader(name);
    }

    @Override
    public String getContentType() {
        return request.getContentType();
    }

}
