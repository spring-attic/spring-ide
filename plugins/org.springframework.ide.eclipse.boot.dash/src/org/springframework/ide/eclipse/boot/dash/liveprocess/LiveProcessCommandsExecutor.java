package org.springframework.ide.eclipse.boot.dash.liveprocess;

import java.util.List;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LiveProcessCommandsExecutor {

	interface Server {
		Flux<CommandInfo> listCommands();
		Mono<Void> executeCommand(CommandInfo cmd);
	}

	static LiveProcessCommandsExecutor getDefault() {
		return new DefaultLiveProcessCommandExecutor();
	}

	List<Server> getLanguageServers();
}
