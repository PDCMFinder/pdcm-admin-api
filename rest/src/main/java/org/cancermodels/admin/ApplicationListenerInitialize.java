package org.cancermodels.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ApplicationListenerInitialize
    implements ApplicationListener<ApplicationReadyEvent> {

  public void onApplicationEvent(ApplicationReadyEvent event) {
    log.info("I waited until Spring Boot finished before getting here!");
  }
}
