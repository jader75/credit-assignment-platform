package br.com.srm.credit.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "br.com.srm.credit")
public class CreditEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreditEngineApplication.class, args);
    }
}
