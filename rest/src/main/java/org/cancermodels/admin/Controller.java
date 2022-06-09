package org.cancermodels.admin;

import org.cancermodels.DummyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {
  private DummyService dummyService;

  public Controller(DummyService dummyService) {
    this.dummyService = dummyService;
  }

  @GetMapping("/setup")
  public void setup() {
    dummyService.setup();
  }

  @GetMapping("/testLoad")
  public void testLoad() {
    dummyService.testLoad();
  }

}
