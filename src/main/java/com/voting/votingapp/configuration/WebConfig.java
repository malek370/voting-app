//package com.voting.votingapp.configuration;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
//import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//public class WebConfig implements WebMvcConfigurer {
//
//    @Override
//    public void addViewControllers(ViewControllerRegistry registry) {
//        // This will serve index.html for Angular routes, but let Angular handle the routing
//        registry.addViewController("/")
//                .setViewName("redirect:/index.html");
//    }
//
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        // Serve all static files from browser directory
//        registry.addResourceHandler("/**")
//                .addResourceLocations("classpath:/static/browser/")
//                .resourceChain(true);
//    }
//}