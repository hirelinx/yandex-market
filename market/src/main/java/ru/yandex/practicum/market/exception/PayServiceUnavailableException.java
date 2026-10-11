package ru.yandex.practicum.market.exception;

public class PayServiceUnavailableException extends RuntimeException {

    public PayServiceUnavailableException(String message) {
        super(message);
    }

    public PayServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
