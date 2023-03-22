package ru.sorokinkv.ocrservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class OcrServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OcrServiceApplication.class, args);
	}

//	@EventListener(ApplicationStartedEvent.class)
//	public void CommandLineRunnerBean() {
//		KafkaCustomConsumerImpl kafkaCustomConsumer =new KafkaCustomConsumerImpl();
//		kafkaCustomConsumer.runAfterStartup();
//	}
}
