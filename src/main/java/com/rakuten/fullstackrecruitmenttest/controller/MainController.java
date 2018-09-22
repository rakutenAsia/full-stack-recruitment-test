package com.rakuten.fullstackrecruitmenttest.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

  @GetMapping("/test")
  @CrossOrigin(origins="*")
  public String testMethod() {
    return "test";
  }
}
