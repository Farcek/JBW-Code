/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.le.farcek.jbw.code.action.view;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mn.le.farcek.jbw.api.action.view.IJsonParser;
import org.codehaus.jackson.map.MapperConfig;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author Farcek
 */
public class IJsonParserImpl implements IJsonParser {

    @Override
    public String toJson(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        return mapper.writeValueAsString(object);
    }

    @Override
    public <T> T fromJson(String jsonString, Class<T> type) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonString, type);
    }

    @Override
    public <T> List<T> fromListByJson(String jsonString, Class<T> type) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        T[] readValue = mapper.readValue(jsonString, mapper.getTypeFactory().constructArrayType(type));
        return Arrays.asList(readValue);
    }

    @Override
    public <T> T fromJson(Reader reader, Class<T> type) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(reader, type);
    }

}
