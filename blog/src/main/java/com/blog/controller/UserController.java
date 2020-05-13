package com.blog.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.blog.exeption.FileStorageException;
import com.blog.model.Category;
import com.blog.model.Post;
import com.blog.model.Priority;
import com.blog.model.Role;
import com.blog.model.User;
import com.blog.repository.CategoryRepository;
import com.blog.repository.PostRepository;
import com.blog.repository.RoleRepository;
import com.blog.repository.UserRepository;
import com.blog.repository.priorityRepository;
import com.blog.service.EmailService;
import com.blog.service.UserService;
import com.google.common.collect.ImmutableMap;

@Controller
public class UserController {

	@Autowired
	UserService userService;
	@Autowired
	UserRepository userRepository;
	@Autowired
	EmailService emailService;
	@Autowired
	RoleRepository roleRepository;
	@Autowired
	PostRepository postRepository;
	@Autowired
	CategoryRepository categoryRepository;
	@Autowired
	BCryptPasswordEncoder encoder;
	@Autowired
	priorityRepository priorityRepository;

	@RequestMapping(value = { "/", "/login" }, method = RequestMethod.GET)
	public ModelAndView login(@Valid User user) {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("login");

		return modelAndView;
	}

	@RequestMapping(value = "/logout")
	public String logoutPage(HttpServletRequest request, HttpServletResponse response) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			new SecurityContextLogoutHandler().logout(request, response, auth);
		}

		return "login";
	}

	@RequestMapping(value = "/home", method = RequestMethod.GET)
	public ModelAndView userHome(Model model, Post post, ModelAndView modelAndView) {

		modelAndView = new ModelAndView("home");
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User userAuth = userRepository.findByEmail(auth.getName());
		List<User> users = userRepository.findAll();
		Post superHighpost = postRepository.findByPriorityAndStatus("SuperHighPost", "Verified");
		Post highPost = postRepository.findByPriorityAndStatus("highPost", "Verified");
		Post mediumPost = postRepository.findByPriorityAndStatus("mediumPost", "Verified");
		List<Category> category = categoryRepository.findAll();
		List<Post> verifiedPosts = postRepository.findByStatus("Verified");
		model.addAttribute("categories", category);
		model.addAttribute("superHighPost", superHighpost);
		model.addAttribute("mediumPost", mediumPost);
		model.addAttribute("highPost", highPost);
		model.addAttribute("users", users);
		model.addAttribute("userAuth", userAuth);
		modelAndView.addObject("post", verifiedPosts);

		return modelAndView;
	}

	@RequestMapping(value = "/autorHome", method = RequestMethod.GET)
	public ModelAndView autorHome(Model model, Post post, ModelAndView modelAndView) {

		modelAndView = new ModelAndView("autorHome");
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User author = userRepository.findByEmail(auth.getName());
		System.out.println(author.getEmail());
		List<Post> postLow = postRepository.findByStatus("Verified");
		Post superHighpost = postRepository.findByPriorityAndStatus("SuperHighPost", "Verified");
		Post highPost = postRepository.findByPriorityAndStatus("highPost", "Verified");
		Post mediumPost = postRepository.findByPriorityAndStatus("mediumPost", "Verified");
		List<Category> category = categoryRepository.findAll();
		List<Priority> priorities = priorityRepository.findAll();
		model.addAttribute("priority", priorities);
		model.addAttribute("categories", category);
		model.addAttribute("author", author);
		model.addAttribute("superHighPost", superHighpost);
		model.addAttribute("highPost", highPost);
		model.addAttribute("mediumPost", mediumPost);
		modelAndView.addObject("post", postLow);

		return modelAndView;
	}

	@RequestMapping(value = "/adminHome", method = RequestMethod.GET)
	public ModelAndView adminHome(Model model, ModelAndView modelAndView) {

		modelAndView = new ModelAndView("adminHome");
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userRepository.findByEmail(auth.getName());
		modelAndView.addObject("user", user.getName());
		List<Category> category = categoryRepository.findAll();
		model.addAttribute("categories", category);

		return modelAndView;
	}

	@RequestMapping(value = "/register", method = RequestMethod.GET)
	public ModelAndView register() {
		ModelAndView modelAndView = new ModelAndView();
		User user = new User();
		modelAndView.addObject("user", user);
		modelAndView.setViewName("register");
		return modelAndView;
	}

	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public ModelAndView registerUser(@Valid User user, BindingResult bindingResult, MultipartFile file,
			ModelMap modelMap) throws IOException {
		ModelAndView modelAndView = new ModelAndView();
		String fileName = StringUtils.cleanPath(file.getOriginalFilename());
		// Check for the validations
		if (bindingResult.hasErrors()) {
			modelAndView.addObject("successMessage", "Please correct the errors in form!");
			modelMap.addAttribute("bindingResult", bindingResult);

		} else if (userService.isUserAlreadyPresent(user)) {
			modelAndView.addObject("successMessage", "user already exists!");
		}
		// we will save the user if, no binding errors
		else {

			// Create Token and Save
			user.setResetToken(UUID.randomUUID().toString().substring(0, 8));
			user.setFileName(fileName);
			user.setData(file.getBytes());
			user.setFileType(file.getContentType());
			userService.saveUser(user);
			if (fileName.contains("..")) {
				throw new FileStorageException(fileName);
			}
			user = new User(fileName, file.getContentType(), file.getBytes());

			modelAndView.addObject("successMessage", "User is registered successfully!");
		}
		modelAndView.addObject("user", new User());
		modelAndView.setViewName("register");
		return modelAndView;
	}

	@RequestMapping(value = "/forgotPassword", method = RequestMethod.GET)
	public ModelAndView displayResetPassword(ModelAndView modelAndView, User user) {
		modelAndView.addObject("user", user);
		modelAndView.setViewName("forgotPassword");
		return modelAndView;
	}

	// Receive the address and send an email
	@RequestMapping(value = "/forgotPassword", method = RequestMethod.POST)
	public ModelAndView forgotUserPassword(@RequestParam("email") String email, ModelAndView modelAndView, User user) {
		User existingUser = userRepository.findByEmail(email);
		if (existingUser != null) {

			// Create the email with verification token
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(existingUser.getEmail());
			mailMessage.setSubject("Complete Password Reset!");
			mailMessage.setFrom("arditmete2@gmail.com");
			mailMessage.setText("To complete the password reset process, please click here:" + "\n"
					+ "http://localhost:8080/reset?token=" + existingUser.getResetToken() + "\n"
					+ "Your verification token is:" + existingUser.getResetToken());

			// Send the email
			emailService.sendEmail(mailMessage);

			modelAndView.addObject("token", existingUser.getResetToken());
			return new ModelAndView("forgotPassword", ImmutableMap.of("message", "Check your email!"));
		} else {
			return new ModelAndView("forgotPassword", ImmutableMap.of("message", "Email is not valid!"));
		}
	}

	@RequestMapping(value = "/reset", method = RequestMethod.GET)
	public ModelAndView displayResetPasswordPage(ModelAndView modelAndView, @RequestParam("token") String token) {

		User user = userService.findUserByResetToken(token);

		if (user != null) { // Token found in DB

			modelAndView.setViewName("resetPassword");
		}
		return modelAndView;
	}

	@PostMapping("/reset")

	public ModelAndView savePassword(User user, Model model, @RequestParam("password") final String password,
			@RequestParam("passwordConfirmation") final String passwordConfirmation,
			@RequestParam("token") String token) {

		// Find the user by token
		user = userService.findUserByResetToken(token);
		if (user != null) {

			if (token == null || token == "") {
				return new ModelAndView("resetPassword", ImmutableMap.of("message", "Please enter token"));
			}
			if (user != null)
				if (password == null || password == "") {
					return new ModelAndView("resetPassword", ImmutableMap.of("message", "Please enter password"));
				}
			if (!password.equalsIgnoreCase(passwordConfirmation)) {
				return new ModelAndView("resetPassword", ImmutableMap.of("message", "Passwords do not match"));
			}

			userService.Save(user, password);
		} else {
			return new ModelAndView("resetPassword", ImmutableMap.of("message", "Token do not match"));
		}

		return new ModelAndView("resetPassword", ImmutableMap.of("message", "Password changed successfully!"));
	}

	@RequestMapping("/users")
	public String users(Model model) {
		List<User> user = userRepository.findAll();
		model.addAttribute("user", user);
		return "users";
	}

	@RequestMapping(value = "/controlUser", params = "authorId")
	public String createAutor(User user, @RequestParam("authorId") Integer autorId) {

		System.out.println(autorId);
		user = userRepository.getOne(autorId);
		Role userRole = roleRepository.findByRole("SITE_AUTOR");
		user.setRoles(new HashSet<Role>(Arrays.asList(userRole)));
		userRepository.save(user);
		return "redirect:/users";

	}

	@RequestMapping(value = "/controlUser", params = "adminId")
	public String createAdmin(User user, @RequestParam("adminId") Integer adminId) {

		System.out.println(adminId);
		user = userRepository.getOne(adminId);
		Role userRole = roleRepository.findByRole("SITE_ADMIN");
		user.setRoles(new HashSet<Role>(Arrays.asList(userRole)));
		userRepository.save(user);
		return "redirect:/users";

	}

	@RequestMapping(value = "/controlUser", params = "userId")
	public String createUser(User user, @RequestParam("userId") Integer userId) {

		System.out.println(userId);
		user = userRepository.getOne(userId);
		Role userRole = roleRepository.findByRole("SITE_USER");
		user.setRoles(new HashSet<Role>(Arrays.asList(userRole)));
		userRepository.save(user);
		return "redirect:/users";
	}

	@RequestMapping(value = "/myProfile")
	public String profile(User user, Model model) {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User users = userRepository.findByEmail(auth.getName());
		users = userRepository.getOne(users.getUserId());
		List<Category> category = categoryRepository.findAll();
		model.addAttribute("categories", category);
		model.addAttribute("user", users);
		model.addAttribute("name", users.getName());
		model.addAttribute("lastName", users.getLastName());
		model.addAttribute("email", users.getEmail());
		model.addAttribute("Id", users.getUserId());

		return "profile";

	}

	@RequestMapping(value = "/editProfile")
	public String editprofile(User users, Model model, @RequestParam("email") String email,
			@RequestParam("lastName") String lastname, @RequestParam("name") String name) throws NullPointerException {
		System.out.println(email);
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		users = userRepository.findByEmail(auth.getName());
		users = userRepository.getOne(users.getUserId());
		users.setLastName(lastname);
		users.setName(name);
		users.setEmail(email);
		userRepository.save(users);
		return "redirect:/login";
	}

	@RequestMapping(value = "/changePassword")
	public String passwordPage(User users, Model model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		users = userRepository.findByEmail(auth.getName());
		users = userRepository.getOne(users.getUserId());
		model.addAttribute("userPass", users.getPassword());

		return "newPassword";
	}

	@PostMapping("/updatePassword")

	public ModelAndView savePasswosrd(User users, Model model, @RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword,
			@RequestParam("confirmNewPassword") String confirmNewPassword) {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		users = userRepository.findByEmail(auth.getName());
		users = userRepository.getOne(users.getUserId());
		// Find the user by token
		if (users != null) {

			if (oldPassword == null || oldPassword == "") {
				return new ModelAndView("newPassword", ImmutableMap.of("message", "Please enter old password"));
			}
			if (users != null)
				if (newPassword == null || newPassword == "") {
					return new ModelAndView("newPassword", ImmutableMap.of("message", "Please enter new password"));
				}
			if (!newPassword.equalsIgnoreCase(confirmNewPassword)) {
				return new ModelAndView("newPassword", ImmutableMap.of("message", "Passwords do not match"));
			}

			userService.Save(users, newPassword);
		} else {
			return new ModelAndView("newPassword", ImmutableMap.of("message", "User do not match"));
		}

		return new ModelAndView("redirect:/myProfile");
	}
}
