package com.blog.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blog.model.Comment;
import com.blog.model.Post;

public interface CommentsRepository extends JpaRepository<Comment, String> {

	public List<Comment> findByPostAndStatus(Optional<Post> post, String status);

	public List<Comment> findByStatus(String status);
}
