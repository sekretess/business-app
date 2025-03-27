package io.sekretess.consumer;

import io.sekretess.service.SekretessBusinessService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQConsumer {

    private final SekretessBusinessService sekretessBusinessService;
    private final String queueName;

    public RabbitMQConsumer(SekretessBusinessService sekretessBusinessService,
                            String queueName) {
        this.sekretessBusinessService = sekretessBusinessService;
        this.queueName = queueName;
    }


    @RabbitListener(queues = "#{@queueName}")
    public void listen(String message) {
        String processedMessage = message.replaceAll("^\"|\"$", "");
        sekretessBusinessService.sendSenderKeyDistributionMessage(processedMessage);
    }
}
