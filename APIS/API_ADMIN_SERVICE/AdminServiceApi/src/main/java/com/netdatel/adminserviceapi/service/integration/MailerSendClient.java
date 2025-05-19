package com.netdatel.adminserviceapi.service.integration;

import com.netdatel.adminserviceapi.dto.external.EmailRequest;
import com.netdatel.adminserviceapi.dto.external.EmailResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "mailer-send", url = "${app.services.mailer-send-url}")
public interface MailerSendClient {

    @PostMapping("/api/email")
    EmailResponse sendEmail(@RequestBody EmailRequest request);
}