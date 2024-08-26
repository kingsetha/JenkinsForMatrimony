package com.ani.matrimony;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.multipart.MultipartFile;

import com.ani.matrimony.model.Admin;
import com.ani.matrimony.model.User;
import com.ani.matrimony.repo.AdminRepo;
import com.ani.matrimony.repo.UserRepo;
import com.ani.matrimony.service.EmailService;
import com.ani.matrimony.service.UserService;
import com.ani.matrimony.serviceimpl.AdminServiceImpl;
import com.ani.matrimony.serviceimpl.UserServiceImpl;

import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.internet.MimeMessage;

import org.junit.jupiter.api.extension.ExtendWith;
@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class TestmatirmonyApplicationTests {

    @Mock
    private UserRepo userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void testDeleteUser() {
        int userid = 5;

        // Call the method under test
        userService.deleteUser(userid);

        // Verify that deleteById was called with the correct userId
        verify(userRepository, times(1)).deleteById(userid);
    }
//    @Test
//    void testUpdateUser1() {
//        int userId = 1;
//        User existingUser = new User();
//        existingUser.setUserid(userId);
//        existingUser.setFirstname("Old Name");
//
//        User updatedUser = new User();
//        updatedUser.setFirstname("New Name");
//
//        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
//        when(userRepository.save(existingUser)).thenReturn(existingUser);
//
//        userService.updateUser(userId, updatedUser);
//
//        assertEquals("New Name", existingUser.getFirstname());
//        verify(userRepository).save(existingUser);
//    }
    @Test
    void testGetUserById() {
        int userId = 1;
        User user = new User();
        user.setUserid(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserById(userId);

        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getUserid());
    }
    @Test
    void testGetAllUsers() {
        User user1 = new User();
        User user2 = new User();
        List<User> users = List.of(user1, user2);

        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
        verify(userRepository).findAll();
    }
    @Test
    void testAuthenticateUser() {
        String email = "test@example.com";
        String password = "password";
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);

        when(userRepository.findByEmailAndPassword(email, password)).thenReturn(Optional.of(user));

        Optional<User> result = userService.authenticate(email, password);

        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());
    }
//    @Test
//    void testGetUsersByParams() {
//        Map<String, String> params = Map.of("firstname", "John", "age", "25");
//        User user = new User();
//        user.setFirstname("John");
//        user.setAge(25);
//
//        when(userRepository.findByfirstnameContainingAndAgeAndReligion(anyString(), anyInt(), anyString(), anyString(), anyString(), anyString()))
//                .thenReturn(List.of(user));
//
//        List<User> result = userService.getUsers(params);
//
//        assertEquals(1, result.size());
//        assertEquals("John", result.get(0).getFirstname());
//        assertEquals(25, result.get(0).getAge());
//    }
    @Test
    void testUpdateUserNotFound1() {
        int userId = 999; // Non-existent user ID
        User updatedUser = new User();
        updatedUser.setFirstname("New Name");

        // Set up the mock to return an empty Optional when trying to find the user by ID
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Call the method to update the user
        userService.updateUser(userId, updatedUser);

        // Verify that the save method was never called, as the user was not found
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testDeleteUser1() {
        int userId = 2;

        doNothing().when(userRepository).deleteById(userId);

        userService.deleteUser(userId);

        verify(userRepository).deleteById(userId);
    }
    @Test
    void testAddUser() {
        User user = new User();
        user.setUserid(100);
        user.setFirstname("John");

        // Call the method under test
        String result = userService.addUser(user);

        // Verify that save was called with the correct user
        verify(userRepository, times(1)).save(user);

        // Assert the expected result
        assertEquals("User added successfully", result);
    }
    @Test
    void testAuthenticateSuccess() {
        User user = new User();
        user.setBlocked(false);
        when(userRepository.findByEmailAndPassword("test@example.com", "password123")).thenReturn(Optional.of(user));

        Optional<User> result = userService.authenticate("test@example.com", "password123");

        assertTrue(result.isPresent());
        assertFalse(result.get().isBlocked());
    }

    @Test
    void testAuthenticateBlockedUser() {
        User user = new User();
        user.setBlocked(true);
        when(userRepository.findByEmailAndPassword("test@example.com", "password123")).thenReturn(Optional.of(user));

        Optional<User> result = userService.authenticate("test@example.com", "password123");

        assertFalse(result.isPresent());
    }

    @Test
    void testAuthenticateFailure() {
        when(userRepository.findByEmailAndPassword("test@example.com", "password123")).thenReturn(Optional.empty());

        Optional<User> result = userService.authenticate("test@example.com", "password123");

        assertFalse(result.isPresent());
    }
    @Test
    void testGetUsers() {
        Map<String, String> params = new HashMap<>();
        params.put("firstname", "John");
        params.put("age", "30");
        params.put("gender", "Male");
        params.put("religion", "Christian");
        params.put("maritalstatus", "Single");
        params.put("occupation", "Engineer");

        List<User> users = new ArrayList<>();
        when(userRepository.findByfirstnameContainingAndAgeAndReligion("John", 30, "Male", "Christian", "Single", "Engineer")).thenReturn(users);

        List<User> result = userService.getUsers(params);

        assertNotNull(result);
        assertEquals(users, result);
    }
//    @Test
//    void testDeleteUser1() {
//        int userId = 2;
//
//        // Call the method under test
//        userService.deleteUser(userId);
//
//        // Verify that deleteById was called with the correct userId
//        verify(userRepository, times(4)).deleteById(userId);
//    }
    @Test
    void testUpdateUserSuccess() {
        int userId = 1;
        User existingUser = new User();
        existingUser.setUserid(userId);
        User userToUpdate = new User();
        userToUpdate.setFirstname("Jane");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        userService.updateUser(userId, userToUpdate);

        verify(userRepository, times(1)).save(existingUser);
        assertEquals("Jane", existingUser.getFirstname());
    }

    @Test
    void testUpdateUserNotFound() {
        int userId = 1;
        User userToUpdate = new User();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        userService.updateUser(userId, userToUpdate);

        verify(userRepository, never()).save(any(User.class));
    }
    @Test
    void testBlockUserSuccess() {
        int userId = 1;
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        String result = userService.blockUser(userId);

        assertEquals("User blocked successfully", result);
        verify(userRepository, times(1)).save(user);
        assertTrue(user.isBlocked());
    }

    @Test
    void testBlockUserNotFound() {
        int userId = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        String result = userService.blockUser(userId);

        assertEquals("User not found", result);
        verify(userRepository, never()).save(any(User.class));
    }
    @Test
    void testUnblockUserSuccess() {
        int userId = 1;
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        String result = userService.unblockUser(userId);

        assertEquals("User unblocked successfully", result);
        verify(userRepository, times(1)).save(user);
        assertFalse(user.isBlocked());
    }

    @Test
    void testUnblockUserNotFound() {
        int userId = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        String result = userService.unblockUser(userId);

        assertEquals("User not found", result);
        verify(userRepository, never()).save(any(User.class));
    }
    @Mock
    private AdminRepo adminRepository;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    void testAuthenticateSuccessAdmin() {
        String email = "admin@example.com";
        String password = "adminpass";
        Admin admin = new Admin();
        admin.setEmail(email);
        admin.setPassword(password);
        
        // Mock the repository behavior
        when(adminRepository.findByEmailAndPassword(email, password)).thenReturn(Optional.of(admin));

        // Call the method under test
        Optional<Admin> result = adminService.authenticate(email, password);

        // Verify the result
        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());
        assertEquals(password, result.get().getPassword());
    }

    @Test
    void testAuthenticateFailureAdmin() {
        String email = "admin@example.com";
        String password = "wrongpass";
        
        // Mock the repository behavior
        when(adminRepository.findByEmailAndPassword(email, password)).thenReturn(Optional.empty());

        // Call the method under test
        Optional<Admin> result = adminService.authenticate(email, password);

        // Verify the result
        assertFalse(result.isPresent());
    }
    @Test
    void testAuthenticateWithSpecialCharacters() {
        String email = "admin!@example.com";
        String password = "admin#pass$";
        Admin admin = new Admin();
        admin.setEmail(email);
        admin.setPassword(password);
        
        // Mock the repository behavior
        when(adminRepository.findByEmailAndPassword(email, password)).thenReturn(Optional.of(admin));

        // Call the method under test
        Optional<Admin> result = adminService.authenticate(email, password);

        // Verify the result
        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());
        assertEquals(password, result.get().getPassword());
    }
    @Test
    void testAuthenticateWithMultipleMatchingEntries() {
        String email = "admin@example.com";
        String password = "adminpass";
        Admin admin1 = new Admin();
        admin1.setEmail(email);
        admin1.setPassword(password);
        
        Admin admin2 = new Admin();
        admin2.setEmail(email);
        admin2.setPassword(password);
        
        // Mock the repository behavior
        when(adminRepository.findByEmailAndPassword(email, password)).thenReturn(Optional.of(admin1));

        // Call the method under test
        Optional<Admin> result = adminService.authenticate(email, password);

        // Verify the result
        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());
        assertEquals(password, result.get().getPassword());
    }
    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private EmailService emailService;

    public void EmailServiceTest() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    void testSendEmailWithAttachment() throws MessagingException, IOException {
        String to = "test@example.com";
        String subject = "Test Subject";
        String text = "<p>Test Email</p>";

        MultipartFile attachment = mock(MultipartFile.class);
        when(attachment.getOriginalFilename()).thenReturn("testfile.txt");
        when(attachment.getBytes()).thenReturn("test content".getBytes());

        MimeMessage mimeMessage = mock(MimeMessage.class);
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(javaMailSender).send(mimeMessage);

        emailService.sendEmail(to, subject, text, attachment);

        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(mimeMessage);
    }
    @Test
    void testSendEmailWithoutAttachment() throws MessagingException, IOException {
        String to = "test@example.com";
        String subject = "Test Subject";
        String text = "<p>Test Email</p>";

        MimeMessage mimeMessage = mock(MimeMessage.class);
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(javaMailSender).send(mimeMessage);

        emailService.sendEmail(to, subject, text, null);

        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(mimeMessage);
    }
    @Test
    void testSendEmailWithAttachment1() throws MessagingException, IOException {
        String to = "test@example.com";
        String subject = "Test Subject";
        String text = "<p>Test Email</p>";

        MultipartFile attachment = mock(MultipartFile.class);
        when(attachment.getOriginalFilename()).thenReturn("testfile.txt");
        when(attachment.getBytes()).thenReturn("test content".getBytes());

        MimeMessage mimeMessage = mock(MimeMessage.class);
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(javaMailSender).send(mimeMessage);

        emailService.sendEmail(to, subject, text, attachment);

        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(mimeMessage);
    }
    @Test
    void testSendEmailWithEmptyAttachment() throws MessagingException, IOException {
        String to = "test@example.com";
        String subject = "Test Subject";
        String text = "<p>Test Email</p>";

        MultipartFile attachment = mock(MultipartFile.class);
        when(attachment.isEmpty()).thenReturn(true);

        MimeMessage mimeMessage = mock(MimeMessage.class);
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(javaMailSender).send(mimeMessage);

        emailService.sendEmail(to, subject, text, attachment);

        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(mimeMessage);
    }
//    @Test
//    void testSendEmailWithMessagingException() throws MessagingException, IOException {
//        String to = "test@example.com";
//        String subject = "Test Subject";
//        String text = "<p>Test Email</p>";
//
//        MimeMessage mimeMessage = mock(MimeMessage.class);
//        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
//
//        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
//        doThrow(new MessagingException("Failed to send email")).when(javaMailSender).send(mimeMessage);
//
//        MessagingException thrown = assertThrows(MessagingException.class, () -> {
//            emailService.sendEmail(to, subject, text, null);
//        });
//
//        assertEquals("Failed to send email", thrown.getMessage());
//    }



}

