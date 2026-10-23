package microarch.delivery.core.ports;

import microarch.delivery.core.domain.model.courier.Courier;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourierRepository {

    Courier create(Courier courier);

    Courier update(Courier courier);

    Optional<Courier> findById(UUID id);

    List<Courier> getAllCouriers();
}
