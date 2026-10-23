package microarch.delivery.adapters.in.http;

import java.util.UUID;

public record CourierResponseDto(UUID id, String name, LocationDto location) {
}
