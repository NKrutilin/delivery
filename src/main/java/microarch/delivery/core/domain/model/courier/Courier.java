package microarch.delivery.core.domain.model.courier;

import libs.ddd.Aggregate;
import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import microarch.delivery.Constants;
import microarch.delivery.core.domain.model.Assignment;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.order.Order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
public class Courier extends Aggregate<UUID> {

    static final int MAX_VOLUME = 20;
    static final int STEP_SIZE = 1;

    private final String name;
    private final Volume maxVolume = Volume.create(MAX_VOLUME).getValueOrThrow();
    private final List<Assignment> assignments = new ArrayList<>();
    private Location location;

    private Courier(final String name, final Location location) {
        super(UUID.randomUUID());
        this.name = name;
        this.location = location;
    }

    public static Result<Courier, Error> create(final String name, final Location location) {
        val nameError = (name == null || name.isBlank())
                ? Error.of("courier.name.is.blank", "Courier name must not be null or blank") : null;
        val locationError = (location == null) ? Error.of(Constants.ERR_CODE_OBJ_IS_NULL, "location must not be null")
                : null;
        val error = Guard.combine(nameError, locationError);

        if (error != null) {
            return Result.failure(error);
        }

        return Result.success(new Courier(name, location));
    }

    public List<Assignment> getAssignments() {
        return Collections.unmodifiableList(assignments);
    }

    public Result<Boolean, Error> canTakeOneMoreOrder(final Volume orderVolume) {
        if (orderVolume == null) {
            return Result.failure(Error.of(Constants.ERR_CODE_OBJ_IS_NULL, "orderVolume must not be null"));
        }

        val plannedVolume = assignments.stream().map(assignment -> assignment.getVolume().getValue())
                .reduce(orderVolume.getValue(), Integer::sum);

        if (plannedVolume > maxVolume.getValue()) {
            return Result.failure(Error.of("courier.volume.limit.exceeded",
                    "Planned volume %d exceeds courier limit %d".formatted(plannedVolume, MAX_VOLUME)));
        }

        return Result.success(true);
    }

    public Result<Courier, Error> takeOrder(final Order order) {
        if (order == null) {
            return Result.failure(Error.of(Constants.ERR_CODE_OBJ_IS_NULL, "order must not be null"));
        }

        val alreadyTaken = assignments.stream().anyMatch(assignment -> assignment.getOrderId().equals(order.getId()));
        if (alreadyTaken) {
            return Result
                    .failure(Error.of("courier.order.already.assigned", "Courier already has order " + order.getId()));
        }

        val capacityCheck = canTakeOneMoreOrder(order.getVolume());
        if (capacityCheck.isFailure()) {
            return Result.failure(capacityCheck.getError());
        }

        val assignment = Assignment.create(order.getId(), order.getVolume(), order.getLocation()).getValueOrThrow();
        assignments.add(assignment);

        return Result.success(this);
    }

    public Result<Courier, Error> completeOrder(final UUID orderId) {
        if (orderId == null) {
            return Result.failure(Error.of(Constants.ERR_CODE_OBJ_IS_NULL, "orderId must not be null"));
        }

        val assignment = assignments.stream().filter(existing -> existing.getOrderId().equals(orderId)).findFirst()
                .orElse(null);

        if (assignment == null) {
            return Result.failure(Error.of("courier.order.not.found", "Courier has no order " + orderId));
        }

        val distance = assignment.getLocation().computeDistance(location).getValueOrThrow();
        if (distance.getValue() > STEP_SIZE) {
            return Result.failure(Error.of("courier.far.from.order", "Courier is too far from order " + orderId));
        }

        val completion = assignment.complete(location);
        if (completion.isFailure()) {
            return Result.failure(completion.getError());
        }

        assignments.remove(assignment);
        return Result.success(this);
    }

    public Result<Courier, Error> moveUp() {
        return moveBy(0, STEP_SIZE);
    }

    public Result<Courier, Error> moveDown() {
        return moveBy(0, -STEP_SIZE);
    }

    public Result<Courier, Error> moveLeft() {
        return moveBy(-STEP_SIZE, 0);
    }

    public Result<Courier, Error> moveRight() {
        return moveBy(STEP_SIZE, 0);
    }

    private Result<Courier, Error> moveBy(final int dx, final int dy) {
        val target = Location.create(location.getX() + dx, location.getY() + dy);

        if (target.isFailure()) {
            return Result.failure(target.getError());
        }

        location = target.getValue();
        return Result.success(this);
    }
}
