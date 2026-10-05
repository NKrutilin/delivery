package microarch.delivery.core.application.command.handler;

import libs.errs.Error;
import libs.errs.UnitResult;
import lombok.AllArgsConstructor;
import lombok.val;
import microarch.delivery.core.application.command.CreateCourierCommand;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.CourierRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
public class CreateCourierHandler {

    static final Location START_LOCATION = Location.create(1, 1).getValueOrThrow();

    private final CourierRepository courierRepository;

    @Transactional
    public UnitResult<Error> handle(final CreateCourierCommand command) {
        val courier = Courier.create(command.name(), START_LOCATION);

        if (courier.isFailure()) {
            return UnitResult.failure(courier.getError());
        }

        courierRepository.create(courier.getValue());
        return UnitResult.success();
    }
}
