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

    public Contact create(Contact contact) {
        Contact createdContact = myKeyValueStorage.create(contact);
        addContactToTire(createdContact);
        return createdContact;
    }

    public Contact update(Contact contact) {
        Contact oldContact = myKeyValueStorage.getById(contact.getId());
        removeContactFromTire(oldContact);
        myKeyValueStorage.update(contact.getId(), contact);
        addContactToTire(contact);
        return contact;
    }

    public Contact get(long contactId) {
        return myKeyValueStorage.getById(contactId);
    }

    public void remove(Long id) {
        Contact contactById = myKeyValueStorage.getById(id);
        removeContactFromTire(contactById);
        myKeyValueStorage.removeById(id);
    }

    private void removeContactFromTire(Contact contact) {
        Map<Long, Contact> contactsMap = contactTireByName.get(contact.getName());
        contactsMap.remove(contact.getId(), contact);
        if (CollectionUtils.isEmpty(contactsMap)) {
            contactTireByName.remove(contact.getName());
        }
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
