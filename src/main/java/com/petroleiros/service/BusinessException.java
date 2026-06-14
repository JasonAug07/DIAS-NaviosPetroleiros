package com.petroleiros.service;

/**
 * Excepção lançada quando uma regra de negócio é violada.
 * Os Controllers apanham esta excepção e mostram a mensagem ao utilizador.
 */
public class BusinessException extends Exception {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
