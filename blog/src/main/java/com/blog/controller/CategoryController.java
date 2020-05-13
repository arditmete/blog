package com.blog.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.blog.model.Category;
import com.blog.model.Post;
import com.blog.repository.CategoryRepository;
import com.blog.repository.PostRepository;


@Controller
public class CategoryController {

	@Autowired
	CategoryRepository categoryRepository;
	@Autowired
	PostRepository postRepository;

	@PostMapping("/addNewCategory")
	public String addNew(Category category, @RequestParam("categoryName") String categoryName) {

		categoryRepository.save(category);
		return "redirect:/adminHome";
	}

	@RequestMapping("/openCategory/{Id}")
	public String openCategory(Model model, @PathVariable("Id") String categoryId) {

		System.out.println(categoryId);
		List<Category> categories = categoryRepository.findAll();
		Optional<Category> category = categoryRepository.findById(categoryId);
		List<Post> post = postRepository.findByCategoryAndStatus(category, "Verified");
		System.out.println(post);
		model.addAttribute("categories", categories);
		model.addAttribute("postEachCategory", post);
		return "openCategory";
	}

	@RequestMapping("/openAuthorCategory/{Id}")
	public String openAuthorCategory(Model model, @PathVariable("Id") String categoryId) {

		System.out.println(categoryId);
		List<Category> categories = categoryRepository.findAll();
		Optional<Category> category = categoryRepository.findById(categoryId);
		List<Post> post = postRepository.findByCategoryAndStatus(category, "Verified");
		System.out.println(post);
		model.addAttribute("categories", categories);
		model.addAttribute("postEachAuthorCategory", post);
		return "openAuthorCategory";
	}

	@RequestMapping("/deleteCategory")
	public String deleteCategory(Model model, @RequestParam("trash") String categoryId) {

		Category categoryPost = categoryRepository.getOne(categoryId);
		List<Post> posts = postRepository.findByCategory(categoryPost);
		System.out.println(posts);
		categoryPost.remove();
		categoryRepository.delete(categoryPost);
		postRepository.deleteAll(posts);

		return "redirect:/adminHome";
	}

	@RequestMapping(value = "/edit/{categoryId}")
	public String editCategoryPage(Model model, Category category, @PathVariable String categoryId) {

		category = categoryRepository.getOne(categoryId);
		model.addAttribute("categoryId", categoryId);
		model.addAttribute("category", category);

		return "editCategory";
	}
//
	@RequestMapping(value = "/edit/{categoryId}", params = "update")
	public String updateCategory(Category category, @RequestParam("categoryEditName") String categoryEditName,
			@RequestParam("update") String categoryId) {
		category = categoryRepository.getOne(categoryId);
		category.setCategoryName(categoryEditName);
		categoryRepository.save(category);
		return "redirect:/adminHome";
	}

}
