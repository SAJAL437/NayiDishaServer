# 🛠️ Nayi Disha Server - Backend API

> **Empowering Citizens via Smart Complaint Redressal**

This is the backend RESTful API for the **Nayi Disha Web App**, built using **Spring Boot**, **JWT** authentication, and **MySQL** for data persistence.

---

## 📦 Tech Stack

* **Spring Boot 3**
* **Spring Security (JWT)**
* **MySQL / JPA / Hibernate**
* **Cloudinary / Multipart File Upload**
* **Lombok**
* **Maven**

---

## 📁 Project Structure

```
nayi-disha-server/
│
├── controller/       // REST APIs
├── dto/              // DTOs for requests & responses
├── entity/           // JPA entities (User, Complaint, Role)
├── exception/        // Custom exceptions
├── repository/       // JPA Repositories
├── security/         // JWT token handling
├── service/          // Business logic
├── util/             // Utility classes (PDF, mail, image)
└── application.properties
```

---

## 🔐 Authentication

### ✅ Signup

**POST** `/api/auth/register`
Registers a new user.

**Body:**

```json
{
  "name": "Sajal",
  "email": "sajal@example.com",
  "password": "secret",
  "phone": "9876543210"
}
```

### 🔐 Login

**POST** `/api/auth/login`
Returns a JWT token on valid login.

**Body:**

```json
{
  "email": "sajal@example.com",
  "password": "secret"
}
```

### 🔄 Verify Email 

**GET** `/api/auth/verify-email?token=...`

---

## 🧑‍💼 User APIs

### 🔍 Get Profile

**GET** `/api/users/profile`
**Header:** `Authorization: Bearer <JWT>`

### 📝 Update Profile

**PUT** `/api/users/profile`
**FormData:** name, phone, profileImage (optional)

---

## 📣 Complaint APIs

### 🆕 Raise Complaint

**POST** `/api/complaints/create`
**Header:** `Authorization: Bearer <JWT>`
**FormData:**

* `title`: "Water Shortage"
* `description`: "No water supply since 2 days"
* `location`: "Gwalior, MP"
* `image`: *file (optional)*

### 📄 Get My Complaints

**GET** `/api/complaints/my`

### 🚦 Update Status (Admin)

**PUT** `/api/admin/complaints/{id}/status`
**Body:** `{ "status": "IN_PROGRESS" }`
**Role:** `ROLE_ADMIN`

### ❌ Delete Complaint

**DELETE** `/api/complaints/{id}`

---

* **Email Trigger:** On complaint status update.

---

## 📊 Admin APIs

### 🔍 Get All Complaints

**GET** `/api/admin/complaints`
With pagination, filter, sort support.

---

## ☁️ Image Upload (Cloudinary)

Uses Cloudinary to store complaint and profile images. The uploaded image URL is stored in DB.

---

## ⚙️ Application Properties

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/nayi_disha
spring.datasource.username=root
spring.datasource.password=admin

jwt.secret=your_jwt_secret_key

cloudinary.cloud-name=your_cloud_name
cloudinary.api-key=your_api_key
cloudinary.api-secret=your_api_secret
```

---

## ✅ Roles Supported

* `ROLE_USER`: Can register, login, raise complaint, view status.
* `ROLE_ADMIN`: Can view/manage all complaints, update status, export reports.

---

## 📬 Contact


