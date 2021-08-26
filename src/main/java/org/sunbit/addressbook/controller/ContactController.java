package org.sunbit.addressbook.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.sunbit.addressbook.ContactService;
import org.sunbit.addressbook.model.Contact;
import org.sunbit.addressbook.validator.CreateGroup;
import org.sunbit.addressbook.validator.UpdateGroup;

import javax.validation.constraints.NotNull;
import java.util.List;

@Log4j2
@RestController
@RequestMapping(value = "/v1/contact", produces = MediaType.APPLICATION_JSON_VALUE)
public class ContactController {

  private final ContactService contactService;

  public ContactController(ContactService contactService) {
    this.contactService = contactService;
  }

  @PostMapping
  public ResponseEntity createContact(
      @RequestBody @Validated(value = {CreateGroup.class}) Contact contact) {
    return new ResponseEntity<>(contactService.createContact(contact), HttpStatus.CREATED);
  }

  @PutMapping("/{contactId}")
  public ResponseEntity<Contact> updateContact(
      @PathVariable(value = "contactId") long contactId,
      @RequestBody
          @Validated(value = {UpdateGroup.class})
          @NotNull(message = "contact must not be null")
          Contact contact) {
    if (hasContactIdMismatch(contactId, contact)) return ResponseEntity.badRequest().build();

    return new ResponseEntity<>(contactService.updateContact(contact), HttpStatus.OK);
  }

  @DeleteMapping("/{contactId}")
  public ResponseEntity delete(@PathVariable(value = "contactId") long contactId) {
    contactService.removeContact(contactId);
    return new ResponseEntity("deleted contactId successfully", HttpStatus.NO_CONTENT);
  }

  @GetMapping("/{contactId}")
  public ResponseEntity<Contact> readContact(@PathVariable(value = "contactId") long contactId) {
    return new ResponseEntity<>(contactService.readContact(contactId), HttpStatus.OK);
  }
  //
  //  // paging ?
    @GetMapping
    public ResponseEntity<List<Contact>> readContactByPrefix(
        @RequestParam("contactPrefix") String contactPrefix) {
      return new ResponseEntity<>(contactService.readByPrefix(contactPrefix), HttpStatus.OK);
    }

  private boolean hasContactIdMismatch(long contactId, Contact contact) {
    return ((contact.getId() == null) || (!contact.getId().equals(contactId)));
  }
}
