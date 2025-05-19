package com.netdatel.identityserviceapi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

@Slf4j
@SpringBootApplication
@EnableScheduling
@EntityScan("com.netdatel.identityserviceapi.domain.entity")
@EnableJpaRepositories("com.netdatel.identityserviceapi.repository")
public class IdentityServiceApiApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(IdentityServiceApiApplication.class);
        Environment env = app.run(args).getEnvironment();
        logApplicationStartup(env);
    }

    private static void logApplicationStartup(Environment env) {
        String protocol = env.getProperty("server.ssl.enabled", "false").equals("true") ? "https" : "http";
        String serverPort = env.getProperty("server.port");
        String contextPath = env.getProperty("server.servlet.context-path", "/");
        if (!contextPath.startsWith("/")) {
            contextPath = "/" + contextPath;
        }
        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("Could not determine host address", e);
        }
        log.info("""
                ----------------------------------------------------------
                Application '{}' is running! Access URLs:
                Local:          {}://localhost:{}{}
                External:       {}://{}:{}{}
                Profile(s):     {}
                ----------------------------------------------------------""",
                env.getProperty("spring.application.name"),
                protocol,
                serverPort,
                contextPath,
                protocol,
                hostAddress,
                serverPort,
                contextPath,
                Arrays.toString(env.getActiveProfiles())
        );
    }
}
