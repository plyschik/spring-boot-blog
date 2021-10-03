# Spring Boot Blog

Spring Boot Blog is a simple blog system that allows you to publish posts that can be categorized, tagged and commented. You can also search and filter posts by author, category, tag and date range. A management panel has been prepared for resource management, where you can view, create, edit and delete resources. A user authentication and authorization system has also been implemented.

User roles:
- `User` - can create comments on posts and edit (only up to 2 minutes after publication) and delete his comments,
- `Editor` - can edit and delete all comments,
- `Administrator` - can create and manage all available resources: posts, categories, tags and comments.

## Features

- Paginated list of posts with title, content, creation date, author, category and tags
- Searching for posts by title and content
- List of the top five categories with the largest number of posts (the most popular categories)
- Posts archive by years and months
- Paginated list of posts by author, category, tags and date range
- Creating, editing and deleting comments on posts (REST API, AJAX, ReactJS)
- Changing language of user interface (available languages: English, Polish)
- Registration of a new user account
- Account activation mechanism after registration (activation link in the e-mail)
- Account password reset mechanism
- "Remember me" mechanism
- Paginated list of resources (posts, categories, tags) that can be searched, sorted and filtered in the management panel
- Create resources (posts, categories, tags) in the management panel
- Edit resources (posts, categories, tags) in the management panel
- Remove resources (posts, categories, tags) in the management panel
- Post content in Markdown

## Technologies

- Spring Boot
- Thymeleaf
- MySQL
- Spring Data JPA
- Liquibase
- Spring Security
- Lombok
- ModelMapper
- Java Mail Sender
- Bootstrap
- ReactJS (used only to build comments application part)
- Webpack
- JUnit + Mockito + AssertJ
- Docker
- GitHub Actions (Continuous Integration)

## How to run?

The preferred way to run the application is to use Docker. Just download this application source code and run two commands: `docker-compose build` - to build Docker images and `docker-compose up -d` - to create and run two Docker containers: one with application and other with MySQL database. After that application should be available at [localhost:8080](http://localhost:8080). The *multi-stage build* approach was used to build images, so they have smaller size.

## Screenshots

[![](docs/screenshots/01.png)](https://raw.githubusercontent.com/plyschik/spring-boot-blog/main/docs/screenshots/01.png)
[![](docs/screenshots/02.png)](https://raw.githubusercontent.com/plyschik/spring-boot-blog/main/docs/screenshots/02.png)
[![](docs/screenshots/03.png)](https://raw.githubusercontent.com/plyschik/spring-boot-blog/main/docs/screenshots/03.png)
[![](docs/screenshots/04.png)](https://raw.githubusercontent.com/plyschik/spring-boot-blog/main/docs/screenshots/04.png)
