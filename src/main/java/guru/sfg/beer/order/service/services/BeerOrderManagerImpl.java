package guru.sfg.beer.order.service.services;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEvents;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.sm.BeerOrderManagerInterceptor;
import guru.sfg.brewery.modal.BeerOrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RequiredArgsConstructor
@Service
public class BeerOrderManagerImpl implements BeerOrderManager {

    private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEvents> smFactory;
    private final BeerOrderRepository beerOrderRepository;
    public static final String BEER_ORDER_HEADER = "beer-order-header";
    private final BeerOrderManagerInterceptor beerOrderManagerInterceptor;
    private EntityManager entityManager;

    //Method to initialize the initial state and save it in the database.
    @Transactional
    @Override
    public BeerOrder newBeerOrder(BeerOrder beerOrder) {
        System.out.println(" Inside newBeerOrder method ");
        beerOrder.setId(null);
        beerOrder.setOrderStatus(BeerOrderStatusEnum.NEW);
        BeerOrder savedBeerOrder = beerOrderRepository.saveAndFlush(beerOrder);
        sendBeerEvent(savedBeerOrder, BeerOrderEvents.VALIDATE_ORDER);
        return savedBeerOrder;
    }

    @Transactional
    @Override
    public void processValidationResult(UUID beerOrderId, boolean isValid) {
        log.debug("Process validation Result for beerOrderId : "
                +beerOrderId+ " Valid "+isValid);

        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderId);

        beerOrderOptional.ifPresentOrElse(beerOrder -> {
        if(isValid){
            sendBeerEvent(beerOrder,BeerOrderEvents.VALIDATION_PASSED);

            //wait for status change
            awaitForStatus(beerOrderId,BeerOrderStatusEnum.VALIDATED);
            BeerOrder validatedBeerOrder = beerOrderRepository.findById(beerOrderId).get();
            sendBeerEvent(validatedBeerOrder,BeerOrderEvents.ALLOCATE_ORDER);
        } else {
            sendBeerEvent(beerOrder,BeerOrderEvents.VALIDATION_FAILED);
        }
        },() -> log.debug("Order Not Found , Id"+beerOrderId));
    }

    @Override
    public void beerOrderAllocationPassed(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());

        beerOrderOptional.ifPresentOrElse(beerOrder -> {
            //wait for status change
            awaitForStatus(beerOrder.getId(),BeerOrderStatusEnum.VALIDATED);
            sendBeerEvent(beerOrder, BeerOrderEvents.ALLOCATION_SUCESS);
            updateAllocatedQty(beerOrderDto);
        },() -> log.debug("Order Not Found , Id"+beerOrderDto.getId()));

    }

    @Override
    public void beerOrderAllocationPendingInventory(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());
        beerOrderOptional.ifPresentOrElse(beerOrder -> {
            //wait for status change
            awaitForStatus(beerOrder.getId(),BeerOrderStatusEnum.VALIDATED);
            sendBeerEvent(beerOrder, BeerOrderEvents.ALLOCATION_NO_INVENTORY);
            updateAllocatedQty(beerOrderDto);
        },() -> log.debug("Order Not Found , Id"+beerOrderDto.getId()));
    }

    @Override
    public void beerOrderAllocationFailed(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());

        beerOrderOptional.ifPresentOrElse(beerOrder -> {
            sendBeerEvent(beerOrder, BeerOrderEvents.ALLOCATION_FAILED);
        },() -> log.debug("Order Not Found , Id"+beerOrderDto.getId()));
    }

    @Override
    public void beerOrderPickedUp(UUID id) {
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(id);

        beerOrderOptional.ifPresentOrElse(beerOrder -> {
            //do Process
            sendBeerEvent(beerOrder,BeerOrderEvents.BEERORDER_PICKED_UP);

        },() -> log.debug("Order Not Found , Id"+id));
    }

    @Override
    public void cancelbeerOrder(UUID id) {
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(id);

        beerOrderOptional.ifPresentOrElse(beerOrder -> {
            //do Process
            sendBeerEvent(beerOrder,BeerOrderEvents.CANCEL_ORDER);

        },() -> log.debug("Order Not Found , Id"+id));
    }

    private void updateAllocatedQty(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> allocatedOrderOptional =
                        beerOrderRepository.findById(beerOrderDto.getId());

        allocatedOrderOptional.ifPresentOrElse(allocatedOrder -> {
            allocatedOrder.getBeerOrderLines().forEach(beerOrderLine -> {
                beerOrderDto.getBeerOrderLines().forEach(beerOrderLineDto -> {
                    if(beerOrderLine.getId() .equals(beerOrderLineDto.getId())){
                        beerOrderLine.setQuantityAllocated(beerOrderLineDto.getQuantityAllocated());
                    }
                });
            });

            beerOrderRepository.saveAndFlush(allocatedOrder);
        },() -> log.debug("Order Not Found , Id"+beerOrderDto.getId()));
    }

    private void awaitForStatus(UUID beerOrderId, BeerOrderStatusEnum statusEnum) {

        AtomicBoolean found = new AtomicBoolean(false);
        AtomicInteger loopCount = new AtomicInteger(0);

        while (!found.get()) {
            if (loopCount.incrementAndGet() > 10) {
                found.set(true);
                log.debug("Loop Retries exceeded");
            }

            beerOrderRepository.findById(beerOrderId).ifPresentOrElse(beerOrder -> {
                if (beerOrder.getOrderStatus().equals(statusEnum)) {
                    found.set(true);
                    log.debug("Order Found");
                } else {
                    log.debug("Order Status Not Equal. Expected: " + statusEnum.name() + " Found: " + beerOrder.getOrderStatus().name());
                }
            }, () -> {
                log.debug("Order Id Not Found");
            });

            if (!found.get()) {
                try {
                    log.debug("Sleeping for retry");
                    Thread.sleep(100);
                } catch (Exception e) {
                    // do nothing
                }
            }
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
