package guru.sfg.brewery.modal.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateOrderResult {
    private UUID orderId;
    private boolean isValid;
}
