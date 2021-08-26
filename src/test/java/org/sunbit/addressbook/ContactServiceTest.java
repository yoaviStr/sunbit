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

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

  @InjectMocks private ContactService contactService;
  @Mock private Trie<String, HashMap<Long, Contact>> contactTireByName;
  @Mock private MyKeyValueStorage<Contact> myKeyValueStorage;

  @Test
  @DisplayName("create contact ")
  void createContact() {
    Contact expected1 = Contact.builder().name("Dodo").phoneNumber("0542357223").id(1L).build();
    Contact input = Contact.builder().name("Dodo").phoneNumber("0542357223").build();

    when(myKeyValueStorage.createContact(input)).thenReturn(expected1);
    when(contactTireByName.get(input.getName())).thenReturn(null);

    contactService.createContact(input);

    verify(myKeyValueStorage, times(1)).createContact(input);
    verify(contactTireByName)
        .put(
            expected1.getName(),
            new HashMap<>() {
              {
                this.put(expected1.getId(), expected1);
              }
            });
  }

  @Test
  void createContact_duplicateName_twoContactsShouldExist() {
    Contact expected = Contact.builder().name("Dodo").phoneNumber("0542357223").id(2L).build();
    Contact input = Contact.builder().name("Dodo").phoneNumber("0542357223").build();

    when(myKeyValueStorage.createContact(input)).thenReturn(expected);
    when(contactTireByName.get(input.getName()))
        .thenReturn(
            new HashMap<>() {
              {
                this.put(
                    1L, Contact.builder().name("Dodo").phoneNumber("9992357223").id(1L).build());
              }
            });
    contactService.createContact(input);
    verify(myKeyValueStorage).createContact(input);
    verifyNoMoreInteractions(contactTireByName);
  }

  @Test
  void updateContact_differentPhoneNumber() {
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

    when(myKeyValueStorage.getContactById(1L)).thenReturn(contactToReplace);
    when(contactTireByName.get(contactToReplace.getName())).thenReturn(map);

    contactService.updateContact(input);
    assertThat(map.size()).isEqualTo(2);

  }

  @Test
  void updateContact_differentName() {
    Contact contactToReplace =
        Contact.builder().name("Dodo").phoneNumber("0542351234").id(1L).build();
    Contact input = Contact.builder().name("notDodo").phoneNumber("0542357223").id(1L).build();
    HashMap<Long, Contact> valueToReplaceMap =
        new HashMap<>() {
          {
            this.put(contactToReplace.getId(), contactToReplace);
          }
        };
    when(myKeyValueStorage.getContactById(contactToReplace.getId())).thenReturn(contactToReplace);
    when(contactTireByName.get(contactToReplace.getName())).thenReturn(valueToReplaceMap);

    contactService.updateContact(input);

    verify(contactTireByName).remove(contactToReplace.getName());
    verify(contactTireByName)
        .put(
            input.getName(),
            new HashMap<>() {
              {
                this.put(input.getId(), input);
              }
            });
    assertThat(valueToReplaceMap.size()).isEqualTo(0);
  }

  @Test
  void updateContact_contactDoesNotExist() {
    Contact contactToReplace =
        Contact.builder().name("Dodo").phoneNumber("0542351234").id(1L).build();

    doThrow(new ResourceNotFoundException(""))
        .when(myKeyValueStorage)
        .getContactById(contactToReplace.getId());

    Assertions.assertThrows(
        ResourceNotFoundException.class, () -> contactService.updateContact(contactToReplace));
  }

  @Test
  void deleteContact() {
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
    when(myKeyValueStorage.getContactById(1L)).thenReturn(contactToDelete);
    when(contactTireByName.get(contactToDelete.getName())).thenReturn(map);

    contactService.removeContact(1L);

    verify(myKeyValueStorage).removeContactById(1L);

    assertThat(map.size()).isEqualTo(1);

  }

  @Test
  void deleteContact_contactDoesNotExist() {
    doThrow(new ResourceNotFoundException("")).when(myKeyValueStorage).getContactById(1L);
    Assertions.assertThrows(
        ResourceNotFoundException.class, () -> contactService.removeContact(1L));
  }
  @Test
  void readById() {
    Contact readContactById =
            Contact.builder().name("Dodo").phoneNumber("0542351234").id(1L).build();

    when(myKeyValueStorage.getContactById(1L)).thenReturn(readContactById);

    Contact contact = contactService.readContact(1L);

    verify(myKeyValueStorage).getContactById(1L);
    assertEquals(contact,readContactById);
    assertThat(readContactById).isEqualTo(contact);
  }

  @Test
  void readById_ItemDoesNotExist() {
    doThrow(new ResourceNotFoundException("")).when(myKeyValueStorage).getContactById(1L);
    Assertions.assertThrows(
            ResourceNotFoundException.class, () -> contactService.readContact(1L));
  }

  @Test
  void readByPrefix_ItemDoesNotExist() {
    Contact expected1 = Contact.builder().name("Dodo").phoneNumber("0542357223").id(1L).build();
    Contact expected2 = Contact.builder().name("Dodo1").phoneNumber("0542357223").id(2L).build();
    Contact expected3 = Contact.builder().name("Dodo12").phoneNumber("0542357223").id(3L).build();
    Contact expected4 = Contact.builder().name("Dodo123").phoneNumber("0542357223").id(4L).build();
    Contact expected5 = Contact.builder().name("Dodo").phoneNumber("0542357223").id(5L).build();




  }





}

  //
  //    Map<Long, Contact> spyMap = Mockito.spy(new HashMap<>());
  //    ReflectionTestUtils.setField(contactService, "idToContactMap", spyMap);
  //    Trie<String, List<Contact>> spyTire = Mockito.spy(new PatriciaTrie<>());
  //    ReflectionTestUtils.setField(contactService, "contactTireByName", spyTire);
  //
  //    Contact updated = Contact.builder().name("Dodo").phoneNumber("2222222222").id(1L).build();
  //
  //    Assertions.assertThrows(
  //        IllegalStateException.class, () -> contactService.updateContact(updated));
  //  }
  //
  //  @Test
  //  void deleteContact_oneContactForName_shouldRemoveTriedEntry() {
  //    Contact created = Contact.builder().name("Dodo").phoneNumber("1111111111").id(1L).build();
  //
  //    Map<Long, Contact> spyMap = Mockito.spy(new HashMap<>());
  //    ReflectionTestUtils.setField(contactService, "idToContactMap", spyMap);
  //    Trie<String, List<Contact>> spyTire = Mockito.spy(new PatriciaTrie<>());
  //    ReflectionTestUtils.setField(contactService, "contactTireByName", spyTire);
  //
  //    spyMap.put(created.getId(), created);
  //    spyTire.put(
  //        created.getName(),
  //        new ArrayList<>() {
  //          {
  //            this.add(created);
  //          }
  //        });
  //
  //    contactService.deleteContact(created.getId());
  //
  //    assertEquals(0, spyTire.size());
  //    assertEquals(0, spyMap.size());
  //  }
  //
  //  @Test
  //  void deleteContact_twoContactForName_shouldNotRemoveTriedEntry() {
  //    Contact created1 = Contact.builder().name("Dodo").phoneNumber("1111111111").id(1L).build();
  //    Contact created2 = Contact.builder().name("Dodo").phoneNumber("1111111111").id(2L).build();
  //
  //    Map<Long, Contact> spyMap = Mockito.spy(new HashMap<>());
  //    ReflectionTestUtils.setField(contactService, "idToContactMap", spyMap);
  //    Trie<String, List<Contact>> spyTire = Mockito.spy(new PatriciaTrie<>());
  //    ReflectionTestUtils.setField(contactService, "contactTireByName", spyTire);
  //
  //    spyMap.put(created1.getId(), created1);
  //    spyMap.put(created2.getId(), created2);
  //    spyTire.put(
  //        created1.getName(),
  //        new ArrayList<>() {
  //          {
  //            this.add(created1);
  //            this.add(created2);
  //          }
  //        });
  //
  //    contactService.deleteContact(created1.getId());
  //
  //    assertEquals(1, spyTire.size());
  //    assertEquals(1, spyMap.size());
  //  }
  //
  //  @Test
  //  void deleteContact_ContactDoesNotExist() {
  //    Assertions.assertThrows(IllegalStateException.class, () ->
  // contactService.deleteContact(1L));
  //  }
  //
  //  @Test
  //  void readContactByPrefix() {
  //    readHelper();
  //    List<Contact> list = contactService.readByPrefix("Dodo");
  //    assertEquals(list.size(), 3);
  //    list.forEach(contact -> assertEquals(contact.getName().substring(0, 4), "Dodo"));
  //  }
  //
  //  @Test
  //  void readById() {
  //    readHelper();
  //    assertEquals(contactService.readContact(1L).getId(), 1L);
  //  }
  //
  //  @Test
  //  void readById_resourceDoesntExist() {
  //    Assertions.assertThrows(
  //        IllegalStateException.class, () -> contactService.readContact(7L).getId());
  //  }
  //
  //  @Test
  //  void readContactByPrefix_emptyResult() {
  //    readHelper();
  //    List<Contact> list = contactService.readByPrefix("shobiDobi");
  //    assertEquals(list.size(), 0);
  //  }
  //
  //  private void readHelper() {
  //    Contact created1 = Contact.builder().name("Dodo").phoneNumber("1111111111").id(1L).build();
  //    Contact created2 = Contact.builder().name("Dodo").phoneNumber("1234566787").id(2L).build();
  //    Contact created3 = Contact.builder().name("Dodo123").phoneNumber("11344343").id(3L).build();
  //    Contact created4 = Contact.builder().name("dobiGal").phoneNumber("77777777").id(4L).build();
  //
  //    Map<Long, Contact> spyMap = Mockito.spy(new HashMap<>());
  //    ReflectionTestUtils.setField(contactService, "idToContactMap", spyMap);
  //    Trie<String, List<Contact>> spyTire = Mockito.spy(new PatriciaTrie<>());
  //    ReflectionTestUtils.setField(contactService, "contactTireByName", spyTire);
  //
  //    spyMap.put(created1.getId(), created1);
  //    spyMap.put(created2.getId(), created2);
  //    spyMap.put(created3.getId(), created3);
  //    spyMap.put(created4.getId(), created4);
  //    spyTire.put(
  //        created1.getName(),
  //        new ArrayList<>() {
  //          {
  //            this.add(created1);
  //            this.add(created2);
  //          }
  //        });
  //    spyTire.put(
  //        created3.getName(),
  //        new ArrayList<>() {
  //          {
  //            this.add(created3);
  //          }
  //        });
  //    spyTire.put(
  //        created4.getName(),
  //        new ArrayList<>() {
  //          {
  //            this.add(created4);
  //          }
  //        });
  //  }
