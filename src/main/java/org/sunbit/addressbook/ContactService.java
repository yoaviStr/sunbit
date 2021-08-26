package org.sunbit.addressbook;

import com.github.veqryn.collect.Trie;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.sunbit.addressbook.model.Contact;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Log4j2
public class ContactService {

  private final Trie<String, Map<Long, Contact>> contactTireByName;
  private final MyKeyValueStorage<Contact> myKeyValueStorage;

  public ContactService(
      Trie<String, Map<Long, Contact>> contactTireByName, MyKeyValueStorage myKeyValueStorage) {
    this.contactTireByName = contactTireByName;
    this.myKeyValueStorage = myKeyValueStorage;
  }

  public Contact createContact(Contact contact) {
    Contact createdContact = myKeyValueStorage.createContact(contact);
    addContactToTire(createdContact);
    return contact;
  }

  public Contact updateContact(Contact contact) {
    Contact oldContact = myKeyValueStorage.getContactById(contact.getId());
    removeContactFromTire(oldContact);
    myKeyValueStorage.updateContact(contact.getId(), contact);
    addContactToTire(contact);
    return contact;
  }

  public void removeContact(Long id) {
    Contact contactById = myKeyValueStorage.getContactById(id);
    removeContactFromTire(contactById);
    myKeyValueStorage.removeContactById(id);
  }

  private void removeContactFromTire(Contact contact) {
    Map<Long, Contact> contactsMap = contactTireByName.get(contact.getName());
    contactsMap.remove(contact.getId(),contact);
    if (CollectionUtils.isEmpty(contactsMap)) {
      contactTireByName.remove(contact.getName());
    }
  }

  public Contact readContact(long contactId) {
    return myKeyValueStorage.getContactById(contactId);
  }

  public List<Contact> readByPrefix(String prefix) {
    return contactTireByName.prefixedByValues(prefix, true).stream()
        .flatMap((coll) -> coll.values().stream())
        .collect(Collectors.toList());
  }

  private void addContactToTire(Contact contact) {
    Map<Long, Contact> contactsMap = contactTireByName.get(contact.getName());
    if (CollectionUtils.isEmpty(contactsMap)) {
      contactsMap = new HashMap<>();
      contactTireByName.put(contact.getName(), contactsMap);
    }
    contactsMap.put(contact.getId(), contact);
  }
}
