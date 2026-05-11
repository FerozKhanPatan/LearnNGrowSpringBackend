-- Insert sample courses
INSERT INTO courses (name, description, category, level, duration, price, instructor, image_url) VALUES
('Core & Advanced Python with AI', 'Master Python from basics to advanced AI concepts including machine learning and deep learning', 'PYTHON', 'All Levels', 40, 299.99, 'Dr. Sarah Johnson', 'https://via.placeholder.com/300x200?text=Python+Course'),
('Core & Advanced Android', 'Build professional Android applications using Kotlin and Jetpack Compose', 'ANDROID', 'Intermediate', 35, 349.99, 'Michael Chen', 'https://via.placeholder.com/300x200?text=Android+Course'),
('English Speaking', 'Improve your communication skills with our comprehensive English speaking course', 'ENGLISH', 'All Levels', 25, 199.99, 'Emma Watson', 'https://via.placeholder.com/300x200?text=English+Course'),
('Advanced Python with AI', 'Deep dive into AI algorithms and implementation with Python', 'PYTHON', 'Advanced', 45, 399.99, 'Dr. Sarah Johnson', 'https://via.placeholder.com/300x200?text=Advanced+Python'),
('Android App Development', 'Complete guide to Android app development from scratch', 'ANDROID', 'Beginner', 30, 249.99, 'Michael Chen', 'https://via.placeholder.com/300x200?text=Android+Beginner');

-- Insert sample live projects
INSERT INTO live_projects (title, description, start_date, end_date, github_repo, course_id) VALUES
('AI Chatbot Development', 'Build a smart chatbot using Python and OpenAI', DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), 'https://github.com/example/ai-chatbot', 1),
('E-commerce App', 'Create a complete e-commerce Android application', DATE_ADD(NOW(), INTERVAL 14 DAY), DATE_ADD(NOW(), INTERVAL 45 DAY), 'https://github.com/example/ecommerce-app', 2),
('English Speaking Practice App', 'Build an interactive English learning app', DATE_ADD(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 28 DAY), 'https://github.com/example/english-app', 3);