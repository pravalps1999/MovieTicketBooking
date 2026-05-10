package com.movie.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfiguration {
    @Bean
    public JavaMailSenderImpl getJavaMailSenderImpl(){
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        //SMTP SERVER DETAILS (Gmail)
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);

        //Replace with your email
        mailSender.setUsername("your_email");

        //Use App Password (NOT normal password)
        mailSender.setPassword("your_password");

        //PROPERTIES
        Properties props = mailSender.getJavaMailProperties();

        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        // Optional but useful
        props.put("mail.debug", "true");

        return mailSender;
    }
}
