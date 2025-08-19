package com.example.productdataetl.config;

import com.example.productdataetl.exception.TransientDatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.classify.Classifier;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.RetryContext;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom retry policy that implements different retry strategies
 * for different types of exceptions.
 */
public class CustomRetryPolicy implements RetryPolicy {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomRetryPolicy.class);
    private final ExceptionClassifierRetryPolicy delegate;
    
    public CustomRetryPolicy(int maxAttempts) {
        this.delegate = new ExceptionClassifierRetryPolicy();
        
        // Configure different retry policies for different exception types
        Map<Class<? extends Throwable>, RetryPolicy> policyMap = new HashMap<>();
        
        // Transient database exceptions - retry up to maxAttempts times
        policyMap.put(TransientDatabaseException.class, new SimpleRetryPolicy(maxAttempts));
        policyMap.put(TransientDataAccessException.class, new SimpleRetryPolicy(maxAttempts));
        policyMap.put(DataAccessResourceFailureException.class, new SimpleRetryPolicy(maxAttempts));
        
        // SQL exceptions that might be transient - retry fewer times
        policyMap.put(SQLException.class, new SimpleRetryPolicy(Math.max(1, maxAttempts - 1)));
        
        // Default policy for other exceptions - no retry
        SimpleRetryPolicy defaultPolicy = new SimpleRetryPolicy(1);
        
        delegate.setPolicyMap(policyMap);
        delegate.setExceptionClassifier(new Classifier<Throwable, RetryPolicy>() {
            @Override
            public RetryPolicy classify(Throwable throwable) {
                RetryPolicy policy = policyMap.get(throwable.getClass());
                if (policy != null) {
                    logger.debug("Using specific retry policy for {}: max attempts = {}", 
                            throwable.getClass().getSimpleName(), 
                            policy instanceof SimpleRetryPolicy ? ((SimpleRetryPolicy) policy).getMaxAttempts() : "unknown");
                    return policy;
                }
                
                // Check for parent classes
                for (Map.Entry<Class<? extends Throwable>, RetryPolicy> entry : policyMap.entrySet()) {
                    if (entry.getKey().isAssignableFrom(throwable.getClass())) {
                        logger.debug("Using parent class retry policy for {}: {}", 
                                throwable.getClass().getSimpleName(), entry.getKey().getSimpleName());
                        return entry.getValue();
                    }
                }
                
                logger.debug("Using default retry policy (no retry) for {}", throwable.getClass().getSimpleName());
                return defaultPolicy;
            }
        });
    }
    
    @Override
    public boolean canRetry(RetryContext context) {
        boolean canRetry = delegate.canRetry(context);
        if (!canRetry && context.getRetryCount() > 0) {
            logger.warn("Retry limit reached for exception after {} attempts: {}", 
                    context.getRetryCount(), 
                    context.getLastThrowable() != null ? context.getLastThrowable().getMessage() : "unknown");
        }
        return canRetry;
    }
    
    @Override
    public RetryContext open(RetryContext parent) {
        return delegate.open(parent);
    }
    
    @Override
    public void close(RetryContext context) {
        delegate.close(context);
    }
    
    @Override
    public void registerThrowable(RetryContext context, Throwable throwable) {
        if (context.getRetryCount() > 0) {
            logger.info("Retrying after exception (attempt {}): {}", 
                    context.getRetryCount() + 1, throwable.getMessage());
        }
        delegate.registerThrowable(context, throwable);
    }
}