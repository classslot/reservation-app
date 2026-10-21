package com.example.reservation.common;

/** クエリパラメータの文字列をenumに変換する（不正値は422） */
public final class Enums {
    private Enums() {}

    public static <E extends Enum<E>> E parse(Class<E> type, String value, String label) {
        if (value == null || value.isBlank()) return null;
        try {
            return Enum.valueOf(type, value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw ApiException.invalid(label + "の指定が正しくありません");
        }
    }
}
