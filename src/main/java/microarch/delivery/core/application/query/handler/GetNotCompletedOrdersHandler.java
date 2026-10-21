package microarch.delivery.core.application.query.handler;

import lombok.AllArgsConstructor;
import lombok.val;
import microarch.delivery.core.application.dto.OrderDto;
import microarch.delivery.core.application.query.GetNotCompletedOrdersQuery;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class GetNotCompletedOrdersHandler {

    private final OrderRepository orderRepository;

    public List<OrderDto> handle(final GetNotCompletedOrdersQuery query) {
        return orderRepository.getAllNotCompleted().stream()
                .map(order -> new OrderDto(order.getId(), order.getLocation().getX(), order.getLocation().getY()))
                .toList();
    }
}
