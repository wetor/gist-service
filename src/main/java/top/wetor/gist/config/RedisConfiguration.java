package top.wetor.gist.config;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.joda.deser.DateTimeDeserializer;
import com.fasterxml.jackson.datatype.joda.ser.DateTimeSerializer;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import org.joda.time.format.DateTimeFormatter;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;



import java.io.IOException;

@Configuration
//@EnableCaching
public class RedisConfiguration extends CachingConfigurerSupport {

    public static class JodaDateTimeJsonSerializer extends JsonSerializer<DateTime> {
        @Override
        public void serialize(DateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.toString("yyyy-MM-dd HH:mm:ss"));
        }
    }

    public static class JodaDateTimeJsonDeSerializer extends JsonDeserializer<DateTime> {
        @Override
        public DateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            String s = p.readValueAs(String.class);
            DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
            return DateTime.parse(s, dateTimeFormatter);
        }
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        //objectMapper.registerModule(new JodaModule());
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(DateTime.class, new DateTimeSerializer());
        simpleModule.addDeserializer(DateTime.class, DateTimeDeserializer.forType(DateTime.class));
        objectMapper.registerModule(simpleModule);
        serializer.setObjectMapper(objectMapper);

        //serializer.setObjectMapper(customObjectMapper);
        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        return RedisCacheManager.builder(connectionFactory).cacheDefaults(cacheConfiguration).build();
    }
}

