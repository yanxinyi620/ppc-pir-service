package cn.webank.wedpr.http;

import cn.webank.wedpr.pir.service.ClientOTService;
import cn.webank.wedpr.pir.service.ServerOTService;
import cn.webank.wedpr.pir.service.ClientDecryptService;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableAutoConfiguration
@EnableTransactionManagement
@ComponentScan(
        basePackageClasses = {
            PpcsPirApplication.class,
            ClientOTService.class,
            ServerOTService.class,
            ClientDecryptService.class
        })
// @ComponentScan(basePackages = {"cn.webank.wedpr.http", "cn.webank.wedpr.pir"})
@MapperScan("cn.webank.wedpr.pir.mapper")
@EntityScan(basePackages = {"cn.webank.wedpr.http", "cn.webank.wedpr.pir"})
public class PpcsPirApplication {
    public static void main(String[] args) {
        SpringApplication.run(PpcsPirApplication.class, args);
        System.out.println("Start PirApplication successfully!");
    }
}
