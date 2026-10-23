package microarch.delivery.core.application.command.handler;

import libs.errs.Error;
import libs.errs.UnitResult;
import lombok.AllArgsConstructor;
import lombok.val;
import microarch.delivery.core.application.command.CreateOrderCommand;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

@Component
@AllArgsConstructor
public class CreateOrderHandler {

    private final OrderRepository orderRepository;

    @Transactional
    public UnitResult<Error> handle(final CreateOrderCommand command) {
        val volume = Volume.create(command.volume());
        if (volume.isFailure()) {
            return UnitResult.failure(volume.getError());
        }

        val random = ThreadLocalRandom.current();
        val location = Location.create(random.nextInt(1, 11), random.nextInt(1, 11)).getValueOrThrow();
        val order = Order.create(command.orderId(), location, volume.getValue());

        if (order.isFailure()) {
            return UnitResult.failure(order.getError());
        }

        orderRepository.create(order.getValue());
        return UnitResult.success();
    }
}
