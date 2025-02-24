package com.ecom.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collector;

import com.ecom.form.UserDtlsForm;
import com.ecom.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.model.UserDtls;
import com.ecom.util.CommonUtil;

import io.micrometer.common.util.StringUtils;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private ProductService productService;

	@Autowired
	private UserService userService;

	@Autowired
	private CommonUtil commonUtil;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private CartService cartService;

	@Autowired
	private ImageService imageService;

	@ModelAttribute
	public void getUserDetails(Principal p, Model m) {
		if (p != null) {
			String email = p.getName();
			UserDtls userDtls = userService.getUserByEmail(email);
			m.addAttribute("user", userDtls);
			Integer countCart = cartService.getCountCart(userDtls.getId());
			m.addAttribute("countCart", countCart);
		}

		List<Category> allActiveCategory = categoryService.getAllActiveCategory();
		m.addAttribute("categorys", allActiveCategory);
	}

	@GetMapping("/")
	public String index(Model m) {

		List<Category> allActiveCategory = categoryService.getAllActiveCategory().stream()
				.sorted((c1, c2) -> c2.getId().compareTo(c1.getId())).limit(6).toList();
		List<Product> allActiveProducts = productService.getAllActiveProducts("").stream()
				.sorted((p1, p2) -> p2.getId().compareTo(p1.getId())).limit(8).toList();
		m.addAttribute("category", allActiveCategory);
		m.addAttribute("products", allActiveProducts);
		return "index";
	}

	@GetMapping("/signin")
	public String login() {
		return "login";
	}

	@GetMapping("/register")
	public String register() {
		return "register";
	}

	@GetMapping("/products")
	public String products(Model m, @RequestParam(value = "category", defaultValue = "") String category,
			@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "12") Integer pageSize,
			@RequestParam(defaultValue = "") String ch) {

		List<Category> categories = categoryService.getAllActiveCategory();
		m.addAttribute("paramValue", category);
		m.addAttribute("categories", categories);

//		List<Product> products = productService.getAllActiveProducts(category);
//		m.addAttribute("products", products);
		Page<Product> page = null;
		if (StringUtils.isEmpty(ch)) {
			page = productService.getAllActiveProductPagination(pageNo, pageSize, category);
		} else {
			page = productService.searchActiveProductPagination(pageNo, pageSize, category, ch);
		}

		List<Product> products = page.getContent();
		m.addAttribute("products", products);
		m.addAttribute("productsSize", products.size());

		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());

		return "product";
	}

	@GetMapping("/product/{id}")
	public String product(@PathVariable int id, Model m) {
		Product productById = productService.getProductById(id);
		m.addAttribute("product", productById);
		return "view_product";
	}

//	@PostMapping("/saveUser")
//	public String saveUser(@ModelAttribute UserDtls user, @RequestParam("img") MultipartFile file, HttpSession session)
//			throws IOException {
//
//		Boolean existsEmail = userService.existsEmail(user.getEmail());
//
//		if (existsEmail) {
//			session.setAttribute("errorMsg", "Email already exist");
//		} else {
//			String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
//			user.setProfileImage(imageName);
//			UserDtls saveUser = userService.saveUser(user);
//
//			if (!ObjectUtils.isEmpty(saveUser)) {
//				if (!file.isEmpty()) {
//					File saveFile = new ClassPathResource("static/img").getFile();
//
//					Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
//							+ file.getOriginalFilename());
//
////					System.out.println(path);
//					Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//				}
//				session.setAttribute("succMsg", "Register successfully");
//			} else {
//				session.setAttribute("errorMsg", "something wrong on server");
//			}
//		}
//
//		return "redirect:/register";
//	}

//@PostMapping("/saveUser")
//public String saveUser(@ModelAttribute UserDtlsForm userForm,
//						@RequestParam("userImage") MultipartFile file,
//						HttpSession session) {
//
//
//		Boolean existsEmail = userService.existsEmail(userForm.getEmail());
//
//		if (existsEmail) {
//			session.setAttribute("errorMsg", "Email already exist");
//		}
//		else {
//
//			try {
////
//
//				String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
//				userForm.setProfileImage(imageName); //
//
//				// Convert DTO to Entity
//				UserDtls user = new UserDtls();
//				user.setId(userForm.getId());
//				user.setName(userForm.getName());
//				user.setMobileNumber(userForm.getMobileNumber());
//				user.setEmail(userForm.getEmail());
//				user.setAddress(userForm.getAddress());
//				user.setCity(userForm.getCity());
//				user.setState(userForm.getState());
//				user.setPincode(userForm.getPincode());
//				user.setPassword(userForm.getPassword());
//			// ✅ Store the default image name
//
//				if(userForm.getUserImage() != null && ! userForm.getUserImage().isEmpty())
//				{
//					String filename =UUID.randomUUID().toString();
//					String fileURL = imageService.uploadImage(userForm.getUserImage(), filename);
//					user.setProfileImage(fileURL);
//					user.setCloudinaryImagePublicId(filename);
//					UserDtls savedUser = userService.saveUser(user);
//					System.out.println(savedUser);
//
//					session.setAttribute("succMsg", "Registered successfully");
//				} else {
//					session.setAttribute("errorMsg", "Something went wrong on the server");
//				}
//			} catch (Exception e) {
//				session.setAttribute("errorMsg", "An error occurred: " + e.getMessage());
//			}
//
//		}
//	return "redirect:/register"; // ✅ Redirects correctly
//}


//@PostMapping("/saveUser")
//public String saveUser(@ModelAttribute UserDtlsForm userForm,
//					   @RequestParam("userImage") MultipartFile file,
//					   HttpSession session) {
//	try {
//		if (file == null || file.isEmpty()) {
//			session.setAttribute("errorMsg", "Profile image is required.");
//			return "redirect:/register";
//		}
//
//		Boolean existsEmail = userService.existsEmail(userForm.getEmail());
//		if (existsEmail) {
//			session.setAttribute("errorMsg", "Email already exists.");
//			return "redirect:/register";
//		}
//
//		// Convert DTO to Entity
//		UserDtls user = new UserDtls();
//		user.setId(userForm.getId());
//		user.setName(userForm.getName());
//		user.setMobileNumber(userForm.getMobileNumber());
//		user.setEmail(userForm.getEmail());
//		user.setAddress(userForm.getAddress());
//		user.setCity(userForm.getCity());
//		user.setState(userForm.getState());
//		user.setPincode(userForm.getPincode());
//		user.setPassword(userForm.getPassword());
//
//		// Upload Image
//		String filename = UUID.randomUUID().toString();
//		String fileURL = imageService.uploadImage(file, filename);
//		user.setProfileImage(fileURL);
//		user.setCloudinaryImagePublicId(filename);
//
//		// Save User
//		userService.saveUser(user);
//		session.setAttribute("succMsg", "Registered successfully.");
//		return "redirect:/register";
//
//	} catch (Exception e) {
//		session.setAttribute("errorMsg", "An error occurred: " + e.getMessage());
//		return "redirect:/register";
//	}
//}
//
//@PostMapping("/saveUser")
//public String saveUser(@Valid @ModelAttribute UserDtlsForm userForm,
//					   BindingResult result,
//					   RedirectAttributes redirectAttrs) {
//	try {
//		if (result.hasErrors()) {
//			redirectAttrs.addFlashAttribute("errorMsg", "Validation failed. Check your inputs.");
//			return "redirect:/register";
//		}
//
//		if (!userForm.getPassword().equals(userForm.getCpassword())) {
//			redirectAttrs.addFlashAttribute("errorMsg", "Passwords do not match.");
//			return "redirect:/register";
//		}
//
//		if (userForm.getUserImage() == null || userForm.getUserImage().isEmpty()) {
//			redirectAttrs.addFlashAttribute("errorMsg", "Profile image is required.");
//			return "redirect:/register";
//		}
//
//		if (userService.existsEmail(userForm.getEmail())) {
//			redirectAttrs.addFlashAttribute("errorMsg", "Email already exists.");
//			return "redirect:/register";
//		}
//
//		UserDtls user = new UserDtls();
//		user.setId(userForm.getId());
//		user.setName(userForm.getName());
//		user.setMobileNumber(userForm.getMobileNumber());
//		user.setEmail(userForm.getEmail());
//		user.setAddress(userForm.getAddress());
//		user.setCity(userForm.getCity());
//		user.setState(userForm.getState());
//		user.setPincode(userForm.getPincode());
//		user.setPassword(userForm.getPassword());
//
//		String filename = UUID.randomUUID().toString();
//		String fileURL = imageService.uploadImage(userForm.getUserImage(), filename);
//		if (fileURL == null || fileURL.isEmpty()) {
//			redirectAttrs.addFlashAttribute("errorMsg", "Image upload failed.");
//			return "redirect:/register";
//		}
//		user.setProfileImage(fileURL);
//		user.setCloudinaryImagePublicId(filename);
//
//		userService.saveUser(user);
//		redirectAttrs.addFlashAttribute("succMsg", "Registered successfully.");
//		return "redirect:/register";
//
//	} catch (Exception e) {
//		redirectAttrs.addFlashAttribute("errorMsg", "An error occurred: " + e.getMessage());
//		return "redirect:/register";
//	}
//}

@PostMapping("/saveUser")
public String saveUser(@Valid @ModelAttribute UserDtlsForm userDtlsForm,
						@RequestParam("userImage") MultipartFile file,
						HttpSession session ,BindingResult result) {

	if (result.hasErrors())
	{
		return "register";
	}

	Boolean existsEmail = userService.existsEmail(userDtlsForm.getEmail());
		if (existsEmail) {
			session.setAttribute("errorMsg", "Email already exists.");
			return "redirect:/register";
		}

	try {
		String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
		userDtlsForm.setProfileImage(imageName); // ✅ Set image name in DTO

		// Convert DTO to Entity
		UserDtls user = new UserDtls();
		user.setId(userDtlsForm.getId());
		user.setName(userDtlsForm.getName());
		user.setMobileNumber(userDtlsForm.getMobileNumber());
		user.setEmail(userDtlsForm.getEmail());
		user.setAddress(userDtlsForm.getAddress());
		user.setCity(userDtlsForm.getCity());
		user.setState(userDtlsForm.getState());
		user.setPincode(userDtlsForm.getPincode());
		user.setPassword(userDtlsForm.getPassword());
		// ✅ Store the default image name

		if(userDtlsForm.getUserImage() != null && ! userDtlsForm.getUserImage().isEmpty())
		{
			String filename =UUID.randomUUID().toString();
			String fileURL = imageService.uploadImage(userDtlsForm.getUserImage(), filename);
			user.setProfileImage(fileURL);
			user.setCloudinaryImagePublicId(filename);
			UserDtls savedUser = userService.saveUser(user);
			System.out.println(savedUser);

			session.setAttribute("succMsg", "Registered successfully");
		} else {
			session.setAttribute("errorMsg", "Something went wrong on the server");
		}
	} catch (Exception e) {
		session.setAttribute("errorMsg", "An error occurred: " + e.getMessage());
	}

	 return "redirect:/register"; // ✅ Redirects correctly
}

//@PostMapping("/saveUser")
//public String saveUser(@ModelAttribute UserDtls user, @RequestParam("img") MultipartFile file, HttpSession session)
//		throws IOException {
//
//	Boolean existsEmail = userService.existsEmail(user.getEmail());
//
//	if (existsEmail) {
//		session.setAttribute("errorMsg", "Email already exist");
//	} else {
//		String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
//
//		user.setProfileImage(imageName);
//		UserDtls saveUser = userService.saveUser(user);
//
//		if (!ObjectUtils.isEmpty(saveUser)) {
//			if (!file.isEmpty()) {
//				File saveFile = new ClassPathResource("static/img").getFile();
//
//				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
//						+ file.getOriginalFilename());
//
////					System.out.println(path);
//				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//			}
//			session.setAttribute("succMsg", "Register successfully");
//		} else {
//			session.setAttribute("errorMsg", "something wrong on server");
//		}
//	}
//
//	return "redirect:/register";
//}

//	Forgot Password Code 

	@GetMapping("/forgot-password")
	public String showForgotPassword() {
		return "forgot_password.html";
	}

	@PostMapping("/forgot-password")
	public String processForgotPassword(@RequestParam String email, HttpSession session, HttpServletRequest request)
			throws UnsupportedEncodingException, MessagingException {

		UserDtls userByEmail = userService.getUserByEmail(email);

		if (ObjectUtils.isEmpty(userByEmail)) {
			session.setAttribute("errorMsg", "Invalid email");
		} else {

			String resetToken = UUID.randomUUID().toString();
			userService.updateUserResetToken(email, resetToken);

			// Generate URL :
			// http://localhost:8080/reset-password?token=sfgdbgfswegfbdgfewgvsrg

			String url = CommonUtil.generateUrl(request) + "/reset-password?token=" + resetToken;

			Boolean sendMail = commonUtil.sendMail(url, email);

			if (sendMail) {
				session.setAttribute("succMsg", "Please check your email..Password Reset link sent");
			} else {
				session.setAttribute("errorMsg", "Somethong wrong on server ! Email not send");
			}
		}

		return "redirect:/forgot-password";
	}

	@GetMapping("/reset-password")
	public String showResetPassword(@RequestParam String token, HttpSession session, Model m) {

		UserDtls userByToken = userService.getUserByToken(token);

		if (userByToken == null) {
			m.addAttribute("msg", "Your link is invalid or expired !!");
			return "message";
		}
		m.addAttribute("token", token);
		return "reset_password";
	}

	@PostMapping("/reset-password")
	public String resetPassword(@RequestParam String token, @RequestParam String password, HttpSession session,
			Model m) {

		UserDtls userByToken = userService.getUserByToken(token);
		if (userByToken == null) {
			m.addAttribute("errorMsg", "Your link is invalid or expired !!");
			return "message";
		} else {
			userByToken.setPassword(passwordEncoder.encode(password));
			userByToken.setResetToken(null);
			userService.updateUser(userByToken);
			// session.setAttribute("succMsg", "Password change successfully");
			m.addAttribute("msg", "Password change successfully");

			return "message";
		}

	}

	@GetMapping("/search")
	public String searchProduct(@RequestParam String ch, Model m) {
		List<Product> searchProducts = productService.searchProduct(ch);
		m.addAttribute("products", searchProducts);
		List<Category> categories = categoryService.getAllActiveCategory();
		m.addAttribute("categories", categories);
		return "product";

	}

}
