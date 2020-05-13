package com.blog.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.blog.model.Category;
import com.blog.model.Post;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {

	public List<Category> findByPosts(Optional<Post> category);
}
