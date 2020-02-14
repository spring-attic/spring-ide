package org.springframework.ide.eclipse.maven.pom;

@FunctionalInterface
public interface IdProvider {

	Object id(DomStructureComparable.Builder node);

}
