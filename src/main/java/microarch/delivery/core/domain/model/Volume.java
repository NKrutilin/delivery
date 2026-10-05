package microarch.delivery.core.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import libs.ddd.ValueObject;
import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.val;

import java.util.List;

@Embeddable
@NoArgsConstructor(force = true)
@Getter
public class Volume extends ValueObject<Volume> {

    @Column(name = "volume")
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
