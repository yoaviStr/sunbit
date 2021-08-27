package org.sunbit.addressbook;

import org.springframework.stereotype.Component;
import org.sunbit.addressbook.exception.ResourceNotFoundException;
import org.sunbit.addressbook.model.Contact;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class MyKeyValueStorage<V extends Contact> {

    Map<Long, V> m = new HashMap<>();
    AtomicLong generator = new AtomicLong(1);

    public V getContactById(Long id) {
        return Optional.ofNullable(m.get(id))
                .orElseThrow(() -> new ResourceNotFoundException("contact id " + id));
    }

    public V createContact(V c) {
        Long andIncrement = generator.getAndIncrement();
        c.setId(andIncrement);
        m.put(andIncrement, c);
        return c;
    }

    public V removeContactById(Long id) {
        return m.remove(id);
    }

    public V updateContact(Long id, V c) {
        return m.put(id, c);
    }

}
