package com.ecom.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Category {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@NotBlank(message = "Category name is required")
	@Size(min = 2, max = 50, message = "Category name must be between 2 and 50 characters")
	private String name;

//	@NotBlank(message = "Image name is required")
	private String imageName;

	@NotNull(message = "Active status is required")
	private Boolean isActive;

	private String cloudinaryImagePublicId;

	// Getters and setters
}
