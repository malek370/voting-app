//package com.voting.votingapp.controllers;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.core.io.Resource;
//import org.springframework.core.io.ResourceLoader;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//
//@Controller
//public class SpaController {
//
//    @RequestMapping(value = "/")
//    public ResponseEntity<Resource> index() {
//        return serveIndex();
//    }
//
//    // Handle single-level paths (not starting with api or static)
//    @RequestMapping(value = "/{path:^(?!api|static).*}")
//    public ResponseEntity<Resource> spaLevel1() {
//        return serveIndex();
//    }
//
//    // Handle multi-level paths - this is the problematic pattern, so we'll use multiple specific ones
//    @RequestMapping(value = "/dashboard/**")
//    public ResponseEntity<Resource> dashboard() {
//        return serveIndex();
//    }
//
//    @RequestMapping(value = "/profile/**")
//    public ResponseEntity<Resource> profile() {
//        return serveIndex();
//    }
//
//    @RequestMapping(value = "/settings/**")
//    public ResponseEntity<Resource> settings() {
//        return serveIndex();
//    }
//
//    // Add more specific routes as needed for your Angular routes
//
//    private ResponseEntity<Resource> serveIndex() {
//        Resource resource = new ClassPathResource("static/browser/index.html");
//        return ResponseEntity.ok()
//                .contentType(MediaType.TEXT_HTML)
//                .body(resource);
//    }
//}