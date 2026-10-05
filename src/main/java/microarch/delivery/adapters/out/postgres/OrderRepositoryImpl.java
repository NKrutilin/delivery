package microarch.delivery.adapters.out.postgres;

import lombok.AllArgsConstructor;
import lombok.val;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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
    public Page<Order> getAllInAssignedStatus(int page, int size) {
        val pageable = PageRequest.of(page, size, Sort.by("id"));
        return orderJpaRepository.findAllByStatus(OrderStatus.Assigned, pageable);
    }
}
