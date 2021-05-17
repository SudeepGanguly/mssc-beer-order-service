package guru.sfg.beer.order.service.services;

import guru.sfg.brewery.modal.CustomerPagedList;
import org.springframework.data.domain.Pageable;

public interface CustomerService {
    CustomerPagedList listCustomers(Pageable pageable);
}