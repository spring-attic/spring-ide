package demo;

public class Deprecater {

	private String newName;
	@Deprecated private String name;

	///////////////////


	@Deprecated
	public String getName() {
		return name;
	}

	@Deprecated
	public void setName(String oldName) {
		this.name = oldName;
	}

	public String getNewName() {
		return newName;
	}

	public void setNewName(String newName) {
		this.newName = newName;
	}

}
