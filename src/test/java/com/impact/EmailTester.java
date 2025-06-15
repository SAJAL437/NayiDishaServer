package com.impact;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.impact.services.EmailServices;

@SpringBootTest
public class EmailTester {


    @Autowired
    private EmailServices services;

    // @Test
    // public void testEmail() {

    //     services.sendEmail("sajaltiwari437@gmail.com",
    //                     "Testing Email", 
    //                     "This Email is from SmartPhoneBook");

    // }



}
