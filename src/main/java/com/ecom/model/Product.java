package com.ecom.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@NotBlank(message = "Title is required")
	@Size(max = 500, message = "Title must not exceed 500 characters")
	private String title;

	@NotBlank(message = "Description is required")
	@Size(max = 5000, message = "Description must not exceed 5000 characters")
	private String description;

	@NotBlank(message = "Category is required")
	private String category;

	@NotNull(message = "Price is required")
	@Positive(message = "Price must be positive")
	private Double price;

	@NotNull(message = "Stock is required")
	@Min(value = 0, message = "Stock cannot be negative")
	private int stock;

	@Min(value = 0, message = "Discount cannot be negative")
	@Max(value = 100, message = "Discount cannot exceed 100%")
	private int discount;

	@NotNull(message = "Discount price is required")
	private Double discountPrice;

	@NotNull(message = "Active status is required")
	private Boolean isActive;

	private String cloudinaryImagePublicId;

	private String image;
}