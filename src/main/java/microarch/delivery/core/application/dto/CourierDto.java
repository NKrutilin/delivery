package microarch.delivery.core.application.dto;

import microarch.delivery.core.domain.model.Location;

import java.util.UUID;

public record CourierDto(UUID id, String name, Location location) {
}
