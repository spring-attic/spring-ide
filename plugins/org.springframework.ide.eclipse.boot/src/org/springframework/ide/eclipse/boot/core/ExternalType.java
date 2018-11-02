package org.springframework.ide.eclipse.boot.core;

/**
 * An instance of this class identifies a known external type. I.e. a type that
 * can be added to a project's classpath somehow but is not yet on the classpath.
 */
public class ExternalType implements Comparable<ExternalType> {
	private String name;
	private String packageName;

	public ExternalType(String name, String pkg) {
		this.name = name;
		this.packageName = pkg;
	}

	public ExternalType(String fqName) {
		int split = fqName.lastIndexOf('.');
		if (split>0) {
			this.name = fqName.substring(split+1);
			this.packageName = fqName.substring(0, split);
		} else {
			throw new IllegalArgumentException("Invalid fqName: "+fqName);
		}
		//throw new Error("This is inefficient, someone should optimize package names to reuse the strings");
	}

	@Override
	public String toString() {
		return name+" in "+packageName;
	}

	public String getName() {
		return name;
	}

	public String getPackage() {
		return packageName;
	}

	public String getFullyQualifiedName() {
		return packageName+"."+name;
	}

	@Override
	public int compareTo(ExternalType o) {
		int comp = name.compareTo(o.name);
		if (comp==0) {
			comp = packageName.compareTo(o.packageName);
		}
		return comp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((packageName == null) ? 0 : packageName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExternalType other = (ExternalType) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (packageName == null) {
			if (other.packageName != null)
				return false;
		} else if (!packageName.equals(other.packageName))
			return false;
		return true;
	}


}
