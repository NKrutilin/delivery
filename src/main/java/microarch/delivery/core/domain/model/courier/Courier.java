package microarch.delivery.core.domain.model.courier;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import libs.ddd.Aggregate;
import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.val;
import microarch.delivery.Constants;
import microarch.delivery.core.domain.model.Distance;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Status;
import microarch.delivery.core.domain.model.Volume;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "courier", indexes = { @Index(name = "courier_name", columnList = "name", unique = true) })
@NoArgsConstructor(force = true)
@Getter
public class Courier extends Aggregate<UUID> {

    static final Volume MAX_VOLUME = Volume.create(20).getValueOrThrow();
    static final int STEP_SIZE = 1;

    private static final Distance COMPLETION_DISTANCE = Distance.create(STEP_SIZE).getValueOrThrow();

    private final String name;
    private final Volume maxVolume = MAX_VOLUME;
    @Transient
    private final List<Assignment> assignments = new ArrayList<>();
    @Embedded
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

        val plannedVolume = assignments.stream().filter(assignment -> assignment.getStatus() == Status.Assigned)
                .map(Assignment::getVolume).reduce(orderVolume, Volume::add);

        if (plannedVolume.isGreaterThan(maxVolume)) {
            return Result.failure(Error.of("courier.volume.limit.exceeded",
                    "Planned volume %s exceeds courier limit %s".formatted(plannedVolume, maxVolume)));
        }

        return Result.success(true);
    }

    public Result<Courier, Error> takeOrder(final UUID orderId, final Volume volume, final Location location) {
        val error = Guard.combine(Guard.againstNullOrEmpty(orderId, "orderId"),
                (volume == null) ? Error.of(Constants.ERR_CODE_OBJ_IS_NULL, "volume must not be null") : null,
                (location == null) ? Error.of(Constants.ERR_CODE_OBJ_IS_NULL, "location must not be null") : null);

        if (error != null) {
            return Result.failure(error);
        }

        val alreadyTaken = assignments.stream().anyMatch(assignment -> assignment.getOrderId().equals(orderId));
        if (alreadyTaken) {
            return Result.failure(Error.of("courier.order.already.assigned", "Courier already has order " + orderId));
        }

        val capacityCheck = canTakeOneMoreOrder(volume);
        if (capacityCheck.isFailure()) {
            return Result.failure(capacityCheck.getError());
        }

        val assignment = Assignment.create(orderId, volume, location).getValueOrThrow();
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
        if (distance.isGreaterThan(COMPLETION_DISTANCE)) {
            return Result.failure(Error.of("courier.far.from.order", "Courier is too far from order " + orderId));
        }

        val completion = assignment.complete(location);
        if (completion.isFailure()) {
            return Result.failure(completion.getError());
        }

        return Result.success(this);
    }

    public Result<Courier, Error> changeLocation(final Location newLocation) {
        if (newLocation == null) {
            return Result.failure(Error.of(Constants.ERR_CODE_OBJ_IS_NULL, "newLocation must not be null"));
        }

        location = newLocation;
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
