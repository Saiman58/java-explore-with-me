package ru.practicum.explorewithme.server.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ApiError {
    private String status;
    private String reason;
    private String message;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private String timestamp;

    private List<String> errors;
}
