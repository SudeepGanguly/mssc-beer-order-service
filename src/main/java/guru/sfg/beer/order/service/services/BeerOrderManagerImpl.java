package guru.sfg.beer.order.service.services;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEvents;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.sm.BeerOrderManagerInterceptor;
import guru.sfg.brewery.modal.BeerOrderDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BeerOrderManagerImpl implements BeerOrderManager {

    private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEvents> smFactory;
    private final BeerOrderRepository beerOrderRepository;
    public static final String BEER_ORDER_HEADER = "beer-order-header";
    private final BeerOrderManagerInterceptor beerOrderManagerInterceptor;

    //Method to initialize the initial state and save it in the database.
    @Transactional
    @Override
    public BeerOrder newBeerOrder(BeerOrder beerOrder) {
        beerOrder.setId(null);
        beerOrder.setOrderStatus(BeerOrderStatusEnum.NEW);
        BeerOrder savedBeerOrder = beerOrderRepository.save(beerOrder);
        sendBeerEvent(savedBeerOrder, BeerOrderEvents.VALIDATE_ORDER);
        return savedBeerOrder;
    }

    @Override
    public void processValidationResult(UUID beerOrderId, boolean isValid) {
        BeerOrder beerOrder = beerOrderRepository.getOne(beerOrderId);
        if(isValid){
            sendBeerEvent(beerOrder,BeerOrderEvents.VALIDATION_PASSED);

            BeerOrder validatedBeerOrder = beerOrderRepository.getOne(beerOrderId);
            sendBeerEvent(validatedBeerOrder,BeerOrderEvents.ALLOCATE_ORDER);
        } else {
            sendBeerEvent(beerOrder,BeerOrderEvents.VALIDATION_FAILED);
        }
    }

    //create the message for sending event and send the event
    public void sendBeerEvent(BeerOrder beerOrder , BeerOrderEvents events){
        StateMachine<BeerOrderStatusEnum, BeerOrderEvents> sm = build(beerOrder);

        Message msg = MessageBuilder.withPayload(events)
                                    .setHeader(BEER_ORDER_HEADER,beerOrder.getId().toString())
                                    .build();

        sm.sendEvent(msg);
    }


    //Method to build the State Machine and change the state
    private StateMachine<BeerOrderStatusEnum,BeerOrderEvents> build(BeerOrder beerOrder){

        //Create a new StateMAchine
        StateMachine<BeerOrderStatusEnum,BeerOrderEvents> sm =
                                        smFactory.getStateMachine((beerOrder.getId()));

        //Make sure it is stopped
        sm.stop();

        //Set the State of the StateMachine
        sm.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                    sma.addStateMachineInterceptor(beerOrderManagerInterceptor);
                    sma.resetStateMachine(
                            new DefaultStateMachineContext<>(beerOrder.getOrderStatus(),
                                                        null,null,null));
                });

        //Start the State Machine
        sm.start();

        return sm;
    }


}
