package guru.sfg.beer.order.service.sm.actions;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.domain.BeerOrderEvents;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.services.BeerOrderManagerImpl;
import guru.sfg.brewery.modal.events.AllocationFailureEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AllocationFailureAction implements Action<BeerOrderStatusEnum, BeerOrderEvents> {

    private final JmsTemplate jmsTemplate;

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEvents> stateContext) {
        //Get hold of the BeerOrderId from the state context
        String beerOrderId = (String)stateContext.getMessage()
                                                .getHeaders()
                                                .get(BeerOrderManagerImpl.BEER_ORDER_HEADER);

            jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_FAILURE_QUEUE,
                    AllocationFailureEvent.builder()
                            .orderId(UUID.fromString(beerOrderId))
                            .build());

            log.debug("Sent Validation request for  queue for order Id : "+beerOrderId);
    }
}
