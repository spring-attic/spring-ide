package com.example.demo;

import java.util.List;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient.ListImagesParam;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Image;

public class SpotifyDockerClientWrapperApplication {

	private static final String DEFAULT_UNIX_DOCKER_URL = "unix:///var/run/docker.sock";

	public static void main(String[] args) throws DockerException, InterruptedException {
		DefaultDockerClient client = DefaultDockerClient.builder().uri(DEFAULT_UNIX_DOCKER_URL).build();
		
		
		List<Image> imgs = client.listImages(ListImagesParam.allImages());
		for (Image image : imgs) {
			System.out.println(image.id());
		}
	}

}
