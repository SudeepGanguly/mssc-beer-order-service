package guru.sfg.beer.order.service.sm;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEvents;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.services.BeerOrderManagerImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

@Configuration
@EnableStateMachineFactory
@RequiredArgsConstructor
public class BeerOrderStateMachineConfig extends StateMachineConfigurerAdapter<BeerOrderStatusEnum, BeerOrderEvents> {

    private final Action<BeerOrderStatusEnum,BeerOrderEvents> validateOrderAction;
    private final Action<BeerOrderStatusEnum,BeerOrderEvents> allocateOrderAction;
    private final Action<BeerOrderStatusEnum,BeerOrderEvents> validationFailureAction;
    private final Action<BeerOrderStatusEnum,BeerOrderEvents> allocationFailureAction;

    @Override
    public void configure(StateMachineStateConfigurer<BeerOrderStatusEnum, BeerOrderEvents> states) throws Exception {
        states.withStates()
                .initial(BeerOrderStatusEnum.NEW)
                .states(EnumSet.allOf(BeerOrderStatusEnum.class))
                .end(BeerOrderStatusEnum.VALIDATION_EXCEPTION)
                .end(BeerOrderStatusEnum.ALLOCATION_EXCEPTION)
                .end(BeerOrderStatusEnum.DELIVERED)
                .end(BeerOrderStatusEnum.DELIVERY_EXCEPTION)
                .end(BeerOrderStatusEnum.PICKED_UP)
                .end(BeerOrderStatusEnum.CANCELLED);

    }

    @Override
    public void configure(StateMachineTransitionConfigurer<BeerOrderStatusEnum, BeerOrderEvents> transitions) throws Exception {
        transitions
           //Validation Transitions
                .withExternal()
                .source(BeerOrderStatusEnum.NEW).target(BeerOrderStatusEnum.VALIDATION_PENDING)
                .event(BeerOrderEvents.VALIDATE_ORDER)
                .action(validateOrderAction)        //todo add a validation action
          .and()
                .withExternal()
                .source(BeerOrderStatusEnum.VALIDATION_PENDING).target(BeerOrderStatusEnum.VALIDATED)
                .event(BeerOrderEvents.VALIDATION_PASSED)
          .and()
                .withExternal()
                .source(BeerOrderStatusEnum.VALIDATION_PENDING).target(BeerOrderStatusEnum.VALIDATION_EXCEPTION)
                .event(BeerOrderEvents.VALIDATION_FAILED)
                .action(validationFailureAction)
          //Allocation Transitions
           .and()
                .withExternal()
                .source(BeerOrderStatusEnum.VALIDATED).target(BeerOrderStatusEnum.ALLOCATION_PENDING)
                .event(BeerOrderEvents.ALLOCATE_ORDER)
                .action(allocateOrderAction)
           .and()
                .withExternal()
                .source(BeerOrderStatusEnum.ALLOCATION_PENDING).target(BeerOrderStatusEnum.PENDING_INVENTORY)
                .event(BeerOrderEvents.ALLOCATION_NO_INVENTORY)
            .and()
                .withExternal()
                .source(BeerOrderStatusEnum.ALLOCATION_PENDING).target(BeerOrderStatusEnum.ALLOCATED)
                .event(BeerOrderEvents.ALLOCATION_SUCESS)
            .and()
                .withExternal()
                .source(BeerOrderStatusEnum.ALLOCATION_PENDING).target(BeerOrderStatusEnum.ALLOCATION_EXCEPTION)
                .event(BeerOrderEvents.ALLOCATION_FAILED)
                .action(allocationFailureAction)
            .and()
                .withExternal()
                .source(BeerOrderStatusEnum.ALLOCATED)
                .target(BeerOrderStatusEnum.PICKED_UP)
                .event(BeerOrderEvents.BEERORDER_PICKED_UP)
            .and()
                .withExternal()
                .source(BeerOrderStatusEnum.VALIDATION_PENDING)
                .target(BeerOrderStatusEnum.CANCELLED)
                .event(BeerOrderEvents.CANCEL_ORDER)
            .and()
                .withExternal()
                .source(BeerOrderStatusEnum.ALLOCATION_PENDING)
                .target(BeerOrderStatusEnum.CANCELLED)
                .event(BeerOrderEvents.CANCEL_ORDER)
            .and()
                .withExternal()
                .source(BeerOrderStatusEnum.VALIDATED)
                .target(BeerOrderStatusEnum.CANCELLED)
                .event(BeerOrderEvents.CANCEL_ORDER)
             .and()
                .withExternal()
                .source(BeerOrderStatusEnum.ALLOCATED)
                .target(BeerOrderStatusEnum.CANCELLED)
                .event(BeerOrderEvents.CANCEL_ORDER);
               //todo add an action

    }

}
