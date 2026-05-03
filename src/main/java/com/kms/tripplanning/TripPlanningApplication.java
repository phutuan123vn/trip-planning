package com.kms.tripplanning;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

import com.kms.tripplanning.utils.BaseRepositoryFactoryBean;
import com.kms.tripplanning.utils.Impl.BaseRepositoryImpl;

@EnableAsync
@SpringBootApplication
@EnableJpaRepositories(
    basePackages = "com.kms.tripplanning.repository",
    repositoryBaseClass = BaseRepositoryImpl.class,
    repositoryFactoryBeanClass = BaseRepositoryFactoryBean.class
)
public class TripPlanningApplication {

    public static void main(String[] args) {
        Dotenv.configure()
                .filename(".env")
                .systemProperties()
                .load();

        var app = new SpringApplication(TripPlanningApplication.class);
        app.run(args);
    }

}
