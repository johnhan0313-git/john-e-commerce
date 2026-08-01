package com.john.ecommerce.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * JS Number 仅安全表示 ±2^53-1。雪花 Long 超出后会在浏览器失真，
 * 导致 /{id}/audit 等路径查不到记录。超出安全整数时序列化为字符串。
 */
@Configuration
public class JacksonConfig {

    private static final long JS_MAX_SAFE = 9007199254740991L;

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer longAsSafeJsCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule("SafeLongModule");
            JsonSerializer<Long> serializer = new SafeLongSerializer();
            module.addSerializer(Long.class, serializer);
            module.addSerializer(Long.TYPE, serializer);
            builder.modulesToInstall(module);
        };
    }

    static final class SafeLongSerializer extends JsonSerializer<Long> {
        @Override
        public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }
            long v = value;
            if (v > JS_MAX_SAFE || v < -JS_MAX_SAFE) {
                gen.writeString(Long.toString(v));
            } else {
                gen.writeNumber(v);
            }
        }
    }
}
