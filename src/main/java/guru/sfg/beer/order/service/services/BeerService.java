package guru.sfg.beer.order.service.services;

import guru.sfg.brewery.modal.BeerDTO;

import java.util.Optional;
import java.util.UUID;

public interface BeerService {

    public Optional<BeerDTO> getBeerById(UUID beerId);
    public Optional<BeerDTO> getBeerByUpc(String upc);
}
