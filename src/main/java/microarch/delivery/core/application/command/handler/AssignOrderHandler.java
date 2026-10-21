package microarch.delivery.core.application.command.handler;

import java.util.List;

import libs.ddd.DomainEventPublisher;
import libs.errs.Error;
import libs.errs.UnitResult;
import lombok.AllArgsConstructor;
import lombok.val;
import microarch.delivery.Constants;
import microarch.delivery.core.application.command.AssignOrderCommand;
import microarch.delivery.core.domain.service.OrderAssignService;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
public class AssignOrderHandler {

    private final OrderRepository orderRepository;
    private final CourierRepository courierRepository;
    private final OrderAssignService orderAssignService;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional
    public UnitResult<Error> handle(final AssignOrderCommand command) {
        val orderCandidate = orderRepository.getAnyInCreatedStatus();
        if (orderCandidate.isEmpty()) {
            return UnitResult.success();
        }

        val order = orderCandidate.get();
        val couriers = courierRepository.getAllCouriers();
        val dispatched = orderAssignService.assign(order, couriers);

        if (dispatched.isFailure()) {
            return dispatched.getError().getCode().equals(Constants.ERR_CODE_NO_COURIERS) ? UnitResult.success()
                    : UnitResult.failure(dispatched.getError());
        }

        courierRepository.update(dispatched.getValue());
        orderRepository.update(order);

        domainEventPublisher.publish(List.of(order));

        return UnitResult.success();
    }
}
