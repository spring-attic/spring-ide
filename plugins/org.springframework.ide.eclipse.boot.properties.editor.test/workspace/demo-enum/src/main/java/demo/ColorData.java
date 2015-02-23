package demo;

import java.util.List;
import java.util.Map;

public class ColorData {

	private double wavelen;
	private String name;
	private Color next;
	private ColorData nested;
	private List<ColorData> children;
	private List<String> tags;
	private Map<String, ColorData> mappedChildren;
	private Map<Color, ColorData> colorChildren;

	public double getWavelen() {
		return wavelen;
	}
	public void setWavelen(double wavelen) {
		this.wavelen = wavelen;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Color getNext() {
		return next;
	}
	public void setNext(Color next) {
		this.next = next;
	}
	public List<ColorData> getChildren() {
		return children;
	}
	public void setChildren(List<ColorData> children) {
		this.children = children;
	}
	public ColorData getNested() {
		return nested;
	}
	public void setNested(ColorData nested) {
		this.nested = nested;
	}
	public Map<String, ColorData> getMappedChildren() {
		return mappedChildren;
	}
	public void setMappedChildren(Map<String, ColorData> mappedChildren) {
		this.mappedChildren = mappedChildren;
	}
	public Map<Color, ColorData> getColorChildren() {
		return colorChildren;
	}
	public void setColorChildren(Map<Color, ColorData> colorChildren) {
		this.colorChildren = colorChildren;
	}
	public List<String> getTags() {
		return tags;
	}
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
}
