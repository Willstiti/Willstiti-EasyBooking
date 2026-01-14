package com.efrei.easybooking.Controller;

import com.efrei.easybooking.DTO.LoginDTO;
import com.efrei.easybooking.DTO.RegisterDTO;
import com.efrei.easybooking.Repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class AuthControllerSystemTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @BeforeEach
    void setUp() {
        utilisateurRepository.deleteAll();
    }

    @Test
    void RegisterRetourne201QuandDonneesValides() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO("test@test.com", "password123");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Compte créé avec succès"));
    }

    @Test
    void RegisterCreeUtilisateurEnBase() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO("test@test.com", "password123");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isCreated());

        assertTrue(utilisateurRepository.findByEmail("test@test.com").isPresent());
        assertEquals("password123", utilisateurRepository.findByEmail("test@test.com").get().getPassword());
    }

    @Test
    void RegisterRetourne400QuandEmailDejaExistant() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO("test@test.com", "password123");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void LoginRetourne200QuandIdentifiantsCorrects() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO("test@test.com", "password123");
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isCreated());

        LoginDTO loginDTO = new LoginDTO("test@test.com", "password123");

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("Connexion réussie"));
    }

    @Test
    void LoginCreeSessionAvecUserIdEtEmail() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO("test@test.com", "password123");
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isCreated());

        LoginDTO loginDTO = new LoginDTO("test@test.com", "password123");

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(request().sessionAttribute("userId", org.hamcrest.Matchers.notNullValue()))
                .andExpect(request().sessionAttribute("email", "test@test.com"));
    }

    @Test
    void LoginRetourne400QuandEmailInconnu() throws Exception {
        LoginDTO loginDTO = new LoginDTO("unknown@test.com", "password123");

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void LoginRetourne400QuandMotDePasseIncorrect() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO("test@test.com", "password123");
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isCreated());

        LoginDTO loginDTO = new LoginDTO("test@test.com", "wrongPassword");

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void LogoutRetourne200EtInvalideSession() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO("test@test.com", "password123");
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isCreated());

        LoginDTO loginDTO = new LoginDTO("test@test.com", "password123");
        var loginResult = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(request().sessionAttribute("userId", org.hamcrest.Matchers.notNullValue()))
                .andReturn();

        var session = loginResult.getRequest().getSession();
        assertNotNull(session.getAttribute("userId"));

        mockMvc.perform(post("/logout")
                        .session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().isOk())
                .andExpect(content().string("Déconnexion réussie"));
    }

    @Test
    void LoginApresRegisterFonctionne() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO("test@test.com", "password123");
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isCreated());

        LoginDTO loginDTO = new LoginDTO("test@test.com", "password123");
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("Connexion réussie"));
    }

    @Test
    void RegisterAvecEmailDifferentFonctionne() throws Exception {
        RegisterDTO registerDTO1 = new RegisterDTO("user1@test.com", "password123");
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO1)))
                .andExpect(status().isCreated());

        RegisterDTO registerDTO2 = new RegisterDTO("user2@test.com", "password456");
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO2)))
                .andExpect(status().isCreated());

        assertTrue(utilisateurRepository.findByEmail("user1@test.com").isPresent());
        assertTrue(utilisateurRepository.findByEmail("user2@test.com").isPresent());
    }
}
