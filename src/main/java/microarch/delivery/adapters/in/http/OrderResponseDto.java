package microarch.delivery.adapters.in.http;

import java.util.UUID;

public record OrderResponseDto(UUID id, LocationDto location) {
}
