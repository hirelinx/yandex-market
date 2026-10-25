package ru.yandex.practicum.market;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.yandex.practicum.market.config.AppProperties;
import ru.yandex.practicum.market.config.PayProperties;

@SpringBootApplication
@EnableConfigurationProperties({PayProperties.class, AppProperties.class})
public class MarketAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(MarketAppApplication.class, args);
	}

}
