package guru.sfg.beer.order.service.web.mappers;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrder.BeerOrderBuilder;
import guru.sfg.beer.order.service.domain.BeerOrderLine;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.brewery.modal.BeerOrderDto;
import guru.sfg.brewery.modal.BeerOrderDto.BeerOrderDtoBuilder;
import guru.sfg.brewery.modal.BeerOrderLineDto;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2021-05-19T11:40:40+0530",
    comments = "version: 1.4.1.Final, compiler: javac, environment: Java 13.0.1 (Oracle Corporation)"
)
@Component
public class BeerOrderMapperImpl implements BeerOrderMapper {

    @Autowired
    private DateMapper dateMapper;
    @Autowired
    private BeerOrderLineMapper beerOrderLineMapper;

    @Override
    public BeerOrderDto beerOrderToDto(BeerOrder beerOrder) {
        if ( beerOrder == null ) {
            return null;
        }

        BeerOrderDtoBuilder beerOrderDto = BeerOrderDto.builder();

        beerOrderDto.id( beerOrder.getId() );
        if ( beerOrder.getVersion() != null ) {
            beerOrderDto.version( beerOrder.getVersion().intValue() );
        }
        beerOrderDto.createdDate( dateMapper.asOffsetDateTime( beerOrder.getCreatedDate() ) );
        beerOrderDto.lastModifiedDate( dateMapper.asOffsetDateTime( beerOrder.getLastModifiedDate() ) );
        beerOrderDto.customerRef( beerOrder.getCustomerRef() );
        beerOrderDto.beerOrderLines( beerOrderLineSetToBeerOrderLineDtoList( beerOrder.getBeerOrderLines() ) );
        if ( beerOrder.getOrderStatus() != null ) {
            beerOrderDto.orderStatus( beerOrder.getOrderStatus().name() );
        }
        beerOrderDto.orderStatusCallbackUrl( beerOrder.getOrderStatusCallbackUrl() );

        return beerOrderDto.build();
    }

    @Override
    public BeerOrder dtoToBeerOrder(BeerOrderDto dto) {
        if ( dto == null ) {
            return null;
        }

        BeerOrderBuilder beerOrder = BeerOrder.builder();

        beerOrder.id( dto.getId() );
        if ( dto.getVersion() != null ) {
            beerOrder.version( dto.getVersion().longValue() );
        }
        beerOrder.createdDate( dateMapper.asTimestamp( dto.getCreatedDate() ) );
        beerOrder.lastModifiedDate( dateMapper.asTimestamp( dto.getLastModifiedDate() ) );
        beerOrder.customerRef( dto.getCustomerRef() );
        beerOrder.beerOrderLines( beerOrderLineDtoListToBeerOrderLineSet( dto.getBeerOrderLines() ) );
        if ( dto.getOrderStatus() != null ) {
            beerOrder.orderStatus( Enum.valueOf( BeerOrderStatusEnum.class, dto.getOrderStatus() ) );
        }
        beerOrder.orderStatusCallbackUrl( dto.getOrderStatusCallbackUrl() );

        return beerOrder.build();
    }

    protected List<BeerOrderLineDto> beerOrderLineSetToBeerOrderLineDtoList(Set<BeerOrderLine> set) {
        if ( set == null ) {
            return null;
        }

        List<BeerOrderLineDto> list = new ArrayList<BeerOrderLineDto>( set.size() );
        for ( BeerOrderLine beerOrderLine : set ) {
            list.add( beerOrderLineMapper.beerOrderLineToDto( beerOrderLine ) );
        }

        return list;
    }

    protected Set<BeerOrderLine> beerOrderLineDtoListToBeerOrderLineSet(List<BeerOrderLineDto> list) {
        if ( list == null ) {
            return null;
        }

        Set<BeerOrderLine> set = new HashSet<BeerOrderLine>( Math.max( (int) ( list.size() / .75f ) + 1, 16 ) );
        for ( BeerOrderLineDto beerOrderLineDto : list ) {
            set.add( beerOrderLineMapper.dtoToBeerOrderLine( beerOrderLineDto ) );
        }

        return set;
    }
}
