package com.netdatel.adminserviceapi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MailerSendResponse {

    @JsonProperty("message")
    private String message;

    @JsonProperty("message_id")
    private String messageId;

    @JsonProperty("errors")
    private Object errors;

    // Método helper para verificar éxito
    public boolean isSuccess() {
        return messageId != null && !messageId.isEmpty();
    }

    // Método helper para obtener mensaje de error
    public String getErrorMessage() {
        if (errors != null) {
            return errors.toString();
        }
        return message != null ? message : "Error desconocido";
    }
}