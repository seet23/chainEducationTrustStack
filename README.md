#Spring Security AngularJS demo
This is a sample project using **Spring MVC**, **Spring Security**, **Spring Data** and **AngularJS**.

This demo implements the basic registration process for a web application. The registration service was implemented as a RESTful API with AngularJS front-end. 
The current features are:
* User registration and login
* New account activation by email
* Password encoding
* Reset password

This demo is partially based on the blog entry [Registration with Spring Security series](http://www.baeldung.com/spring-security-registration) and the demo project [ssng-project](https://github.com/samer-abdelkafi/ssng-project). 
Thanks to the authors!

##Database
This demo is based on MySQL database. Specify your database connection properties in `datasource.properties` in the resource directory.

##Mail Config
Update your email provider properties in `mail.properties` in the resource directory.

##Requirements
This demo is built with Node, NPM, Maven and Java 1.8.

##Build
To build the project run these commands:
```
npm install
bower install
gulp
mvn clean package
```

##Deployment
The project war can be deployed in a servlet container or application server.

##License
MIT license.