package microarch.delivery.adapters.out.postgres;

import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findFirstByStatus(OrderStatus status);

    List<Order> findAllByStatusIn(Collection<OrderStatus> statuses);
}
