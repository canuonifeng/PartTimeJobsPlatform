package com.parttime.cservice.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Objects;


public class JsonConverter {

    private static final JsonConverter JSON_CONVERTER = new JsonConverter();

    @Getter
    private final ObjectMapper mapper;

    private JsonConverter() {
        mapper = new ObjectMapper();
        // 允许对象忽略json中不存在的属性
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 允许出现特殊字符和转义符
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        // 允许出现单引号
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        // 忽视为空的属性
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Date.class, new DateDeserializer());
        mapper.registerModule(module);
    }

    public static JsonConverter create() {
        return JSON_CONVERTER;
    }

    public <T> T toObject(String json, Class<T> clazz) {
        try {
            if(StringUtils.isBlank(json)) {
                return null;
            }
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException("将json字符转换为对象时失败!", e);
        }
    }

    public <T> List<T> toArray(String json, Class<T> clazz) {
        try {
            if(StringUtils.isBlank(json)) {
                return null;
            }
            CollectionType javaType = mapper.getTypeFactory().constructCollectionType(List.class, clazz);
            return mapper.readValue(json, javaType);
        } catch (IOException e) {
            throw new RuntimeException("将json字符转换为对象时失败!", e);
        }
    }

    public <T> T toObject(String json, TypeReference<T> clazz) {
        try {
            if(StringUtils.isBlank(json)) {
                return null;
            }
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException("将json字符转换为对象时失败!", e);
        }
    }

    public <T> T toObject(String json, Type clazz) {
        try {
            if(StringUtils.isBlank(json)) {
                return null;
            }
            return mapper.readValue(json, new CustomTypeReference<T>(clazz));
        } catch (IOException e) {
            throw new RuntimeException("将json字符转换为对象时失败!", e);
        }
    }

    public <T> T toObject(JsonParser parser, Type clazz) {
        try {
            if(Objects.isNull(parser)) {
                return null;
            }
            return mapper.readValue(parser, new CustomTypeReference<T>(clazz));
        } catch (IOException e) {
            throw new RuntimeException("将json字符转换为对象时失败!", e);
        }
    }
}

class CustomTypeReference<T> extends TypeReference<T>{
    private final Type type;

    public CustomTypeReference(Type pt){
        this.type = pt;
    }

    @Override
    public Type getType() {
        return type;
    }
}


@Slf4j
class DateDeserializer extends JsonDeserializer<Date> {

    @Override
    public Date deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        String value = node.asText();
        if(isNumeric(value)) {
            return new Date(node.asLong());
        } else if(StringUtils.isNotBlank(value)) {
            try {
                if(value.contains(":")) {
                    return DateUtils.parseDate(value, "yyyy-MM-dd HH:mm:ss");
                } else {
                    return DateUtils.parseDate(value, "yyyy-MM-dd");
                }
            } catch (ParseException e) {
                log.error("json deserialize error: ", e);
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch(Exception e){
            return false;
        }
    }
}
