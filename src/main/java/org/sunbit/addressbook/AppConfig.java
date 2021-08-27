package org.sunbit.addressbook;

import com.github.veqryn.collect.PatriciaTrie;
import com.github.veqryn.collect.Trie;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.sunbit.addressbook.model.Contact;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Map;

@Configuration
public class AppConfig {

    @Bean
    public Trie<String, Map<Long, Contact>> contactTireByName() {
        return new PatriciaTrie<>();
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }
}
