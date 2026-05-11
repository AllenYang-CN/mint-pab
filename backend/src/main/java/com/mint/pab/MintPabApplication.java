package com.mint.pab;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.mint.pab.repository")
public class MintPabApplication {

    public static void main(String[] args) {
        SpringApplication.run(MintPabApplication.class, args);
    }

}
