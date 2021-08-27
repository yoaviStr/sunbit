package org.sunbit.addressbook;

import org.springframework.stereotype.Component;
import org.sunbit.addressbook.exception.ResourceNotFoundException;
import org.sunbit.addressbook.model.BaseEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class MyKeyValueStorage<V extends BaseEntity> {

    private Map<Long, V> m = new HashMap<>();
    private AtomicLong generator = new AtomicLong(1L);

    public V getById(Long id) {
        return Optional.ofNullable(m.get(id))
                .orElseThrow(() -> new ResourceNotFoundException("contact id " + id));
    }

    public V create(V c) {
        Long andIncrement = generator.getAndIncrement();
        c.setId(andIncrement);
        m.put(andIncrement, c);
        return c;
    }

    public V removeById(Long id) {
        return m.remove(id);
    }

    public V update(Long id, V c) {
        return m.put(id, c);
    }

}
