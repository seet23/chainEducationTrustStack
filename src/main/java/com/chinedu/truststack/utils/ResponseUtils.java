package com.chinedu.truststack.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ResponseUtils {
    private static final Logger logger = LoggerFactory.getLogger(ResponseUtils.class);
    private static ObjectMapper mapper = new ObjectMapper();

    public static String createErrorJson(String errorCode, String errorMessage) {
        String error = "";
        try {
            Map<String,String> errorMap = new HashMap<String,String>();
            errorMap.put("code", errorCode);
            errorMap.put("message", errorMessage);
            error = mapper.writeValueAsString(errorMap);

        } catch (Exception e) {
            logger.error("createErrorJson " + e.getMessage(), e);
        }

        return error;
    }
}
