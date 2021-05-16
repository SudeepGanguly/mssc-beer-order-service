package guru.sfg.beer.order.service.sm.actions;

import guru.sfg.beer.order.service.domain.BeerOrderEvents;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.services.BeerOrderManagerImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ValidationFailureAction implements Action<BeerOrderStatusEnum, BeerOrderEvents> {

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEvents> stateContext) {
        String beerOrderId = (String) stateContext.getMessage().getHeaders()
                                                .get(BeerOrderManagerImpl.BEER_ORDER_HEADER);

        log.error(" Validtion Failed for orderId"+beerOrderId);
    }
}
