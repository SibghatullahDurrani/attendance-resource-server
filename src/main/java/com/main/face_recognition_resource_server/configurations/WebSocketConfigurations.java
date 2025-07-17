package com.main.face_recognition_resource_server.configurations;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.DefaultContentTypeResolver;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
//@EnableWebSocketSecurity
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@Slf4j
public class WebSocketConfigurations implements WebSocketMessageBrokerConfigurer {
  @Value("${front-end-url}")
  private String FRONT_END_URL;

  private final AuthenticationManager authenticationManager;

  public WebSocketConfigurations(AuthenticationManager authenticationManager) {
    this.authenticationManager = authenticationManager;
  }

//  @Bean
//  @Primary // Mark this as the primary executor
//  public TaskExecutor taskExecutor() {
//    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//    executor.setCorePoolSize(10); // Adjust as needed
//    executor.setMaxPoolSize(20); // Adjust as needed
//    executor.setThreadNamePrefix("ws-task-executor-");
//    executor.initialize();
//    return executor;
//  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.setApplicationDestinationPrefixes("/app");
    registry.enableSimpleBroker("/topic");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws").setAllowedOrigins(FRONT_END_URL)
            .setHandshakeHandler(new MyHandshakeHandler())
            .withSockJS();
  }

//  @Override
//  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
//    argumentResolvers.add(new AuthenticationPrincipalArgumentResolver());
//  }

  @Override
  public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
    DefaultContentTypeResolver resolver = new DefaultContentTypeResolver();
    resolver.setDefaultMimeType(MediaType.APPLICATION_JSON);
    MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
    converter.setObjectMapper(new ObjectMapper());
    converter.setContentTypeResolver(resolver);
    messageConverters.add(converter);

    return false;
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(new MyInboundChannelInterceptor());
  }

  @Override
  public void configureClientOutboundChannel(ChannelRegistration registration) {
    registration.interceptors(new MyOutBoundChannelInterceptor());
  }

  private void logOutboundMessage(Message<?> message) {
    var accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
    log.info("================= MyoutboundChannelInterceptor =================");
    if (accessor != null) {
      String destination = accessor.getDestination();
      String payload = convertPayload(message.getPayload());
      log.info("OUTBOUND to [{}]: {}", destination, payload);
    }
  }

  private String convertPayload(Object payload) {
    if (payload instanceof byte[]) {
      return new String((byte[]) payload, StandardCharsets.UTF_8);
    }
    return payload.toString();
  }

  private class MyHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
      log.info("============== MyHandleShakeHandler =============");
      log.info("thread.id: {}", Thread.currentThread().getId());
      log.info("thread.name: {}", Thread.currentThread().getName());
      String token = request.getURI().getQuery().substring(6);
      var user = authenticationManager.authenticate(new BearerTokenAuthenticationToken(token));
      return user;
    }
  }

  private class MyInboundChannelInterceptor implements ChannelInterceptor {
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
      log.info("================= MyInboundChannelInterceptor =================");
      log.info("thread.id: {}", Thread.currentThread().getId());
      log.info("thread.name: {}", Thread.currentThread().getName());

      final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
      log.info("accessor.user: {}", accessor.getUser());

//      if (StompCommand.CONNECT == accessor.getCommand()) {
//        log.info("=============== CONNECT =============");
//        MessageHeaders headers = message.getHeaders();
//        headers.forEach((h, index) -> {
//          log.info("{} -> {}", h, headers.get(h));
//          String token = accessor.getFirstNativeHeader("access_token");
//          log.info("token: {}", token);
//
//          JwtAuthenticationToken user = (JwtAuthenticationToken) authenticationManager.authenticate(new BearerTokenAuthenticationToken(token));
//          log.info("simpUser: {}", user);
//          log.info("name: {}", user.getName());
//          log.info("token.subject: {}", user.getToken().getSubject());
//          accessor.setUser(user);
//        });
//      }
      if (StompCommand.SEND == accessor.getCommand()) {
        log.info("=============== SEND =============");
        MessageHeaders headers = message.getHeaders();
        headers.forEach((h, index) -> {
          log.info("{} -> {}", h, headers.get(h));
        });

      }
      return message;
    }
  }

  private class MyOutBoundChannelInterceptor implements ChannelInterceptor {
    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
//      var accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
      ChannelInterceptor.super.afterSendCompletion(message, channel, sent, ex);
      logOutboundMessage(message);

    }
  }
}
