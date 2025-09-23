package com.ftn.pki.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("api/admin")
public class DemoController {

    @PostMapping("/certificates")
    @ResponseBody
    public String hello() {
        return "Hello from admin controller!";
    }


}
