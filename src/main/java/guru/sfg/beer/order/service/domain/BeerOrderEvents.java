    package guru.sfg.beer.order.service.domain;

    public enum BeerOrderEvents {
        VALIDATE_ORDER , VALIDATION_PASSED , VALIDATION_FAILED,
        ALLOCATION_SUCESS , ALLOCATION_FAILED , ALLOCATION_NO_INVENTORY,
        BEERORDER_PICKED_UP;
    }
