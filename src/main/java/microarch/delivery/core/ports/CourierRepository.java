package microarch.delivery.core.ports;

import microarch.delivery.core.domain.model.courier.Courier;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface CourierRepository {

    Courier create(Courier courier);

    Courier update(Courier courier);

    Optional<Courier> findById(UUID id);

    Page<Courier> getAll(int page, int size);
}
