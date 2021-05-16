package guru.sfg.beer.order.service.testComponents;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.brewery.modal.events.AllocateBeerOrderRequest;
import guru.sfg.brewery.modal.events.AllocateBeerOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderAllocationListener {

    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.ALLOCATE_BEER_ORDER_QUEUE)
    public void listen(Message msg){
        boolean allocationError = false;
        boolean pendingInventory = false;
        AllocateBeerOrderRequest request = (AllocateBeerOrderRequest) msg.getPayload();

        log.debug("Recieved the allocation request for beer orderId :"
                                                        +request.getBeerOrder().getId());

        if(request.getBeerOrder().getCustomerRef() != null &&
                request.getBeerOrder().getCustomerRef().equals("fail-allocation")){
            allocationError=true;
        }
        if(request.getBeerOrder().getCustomerRef() != null &&
                request.getBeerOrder().getCustomerRef().equals("partial-allocation")){
            pendingInventory=true;
        }

        boolean finalPendingInventory = pendingInventory;
        request.getBeerOrder().getBeerOrderLines().forEach(beerOrderLineDto -> {

            if(finalPendingInventory){
                //Indicates that we did not do  a full allocation of what was demanded
                beerOrderLineDto.setQuantityAllocated(beerOrderLineDto.getOrderQuantity() -1 );
            }
            //Indicates that we did a full allocation of what was demanded
            beerOrderLineDto.setQuantityAllocated(beerOrderLineDto.getOrderQuantity());
        });


        log.debug("Sending back the successfull allocation result for beer order Id"
                                                        +request.getBeerOrder().getId());

        jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESULT_QUEUE,
                AllocateBeerOrderResult.builder()
                            .beerOrderDto(request.getBeerOrder())
                            .allocationError(allocationError)
                            .pendingInventory(pendingInventory)
                            .build());
    }
}
