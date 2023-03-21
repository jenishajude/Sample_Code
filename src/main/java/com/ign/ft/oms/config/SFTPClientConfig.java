package com.ign.ft.oms.config;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.file.FileNameGenerator;
import org.springframework.integration.sftp.outbound.SftpMessageHandler;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;


@Configuration
@ComponentScan
public class SFTPClientConfig {

	
	  @Value("${sftp.host}") 
	  private String sftpHost;
	  
	  @Value("${sftp.user}") 
	  private String sftpUser;
	  
	  @Value("${sftp.password}") 
	  private String sftpPassword;
	  
	  @Value("${sftp.port}") 
	  private int sftpPort;
	  
	  @Value("${sftp.remote.directory:/}") 
	  private String sftpRemoteDirectory;
	  
	  @Value("${sftp.privateKey:#{null}}")
	  private Resource sftpPrivateKey;

	  @Value("${sftp.privateKeyPassphrase:}")
	  private String sftpPrivateKeyPassphrase;
	  
	  
	  @Bean
	    public DefaultSftpSessionFactory  sftpSessionFactory() {
	        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);
	        factory.setHost(sftpHost);
	        factory.setPort(sftpPort);
	        factory.setUser(sftpUser);
	        factory.setPassword(sftpPassword);
	        factory.setAllowUnknownKeys(true);
	        return factory;
	    }
	    @Bean
	    @ServiceActivator(inputChannel = "toDscoSftpChannel")
	    public MessageHandler handlerDsco() {
	        SftpMessageHandler handler = new SftpMessageHandler(sftpSessionFactory());
	        handler.setUseTemporaryFileName(false);
	        handler.setRemoteDirectoryExpression(new LiteralExpression(sftpRemoteDirectory));
	        handler.setFileNameGenerator(new FileNameGenerator() {
	            @Override
	            public String generateFileName(Message<?> message) {
	                if (message.getPayload() instanceof File) {
	                    return ((File) message.getPayload()).getName();
	                } else {
	                    throw new IllegalArgumentException("File expected as payload.");
	                }
	            }
	        });
	        return handler;
	    } 
	    
	    @MessagingGateway
	    public interface UploadGateway {
	    	
	    	@Gateway(requestChannel = "toDscoSftpChannel")
	    	void uploadDsco(File file); 
	    }
	     
	    @Bean
	    public SftpRemoteFileTemplate template() {
	        return new SftpRemoteFileTemplate(sftpSessionFactory());
	    }
	    

}
