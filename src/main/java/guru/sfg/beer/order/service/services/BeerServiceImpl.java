package guru.sfg.beer.order.service.services;

import guru.sfg.brewery.modal.BeerDTO;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@ConfigurationProperties(prefix="sfg.brewery",ignoreUnknownFields = false)
@Service
public class BeerServiceImpl implements BeerService {

    public static final String BEER_PATH_V1 = "/api/v1/beer/";
    public static final String BEER_UPC_PATH_V1 = "/api/v1/beer/beerUpc/";
    private final RestTemplate restTemplate;

    private String beerServiceHost;

    public BeerServiceImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public Optional<BeerDTO> getBeerById(UUID uuid){
        return Optional.of(restTemplate.getForObject(beerServiceHost + BEER_PATH_V1 + uuid.toString(), BeerDTO.class));
    }

    @Override
    public Optional<BeerDTO> getBeerByUpc(String upc) {
        BeerDTO beer = restTemplate.getForObject(beerServiceHost + BEER_UPC_PATH_V1 + upc, BeerDTO.class);
        return Optional.of(beer);
    }

    public void setBeerServiceHost(String beerServiceHost) {
        this.beerServiceHost = beerServiceHost;
    }
}
