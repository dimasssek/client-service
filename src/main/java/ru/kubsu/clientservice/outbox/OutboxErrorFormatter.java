package ru.kubsu.clientservice.outbox;

/**
 * Форматирование текста ошибки outbox без stacktrace.
 */
public final class OutboxErrorFormatter {

    private OutboxErrorFormatter() {
    }

    /**
     * Формирует краткое описание ошибки: что произошло и где.
     *
     * @param location       место возникновения ошибки
     * @param exception      исключение
     * @param maxErrorLength максимальная длина текста
     * @return текст для varchar-поля
     */
    public static String format(String location, Throwable exception, int maxErrorLength) {
        String exceptionName = exception.getClass().getSimpleName();
        String message = exception.getMessage() != null ? exception.getMessage() : "без сообщения";
        String formatted = exceptionName + ": " + message + " at " + location;
        if (formatted.length() <= maxErrorLength) {
            return formatted;
        }
        return formatted.substring(0, maxErrorLength);
    }
}
