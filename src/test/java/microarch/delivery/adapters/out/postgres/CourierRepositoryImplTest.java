package microarch.delivery.adapters.out.postgres;

import lombok.val;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Status;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Import(CourierRepositoryImpl.class)
class CourierRepositoryImplTest extends BaseJpaTest {

    @Autowired
    private CourierRepositoryImpl courierRepository;

    @Autowired
    private CourierJpaRepository courierJpaRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void savesNewCourier() {
        val courier = Courier.create("courier-create", Location.create(2, 3).getValue()).getValue();

        val saved = courierRepository.create(courier);
        courierJpaRepository.flush();

        assertThat(saved.getId()).isEqualTo(courier.getId());
        assertThat(courierJpaRepository.count()).isEqualTo(1);
    }

    @Test
    void updatesCourierLocation() {
        val courier = courierRepository
                .create(Courier.create("courier-update", Location.create(1, 1).getValue()).getValue());
        courier.moveRight();
        courier.moveUp();

        courierRepository.update(courier);
        courierJpaRepository.flush();
        entityManager.clear();

        val found = courierJpaRepository.findById(courier.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getLocation()).isEqualTo(Location.create(2, 2).getValue());
    }

    @Test
    void findsCourierById() {
        val courier = courierRepository
                .create(Courier.create("courier-find", Location.create(4, 4).getValue()).getValue());

        assertThat(courierRepository.findById(courier.getId())).isPresent().get().extracting(Courier::getName)
                .isEqualTo("courier-find");

        assertThat(courierRepository.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void restoresCourierAssignmentsAfterContextClear() {
        val courier = Courier.create("courier-assignments", Location.create(3, 3).getValue()).getValue();
        val order = Order.create(UUID.randomUUID(), Location.create(3, 3).getValue(), Volume.create(5).getValue())
                .getValue();
        courier.takeOrder(order.getId(), order.getVolume(), order.getLocation());

        courierRepository.create(courier);
        courierJpaRepository.flush();
        entityManager.clear();

        val found = courierJpaRepository.findById(courier.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getAssignments()).hasSize(1);
        assertThat(found.get().getAssignments().get(0).getOrderId()).isEqualTo(order.getId());
        assertThat(found.get().getAssignments().get(0).getStatus()).isEqualTo(Status.Assigned);
        assertThat(found.get().getAssignments().get(0).getVolume()).isEqualTo(order.getVolume());
        assertThat(found.get().getAssignments().get(0).getLocation()).isEqualTo(order.getLocation());
    }
}
