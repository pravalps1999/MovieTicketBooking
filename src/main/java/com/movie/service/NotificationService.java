package com.movie.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.movie.resource.TicketMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    @Autowired
    JavaMailSenderImpl javaMailSender;
    public void sendNotification(TicketMessage message){
        try{
            sendEmailToUser(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try{
            sendSMSToUser(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void sendEmailToUser(TicketMessage message) throws JsonProcessingException {
        SimpleMailMessage mailMessage=new SimpleMailMessage();
        mailMessage.setTo("rajputshivani2611@gmail.com");
        mailMessage.setSubject("Your_Movie_Ticket");
        mailMessage.setText(message.toString());
        javaMailSender.send(mailMessage);
    }
    public void sendSMSToUser(TicketMessage message){
    }
}
