package httpclients;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbstractReply {
    private static final Log logger = LogFactory.getLog(AbstractReply.class);

    public static <T extends AbstractReply> T jsonToReply(String json, Class<? extends AbstractReply> c) {
        T reply = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerSubtypes(c);
            reply = (T) mapper.readValue(json, c);
            return reply;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return reply;
    }

    public String replyToJson() {
        String json = "";
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerSubtypes(this.getClass());
            json = mapper.writeValueAsString(this);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return json;
    }

}
