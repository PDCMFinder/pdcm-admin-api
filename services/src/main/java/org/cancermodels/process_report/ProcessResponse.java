package org.cancermodels.process_report;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ProcessResponse {
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
  private LocalDateTime timestamp;
  private Map<String, String> response;

  private ProcessResponse() {
    timestamp = LocalDateTime.now();
  }

  public ProcessResponse(Map<String, String> response) {
    this();
    this.response = response;
  }

  public ProcessResponse(String message) {
    this();
    Map<String, String> response = new HashMap<>();
    response.put("message", message);
    this.response = response;
  }
}
