package microarch.delivery.adapters.in.scheduler;

import lombok.AllArgsConstructor;
import lombok.val;
import microarch.delivery.core.application.command.AssignOrderCommand;
import microarch.delivery.core.application.command.handler.AssignOrderHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AssignOrdersJob {

    private final AssignOrderHandler assignOrderHandler;

    @Scheduled(fixedRate = 1000)
    public void run() {
        val result = assignOrderHandler.handle(new AssignOrderCommand());
        if (result.isFailure()) {
            System.out.println("Assign failed: " + result.getError().serialize());
        }
    }
}
