package com.example.chess.model;

public abstract class Result {
    private Result() {}

    public boolean isSuccess() {
        return this instanceof Success;
    }

    public static final class Success extends Result {
        private final User user; // Usa il TUO modello User

        public Success(User user) {
            this.user = user;
        }

        public User getUser() {
            return user;
        }
    }

    public static final class Error extends Result {
        private final String message;

        public Error(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}