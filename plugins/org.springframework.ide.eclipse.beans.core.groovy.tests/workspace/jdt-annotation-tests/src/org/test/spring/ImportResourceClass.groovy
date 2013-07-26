package org.test.spring;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource("classpath:/com/acme/database-config.xml")
@Import(value={SimpleConfigurationClass.class, SimpleBeanClass.class})
public class ImportResourceClass {
}
