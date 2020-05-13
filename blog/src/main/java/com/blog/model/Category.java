package com.blog.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "category")
public class Category {

	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "categoryId")
	private String categoryId;
	@Column(name = "categoryname")
	private String categoryName;
	@Column(name = "sorting")
	private int sorting;

	@ManyToMany(cascade = {  CascadeType.MERGE, CascadeType.DETACH })
	@JoinTable(name = "category_post", joinColumns = @JoinColumn(name = "categoryId"), inverseJoinColumns = @JoinColumn(name = "postId"))
	private List<Post> posts = new ArrayList<Post>();

	public void removePosts(Post post) {
		posts.remove(post);
		post.getCategory().remove(this);
	}

	public void remove() {
		for (Post post : new ArrayList<>(posts)) {
			removePosts(post);
		}
	}

	public Category() {
	}

	public int getSorting() {
		return sorting;
	}

	public void setSorting(int sorting) {
		this.sorting = sorting;
	}

	public List<Post> getPosts() {
		return posts;
	}

	public void setPosts(List<Post> posts) {
		this.posts = posts;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

}
