package microarch.delivery.core.domain.model;

import libs.ddd.ValueObject;
import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.Getter;
import lombok.val;

import java.util.List;

@Getter
public class Volume extends ValueObject<Volume> {

    private final int value;

    private Volume(int value) {
        this.value = value;
    }

    public static Result<Volume, Error> create(final int value) {
        val error = Guard.againstLessOrEqual(value, 0, "value");

        if (error != null) {
            return Result.failure(error);
        }

        return Result.success(new Volume(value));
    }

    @Override
    protected Iterable<Object> equalityComponents() {
        return List.of(value);
    }
}
