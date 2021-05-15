package guru.sfg.brewery.event;

import guru.sfg.brewery.modal.BeerOrderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ValidateBeerOrderRequest {
    private BeerOrderDto beerOrder;
}
