package microarch.delivery.core.application.command.handler;

import libs.errs.Error;
import libs.errs.Result;
import libs.errs.UnitResult;
import lombok.AllArgsConstructor;
import lombok.val;
import microarch.delivery.Constants;
import microarch.delivery.core.application.command.MoveCourierCommand;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.CourierRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
public class MoveCourierHandler {

    private final CourierRepository courierRepository;

    @Transactional
    public UnitResult<Error> handle(final MoveCourierCommand command) {
        val target = Location.create(command.x(), command.y());
        if (target.isFailure()) {
            return UnitResult.failure(target.getError());
        }

        val courier = courierRepository.findById(command.courierId());
        if (courier.isEmpty()) {
            return UnitResult.failure(Error.of(Constants.ERR_CODE_COURIER_NOT_FOUND,
                    "Courier " + command.courierId() + " does not exist"));
        }

        val moved = moveOneStepTowards(courier.get(), target.getValue());
        if (moved.isFailure()) {
            return moved;
        }

        courierRepository.update(courier.get());
        return UnitResult.success();
    }

    private UnitResult<Error> moveOneStepTowards(final Courier courier, final Location target) {
        val dx = target.getX() - courier.getLocation().getX();
        val dy = target.getY() - courier.getLocation().getY();

        if (dx > 0) {
            return asUnit(courier.moveRight());
        }
        if (dx < 0) {
            return asUnit(courier.moveLeft());
        }
        if (dy > 0) {
            return asUnit(courier.moveUp());
        }
        return asUnit(courier.moveDown());
    }

    private UnitResult<Error> asUnit(final Result<Courier, Error> result) {
        return result.isFailure() ? UnitResult.failure(result.getError()) : UnitResult.success();
    }
}
