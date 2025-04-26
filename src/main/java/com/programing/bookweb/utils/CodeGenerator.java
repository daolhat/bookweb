package com.programing.bookweb.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class CodeGenerator {

    private static final Random random = new Random();
    private static final String ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

    public static String generateCategoryCode(String categoryName) {
        if (categoryName == null || categoryName.isEmpty()) {
            return "X" + String.format("%03d", random.nextInt(1000));
        }

        String firstChar = categoryName.trim().substring(0, 2).toUpperCase();
        int randomNum = random.nextInt(1000);

        return firstChar + String.format("%03d", randomNum);
    }


    public static String generateProductCode(String categoryCode) {
        if (categoryCode == null || categoryCode.length() != 5) {
            categoryCode = "XX" + String.format("%03d", random.nextInt(1000));
        }

        int randomNum = random.nextInt(1000000);

        return categoryCode + String.format("%06d", randomNum);
    }

    public static String generateOrderCode(String userName, LocalDateTime orderDateTime, int productCount) {
        StringBuilder randomPrefix = new StringBuilder();
        for (int i = 0; i < 2; i++) {
            randomPrefix.append(ALPHA.charAt(random.nextInt(ALPHA.length())));
        }

        String userPrefix;
        if (userName == null || userName.length() < 2) {
            userPrefix = "XX";
        } else {
            userPrefix = userName.trim().substring(0, 2).toUpperCase();
        }

        String dateString = orderDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));

        String countString = String.format("%02d", Math.min(productCount, 99));

        int randomNum = random.nextInt(99);

        return randomPrefix.toString() + userPrefix + dateString + countString + String.format("%02d", randomNum);
    }

}
