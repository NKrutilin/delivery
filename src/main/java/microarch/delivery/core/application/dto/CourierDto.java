package microarch.delivery.core.application.dto;

import java.util.UUID;

public record CourierDto(UUID id, String name, int x, int y) {
}
