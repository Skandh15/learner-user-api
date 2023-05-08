package com.learner.LearnerUser;

import com.learner.LearnerUser.controller.UserByEmail;
import com.learner.LearnerUser.entity.User;
import com.learner.LearnerUser.repository.UserRepository;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @Before
    public void setUp() {
        testUser = new User();
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPassword("password");
        userRepository.save(testUser);
    }

    @After
    public void tearDown() {
        userRepository.delete(testUser);
    }

    @Test
    public void testGetAllUsers() {
        ResponseEntity<List<User>> response = restTemplate.exchange("/api/users", HttpMethod.GET, null, new ParameterizedTypeReference<List<User>>() {
        });
        Assert.assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        Assert.assertNotNull(response.getBody());
    }

    @Test
    public void testGetUserByUserId() {
        ResponseEntity<User> response = restTemplate.getForEntity("/api/users/getUserById?userId=" + testUser.getUserId(), User.class);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(testUser.getUserId(), response.getBody().getUserId());
    }

    @Test
    public void testGetUserByUserIdNotFound() {
        ResponseEntity<User> response = restTemplate.getForEntity("/api/users/getUserById?userId=-1", User.class);
        Assert.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void testGetUserByEmailAndPassword() {
        ResponseEntity<UserByEmail> response = restTemplate.getForEntity("/api/users/getDetails?email=" + testUser.getEmail() + "&password=" + testUser.getPassword(), UserByEmail.class);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(testUser.getEmail(), response.getBody().getEmail());
        Assert.assertEquals(testUser.getFirstName(), response.getBody().getFirstName());
        Assert.assertEquals(testUser.getLastName(), response.getBody().getLastName());
        Assert.assertEquals(testUser.getUserId(), response.getBody().getId());
    }

    @Test
    public void testGetUserByEmailAndPasswordNotFound() {
        ResponseEntity<Object> response = restTemplate.getForEntity("/api/users/getDetails?email=invalid-email&password=invalid-password", Object.class);
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Assert.assertFalse(Objects.requireNonNull(response.getBody()).toString().contains("Email or password is incorrect"));
    }

    @Test
    public void testCreateUser() {
        User newUser = new User();
        newUser.setFirstName("Jane");
        newUser.setLastName("Doe");
        newUser.setEmail("jane.doe@example.com");
        newUser.setPassword("password");
        HttpEntity<User> request = new HttpEntity<>(newUser);
        ResponseEntity<User> response = restTemplate.exchange("/api/users", HttpMethod.POST, request, User.class);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(newUser.getEmail(), response.getBody().getEmail());
        Assert.assertEquals(newUser.getFirstName(), response.getBody().getFirstName());
        Assert.assertEquals(newUser.getLastName(), response.getBody().getLastName());
        Assert.assertEquals(newUser.getPassword(), response.getBody().getPassword());
        Assert.assertNotNull(response.getBody().getUserId());
        userRepository.delete(response.getBody());
    }

    @Test
    public void testCreateUserWithExistingEmail() {
        HttpEntity<User> request = new HttpEntity<>(testUser);
        ResponseEntity<Object> response = restTemplate.exchange("/api/users", HttpMethod.POST, request, Object.class);
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Assert.assertFalse(response.getBody().toString().contains("User with email " + testUser.getEmail() + " already exists"));
    }

    @Test
    public void testConstructor() {
        String firstName = "John";
        String lastName = "Doe";
        String email = "john.doe@example.com";
        String password = "password";

        User user = new User(firstName, lastName, email, password);

        assertEquals(firstName, user.getFirstName());
        assertEquals(lastName, user.getLastName());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
    }
}
