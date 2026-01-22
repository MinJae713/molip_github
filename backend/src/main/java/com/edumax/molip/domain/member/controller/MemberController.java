package com.edumax.molip.domain.member.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/members")
public class MemberController {
  @GetMapping("/tst")
  public String testAPI() {
    return "헬로 월도";
  }
}
