package guru.sfg.beer.order.service.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;


@Configuration
public class JmsConfig {

    public static final String VALIDATE_BEER_ORDER = "validate-beer-order";
    public static final String VALIDATE_ORDER_RESULT = "validate-order-result";
    public static final String ALLOCATE_BEER_ORDER_QUEUE = "allocate-beer-order";
    public static final String ALLOCATE_ORDER_RESULT_QUEUE = "allocate-order-result";
    public static final String ALLOCATE_FAILURE_QUEUE = "allocation-queue";
    public static final String DEALLOCATE_ORDER_QUEUE = "deallocate-order-queue";

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper){
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        converter.setObjectMapper(objectMapper);
        return converter;
    }
}
