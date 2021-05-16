package guru.sfg.beer.order.service.services.listeners;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.services.BeerOrderManagerImpl;
import guru.sfg.brewery.modal.events.ValidateOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class ValidationResultListener {
private final BeerOrderManagerImpl manager;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_RESULT)
    public void listen(ValidateOrderResult result){
        final UUID beerOrderId = result.getOrderId();

        log.debug("Validation result for Order Id "+ beerOrderId);
        System.out.println(" ValidationResultListener in lIsteners");
        manager.processValidationResult(beerOrderId,result.isValid());
    }
}
