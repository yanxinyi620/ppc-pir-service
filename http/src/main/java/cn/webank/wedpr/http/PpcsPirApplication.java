package cn.webank.wedpr.http;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableAutoConfiguration
@EnableTransactionManagement
public class PpcsPirApplication {
    public static void main(String[] args) {
        SpringApplication.run(PpcsPirApplication.class, args);
        System.out.println("Start PirApplication successfully!");
    }
}
