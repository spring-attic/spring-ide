package com.example.demo;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("my")
public class MyConfProps {
	
	private String better;

	public String getBetter() {
		return better;
	}

	public void setBetter(String better) {
		this.better = better;
	}


}
