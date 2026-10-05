package microarch.delivery.core.domain.model;

import libs.ddd.ValueObject;
import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Distance extends ValueObject<Distance> {

    static final int MIN_VALUE = 0;

    private final int value;

    public static Result<Distance, Error> create(final int value) {
        val error = Guard.againstLessThan(value, MIN_VALUE, "value");

        if (error != null) {
            return Result.failure(error);
        }

        return Result.success(new Distance(value));
    }

    @Override
    protected Iterable<Object> equalityComponents() {
        return List.of(value);
    }
}
