package com.blog.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.blog.model.Category;
import com.blog.model.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, String> {

	public List<Post> findByStatus(String status);

	public Post findByPostName(String name);

	public List<Post> findByCategory(Category categoryPost);

	public List<Post> findByCategoryAndStatus(Optional<Category> category, String status);

	public Post findByPriorityAndStatus(String priorityHigh, String status);
}
