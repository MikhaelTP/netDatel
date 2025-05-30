package com.netdatel.adminserviceapi.dto.external;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.netdatel.adminserviceapi.dto.response.MailerSendResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmailResponse {
    private Boolean success;
    private String messageId;
    private String error;

    // Constructor desde MailerSendResponse
    public static EmailResponse fromMailerSend(MailerSendResponse mailerSendResponse) {
        return EmailResponse.builder()
                .success(mailerSendResponse.isSuccess())
                .messageId(mailerSendResponse.getMessageId())
                .error(mailerSendResponse.isSuccess() ? null : mailerSendResponse.getErrorMessage())
                .build();
    }
}