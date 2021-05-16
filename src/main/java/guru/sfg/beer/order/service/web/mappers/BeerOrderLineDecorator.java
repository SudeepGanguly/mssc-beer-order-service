package guru.sfg.beer.order.service.web.mappers;

import guru.sfg.beer.order.service.domain.BeerOrderLine;
import guru.sfg.beer.order.service.services.BeerService;
import guru.sfg.brewery.modal.BeerDTO;
import guru.sfg.brewery.modal.BeerOrderLineDto;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Optional;

@NoArgsConstructor
public abstract class BeerOrderLineDecorator implements BeerOrderLineMapper{
    private  BeerOrderLineMapper beerOrderLineMapper;
    private  BeerService beerService;

    @Autowired
    @Qualifier("delegate")
    public void setBeerOrderLineMapper(BeerOrderLineMapper beerOrderLineMapper){
        this.beerOrderLineMapper=beerOrderLineMapper;
    }

    @Autowired
    public void setBeerService(BeerService beerService){
        this.beerService=beerService;
    }

    @Override
    public BeerOrderLineDto beerOrderLineToDto(BeerOrderLine line) {
        Optional<BeerDTO> beerDTO = beerService.getBeerByUpc(line.getUpc());
        BeerOrderLineDto beerOrderLineDto = beerOrderLineMapper.beerOrderLineToDto(line);

        beerDTO.ifPresent(beerDto -> {
            beerOrderLineDto.setBeerName(beerDto.getBeerName());
            beerOrderLineDto.setPrice(beerDto.getPrice());
            beerOrderLineDto.setBeerStyle(beerDto.getBeerStyle());
        });
        return beerOrderLineDto;
    }

    @Override
    public BeerOrderLine dtoToBeerOrderLine(BeerOrderLineDto dto) {
        return beerOrderLineMapper.dtoToBeerOrderLine(dto);
    }
}
