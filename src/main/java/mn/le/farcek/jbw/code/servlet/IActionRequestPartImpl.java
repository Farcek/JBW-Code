/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.le.farcek.jbw.code.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import javax.servlet.http.Part;
import mn.le.farcek.jbw.api.action.IActionRequestPart;


public class IActionRequestPartImpl implements IActionRequestPart {
    private final Part part;

    public IActionRequestPartImpl(Part part) {
        this.part = part;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return part.getInputStream();
    }

    @Override
    public String getContentType() {
        return part.getContentType();
    }

    @Override
    public String getName() {
        return part.getName();
    }

    @Override
    public String getSubmittedFileName() {
        return part.getSubmittedFileName();
    }

    @Override
    public long getSize() {
        return part.getSize();
    }

    @Override
    public void write(String fileName) throws IOException {
        part.write(fileName);
    }

    @Override
    public void delete() throws IOException {
        part.delete();
    }

    @Override
    public String getHeader(String name) {
        return part.getHeader(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return part.getHeaders(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return part.getHeaderNames();
    }

    @Override
    public String toString() {
        return part.toString();
    }
    
    
    
}
