package guru.sfg.brewery.modal.events;

import guru.sfg.brewery.modal.BeerOrderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocateBeerOrderResult {

    private BeerOrderDto beerOrderDto;
    private boolean allocationError = false;
    private boolean pendingInventory = false;
}
