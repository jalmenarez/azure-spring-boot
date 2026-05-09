/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package sample.cloudfoundry.servicebus;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.ServiceBusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@RestController
public class ServiceBusRestController {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceBusRestController.class);
    private static final String CR = "</BR>";

    @Autowired
    @Qualifier("queueSenderClient")
    private ServiceBusSenderClient queueSenderClient;

    @Autowired
    @Qualifier("queueReceiverClient")
    private ServiceBusReceiverClient queueReceiverClient;

    @RequestMapping(value = "/sb", method = RequestMethod.GET)
    @ResponseBody
    public String processMessages(final HttpServletResponse response) {
        final StringBuilder result = new StringBuilder();
        result.append("starting...").append(CR);
        try {
            result.append("sending queue message").append(CR);
            sendQueueMessage();

            result.append("receiving queue message").append(CR);
            Thread.sleep(2000);
            receiveQueueMessage(result);

            result.append("done!").append(CR);
        } catch (InterruptedException e) {
            LOG.error("Error processing messages", e);
            Thread.currentThread().interrupt();
        }
        return result.toString();
    }

    private void sendQueueMessage() {
        final String messageBody = "queue message";
        LOG.debug("Sending message: {}", messageBody);
        queueSenderClient.sendMessage(new ServiceBusMessage(messageBody));
    }

    private void receiveQueueMessage(final StringBuilder result) {
        final Optional<ServiceBusReceivedMessage> message = queueReceiverClient.receiveMessages(1)
                .stream().findFirst();
        message.ifPresent(m -> {
            final String body = m.getBody().toString();
            LOG.debug("Received message: {}", body);
            result.append("Received message: ").append(body).append(CR);
            queueReceiverClient.complete(m);
        });
    }
}
