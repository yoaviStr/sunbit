package org.sunbit.addressbook;

import com.github.veqryn.collect.PatriciaTrie;
import com.github.veqryn.collect.Trie;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.sunbit.addressbook.model.Contact;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class AppConfig {

  @Bean
  public Trie<String, Map<Long,Contact>> contactTireByName() {
    return new PatriciaTrie<>();
  }
}
