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
        boolean isValid = true;
        boolean sendResponse = true;

        ValidateBeerOrderRequest request = (ValidateBeerOrderRequest) msg.getPayload();

        if(request.getBeerOrder().getCustomerRef() != null &&
                request.getBeerOrder().getCustomerRef().equals( "fail-validation")){
            isValid = false;
        }else if(request.getBeerOrder().getCustomerRef() != null &&
                request.getBeerOrder().getCustomerRef().equals("dont-validate")){
            sendResponse = false;
        }

        if(sendResponse){
            jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER_RESULT,
                    ValidateOrderResult.builder()
                            .isValid(isValid)
                            .orderId(request.getBeerOrder().getId())
                            .build());
        }
    }
}
