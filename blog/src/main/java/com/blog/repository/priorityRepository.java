package com.blog.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blog.model.Priority;

public interface priorityRepository extends JpaRepository<Priority, Integer> {

	public Optional<Priority> findByPriorityName(String priorityName);
}
