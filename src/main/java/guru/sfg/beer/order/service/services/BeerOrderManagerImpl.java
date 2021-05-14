package guru.sfg.beer.order.service.services;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEvents;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class BeerOrderManagerImpl implements BeerOrderManager {

    private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEvents> smFactory;
    private final BeerOrderRepository beerOrderRepository;

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

    //create the message for sending event and send the event
    public void sendBeerEvent(BeerOrder beerOrder , BeerOrderEvents events){
        StateMachine<BeerOrderStatusEnum, BeerOrderEvents> sm = build(beerOrder);

        Message msg = MessageBuilder.withPayload(events)
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
                    sma.resetStateMachine(
                            new DefaultStateMachineContext<>(beerOrder.getOrderStatus(),
                                                        null,null,null));
                });

        //Start the State Machine
        sm.start();

        return sm;
    }
}
