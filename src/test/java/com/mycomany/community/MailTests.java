package com.mycomany.community;

import com.mycomany.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {

   @Autowired
    private MailClient mailClient;

   @Autowired
   private TemplateEngine templateEgine;

   @Test
    public void testTextMail(){
       mailClient.sendMail("huangxin2525074@gmail.com","Test","Welcome!");
   }

   @Test
    public void testHtmlEmail(){
       Context context = new Context();
       context.setVariable("username","sunday");

      String contents = templateEgine.process("/mail/demo",context);
      System.out.println(contents);

       mailClient.sendMail("huangxin2525074@gmail.com","html",contents);
   }


}
