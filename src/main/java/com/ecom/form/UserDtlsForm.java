package com.ecom.form;

import com.ecom.validators.ValidFile;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserDtlsForm {
    private Integer id;

    @NotBlank(message = "Full Name is required")
    private String name;

    @NotBlank(message = "Mobile Number is required")
    private String mobileNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid Email format")
    private String email;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "Invalid Pincode")
    private String pincode;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Confirm Password is required")
    private String cpassword;

    private String profileImage;

    @ValidFile(message = "Invalid File")
    private MultipartFile userImage;

    // Ensure password and confirm password match
    public boolean isPasswordMatching() {
        return this.password != null && this.password.equals(this.cpassword);
    }
}
