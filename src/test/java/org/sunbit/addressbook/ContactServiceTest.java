package org.sunbit.addressbook;

import com.github.veqryn.collect.Trie;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sunbit.addressbook.exception.ResourceNotFoundException;
import org.sunbit.addressbook.model.Contact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @InjectMocks
    private ContactService contactService;
    @Mock
    private Trie<String, HashMap<Long, Contact>> contactTireByName;
    @Mock
    private MyKeyValueStorage<Contact> myKeyValueStorage;

    @Test
    @DisplayName("create contact ")
    void create() {
        Contact expected = Contact.builder().name("Dodo").phoneNumber("0542357223").id(1L).build();
        Contact input = Contact.builder().name("Dodo").phoneNumber("0542357223").build();

        when(myKeyValueStorage.create(input)).thenReturn(expected);
        when(contactTireByName.get(input.getName())).thenReturn(null);

        Contact contact = contactService.create(input);

        verify(myKeyValueStorage).create(input);
        verify(contactTireByName)
                .put(
                        expected.getName(),
                        new HashMap<>() {
                            {
                                this.put(expected.getId(), expected);
                            }
                        });
        assertThat(contact).isEqualTo(expected);
    }

    @Test
    void create_duplicateName_twoContactsShouldExist() {
        Contact expected = Contact.builder().name("Dodo").phoneNumber("0542357223").id(2L).build();
        Contact input = Contact.builder().name("Dodo").phoneNumber("0542357223").build();

        when(myKeyValueStorage.create(input)).thenReturn(expected);
        when(contactTireByName.get(input.getName()))
                .thenReturn(
                        new HashMap<>() {
                            {
                                this.put(
                                        1L, Contact.builder().name("Dodo").phoneNumber("9992357223").id(1L).build());
                            }
                        });
        Contact contact = contactService.create(input);

        verify(myKeyValueStorage).create(input);
        verifyNoMoreInteractions(contactTireByName);
        assertThat(contact).isEqualTo(expected);

    }

    @Test
    void update_differentPhoneNumber() {
        Contact contactToReplace =
                Contact.builder().name("Dodo").phoneNumber("0542351234").id(1L).build();
        Contact existingContact =
                Contact.builder().name("Dodo").phoneNumber("7777777777").id(9L).build();
        Contact input = Contact.builder().name("Dodo").phoneNumber("99999999999").id(1L).build();
        HashMap<Long, Contact> map =
                new HashMap<>() {
                    {
                        this.put(contactToReplace.getId(), contactToReplace);
                        this.put(existingContact.getId(), existingContact);
                    }
                };

        when(myKeyValueStorage.getById(1L)).thenReturn(contactToReplace);
        when(contactTireByName.get(contactToReplace.getName())).thenReturn(map);

        Contact update = contactService.update(input);

        assertThat(map.size()).isEqualTo(2);
        assertThat(update).isEqualTo(input);
    }

    @Test
    void update_differentName() {
        Contact contactToReplace =
                Contact.builder().name("Dodo").phoneNumber("0542351234").id(1L).build();
        Contact input = Contact.builder().name("notDodo").phoneNumber("0542357223").id(1L).build();
        HashMap<Long, Contact> valueToReplaceMap =
                new HashMap<>() {
                    {
                        this.put(contactToReplace.getId(), contactToReplace);
                    }
                };
        when(myKeyValueStorage.getById(contactToReplace.getId())).thenReturn(contactToReplace);
        when(contactTireByName.get(contactToReplace.getName())).thenReturn(valueToReplaceMap);

        Contact update = contactService.update(input);

        verify(contactTireByName).remove(contactToReplace.getName());
        verify(contactTireByName)
                .put(
                        input.getName(),
                        new HashMap<>() {
                            {
                                this.put(input.getId(), input);
                            }
                        });
        assertThat(update).isEqualTo(input);
        assertThat(valueToReplaceMap.size()).isEqualTo(0);
    }

    @Test
    void update_contactDoesNotExist() {
        Contact contactToReplace =
                Contact.builder().name("Dodo").phoneNumber("0542351234").id(1L).build();

        doThrow(new ResourceNotFoundException(""))
                .when(myKeyValueStorage)
                .getById(contactToReplace.getId());

        Assertions.assertThrows(
                ResourceNotFoundException.class, () -> contactService.update(contactToReplace));
    }

    @Test
    void delete() {
        Contact contactToDelete =
                Contact.builder().name("Dodo").phoneNumber("0542351234").id(1L).build();
        Contact existingContact =
                Contact.builder().name("Dodo").phoneNumber("7777777777").id(9L).build();
        HashMap<Long, Contact> map =
                new HashMap<>() {
                    {
                        this.put(contactToDelete.getId(), contactToDelete);
                        this.put(existingContact.getId(), existingContact);
                    }
                };
        when(myKeyValueStorage.getById(1L)).thenReturn(contactToDelete);
        when(contactTireByName.get(contactToDelete.getName())).thenReturn(map);

        contactService.remove(1L);

        verify(myKeyValueStorage).removeById(1L);
        assertThat(map.size()).isEqualTo(1);
    }

    @Test
    void delete_contactDoesNotExist() {
        doThrow(new ResourceNotFoundException("")).when(myKeyValueStorage).getById(1L);
        Assertions.assertThrows(
                ResourceNotFoundException.class, () -> contactService.remove(1L));
    }

    @Test
    void readById() {
        Contact readContactById =
                Contact.builder().name("Dodo").phoneNumber("0542351234").id(1L).build();

        when(myKeyValueStorage.getById(1L)).thenReturn(readContactById);

        Contact contact = contactService.get(1L);

        verify(myKeyValueStorage).getById(1L);
        assertEquals(contact, readContactById);
        assertThat(readContactById).isEqualTo(contact);
    }

    @Test
    void readById_ItemDoesNotExist() {
        doThrow(new ResourceNotFoundException("")).when(myKeyValueStorage).getById(1L);
        Assertions.assertThrows(
                ResourceNotFoundException.class, () -> contactService.get(1L));
    }

    @Test
    void readByPrefix() {
        Contact expected1 = Contact.builder().name("Dodo").phoneNumber("0542357223").id(1L).build();
        Contact expected2 = Contact.builder().name("Dodo1").phoneNumber("0542357223").id(2L).build();

        ArrayList<HashMap<Long, Contact>> maps = new ArrayList<>();
        HashMap<Long, Contact> e1 = new HashMap<>();
        e1.put(1L, expected1);
        HashMap<Long, Contact> e2 = new HashMap<>();
        e2.put(2L, expected2);
        maps.add(e1);
        maps.add(e2);

        when(contactTireByName.prefixedByValues("Dodo", true)).thenReturn(maps);

        List<Contact> list = contactService.readByPrefix("Dodo");
        assertThat(list.size()).isEqualTo(2);
    }

    @Test
    void readByPrefix_emptyResult() {
        ArrayList<HashMap<Long, Contact>> maps = new ArrayList<>();

        when(contactTireByName.prefixedByValues("Dodo", true)).thenReturn(maps);

        List<Contact> list = contactService.readByPrefix("Dodo");
        assertThat(list.size()).isEqualTo(0);
    }

}

