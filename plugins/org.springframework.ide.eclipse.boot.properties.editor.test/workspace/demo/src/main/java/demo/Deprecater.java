package demo;

public class Deprecater {

	@Deprecated
	private String oldName;

	@Deprecated
	public String getOldName() {
		return oldName;
	}

	@Deprecated
	public void setOldName(String oldName) {
		this.oldName = oldName;
	}

}
