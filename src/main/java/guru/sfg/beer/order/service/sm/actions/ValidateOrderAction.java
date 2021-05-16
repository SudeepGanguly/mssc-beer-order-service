package guru.sfg.beer.order.service.sm.actions;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEvents;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.services.BeerOrderManagerImpl;
import guru.sfg.beer.order.service.web.mappers.BeerOrderMapper;
import guru.sfg.brewery.modal.events.ValidateBeerOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidateOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEvents> {

    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderMapper beerOrderMapper;
    private final JmsTemplate jmsTemplate;

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEvents> stateContext) {
        //Get hold of the BeerOrderId from the state context
        String beerOrderId = (String)stateContext.getMessage()
                                                .getHeaders()
                                                .get(BeerOrderManagerImpl.BEER_ORDER_HEADER);

        //Get hold of the BeerOrder
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(UUID.fromString(beerOrderId));

        beerOrderOptional.ifPresentOrElse(beerOrder -> {
            //Send the Order to the inventor service.
            //The event ValidateBeerOrderRequest takes in BeerOrderDTO so mapper is used

            ValidateBeerOrderRequest req = ValidateBeerOrderRequest.builder()
                    .beerOrder(beerOrderMapper.beerOrderToDto(beerOrder))
                    .build();

            jmsTemplate.convertAndSend(JmsConfig.VALIDATE_BEER_ORDER,ValidateBeerOrderRequest.builder()
                    .beerOrder(beerOrderMapper.beerOrderToDto(beerOrder))
                    .build());

            System.out.println("Message : Inside validate order action");
            log.debug("Sent Validation request for  queue for order Id : "+beerOrderId);
        },()->log.debug("Valid Header not found"));
    }
}
