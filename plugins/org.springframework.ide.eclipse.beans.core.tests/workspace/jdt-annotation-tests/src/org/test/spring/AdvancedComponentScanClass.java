package org.test.spring;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

@ComponentScan(excludeFilters = @Filter({ Service.class, Configuration.class, SimpleBeanClass.class }))
public class AdvancedComponentScanClass {
}
