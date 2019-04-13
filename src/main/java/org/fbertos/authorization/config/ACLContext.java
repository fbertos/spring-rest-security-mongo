package org.fbertos.authorization.config;

import java.net.UnknownHostException;

import org.fbertos.persistence.dao.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.data.authentication.UserCredentials;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.acls.AclPermissionCacheOptimizer;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.dao.AclRepository;
import org.springframework.security.acls.domain.AclAuthorizationStrategy;
import org.springframework.security.acls.domain.AclAuthorizationStrategyImpl;
import org.springframework.security.acls.domain.ConsoleAuditLogger;
import org.springframework.security.acls.domain.DefaultPermissionGrantingStrategy;
import org.springframework.security.acls.domain.EhCacheBasedAclCache;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.acls.mongodb.BasicLookupStrategy;
import org.springframework.security.acls.mongodb.MongoDBMutableAclService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.mongodb.MongoClient;

@Configuration
@EnableMongoRepositories(basePackageClasses = {AclRepository.class, UserRepository.class })
public class ACLContext {
	@Autowired
    private Environment environment;
	
	@Autowired
	private AclRepository aclRepository;
	
	@Bean
    public MongoTemplate mongoTemplate() throws UnknownHostException
    {
		String host = environment.getProperty("spring.data.mongodb.host");
		String port = environment.getProperty("spring.data.mongodb.port");
		String db = environment.getProperty("spring.data.mongodb.database");
		String user = environment.getProperty("spring.data.mongodb.username");
		String pwd = environment.getProperty("spring.data.mongodb.password");
        MongoClient mongoClient = new MongoClient(host, new Integer(port));
        return new MongoTemplate(mongoClient, db);
    }

    @Bean
    public AclAuthorizationStrategy aclAuthorizationStrategy() {
        return new AclAuthorizationStrategyImpl(new SimpleGrantedAuthority("USER_READ"));
    }

    @Bean
    public PermissionGrantingStrategy permissionGrantingStrategy() {
        ConsoleAuditLogger consoleAuditLogger = new ConsoleAuditLogger();
        return new DefaultPermissionGrantingStrategy(consoleAuditLogger);
    }

    @Bean
    public LookupStrategy lookupStrategy() throws UnknownHostException {
        return new BasicLookupStrategy(mongoTemplate(), aclCache(), aclAuthorizationStrategy(), permissionGrantingStrategy());
    }

    @Bean
    public EhCacheBasedAclCache aclCache() {
        return new EhCacheBasedAclCache(aclEhCacheFactoryBean().getObject(), permissionGrantingStrategy(), aclAuthorizationStrategy());
    }

    @Bean
    public EhCacheFactoryBean aclEhCacheFactoryBean() {
        EhCacheFactoryBean ehCacheFactoryBean = new EhCacheFactoryBean();
        ehCacheFactoryBean.setCacheManager(aclCacheManager().getObject());
        ehCacheFactoryBean.setCacheName("aclCache");
        return ehCacheFactoryBean;
    }

    @Bean
    public EhCacheManagerFactoryBean aclCacheManager() {
        return new EhCacheManagerFactoryBean();
    }
    
    @Bean
    public AclService aclService() throws UnknownHostException {
        return new MongoDBMutableAclService(aclRepository, lookupStrategy(), aclCache());
    }
    
    @Bean
    public MethodSecurityExpressionHandler defaultMethodSecurityExpressionHandler() throws UnknownHostException {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        AclPermissionEvaluator permissionEvaluator = new AclPermissionEvaluator(aclService());
        expressionHandler.setPermissionEvaluator(permissionEvaluator);
        expressionHandler.setPermissionCacheOptimizer(new AclPermissionCacheOptimizer(aclService()));
        return expressionHandler;
    }
}


