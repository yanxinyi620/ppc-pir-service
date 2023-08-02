package cn.webank.wedpr.http.message;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientJobResponse {
    private int code;
    private String message;
    private Object data;

    public static ClientJobResponse successResponse() {
        return builder().code(0).message("success").build();
    }

    public static ClientJobResponse failureResponse(int errorCode, String message) {
        return builder().code(errorCode).message(message).build();
    }
}
