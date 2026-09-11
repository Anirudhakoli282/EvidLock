package com.evidlock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ServletComponentScan
@EnableScheduling
public class EvidLockApplication {

    public static void main(String[] args) {
        SpringApplication.run(EvidLockApplication.class, args);
        System.out.println("==================================================================");
        System.out.println("  EVIDLOCK: Digital Evidence Chain-of-Custody System Initialized  ");
        System.out.println("  Cryptographic Hash Chains Active | Legal Hold Enforced          ");
        System.out.println("==================================================================");
    }
}
