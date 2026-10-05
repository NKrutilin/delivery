package microarch.delivery.core.application.query.handler;

import lombok.AllArgsConstructor;
import lombok.val;
import microarch.delivery.core.application.dto.CourierDto;
import microarch.delivery.core.application.query.GetAllCouriersQuery;
import microarch.delivery.core.ports.CourierRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class GetAllCouriersHandler {

    private final CourierRepository courierRepository;

    public List<CourierDto> handle(final GetAllCouriersQuery query) {
        return courierRepository.getAllCouriers().stream()
                .map(courier -> new CourierDto(courier.getId(), courier.getName(), courier.getLocation())).toList();
    }
}
