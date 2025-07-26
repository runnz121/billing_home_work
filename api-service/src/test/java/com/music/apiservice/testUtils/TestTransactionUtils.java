package com.music.apiservice.testUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class TestTransactionUtils {

    private static TransactionalOperator operator;

    @Autowired
    public TestTransactionUtils(TransactionalOperator operator) {
        TestTransactionUtils.operator = operator;
    }

    public static <T> Mono<T> withRollBack(Mono<T> publisher){
        return operator.execute(tx->{
            tx.setRollbackOnly();
            return publisher;
        }).next();
    }

    public static <T> Flux<T> withRollBack(Flux<T> publisher){
        return operator.execute(tx->{
            tx.setRollbackOnly();
            return publisher;
        });
    }
}
