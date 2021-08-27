package org.sunbit.addressbook.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.sunbit.addressbook.ContactService;
import org.sunbit.addressbook.model.Contact;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Log4j2
class ContactControllerTest {
    @LocalServerPort
    private int port;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ContactService contactService;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    @SneakyThrows
    void create() {
        Contact dodo = Contact.builder().name("Dodo").phoneNumber("123456").build();
        String body = objectMapper.writeValueAsString(dodo);
        ResponseEntity<Contact> response =
                restTemplate.exchange(
                        getBaseUrl(), HttpMethod.POST, new HttpEntity(body, getHttpHeaders()), Contact.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(contactService.readContact(response.getBody().getId()));
    }

    @Test
    @SneakyThrows
    void create_itemWithId_shouldFail() {
        Contact dodo = Contact.builder().name("Dodo").phoneNumber("123456").id(1L).build();
        String body = objectMapper.writeValueAsString(dodo);
        ResponseEntity response =
                restTemplate.exchange(
                        getBaseUrl(), HttpMethod.POST, new HttpEntity(body, getHttpHeaders()), Contact.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @SneakyThrows
    void create_itemWithoutName_shouldFail() {
        Contact dodo = Contact.builder().phoneNumber("123456").build();
        String body = objectMapper.writeValueAsString(dodo);
        ResponseEntity response =
                restTemplate.exchange(
                        getBaseUrl(), HttpMethod.POST, new HttpEntity(body, getHttpHeaders()), Contact.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @SneakyThrows
    void update() {
        Contact createdContact =
                contactService.createContact(Contact.builder().name("Dodo").phoneNumber("123456").build());
        Long id = createdContact.getId();
        Contact update = Contact.builder().name("Dodo_aaa").phoneNumber("9876543").id(id).build();

        String body = objectMapper.writeValueAsString(update);
        ResponseEntity<Contact> response =
                restTemplate.exchange(
                        getBaseUrl() + "/" + id, HttpMethod.PUT, new HttpEntity(body, getHttpHeaders()), Contact.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(update).isEqualTo(contactService.readContact(id));

    }

    @Test
    @SneakyThrows
    void update_nonExistingId() {
        Long id = 99L;
        Contact update = Contact.builder().name("Dodo_aaa").phoneNumber("9876543").id(id).build();

        String body = objectMapper.writeValueAsString(update);
        ResponseEntity<Contact> response =
                restTemplate.exchange(
                        getBaseUrl() + "/" + id, HttpMethod.PUT, new HttpEntity(body, getHttpHeaders()), Contact.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    }

    @Test
    @SneakyThrows
    void update_idMismatch() {
        Long id = 99L;
        Contact update = Contact.builder().name("Dodo_aaa").phoneNumber("9876543").id(id).build();

        String body = objectMapper.writeValueAsString(update);
        ResponseEntity<Contact> response =
                restTemplate.exchange(
                        getBaseUrl() + "/" + id + 1L, HttpMethod.PUT, new HttpEntity(body, getHttpHeaders()),
                        Contact.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void delete() {
    }

    @Test
    void readContact() {
    }

    @Test
    void readContactByPrefix() {
        Stream.of(
                Contact.builder().name("dobi").phoneNumber("123456").build(),
                Contact.builder().name("dobi12").phoneNumber("123456").build(),
                Contact.builder().name("albert").phoneNumber("122226").build(),
                Contact.builder().name("albert12").phoneNumber("88888888").build(),
                Contact.builder().name("yo12").phoneNumber("9999998").build(),
                Contact.builder().name("yo123").phoneNumber("77777777").build()
        ).map(it -> contactService.createContact(it)).collect(Collectors.toList());

        ResponseEntity<List> response =
                restTemplate.exchange(
                        getBaseUrl() + "?contactPrefix=albert", HttpMethod.GET, new HttpEntity(getHttpHeaders()),
                        List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().size()).isEqualTo(2);
    }
    @Test
    void readContactByPrefix_empty() {
        Stream.of(
                Contact.builder().name("dobi").phoneNumber("123456").build(),
                Contact.builder().name("dobi12").phoneNumber("123456").build(),
                Contact.builder().name("albert").phoneNumber("122226").build(),
                Contact.builder().name("albert12").phoneNumber("88888888").build(),
                Contact.builder().name("yo12").phoneNumber("9999998").build(),
                Contact.builder().name("yo123").phoneNumber("77777777").build()
        ).map(it -> contactService.createContact(it)).collect(Collectors.toList());

        ResponseEntity<List> response =
                restTemplate.exchange(
                        getBaseUrl() + "?contactPrefix=xxxx", HttpMethod.GET, new HttpEntity(getHttpHeaders()),
                        List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().size()).isEqualTo(0);
    }


    private String getBaseUrl() {
        return "http://localhost:" + port + "/v1/contact";
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

}