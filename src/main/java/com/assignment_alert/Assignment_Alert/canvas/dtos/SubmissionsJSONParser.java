package com.assignment_alert.Assignment_Alert.canvas.dtos;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class SubmissionsJSONParser extends JsonDeserializer<Double> {

    @Override
    public Double deserialize(JsonParser jp, DeserializationContext dc) throws IOException, JacksonException {
        JsonNode node = jp.getCodec().readTree(jp);

        if(node != null && node.get("score") != null) {
            return node.get("score").asDouble();
        }
        
        return null;
    }
    
}
