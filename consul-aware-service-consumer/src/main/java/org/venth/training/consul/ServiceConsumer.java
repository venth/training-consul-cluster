package org.venth.training.consul;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author Venth on 17/10/2016
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableCircuitBreaker
@RestController
public class ServiceConsumer {

    @Bean
    @LoadBalanced
    RestTemplate restTemplate(){
        return new RestTemplate();
    }

    @Autowired
    RestTemplate restTemplate;

    @RequestMapping("/health")
    public String health() {
        return "OK";
    }

    @HystrixCommand(fallbackMethod = "serviceNotFoundResponse")
    @RequestMapping("/called")
    public String callSpringCloudService() {
        return restTemplate.getForObject("http://consule-aware-service/home", String.class);
    }

    protected String serviceNotFoundResponse() {
        return "CANNOT FIND SERVICE";
    }


    public static void main(String[] args) {
        new SpringApplicationBuilder(ServiceConsumer.class).web(true).run(args);
    }

}