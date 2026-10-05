package microarch.delivery.core.application.command;

import microarch.delivery.core.domain.model.Location;

import java.util.UUID;

public record MoveCourierCommand(UUID courierId, Location location) {
}
