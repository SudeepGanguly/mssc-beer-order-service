package guru.sfg.beer.order.service.sm.actions;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEvents;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.services.BeerOrderManagerImpl;
import guru.sfg.beer.order.service.web.mappers.BeerOrderMapper;
import guru.sfg.brewery.modal.events.AllocateBeerOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class AllocateOrderAction implements Action<BeerOrderStatusEnum , BeerOrderEvents> {

    private final BeerOrderRepository beerOrderRepository;
    private final JmsTemplate jmsTemplate;
    private final BeerOrderMapper beerOrderMapper;

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEvents> stateContext) {
        //Step 1 : Fetch the orderId
        String orderId = (String)stateContext.getMessage()
                                .getHeaders()
                                .get(BeerOrderManagerImpl.BEER_ORDER_HEADER);

        //Step 2 : Fetch the BeerOrder using the OrderId
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(UUID.fromString(orderId));

        beerOrderOptional.ifPresentOrElse(beerOrder -> {
            jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_BEER_ORDER_QUEUE,
                    AllocateBeerOrderRequest.builder()
                            .beerOrder(beerOrderMapper.beerOrderToDto(beerOrder))
                            .build());

            log.debug("Sent Allocation Request for order id: "+ orderId);
        },()-> log.debug("Valid Header not found"));

    }
}
