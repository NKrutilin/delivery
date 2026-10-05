package microarch.delivery.adapters.out.postgres;

import lombok.val;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Import(OrderRepositoryImpl.class)
class OrderRepositoryImplTest extends BaseJpaTest {

    @Autowired
    private OrderRepositoryImpl orderRepository;

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void savesNewOrder() {
        val order = newOrder();

        val saved = orderRepository.create(order);
        orderJpaRepository.flush();

        assertThat(saved.getId()).isEqualTo(order.getId());
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.Created);
        assertThat(saved.getLocation()).isEqualTo(order.getLocation());
        assertThat(saved.getVolume()).isEqualTo(order.getVolume());
    }

    @Test
    void updatesOrderStatus() {
        val order = orderRepository.create(newOrder());
        order.markAsAssigned();

        orderRepository.update(order);
        orderJpaRepository.flush();
        entityManager.clear();

        val found = orderJpaRepository.findById(order.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(OrderStatus.Assigned);
    }

    @Test
    void findsOrderById() {
        val order = orderRepository.create(newOrder());

        assertThat(orderRepository.findById(order.getId())).isPresent();

        assertThat(orderRepository.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void returnsAnyOrderInCreatedStatus() {
        val assigned = newOrder();
        assigned.markAsAssigned();
        val created = newOrder();
        orderJpaRepository.saveAllAndFlush(List.of(assigned, created));

        val found = orderRepository.getAnyInCreatedStatus();

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(created.getId());
        assertThat(found.get().getStatus()).isEqualTo(OrderStatus.Created);
    }

    @Test
    void returnsEmptyWhenNoCreatedOrders() {
        assertThat(orderRepository.getAnyInCreatedStatus()).isEmpty();
    }

    @Test
    void returnsPagedAssignedOrders() {
        val assignedIdSet = Set.copyOf(orderJpaRepository.saveAllAndFlush(List.of(newAssigned(), newAssigned()))
                .stream().map(Order::getId).toList());
        orderJpaRepository.saveAndFlush(newOrder());

        val expectedIds = orderJpaRepository.findAll(Sort.by("id")).stream().map(Order::getId)
                .filter(assignedIdSet::contains).toList();

        val page = orderRepository.getAllInAssignedStatus(0, 2);

        assertThat(page.getContent()).extracting(Order::getId).containsExactlyElementsOf(expectedIds);
        assertThat(page.getContent()).extracting(Order::getStatus).containsOnly(OrderStatus.Assigned);
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    private Order newOrder() {
        return Order.create(UUID.randomUUID(), Location.create(5, 5).getValue(), Volume.create(10).getValue())
                .getValue();
    }

    private Order newAssigned() {
        val order = newOrder();
        order.markAsAssigned();
        return order;
    }
}
