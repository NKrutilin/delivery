package microarch.delivery.core.domain.model;

import libs.ddd.ValueObject;
import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import microarch.delivery.Constants;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Location extends ValueObject<Location> {

    static final int MIN_X = 1;
    static final int MAX_X = 10;
    static final int MIN_Y = 1;
    static final int MAX_Y = 10;

    private final int x;
    private final int y;

    public static Result<Location, Error> create(final int x, final int y) {
        val xError = Guard.againstOutOfRange(x, MIN_X, MAX_X, "x");
        val yError = Guard.againstOutOfRange(y, MIN_Y, MAX_Y, "y");
        val error = Guard.combine(xError, yError);

        if (error != null) {
            return Result.failure(error);
        }

        return Result.success(new Location(x, y));
    }

    public Result<Distance, Error> computeDistance(final Location to) {
        if (to == null) {
            return Result.failure(Error.of(Constants.ERR_CODE_OBJ_IS_NULL, "Target location must not be null"));
        }

        return Distance.create(Math.abs(to.x - x) + Math.abs(to.y - y));
    }

    @Override
    protected Iterable<Object> equalityComponents() {
        return List.of(x, y);
    }
}
