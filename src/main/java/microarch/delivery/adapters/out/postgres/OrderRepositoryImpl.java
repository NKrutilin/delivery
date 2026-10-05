package microarch.delivery.adapters.out.postgres;

import lombok.AllArgsConstructor;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Order create(final Order order) {
        return orderJpaRepository.save(order);
    }

    @Override
    public Order update(final Order order) {
        return orderJpaRepository.save(order);
    }

    @Override
    public Optional<Order> findById(final UUID id) {
        return orderJpaRepository.findById(id);
    }

    @Override
    public Optional<Order> getAnyInCreatedStatus() {
        return orderJpaRepository.findFirstByStatus(OrderStatus.Created);
    }

    @Override
    public List<Order> getAllNotCompleted() {
        return orderJpaRepository.findAllByStatusIn(List.of(OrderStatus.Created, OrderStatus.Assigned));
    }
}
