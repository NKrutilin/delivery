package microarch.delivery.adapters.out.postgres;

import lombok.val;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.courier.Courier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;

import java.util.List;
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
    void returnsPagedCouriersSortedById() {
        courierJpaRepository
                .saveAllAndFlush(List.of(Courier.create("courier-1", Location.create(1, 1).getValue()).getValue(),
                        Courier.create("courier-2", Location.create(2, 2).getValue()).getValue(),
                        Courier.create("courier-3", Location.create(3, 3).getValue()).getValue()));

        val sortedIds = courierJpaRepository.findAll(Sort.by("id")).stream().map(Courier::getId).toList();

        val firstPage = courierRepository.getAll(0, 2);

        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getContent()).extracting(Courier::getId).containsExactly(sortedIds.get(0),
                sortedIds.get(1));

        val secondPage = courierRepository.getAll(1, 2);
        assertThat(secondPage.getContent()).extracting(Courier::getId).containsExactly(sortedIds.get(2));
    }
}
