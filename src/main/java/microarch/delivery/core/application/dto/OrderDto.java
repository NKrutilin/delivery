package microarch.delivery.core.application.dto;

import microarch.delivery.core.domain.model.Location;

import java.util.UUID;

public record OrderDto(UUID id, Location location) {
}
