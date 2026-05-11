-- Insert Users
INSERT INTO users (name, email, password, role) VALUES
('John Doe', 'john.doe@example.com', 'password123', 'STUDENT'),
('Jane Smith', 'jane.smith@example.com', 'password123', 'INSTRUCTOR'),
('Admin User', 'admin@example.com', 'admin123', 'ADMIN'),
('Alice Johnson', 'alice.johnson@example.com', 'password123', 'STUDENT'),
('Bob Wilson', 'bob.wilson@example.com', 'password123', 'STUDENT');

-- Insert Courses
INSERT INTO courses (name, description, category, level, duration, price, instructor_id, image_url) VALUES
('Java Programming Basics', 'Learn the fundamentals of Java programming from scratch', 'Programming', 'BEGINNER', 40, 99.99, 2, 'https://example.com/java-basics.jpg'),
('Advanced Spring Boot', 'Master Spring Boot framework for enterprise applications', 'Programming', 'ADVANCED', 60, 199.99, 2, 'https://example.com/spring-boot.jpg'),
('Web Development with React', 'Build modern web applications using React', 'Web Development', 'INTERMEDIATE', 50, 149.99, 2, 'https://example.com/react.jpg'),
('Database Design Fundamentals', 'Learn database design principles and SQL', 'Database', 'BEGINNER', 30, 79.99, 2, 'https://example.com/database.jpg'),
('Cloud Computing with AWS', 'Master AWS cloud services and deployment', 'Cloud', 'INTERMEDIATE', 45, 179.99, 2, 'https://example.com/aws.jpg');

-- Insert Live Projects
INSERT INTO live_projects (title, description, start_date, end_date, github_repo, live_url, course_id) VALUES
('E-Commerce Website', 'Build a full-stack e-commerce platform with Spring Boot and React', '2024-01-15', '2024-03-15', 'https://github.com/example/ecommerce', 'https://ecommerce-demo.com', 3),
('REST API Development', 'Create a RESTful API for a task management system', '2024-02-01', '2024-02-28', 'https://github.com/example/task-api', 'https://task-api-demo.com', 2),
('Blog Application', 'Develop a blog platform with user authentication', '2024-01-20', '2024-02-20', 'https://github.com/example/blog', 'https://blog-demo.com', 3),
('Inventory Management System', 'Build a database-driven inventory system', '2024-03-01', '2024-04-01', 'https://github.com/example/inventory', 'https://inventory-demo.com', 4),
('AWS Deployment Pipeline', 'Set up CI/CD pipeline for cloud deployment', '2024-02-15', '2024-03-15', 'https://github.com/example/aws-pipeline', 'https://pipeline-demo.com', 5);

-- Insert Enrollments
INSERT INTO enrollments (user_id, course_id, enrollment_date, status, progress) VALUES
(1, 1, '2024-01-10 10:00:00', 'ACTIVE', 75),
(1, 3, '2024-01-12 14:30:00', 'ACTIVE', 50),
(4, 1, '2024-01-11 09:15:00', 'COMPLETED', 100),
(4, 4, '2024-01-15 16:45:00', 'ACTIVE', 30),
(5, 2, '2024-01-08 11:20:00', 'ACTIVE', 60),
(5, 5, '2024-01-14 13:10:00', 'ACTIVE', 25);

-- Insert Certificates
INSERT INTO certificates (certificate_number, user_id, course_id, issue_date, verification_url) VALUES
('CERT-ABC12345', 4, 1, '2024-01-25', 'https://elearning.com/verify/CERT-ABC12345'),
('CERT-DEF67890', 1, 1, '2024-01-28', 'https://elearning.com/verify/CERT-DEF67890');

-- Insert Support Tickets
INSERT INTO support_tickets (subject, message, status, user_id, created_date) VALUES
('Login Issue', 'I am unable to login to my account. Please help.', 'RESOLVED', 1, '2024-01-20 09:30:00'),
('Course Access Problem', 'I cannot access the Java Programming course after enrollment.', 'OPEN', 5, '2024-01-22 14:15:00'),
('Certificate Not Generated', 'I completed the course but did not receive my certificate.', 'IN_PROGRESS', 4, '2024-01-26 10:45:00'),
('Payment Issue', 'My payment was processed but course is not showing in my dashboard.', 'OPEN', 1, '2024-01-24 16:20:00'),
('Technical Support', 'The video lectures are not loading properly.', 'RESOLVED', 5, '2024-01-21 11:00:00');
