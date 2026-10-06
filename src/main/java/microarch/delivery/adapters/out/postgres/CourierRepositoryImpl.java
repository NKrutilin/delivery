package microarch.delivery.adapters.out.postgres;

import lombok.AllArgsConstructor;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.CourierRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CourierRepositoryImpl implements CourierRepository {

    private final CourierJpaRepository courierJpaRepository;

    @Override
    public Courier create(final Courier courier) {
        return courierJpaRepository.save(courier);
    }

    @Override
    public Courier update(final Courier courier) {
        return courierJpaRepository.save(courier);
    }

    @Override
    public Optional<Courier> findById(final UUID id) {
        return courierJpaRepository.findById(id);
    }
}
