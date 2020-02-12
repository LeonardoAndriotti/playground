package br.com.playground.application;

import br.com.playground.usecase.UseCase;
import org.springframework.beans.factory.annotation.Autowired;

public class Application {

    @Autowired
    private UseCase api;
}
