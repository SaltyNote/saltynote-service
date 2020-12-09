package net.hzhou.demo.jwt.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import net.hzhou.demo.jwt.entity.Blog;

public interface BlogRepository
    extends JpaRepository<Blog, Integer>, JpaSpecificationExecutor<Blog> {
  List<Blog> findAllByUserId(Integer userId);
}
