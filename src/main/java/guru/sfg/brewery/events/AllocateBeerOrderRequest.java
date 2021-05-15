package guru.sfg.brewery.events;

import guru.sfg.brewery.modal.BeerOrderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AllocateBeerOrderRequest {
    private BeerOrderDto beerOrder;
}
