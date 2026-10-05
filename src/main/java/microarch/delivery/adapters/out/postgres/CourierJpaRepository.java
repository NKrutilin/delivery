package microarch.delivery.adapters.out.postgres;

import microarch.delivery.core.domain.model.courier.Courier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CourierJpaRepository extends JpaRepository<Courier, UUID> {
}
