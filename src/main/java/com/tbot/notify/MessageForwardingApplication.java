package com.tbot.notify;

import com.tbot.notify.config.NotifyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(NotifyProperties.class)
public class MessageForwardingApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessageForwardingApplication.class, args);
    }
}
