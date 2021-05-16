package guru.sfg.beer.order.service.testComponents;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.brewery.modal.events.ValidateBeerOrderRequest;
import guru.sfg.brewery.modal.events.ValidateOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BeerOrderValidationListener {

    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.VALIDATE_BEER_ORDER)
    private void listen(Message msg){
        ValidateBeerOrderRequest request = (ValidateBeerOrderRequest) msg.getPayload();

        System.out.println(" BeerOrderValidationListener in the testComponents");
        jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER_RESULT,
                ValidateOrderResult.builder()
                            .isValid(true)
                            .orderId(request.getBeerOrder().getId())
                            .build());
    }
}
