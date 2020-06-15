package org.springframework.ide.eclipse.boot.wizard.github;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;

import com.fasterxml.jackson.databind.ObjectMapper;

@Produces(MediaType.APPLICATION_JSON)
@SuppressWarnings({"rawtypes"})
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {

	//See: https://www.pivotaltracker.com/story/show/173266064

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Override
  public ObjectMapper getContext(Class<?> type) {
    return OBJECT_MAPPER;
  }

  public static ObjectMapper objectMapper() {
    return OBJECT_MAPPER;
  }


}