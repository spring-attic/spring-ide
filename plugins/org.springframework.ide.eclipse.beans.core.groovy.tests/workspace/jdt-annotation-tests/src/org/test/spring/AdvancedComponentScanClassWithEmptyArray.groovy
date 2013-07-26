package org.test.spring;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;

@ComponentScan(excludeFilters = @Filter({ }))
public class AdvancedComponentScanClassWithEmptyArray {
}
