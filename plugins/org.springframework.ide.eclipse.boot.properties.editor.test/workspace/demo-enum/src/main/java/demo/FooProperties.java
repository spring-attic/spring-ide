package demo;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("foo")
public class FooProperties {

	private Color color;
	private Map<Color,String> colorNames;
	private Map<String,Color> nameColors;

	public Color getColor() {
		return color;
	}
	public void setColor(Color color) {
		this.color = color;
	}

	public Map<Color,String> getColorNames() {
		return colorNames;
	}
	public void setColorNames(Map<Color,String> colorNames) {
		this.colorNames = colorNames;
	}
	public Map<String,Color> getNameColors() {
		return nameColors;
	}
	public void setNameColors(Map<String,Color> nameColors) {
		this.nameColors = nameColors;
	}
}
