package microarch.delivery.core.application.command.handler;

import java.util.List;

import libs.ddd.DomainEventPublisher;
import libs.errs.Error;
import libs.errs.UnitResult;
import lombok.AllArgsConstructor;
import lombok.val;
import microarch.delivery.Constants;
import microarch.delivery.core.application.command.CompleteOrderCommand;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
public class CompleteOrderHandler {

    private final CourierRepository courierRepository;
    private final OrderRepository orderRepository;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional
    public UnitResult<Error> handle(final CompleteOrderCommand command) {
        val courier = courierRepository.findById(command.courierId());
        if (courier.isEmpty()) {
            return UnitResult.failure(Error.of(Constants.ERR_CODE_COURIER_NOT_FOUND,
                    "Courier " + command.courierId() + " does not exist"));
        }

        val order = orderRepository.findById(command.orderId());
        if (order.isEmpty()) {
            return UnitResult.failure(
                    Error.of(Constants.ERR_CODE_ORDER_NOT_FOUND, "Order " + command.orderId() + " does not exist"));
        }

        val completed = courier.get().completeOrder(command.orderId());
        if (completed.isFailure()) {
            return UnitResult.failure(completed.getError());
        }

        val marked = order.get().markAsCompleted();
        if (marked.isFailure()) {
            return UnitResult.failure(marked.getError());
        }

        courierRepository.update(courier.get());
        orderRepository.update(order.get());

        domainEventPublisher.publish(List.of(order.get()));

        return UnitResult.success();
    }
}
