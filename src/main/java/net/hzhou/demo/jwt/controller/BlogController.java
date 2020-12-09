package net.hzhou.demo.jwt.controller;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import net.hzhou.demo.jwt.domain.JwtUser;
import net.hzhou.demo.jwt.entity.Blog;
import net.hzhou.demo.jwt.repository.BlogRepository;

@RestController
@Slf4j
public class BlogController {
  @Autowired private BlogRepository blogRepository;

  @GetMapping("/blog/{id}")
  public ResponseEntity<Blog> getBlog(@PathVariable("id") Integer id) {
    Optional<Blog> blog = blogRepository.findById(id);
    return blog.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping("/blogs")
  public List<Blog> getBlogs(Authentication auth) {
    JwtUser user = (JwtUser) auth.getPrincipal();
    return blogRepository.findAllByUserId(user.getId());
  }

  @PostMapping("/blog")
  public ResponseEntity<Blog> createBlog(@Valid @RequestBody Blog blog, Authentication auth) {
    JwtUser user = (JwtUser) auth.getPrincipal();
    blog.setUserId(user.getId());
    blog = blogRepository.save(blog);
    if (blog.getId() > 0) {
      return ResponseEntity.ok(blog);
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
