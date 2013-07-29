package com.springsource;

import org.springframework.beans.factory.annotation.Qualifier;

@Qualifier("special")
@SimpleValueQualifier("special")
public class SpecialPerson extends Person {
}
