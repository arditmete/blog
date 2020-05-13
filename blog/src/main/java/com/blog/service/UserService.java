package com.blog.service;

import com.blog.model.User;

public interface UserService {

	public void saveUser(User user);

	public boolean isUserAlreadyPresent(User user);

	public User findUserByResetToken(String resetToken);

	public User findByEmail(String email);

	public void Save(User user, String password);

}
