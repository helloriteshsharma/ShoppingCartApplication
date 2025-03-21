package com.ecom.form;

import com.ecom.validators.ValidFile;
import org.springframework.web.multipart.MultipartFile;


import com.ecom.validators.ValidFile;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProfileUpdateForm {

    private Integer id;

    private String name;

    private String mobileNumber;

    private String email;

    private String address;

    private String city;

    private String state;

    private String pincode;

    private String profileImage;

    @ValidFile(message = "Invalid File")
    private MultipartFile userImage;

}
