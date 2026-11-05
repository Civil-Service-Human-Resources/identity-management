//package uk.gov.cshr.config;
//
//import net.javacrumbs.schedlock.core.LockProvider;
//import net.javacrumbs.schedlock.spring.LockingTaskExecutor;
//import net.javacrumbs.schedlock.spring.DefaultLockingTaskExecutor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class SchedLockConfig {
//
//    @Bean
//    public LockingTaskExecutor lockingTaskExecutor(LockProvider lockProvider) {
//        return new DefaultLockingTaskExecutor(lockProvider);
//    }
//}
