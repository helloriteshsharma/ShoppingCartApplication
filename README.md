
# Shopping Cart Application

This is a fully functional shopping cart application built with Spring Boot for the backend and Thymeleaf, JavaScript, Bootstrap, and HTML for the frontend. It provides a seamless shopping experience with user authentication, product listings, cart
management, and order processing. The platform supports both users and admins, enabling efficient product, category, inventory, and order management.


## Tech Stack

**Backend:** Java, Spring Boot, Spring MVC, Spring Security, Spring Data JPA, MySQL, Postman, Cloudinary, Docker, etc.

**Frontend:** HTML,CSS, Bootstrap, Thymeleaf


## Table of Contents
- *Software and Tools required*
- *Installation*
- *Running the Project*
- *API Endpoints*
    - *User*
        - *User Register*
        - *Login User*
        - *User Profile*
        - *Add Product to the Cart*
        - *Order the Product*
    - *Admin*
        - *Login Admin*
        - *Admin Profile*
        - *Add Category*
        - *Add Product*
        - *Update Product*
        - *Add Admin*
        - *View all Admin*
        - *View all User*
        - *View all Orders*
- *Snapshots*
## Software and Tools required
- *JDK 17 or more*
- *Git*
- *MySQL Client*
- *Docker*
- *IDE or Editors*
- *IntelliJ Idea (Community / Ultimate)*
- *Spring Tool Suite (STS)*
- *Eclipse*
- *Visual Studio Code (VS Code)*
## Installation

To install this application, run the following commands:

**Clone the project repository:**

```bash
  git clone https://github.com/helloriteshsharma/SmartContactManager.git
```
**Navigate to the project directory:**


```bash
  cd SmartContactManager
```

This will get a copy of the project installed locally. To configure all of its dependencies and start each app, follow the instructions below.

**Configure Database**

Once MySQL is installed you must configure a username and password. By default the user and password should be ```
  root``` 
 If not, you must configure in the file ```
  application.configure``` located in the path src/main/resources/.

In the file ```application.configure``` you must edit the parameters ```spring.datasource.username```  and  ```spring.datasource.password``` with the values you defined.

```bash
    server.port=8085

    spring.datasource.url=jdbc:mysql://localhost:3306/scmdb
    spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
    spring.datasource.username=SQLUsername(i.e=root)
    spring.datasource.password=SQLUserPassword(i.e=root)

    spring.mail.host=${EMAIL_HOST}
    spring.mail.port=${EMAIL_PORT}
    spring.mail.username=${EMAIL_USERNAME}
    spring.mail.password=${EMAIL_PASSWORD}
    spring.mail.properties.mail.smtp.auth=true
    spring.mail.properties.mail.smtp.starttls.enable=true


    cloudinary.api.key=${CLOUDINARY_API_KEY}
    cloudinary.api.secret=${CLOUDINARY_API_SECRET}
    cloudinary.cloud.name=${CLOUDINARY_NAME}
```
**Build and run the Spring Boot application using CMD**

```bash
  mvnw spring-boot:run
```

**https://localhost:8085/**

## API Endpoints



#### HomeController

```http
POST  /api/auth/login
```
```http
POST  /api/auth/register
```
```http
GET  /api/products
```
```http
GET  /api/products/{id}
```
```http
POST  /api/products
```

#### AdminController

```http
GET  /admin/
```
```http
GET  /api/auth/register
```
```http
GET  /admin/loadAddProduct
```
```http
GET  /admin/category
```
```http
POST  /admin/saveCategory
```
```http
GET  /admin/deleteCategory/{id}
```
```http
GET  /admin/loadEditCategory/{id}
```
```http
POST  /admin/updateCategory
```
```http
POST  /admin/saveProduct
```
```http
GET   /admin/products
```
```http
GET  /admin/deleteProduct/{id}
```
```http
GET  /admin/editProduct/{id}
```
```http
POST  /admin/updateProduct
```
```http
GET   /admin/users
```
```http
GET   /admin/updateSts
```
```http
GET   /admin/orders
```
```http
POST  /admin/update-order-status
```
```http
GET   /admin/search-order
```
```http
GET   /admin/add-admin
```
```http
POST  /admin/save-admin
```
```http
GET   /admin/profile
```
```http
POST  /admin/update-profile
```
```http
POST  /admin/change-password
```

#### UserController

```http
GET   /user/
```
```http
GET   /user/addCart
```
```http
GET   /user/cart
```
```http
GET   /user/cartQuantityUpdate
```
```http
GET   /user/orders
```
```http
POST  /user/save-order
```
```http
GET  	/user/success
```
```http
GET  	/user/user-orders
```
```http
GET   /user/update-status
```
```http
GET  	/user/profile
```
```http
POST   /user/update-profile
```
```http
POST  /user/change-password
```








## Screenshots

![App Screenshot](ShoppingCartApplication/screenshot/sp01.png)

