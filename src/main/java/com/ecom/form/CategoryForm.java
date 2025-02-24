package com.ecom.form;


import com.ecom.validators.ValidFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CategoryForm {

    private Integer id;

    @NotBlank(message = "Name is required")
    @Size(max = 30, message = "Category name must be less than 100 characters")
    private String name;

    private Boolean isActive;

    @ValidFile(message = "Invalid Image")
    @NotNull(message = "Image  is required")
    private MultipartFile imageName;

    private String image;

}

