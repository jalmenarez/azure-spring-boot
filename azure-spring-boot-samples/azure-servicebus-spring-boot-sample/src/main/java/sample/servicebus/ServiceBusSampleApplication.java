/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package sample.servicebus;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.ServiceBusMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Optional;

@SpringBootApplication
public class ServiceBusSampleApplication implements CommandLineRunner {

    @Autowired
    @Qualifier("queueSenderClient")
    private ServiceBusSenderClient queueSenderClient;

    @Autowired
    @Qualifier("topicSenderClient")
    private ServiceBusSenderClient topicSenderClient;

    @Autowired
    @Qualifier("queueReceiverClient")
    private ServiceBusReceiverClient queueReceiverClient;

    @Autowired
    @Qualifier("subscriptionReceiverClient")
    private ServiceBusReceiverClient subscriptionReceiverClient;

    public static void main(final String[] args) {
        SpringApplication.run(ServiceBusSampleApplication.class);
    }

    public void run(final String... var1) {
        sendQueueMessage();
        receiveQueueMessage();

        sendTopicMessage();
        receiveSubscriptionMessage();
    }

    // NOTE: Please be noted that below are the minimum code for demonstrating the usage of autowired clients.
    // For complete documentation of Service Bus, reference https://azure.microsoft.com/en-us/services/service-bus/
    private void sendQueueMessage() {
        final String messageBody = "queue message";
        System.out.println("Sending message: " + messageBody);
        queueSenderClient.sendMessage(new ServiceBusMessage(messageBody));
    }

    private void receiveQueueMessage() {
        final Optional<ServiceBusReceivedMessage> message = queueReceiverClient.receiveMessages(1)
                .stream().findFirst();
        message.ifPresent(m -> {
            System.out.println("Received message: " + m.getBody().toString());
            queueReceiverClient.complete(m);
        });
    }

    private void sendTopicMessage() {
        final String messageBody = "topic message";
        System.out.println("Sending message: " + messageBody);
        topicSenderClient.sendMessage(new ServiceBusMessage(messageBody));
    }

    private void receiveSubscriptionMessage() {
        final Optional<ServiceBusReceivedMessage> message = subscriptionReceiverClient.receiveMessages(1)
                .stream().findFirst();
        message.ifPresent(m -> {
            System.out.println("Received message: " + m.getBody().toString());
            subscriptionReceiverClient.complete(m);
        });
    }
}
