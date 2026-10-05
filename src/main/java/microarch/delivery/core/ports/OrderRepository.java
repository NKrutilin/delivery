package microarch.delivery.core.ports;

import microarch.delivery.core.domain.model.order.Order;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {

    Order create(Order order);

    Order update(Order order);

    Optional<Order> findById(UUID id);

    Optional<Order> getAnyInCreatedStatus();

    Page<Order> getAllInAssignedStatus(int page, int size);
}
