package com.blog.controller;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.blog.exeption.FileStorageException;
import com.blog.model.Category;
import com.blog.model.Comment;
import com.blog.model.Post;
import com.blog.model.User;
import com.blog.repository.CategoryRepository;
import com.blog.repository.CommentsRepository;
import com.blog.repository.PostRepository;
import com.blog.repository.UserRepository;
import com.blog.repository.priorityRepository;

@Controller
public class PostController {

	@Autowired
	PostRepository postRepository;
	@Autowired
	CategoryRepository categoryRepository;
	@Autowired
	CommentsRepository commentRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	priorityRepository priorityRepository;
	private static final DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	@PostMapping("/addNewPost")
	public String addNewPost(@Valid Post post, Model model, @RequestParam("postName") String postName, User user,
			@RequestParam("checkbox") List<String> checkCategory, @RequestParam("postSummary") String postSummary,
			MultipartFile file) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		user = userRepository.findByEmail(auth.getName());
		String fileName = StringUtils.cleanPath(file.getOriginalFilename());
		List<Category> categori = categoryRepository.findAllById(checkCategory);
		try {
			if (fileName.contains("..")) {
				throw new FileStorageException(fileName);
			}
			post = new Post(fileName, file.getContentType(), file.getBytes());
		} catch (IOException ex) {
			throw new FileStorageException(fileName, ex);
		}
		Date date = new Date();
		post.setPostCreated(sdf.format(date));
		post.setPostName(postName);
		post.setPostSummary(postSummary);
		post.addCategory(categori);
		post.setStatus("Pending");
		post.setUser(user);
		post.setPriority("Low");
		postRepository.save(post);
		return "redirect:/autorHome";
	}

	@RequestMapping("/openAuthorPost/{postId}")
	public String OpenPost(Model model, Post post, @PathVariable String postId) {
		post = postRepository.getOne(postId);
		model.addAttribute("postName", post.getPostName());
		model.addAttribute("postText", post.getPostText());
		model.addAttribute("postCreated", post.getPostCreated());
		model.addAttribute("postId", post.getPostId());

		return "openPost";
	}

	@RequestMapping("/openUserPost/{postId}")
	public String OpenUserPost(Model model, Post post, @PathVariable String postId) {

		post = postRepository.getOne(postId);
		List<Post> posts = postRepository.findAll();
		Optional<Post> posting = postRepository.findById(postId);
		List<Category> category = categoryRepository.findByPosts(posting);
		List<Comment> comment = commentRepository.findByPostAndStatus(posting, "Verified");

		model.addAttribute("comment", comment);
		model.addAttribute("categories", category);
		model.addAttribute("post", posts);
		model.addAttribute("postName", post.getPostName());
		model.addAttribute("postText", post.getPostText());
		model.addAttribute("postCreated", post.getPostCreated());
		model.addAttribute("postId", post.getPostId());
		return "openUserPost";
	}

	@RequestMapping(value = "/savePostText")
	public String savePostText(Model model, Post post, @RequestParam("postText") String text,
			@RequestParam("saveText") String postId) {
		post = postRepository.getOne(postId);
		post.setPostText(text);
		postRepository.save(post);

		return "redirect:/openAuthorPost/" + postId;
	}

	@RequestMapping("/pendingPosts")
	public String pendingPosts(Model model, User user) {

		List<Post> post = postRepository.findByStatus("Pending");
		model.addAttribute("pendingPost", post);

		return "pendingPosts";
	}

	@RequestMapping("/priority")
	public String priority(Model model, User user) {

		List<Post> post = postRepository.findByStatus("Verified");
		model.addAttribute("verifiedPosts", post);

		return "priority";
	}

	@RequestMapping("/pendingAuthorPosts")
	public String pendingAuthorPosts(Model model, User user) {
		List<Category> category = categoryRepository.findAll();

		List<Post> post = postRepository.findByStatus("Pending");
		model.addAttribute("pendingAuthorPost", post);
		model.addAttribute("categories", category);

		return "pendingAuthorPosts";
	}

	@RequestMapping("/verifiedPosts/{postId}")
	public String verifiedPosts(Model model, Post posts, @PathVariable String postId) {
		posts = postRepository.getOne(postId);
		posts.setStatus("Verified");
		postRepository.save(posts);

		return "redirect:/pendingPosts";
	}

	@RequestMapping("/cancelPosts/{postId}")
	public String cancelPosts(Model model, Post posts, @PathVariable String postId) {
		posts = postRepository.getOne(postId);
		posts.setStatus("Canceled");
		postRepository.save(posts);
		return "redirect:/pendingPosts";
	}

	@RequestMapping("/superHighPost/{postId}")
	public String superHighPost(Model model, Post posts, @PathVariable String postId) {
		posts = postRepository.getOne(postId);
		Post postSuperHigh = postRepository.findByPriorityAndStatus("SuperHighPost", "Verified");

		if (postSuperHigh != null) {
			postSuperHigh.setData(postSuperHigh.getData());
			postSuperHigh.setFileName(postSuperHigh.getFileName());
			postSuperHigh.setFileType(postSuperHigh.getFileType());
			postSuperHigh.setPostCreated(postSuperHigh.getPostCreated());
			postSuperHigh.setPostName(postSuperHigh.getPostName());
			postSuperHigh.setPostSummary(postSuperHigh.getPostSummary());
			postSuperHigh.setStatus(postSuperHigh.getStatus());
			postSuperHigh.setPostText(postSuperHigh.getPostText());
			postSuperHigh.setCategory(postSuperHigh.getCategory());
			posts.setPriority("SuperHighPost");
			postSuperHigh.setPriority("lowPost");
			postRepository.save(posts);
			postRepository.save(postSuperHigh);
		} else if (postSuperHigh == null) {
			posts.setPriority("SuperHighPost");
			postRepository.save(posts);
		} 
		return "redirect:/priority";
	}

	@RequestMapping("/highPost/{postId}")
	public String HighPost(Model model, Post posts, @PathVariable String postId) {
		posts = postRepository.getOne(postId);
		Post postsHigh = postRepository.findByPriorityAndStatus("highPost", "Verified");
		if (postsHigh != null) {
			postsHigh.setData(postsHigh.getData());
			postsHigh.setFileName(postsHigh.getFileName());
			postsHigh.setFileType(postsHigh.getFileType());
			postsHigh.setPostCreated(postsHigh.getPostCreated());
			postsHigh.setPostName(postsHigh.getPostName());
			postsHigh.setPostSummary(postsHigh.getPostSummary());
			postsHigh.setStatus(postsHigh.getStatus());
			postsHigh.setPostText(postsHigh.getPostText());
			postsHigh.setCategory(postsHigh.getCategory());
			posts.setPriority("highPost");
			postsHigh.setPriority("lowPost");
			postRepository.save(posts);
			postRepository.save(postsHigh);

		} else {
			posts.setPriority("highPost");
			postRepository.save(posts);
		}
		return "redirect:/priority";
	}

	@RequestMapping("/mediumPost/{postId}")
	public String mediumPost(Model model, Post posts, @PathVariable String postId) {
		posts = postRepository.getOne(postId);
		Post mediumPost = postRepository.findByPriorityAndStatus("mediumPost", "Verified");
		if (mediumPost != null) {
			mediumPost.setData(mediumPost.getData());
			mediumPost.setFileName(mediumPost.getFileName());
			mediumPost.setFileType(mediumPost.getFileType());
			mediumPost.setPostCreated(mediumPost.getPostCreated());
			mediumPost.setPostName(mediumPost.getPostName());
			mediumPost.setPostSummary(mediumPost.getPostSummary());
			mediumPost.setStatus(mediumPost.getStatus());
			mediumPost.setPostText(mediumPost.getPostText());
			mediumPost.setCategory(mediumPost.getCategory());
			posts.setPriority("mediumPost");
			mediumPost.setPriority("lowPost");
			postRepository.save(posts);
			postRepository.save(mediumPost);

		} else {
			posts.setPriority("mediumPost");
			postRepository.save(posts);
		}
		return "redirect:/priority";
	}

}
