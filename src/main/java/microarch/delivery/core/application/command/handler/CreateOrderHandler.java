package microarch.delivery.core.application.command.handler;

import libs.errs.Error;
import libs.errs.UnitResult;
import lombok.AllArgsConstructor;
import lombok.val;
import microarch.delivery.core.application.command.CreateOrderCommand;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.GeoService;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
public class CreateOrderHandler {

    private final OrderRepository orderRepository;
    private final GeoService geoService;

    @Transactional
    public UnitResult<Error> handle(final CreateOrderCommand command) {
        val volume = Volume.create(command.volume());
        if (volume.isFailure()) {
            return UnitResult.failure(volume.getError());
        }

        val location = geoService.getGeolocation(command.street());
        if (location.isFailure()) {
            return UnitResult.failure(location.getError());
        }

        val order = Order.create(command.orderId(), location.getValue(), volume.getValue());

        if (order.isFailure()) {
            return UnitResult.failure(order.getError());
        }

        orderRepository.create(order.getValue());
        return UnitResult.success();
    }
}
