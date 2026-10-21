package microarch.delivery.core.domain.model.courier;

import lombok.val;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Status;
import microarch.delivery.core.domain.model.Volume;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AssignmentTest {

    @Test
    void createsAssignmentInAssignedStatus() {
        val assignment = Assignment
                .create(UUID.randomUUID(), Volume.create(2).getValue(), Location.create(3, 3).getValue()).getValue();

        assertThat(assignment.getStatus()).isEqualTo(Status.Assigned);
        assertThat(assignment.getId()).isNotNull();
    }

    @Test
    void rejectsNullOrderId() {
        val result = Assignment.create(null, Volume.create(2).getValue(), Location.create(3, 3).getValue());

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("value.is.required");
    }

    @Test
    void rejectsEmptyOrderId() {
        val result = Assignment.create(new UUID(0, 0), Volume.create(2).getValue(), Location.create(3, 3).getValue());

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("value.is.required");
    }

    @Test
    void rejectsNullVolume() {
        val result = Assignment.create(UUID.randomUUID(), null, Location.create(3, 3).getValue());

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("object.is.null");
    }

    @Test
    void rejectsNullLocation() {
        val result = Assignment.create(UUID.randomUUID(), Volume.create(2).getValue(), null);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("object.is.null");
    }

    @Test
    void completesWhenCourierIsOnTheSameCell() {
        val assignment = Assignment
                .create(UUID.randomUUID(), Volume.create(2).getValue(), Location.create(5, 5).getValue()).getValue();

        val result = assignment.complete(Location.create(5, 5).getValue());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getStatus()).isEqualTo(Status.Completed);
    }

    @Test
    void completesWhenCourierIsOneStepAway() {
        val assignment = Assignment
                .create(UUID.randomUUID(), Volume.create(2).getValue(), Location.create(5, 5).getValue()).getValue();

        val result = assignment.complete(Location.create(6, 5).getValue());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getStatus()).isEqualTo(Status.Completed);
    }

    @Test
    void doesNotCompleteWhenCourierIsFarAway() {
        val assignment = Assignment
                .create(UUID.randomUUID(), Volume.create(2).getValue(), Location.create(1, 1).getValue()).getValue();

        val result = assignment.complete(Location.create(9, 9).getValue());

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("out.of.completion.zone");
    }

    @Test
    void doesNotCompleteTwice() {
        val assignment = Assignment
                .create(UUID.randomUUID(), Volume.create(2).getValue(), Location.create(5, 5).getValue()).getValue();

        assignment.complete(Location.create(5, 5).getValue());
        val secondAttempt = assignment.complete(Location.create(5, 5).getValue());

        assertThat(secondAttempt.isFailure()).isTrue();
        assertThat(secondAttempt.getError().getCode()).isEqualTo("assignment.already.completed");
    }

    @Test
    void restoresAssignmentWithExistingId() {
        val id = UUID.randomUUID();
        val volume = Volume.create(2).getValue();
        val location = Location.create(3, 3).getValue();

        val result = Assignment.restore(id, UUID.randomUUID(), volume, location, Status.Completed);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getId()).isEqualTo(id);
        assertThat(result.getValue().getStatus()).isEqualTo(Status.Completed);
    }

    @Test
    void rejectsNullIdOnRestore() {
        val result = Assignment.restore(null, UUID.randomUUID(), Volume.create(2).getValue(),
                Location.create(3, 3).getValue(), Status.Assigned);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("value.is.required");
    }
}
