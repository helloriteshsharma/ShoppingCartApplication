package com.ecom.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import com.ecom.form.CategoryForm;
import com.ecom.form.ProductDTO;
import com.ecom.form.UserDtlsForm;
import com.ecom.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.model.ProductOrder;
import com.ecom.model.UserDtls;
import com.ecom.util.CommonUtil;
import com.ecom.util.OrderStatus;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private ProductService productService;

	@Autowired
	private UserService userService;

	@Autowired
	private CartService cartService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private CommonUtil commonUtil;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	ImageService imageService;

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
	public String index() {
		return "admin/index";
	}

	@GetMapping("/loadAddProduct")
	public String loadAddProduct(Model m) {

		List<Category> categories = categoryService.getAllCategory();
		m.addAttribute("categories", categories);
		return "admin/add_product";
	}

	@GetMapping("/category")
	public String category(Model m, @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
		// m.addAttribute("categorys", categoryService.getAllCategory());
		Page<Category> page = categoryService.getAllCategorPagination(pageNo, pageSize);
		List<Category> categorys = page.getContent();
		m.addAttribute("categorys", categorys);

		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());

		return "admin/category";
	}

//	@PostMapping("/saveCategory")
//@RequestMapping("/saveCategory")
//public String saveCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file,
//			HttpSession session) throws IOException {
//
//		String imageName = file != null ? file.getOriginalFilename() : "default.jpg";
//		category.setImageName(imageName);
//
//		Boolean existCategory = categoryService.existCategory(category.getName());
//
//		if (existCategory) {
//			session.setAttribute("errorMsg", "Category Name already exists");
//		} else {
//
//			Category saveCategory = categoryService.saveCategory(category);
//
//			if (ObjectUtils.isEmpty(saveCategory)) {
//				session.setAttribute("errorMsg", "Not saved ! internal server error");
//			} else {
//
//				File saveFile = new ClassPathResource("static/img").getFile();
//
//				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_img" + File.separator
//						+ file.getOriginalFilename());
//
//				// System.out.println(path);
//				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//
//				session.setAttribute("succMsg", "Saved successfully");
//			}
//		}
//
//		return "redirect:admin/category";
//
//
//	}

	@RequestMapping("/saveCategory")
	public String saveCategory(@Valid  @ModelAttribute CategoryForm categoryForm, BindingResult result, HttpSession session) throws IOException {


		if(result.hasErrors())
		{
			return "admin/category";
		}

		MultipartFile file = categoryForm.getImageName();
		String imageName = (file != null && !file.isEmpty()) ? file.getOriginalFilename() : "default.jpg";

		Category category = new Category();
		category.setId(categoryForm.getId());
		category.setName(categoryForm.getName());
		category.setIsActive(categoryForm.getIsActive());

		Boolean existCategory = categoryService.existCategory(category.getName());

		if (existCategory) {
			session.setAttribute("errorMsg", "Category Name already exists");
		} else {
			// First, save the category without an image
			Category savedCategory = categoryService.saveCategory(category);

			if (ObjectUtils.isEmpty(savedCategory)) {
				session.setAttribute("errorMsg", "Not saved! Internal server error");
			} else {
				if (file != null && !file.isEmpty()) {
					// Upload Image to Cloudinary
					String filename = UUID.randomUUID().toString();
					String fileURL = imageService.uploadImage(file, filename);

					// Update the category with image details
					savedCategory.setImageName(fileURL);
					savedCategory.setCloudinaryImagePublicId(filename);

					// Save the updated category with the image details
					categoryService.saveCategory(savedCategory);

					session.setAttribute("succMsg", "Saved successfully");
				} else {
					session.setAttribute("errorMsg", "Something went wrong on the server");
				}
			}
		}

		return "admin/category";
	}



	@GetMapping("/deleteCategory/{id}")
	public String deleteCategory(@PathVariable int id, HttpSession session) {
		Boolean deleteCategory = categoryService.deleteCategory(id);

		if (deleteCategory) {
			session.setAttribute("succMsg", "category delete success");
		} else {
			session.setAttribute("errorMsg", "something wrong on server");
		}

		return "redirect:/admin/category";
	}

	@GetMapping("/loadEditCategory/{id}")
	public String loadEditCategory(@PathVariable int id, Model m) {
		m.addAttribute("category", categoryService.getCategoryById(id));
		return "admin/edit_category";
	}

//	@PostMapping("/updateCategory")
//	public String updateCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file,
//			HttpSession session) throws IOException {
//
//		Category oldCategory = categoryService.getCategoryById(category.getId());
//		String imageName = file.isEmpty() ? oldCategory.getImageName() : file.getOriginalFilename();
//
//		if (!ObjectUtils.isEmpty(category)) {
//
//			oldCategory.setName(category.getName());
//			oldCategory.setIsActive(category.getIsActive());
//			oldCategory.setImageName(imageName);
//		}
//
//		Category updateCategory = categoryService.saveCategory(oldCategory);
//
//		if (!ObjectUtils.isEmpty(updateCategory)) {
//
//			if (!file.isEmpty()) {
//				File saveFile = new ClassPathResource("static/img").getFile();
//
//				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_img" + File.separator
//						+ file.getOriginalFilename());
//
//				// System.out.println(path);
//				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//			}
//
//			session.setAttribute("succMsg", "Category update success");
//		} else {
//			session.setAttribute("errorMsg", "something wrong on server");
//		}
//
////		return "redirect:/admin/loadEditCategory/" + category.getId();
//		return "redirect:/admin/category" ;
//
//	}

//@PostMapping("/updateCategory")
//public String updateCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file,
//							 HttpSession session) throws IOException {
//
//	Category oldCategory = categoryService.getCategoryById(category.getId());
//	String imageName = file.isEmpty() ? oldCategory.getImageName() : file.getOriginalFilename();
//
//	if (!ObjectUtils.isEmpty(category)) {
//
//		oldCategory.setName(category.getName());
//		oldCategory.setIsActive(category.getIsActive());
//		oldCategory.setImageName(imageName);
//	}
//
//	Category updateCategory = categoryService.saveCategory(oldCategory);
//
//	if (!ObjectUtils.isEmpty(updateCategory)) {
//
//		if (!file.isEmpty()) {
//			String filename = UUID.randomUUID().toString();
//			String fileURL = imageService.uploadImage(file, filename);
//			oldCategory.setImageName(fileURL);
//			oldCategory.setCloudinaryImagePublicId(filename);
//			categoryService.saveCategory(oldCategory);
//		}
//
//		session.setAttribute("succMsg", "Category update success");
//	} else {
//		session.setAttribute("errorMsg", "something wrong on server");
//	}
//
//	return "redirect:/admin/category";
//}

//@PostMapping("/updateCategory")
//public String updateCategory(@Valid @ModelAttribute Category category, @RequestParam("file") MultipartFile file,
//							 HttpSession session, BindingResult result) throws IOException {
//
//	if(result.hasErrors())
//	{
//		return "admin/edit_category";
//	}
//
//	Category oldCategory = categoryService.getCategoryById(category.getId());
//	String imageName = file.isEmpty() ? oldCategory.getImageName() : file.getOriginalFilename();
//
//	if (!ObjectUtils.isEmpty(category)) {
//
//		oldCategory.setName(category.getName());
//		oldCategory.setIsActive(category.getIsActive());
//		oldCategory.setImageName(imageName);
//	}
//
//	Category updateCategory = categoryService.saveCategory(oldCategory);
//
//	if (!ObjectUtils.isEmpty(updateCategory)) {
//
//		if (!file.isEmpty()) {
//			try {
//				String filename = UUID.randomUUID().toString();
//				String fileURL = imageService.uploadImage(file, filename);
//				oldCategory.setImageName(fileURL);
//				oldCategory.setCloudinaryImagePublicId(filename);
//				categoryService.saveCategory(oldCategory);
//			} catch (Exception e) {
//				session.setAttribute("errorMsg", "Image upload failed: " + e.getMessage());
//				return "redirect:/admin/category";
//			}
//		}
//
//		session.setAttribute("succMsg", "Category update success");
//	} else {
//		session.setAttribute("errorMsg", "something wrong on server");
//	}
//
//	return "redirect:/admin/category";
//}


//@PostMapping("/updateCategory")
//public String updateCategory(@Valid @ModelAttribute Category category, @RequestParam("file") MultipartFile file,
//							 HttpSession session, BindingResult result) throws IOException {
//
//	if (result.hasErrors() || category.getName().trim().isEmpty() || file.isEmpty()) {
//		session.setAttribute("errorMsg", "All fields are required.");
//		return "admin/edit_category";
//	}
//
//	Category oldCategory = categoryService.getCategoryById(category.getId());
//	String imageName = file.isEmpty() ? oldCategory.getImageName() : file.getOriginalFilename();
//
//	oldCategory.setName(category.getName());
//	oldCategory.setIsActive(category.getIsActive());
//	oldCategory.setImageName(imageName);
//
//	Category updateCategory = categoryService.saveCategory(oldCategory);
//
//	if (!ObjectUtils.isEmpty(updateCategory)) {
//		if (!file.isEmpty()) {
//			try {
//				String filename = UUID.randomUUID().toString();
//				String fileURL = imageService.uploadImage(file, filename);
//				oldCategory.setImageName(fileURL);
//				oldCategory.setCloudinaryImagePublicId(filename);
//				categoryService.saveCategory(oldCategory);
//			} catch (Exception e) {
//				session.setAttribute("errorMsg", "Image upload failed: " + e.getMessage());
//				return "redirect:/admin/category";
//			}
//		}
//
//		session.setAttribute("succMsg", "Category update success");
//	} else {
//		session.setAttribute("errorMsg", "Something went wrong on the server");
//	}
//
//	return "redirect:/admin/category";
//}

//@PostMapping("/updateCategory")
//public String updateCategory(@Valid @ModelAttribute Category category, @RequestParam("file") MultipartFile file,
//							 HttpSession session, BindingResult result) throws IOException {
//
//	if (result.hasErrors() || category.getName() == null || category.getName().trim().isEmpty() || file.isEmpty()) {
//		session.setAttribute("errorMsg", "All fields are required.");
//		return "admin/edit_category";
//	}
//
//	Category oldCategory = categoryService.getCategoryById(category.getId());
//	String imageName = file.isEmpty() ? oldCategory.getImageName() : file.getOriginalFilename();
//
//	oldCategory.setName(category.getName().trim());
//	oldCategory.setIsActive(category.getIsActive());
//	oldCategory.setImageName(imageName);
//
//	Category updateCategory = categoryService.saveCategory(oldCategory);
//
//	if (!ObjectUtils.isEmpty(updateCategory)) {
//		if (!file.isEmpty()) {
//			try {
//				String filename = UUID.randomUUID().toString();
//				String fileURL = imageService.uploadImage(file, filename);
//				oldCategory.setImageName(fileURL);
//				oldCategory.setCloudinaryImagePublicId(filename);
//				categoryService.saveCategory(oldCategory);
//			} catch (Exception e) {
//				session.setAttribute("errorMsg", "Image upload failed: " + e.getMessage());
//				return "redirect:/admin/category";
//			}
//		}
//
//		session.setAttribute("succMsg", "Category update success");
//	} else {
//		session.setAttribute("errorMsg", "Something went wrong on the server");
//	}
//
//	return "redirect:/admin/category";
//}

@PostMapping("/updateCategory")
public String updateCategory(@Valid @ModelAttribute("category") Category category,
							 BindingResult result,
							 @RequestParam("file") MultipartFile file,
							 HttpSession session) throws IOException {

	if (result.hasErrors()) {
		session.setAttribute("errorMsg", "All fields are required.");
		return "admin/edit_category";
	}

	Category oldCategory = categoryService.getCategoryById(category.getId());
	String imageName = file.isEmpty() ? oldCategory.getImageName() : file.getOriginalFilename();



	oldCategory.setName(category.getName().trim());
	oldCategory.setIsActive(category.getIsActive());
	oldCategory.setImageName(imageName);

	Category updatedCategory = categoryService.saveCategory(oldCategory);

	if (updatedCategory != null) {
		if (!file.isEmpty()) {
			try {
				String filename = UUID.randomUUID().toString();
				String fileURL = imageService.uploadImage(file, filename);
				oldCategory.setImageName(fileURL);
				oldCategory.setCloudinaryImagePublicId(filename);
				categoryService.saveCategory(oldCategory);
			} catch (Exception e) {
				session.setAttribute("errorMsg", "Image upload failed: " + e.getMessage());
				return "redirect:/admin/category";
			}
		}
		session.setAttribute("succMsg", "Category updated successfully.");
	} else {
		session.setAttribute("errorMsg", "Something went wrong on the server.");
	}

	return "redirect:/admin/category";
}


//	@PostMapping("/saveProduct")
//	public String saveProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile image,
//			HttpSession session) throws IOException {
//
//
//
////		String imageName = image.isEmpty() ? "default.jpg" : image.getOriginalFilename();
//
////		product.setImage(imageName);
//
//		product.setDiscount(0);
//		product.setDiscountPrice(product.getPrice());
//
//
//		if(product.getImage() != null && !product.getImage().isEmpty())
//		{
//			String filename = UUID.randomUUID().toString();
//			String fileURL = imageService.uploadImage(product.getProductImage(), filename);
//			product.setImage(fileURL);
//			product.setCloudinaryImagePublicId(filename);
//			session.setAttribute("succMsg", "Product Saved Success");
//		}
//		else
//		{
//			session.setAttribute("errorMsg", "something wrong on server");
//		}
//
//		Product saveProduct = productService.saveProduct(product);
////		if (!ObjectUtils.isEmpty(saveProduct)) {
////
////			File saveFile = new ClassPathResource("static/img").getFile();
////
////			Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "product_img" + File.separator
////					+ image.getOriginalFilename());
////
////			// System.out.println(path);
////			Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
////
////			session.setAttribute("succMsg", "Product Saved Success");
////		} else {
////			session.setAttribute("errorMsg", "something wrong on server");
////		}
//		return "redirect:/admin/loadAddProduct";
//	}


@PostMapping("/saveProduct")
public String saveProduct(@Valid @ModelAttribute("productDTO") ProductDTO productDTO, BindingResult result, HttpSession session) throws IOException {
	if (result.hasErrors()) {
		return "admin/add_product";
	}

	Product product = new Product();
	product.setTitle(productDTO.getTitle());
	product.setDescription(productDTO.getDescription());
	product.setCategory(productDTO.getCategory());
	product.setPrice(productDTO.getPrice());
	product.setStock(productDTO.getStock());
	product.setDiscount(productDTO.getDiscount() != null ? productDTO.getDiscount() : 0);
	product.setDiscountPrice(productDTO.getDiscountPrice() != null ? productDTO.getDiscountPrice() : productDTO.getPrice());
	product.setIsActive(productDTO.getIsActive());

	if (productDTO.getProductImage() != null && !productDTO.getProductImage().isEmpty()) {
		String filename = UUID.randomUUID().toString();
		String fileURL = imageService.uploadImage(productDTO.getProductImage(), filename);
		product.setImage(fileURL);
		product.setCloudinaryImagePublicId(filename);
		session.setAttribute("succMsg", "Product Saved Successfully");
	} else {
		session.setAttribute("errorMsg", "Image upload failed");
	}

	productService.saveProduct(product);
	return "redirect:/admin/loadAddProduct";
}

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}


	@GetMapping("/products")
	public String loadViewProduct(Model m, @RequestParam(defaultValue = "") String ch,
			@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

//		List<Product> products = null;
//		if (ch != null && ch.length() > 0) {
//			products = productService.searchProduct(ch);
//		} else {
//			products = productService.getAllProducts();
//		}
//		m.addAttribute("products", products);

		Page<Product> page = null;
		if (ch != null && ch.length() > 0) {
			page = productService.searchProductPagination(pageNo, pageSize, ch);
		} else {
			page = productService.getAllProductsPagination(pageNo, pageSize);
		}
		m.addAttribute("products", page.getContent());

		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());

		return "admin/products";
	}

	@GetMapping("/deleteProduct/{id}")
	public String deleteProduct(@PathVariable int id, HttpSession session) {
		Boolean deleteProduct = productService.deleteProduct(id);
		if (deleteProduct) {
			session.setAttribute("succMsg", "Product delete success");
		} else {
			session.setAttribute("errorMsg", "Something wrong on server");
		}
		return "redirect:/admin/products";
	}

	@GetMapping("/editProduct/{id}")
	public String editProduct(@PathVariable int id, Model m) {
		m.addAttribute("product", productService.getProductById(id));
		m.addAttribute("categories", categoryService.getAllCategory());
		return "admin/edit_product";
	}

//	@PostMapping("/updateProduct")
//	public String updateProduct(@Valid @ModelAttribute Product product, @RequestParam("file") MultipartFile image,
//			HttpSession session, Model m, BindingResult result) {
//
//	if(result.hasErrors())
//	{
//		return "redirect:/admin/editProduct/" + product.getId();
//	}
//
//		if (product.getDiscount() < 0 || product.getDiscount() > 100) {
//			session.setAttribute("errorMsg", "invalid Discount");
//		} else {
//			Product updateProduct = productService.updateProduct(product, image);
//			if (!ObjectUtils.isEmpty(updateProduct)) {
//				session.setAttribute("succMsg", "Product update success");
//			} else {
//				session.setAttribute("errorMsg", "Something wrong on server");
//			}
//		}
////		return "redirect:/admin/editProduct/" + product.getId();
//
//		return "redirect:/admin/editProduct/" + product.getId();
//
//
//
//	}
//
@PostMapping("/updateProduct")
public String updateProduct(@Valid @ModelAttribute("product") Product product,
							@RequestParam("file") MultipartFile image,
							HttpSession session, Model m, BindingResult result) {
	if (result.hasErrors()) {
		session.setAttribute("errorMsg","All fields are required.");
		return "admin/editProduct";
	}

	if (product.getDiscount() < 0 || product.getDiscount() > 100) {
		session.setAttribute("errorMsg", "Invalid discount value (0-100)");
		return "redirect:/admin/editProduct/" + product.getId();
	}

	try {
		Product updatedProduct = productService.updateProduct(product, image);
		if (updatedProduct != null) {
			session.setAttribute("succMsg", "Product updated successfully");
		} else {
			session.setAttribute("errorMsg", "Something went wrong on the server");
		}
	} catch (Exception e) {
		session.setAttribute("errorMsg", "Error while updating product: " + e.getMessage());
	}

	return "redirect:/admin/editProduct/" + product.getId();
//		return "admin/products";
}

//@PostMapping("/updateProduct")
//public String updateProduct(@Valid @ModelAttribute("product") Product product,
//							BindingResult result,
//							@RequestParam("file") MultipartFile image,
//							HttpSession session, Model m) {
//	if (result.hasErrors()) {
//		m.addAttribute("product", product);
//		session.setAttribute("errorMsg", "Please correct the errors below.");
//		return "admin/editProduct";
//	}
//
//	if (product.getDiscount() < 0 || product.getDiscount() > 100) {
//		session.setAttribute("errorMsg", "Invalid discount value (0-100)");
//		return "admin/editProduct";
//	}
//
//	try {
//		Product updatedProduct = productService.updateProduct(product, image);
//		if (updatedProduct != null) {
//			session.setAttribute("succMsg", "Product updated successfully");
//		} else {
//			session.setAttribute("errorMsg", "Something went wrong on the server");
//		}
//	} catch (Exception e) {
//		session.setAttribute("errorMsg", "Error while updating product: " + e.getMessage());
//	}
//
//	return "redirect:/admin/editProduct/" + product.getId();
//}
////
//@PostMapping("/updateProduct")
//public String updateProduct(@Valid @ModelAttribute("product") Product product,
//							BindingResult result,
//							@RequestParam("file") MultipartFile image,
//							HttpSession session, Model m) {
//	if (result.hasErrors()) {
//		m.addAttribute("product", product);
//		session.setAttribute("errorMsg", "All fields are required.");
//		return "admin/editProduct";
//	}
//
//	if (product.getDiscount() < 0 || product.getDiscount() > 100) {
//		session.setAttribute("errorMsg", "Invalid discount value (0-100)");
//		return "admin/editProduct";
//	}
//
//	try {
//		Product updatedProduct = productService.updateProduct(product, image);
//		if (updatedProduct != null) {
//			session.setAttribute("succMsg", "Product updated successfully");
//		} else {
//			session.setAttribute("errorMsg", "Something went wrong on the server");
//		}
//	} catch (Exception e) {
//		session.setAttribute("errorMsg", "Error while updating product: " + e.getMessage());
//	}
//
//	return "redirect:/admin/editProduct/" + product.getId();
//}

//@PostMapping("/updateProduct")
//public String updateProduct(@Valid @ModelAttribute("product") Product product,
//							@RequestParam("file") MultipartFile image,
//							HttpSession session, Model m, BindingResult result) {
//	if (result.hasErrors()) {
////		m.addAttribute("categories", categoryService.getAllCategories());
//		return "admin/editProduct";
//	}
//
//	if (product.getDiscount() < 0 || product.getDiscount() > 100) {
//		session.setAttribute("errorMsg", "Invalid discount value (0-100)");
//		return "redirect:/admin/editProduct/" + product.getId();
//	}
//
//	Product updatedProduct = productService.updateProduct(product, image);
//	if (!ObjectUtils.isEmpty(updatedProduct)) {
//		session.setAttribute("succMsg", "Product updated successfully");
//	} else {
//		session.setAttribute("errorMsg", "Something went wrong on the server");
//	}
//
//	return "redirect:/admin/editProduct/" + product.getId();
//}


	@GetMapping("/users")
	public String getAllUsers(Model m, @RequestParam Integer type) {
		List<UserDtls> users = null;
		if (type == 1) {
			users = userService.getUsers("ROLE_USER");
		} else {
			users = userService.getUsers("ROLE_ADMIN");
		}
		m.addAttribute("userType",type);
		m.addAttribute("users", users);
//		return "/admin/users";
		return "admin/users";
	}

	@GetMapping("/updateSts")
	public String updateUserAccountStatus(@RequestParam Boolean status, @RequestParam Integer id,@RequestParam Integer type, HttpSession session) {
		Boolean f = userService.updateAccountStatus(id, status);
		if (f) {
			session.setAttribute("succMsg", "Account Status Updated");
		} else {
			session.setAttribute("errorMsg", "Something wrong on server");
		}
		return "redirect:/admin/users?type="+type;
	}

	@GetMapping("/orders")
	public String getAllOrders(Model m, @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
//		List<ProductOrder> allOrders = orderService.getAllOrders();
//		m.addAttribute("orders", allOrders);
//		m.addAttribute("srch", false);

		Page<ProductOrder> page = orderService.getAllOrdersPagination(pageNo, pageSize);
		m.addAttribute("orders", page.getContent());
		m.addAttribute("srch", false);

		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());

//		return "/admin/orders";
		return "admin/orders";
	}

	@PostMapping("/update-order-status")
	public String updateOrderStatus(@RequestParam Integer id, @RequestParam Integer st, HttpSession session) {

		OrderStatus[] values = OrderStatus.values();
		String status = null;

		for (OrderStatus orderSt : values) {
			if (orderSt.getId().equals(st)) {
				status = orderSt.getName();
			}
		}

		ProductOrder updateOrder = orderService.updateOrderStatus(id, status);

		try {
			commonUtil.sendMailForProductOrder(updateOrder, status);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!ObjectUtils.isEmpty(updateOrder)) {
			session.setAttribute("succMsg", "Status Updated");
		} else {
			session.setAttribute("errorMsg", "status not updated");
		}
		return "redirect:/admin/orders";
	}

	@GetMapping("/search-order")
	public String searchProduct(@RequestParam String orderId, Model m, HttpSession session,
			@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

		if (orderId != null && orderId.length() > 0) {

			ProductOrder order = orderService.getOrdersByOrderId(orderId.trim());

			if (ObjectUtils.isEmpty(order)) {
				session.setAttribute("errorMsg", "Incorrect orderId");
				m.addAttribute("orderDtls", null);
			} else {
				m.addAttribute("orderDtls", order);
			}

			m.addAttribute("srch", true);
		} else {
//			List<ProductOrder> allOrders = orderService.getAllOrders();
//			m.addAttribute("orders", allOrders);
//			m.addAttribute("srch", false);

			Page<ProductOrder> page = orderService.getAllOrdersPagination(pageNo, pageSize);
			m.addAttribute("orders", page);
			m.addAttribute("srch", false);

			m.addAttribute("pageNo", page.getNumber());
			m.addAttribute("pageSize", pageSize);
			m.addAttribute("totalElements", page.getTotalElements());
			m.addAttribute("totalPages", page.getTotalPages());
			m.addAttribute("isFirst", page.isFirst());
			m.addAttribute("isLast", page.isLast());

		}
		return "/admin/orders";

	}

	@GetMapping("/add-admin")
	public String loadAdminAdd() {

//		return "/admin/add_admin";
		return "admin/add_admin";
	}

//	@PostMapping("/save-admin")
//	public String saveAdmin(@ModelAttribute UserDtls user, @RequestParam("img") MultipartFile file, HttpSession session)
//			throws IOException {
//
//		String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
//		user.setProfileImage(imageName);
//		UserDtls saveUser = userService.saveAdmin(user);
//
//		if (!ObjectUtils.isEmpty(saveUser)) {
//			if (!file.isEmpty()) {
//				File saveFile = new ClassPathResource("static/img").getFile();
//
//				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
//						+ file.getOriginalFilename());
//
////				System.out.println(path);
//				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//			}
//			session.setAttribute("succMsg", "Register successfully");
//		} else {
//			session.setAttribute("errorMsg", "something wrong on server");
//		}
//
//		return "redirect:admin/add-admin";
//	}


@PostMapping("/save-admin")
public String saveAdmin(@ModelAttribute UserDtlsForm userForm,
						@RequestParam("userImage") MultipartFile file,
						HttpSession session) {
	try {
		String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
		userForm.setProfileImage(imageName); // ✅ Set image name in DTO

		// Convert DTO to Entity
		UserDtls user = new UserDtls();
		user.setId(userForm.getId());
		user.setName(userForm.getName());
		user.setMobileNumber(userForm.getMobileNumber());
		user.setEmail(userForm.getEmail());
		user.setAddress(userForm.getAddress());
		user.setCity(userForm.getCity());
		user.setState(userForm.getState());
		user.setPincode(userForm.getPincode());
		user.setPassword(userForm.getPassword());
		 // ✅ Store the default image name

		if(userForm.getUserImage() != null && ! userForm.getUserImage().isEmpty())
		{
			String filename =UUID.randomUUID().toString();
			String fileURL = imageService.uploadImage(userForm.getUserImage(), filename);
			user.setProfileImage(fileURL);
			user.setCloudinaryImagePublicId(filename);
			UserDtls savedUser = userService.saveAdmin(user);
			System.out.println(savedUser);

			session.setAttribute("succMsg", "Registered successfully");
		} else {
			session.setAttribute("errorMsg", "Something went wrong on the server");
		}
	} catch (Exception e) {
		session.setAttribute("errorMsg", "An error occurred: " + e.getMessage());
	}

	return "redirect:/admin/add-admin"; // ✅ Redirects correctly
}



	@GetMapping("/profile")
	public String profile() {
//		return "/admin/profile";
		return "admin/profile";
	}

	@PostMapping("/update-profile")
	public String updateProfile(@ModelAttribute UserDtls user, @RequestParam MultipartFile img, HttpSession session) {
		UserDtls updateUserProfile = userService.updateUserProfile(user, img);
		if (ObjectUtils.isEmpty(updateUserProfile)) {
			session.setAttribute("errorMsg", "Profile not updated");
		} else {
			session.setAttribute("succMsg", "Profile updated");
		}
		return "redirect:/admin/profile";
	}

	@PostMapping("/change-password")
	public String changePassword(@RequestParam String newPassword, @RequestParam String currentPassword, Principal p,
			HttpSession session) {
		UserDtls loggedInUserDetails = commonUtil.getLoggedInUserDetails(p);

		boolean matches = passwordEncoder.matches(currentPassword, loggedInUserDetails.getPassword());

		if (matches) {
			String encodePassword = passwordEncoder.encode(newPassword);
			loggedInUserDetails.setPassword(encodePassword);
			UserDtls updateUser = userService.updateUser(loggedInUserDetails);
			if (ObjectUtils.isEmpty(updateUser)) {
				session.setAttribute("errorMsg", "Password not updated !! Error in server");
			} else {
				session.setAttribute("succMsg", "Password Updated sucessfully");
			}
		} else {
			session.setAttribute("errorMsg", "Current Password incorrect");
		}

		return "redirect:/admin/profile";
	}

}
