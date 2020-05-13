package com.blog.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.blog.model.Category;
import com.blog.model.Comment;
import com.blog.model.Post;
import com.blog.model.User;
import com.blog.repository.CategoryRepository;
import com.blog.repository.CommentsRepository;
import com.blog.repository.PostRepository;
import com.blog.repository.UserRepository;

@Controller
public class CommentController {
	@Autowired
	CommentsRepository commentRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	PostRepository postRepository;
	@Autowired
	CategoryRepository categoryRepository;

	@RequestMapping("/addComment")
	public String addComment(Model model, Comment comment, @RequestParam("commentText") String commentText,
			@RequestParam("postId") String postId) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userRepository.findByEmail(auth.getName());
		Post post = postRepository.getOne(postId);
		if (!commentText.isEmpty()) {
			comment.setPost(post);
			comment.setUser(user);
			comment.setStatus("Pending");
			comment.setCommentText(commentText);
			commentRepository.save(comment);
			return "redirect:/openUserPost/" + postId;
		} else {
			return "redirect:/openUserPost/" + postId;
		}
	}

	@RequestMapping("/pendingComments")
	public String pendingAuthorPosts(Model model, User user) {
		List<Category> category = categoryRepository.findAll();

		List<Comment> comment = commentRepository.findByStatus("Pending");
		model.addAttribute("pendingComments", comment);
		model.addAttribute("categories", category);

		return "pendingComments";
	}

	@RequestMapping("/verifiedComments/{commentId}")
	public String verifiedPosts(Model model, Comment comment, @PathVariable("commentId") String commentId) {
		comment = commentRepository.getOne(commentId);
		comment.setStatus("Verified");
		commentRepository.save(comment);

		return "redirect:/pendingComments";
	}

	@RequestMapping("/cancelComments/{commentId}")
	public String cancelPosts(Model model, Comment comment, @PathVariable("commentId") String commentId) {
		comment = commentRepository.getOne(commentId);
		comment.setStatus("Cancel");
		commentRepository.save(comment);
		return "redirect:/pendingComments";
	}
}
