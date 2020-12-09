package net.hzhou.demo.jwt.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import net.hzhou.demo.jwt.domain.JwtUser;
import net.hzhou.demo.jwt.entity.Blog;

@RestController
@Slf4j
public class BlogController {

  @GetMapping("/blog")
  public ResponseEntity<Blog> getTestBlog(Authentication auth) {
    JwtUser user = (JwtUser) auth.getPrincipal();
    log.info("JwtUser = {}", user);
    Blog blog = new Blog().setContent("content").setTitle("title").setUserId(1);
    return ResponseEntity.ok(blog);
  }
}
