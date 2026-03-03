package frontoffice.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class HttpErrorParser {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String bestEffortMessage(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "";
        }

        try {
            Map<?, ?> asMap = mapper.readValue(responseBody, Map.class);
            Object message = asMap.get("message");
            if (message != null && !String.valueOf(message).isBlank()) {
                return String.valueOf(message);
            }
            Object error = asMap.get("error");
            if (error != null && !String.valueOf(error).isBlank()) {
                return String.valueOf(error);
            }
        } catch (Exception ignored) {
        }

        String trimmed = responseBody.trim();
        return trimmed.length() > 300 ? trimmed.substring(0, 300) + "..." : trimmed;
    }
}
