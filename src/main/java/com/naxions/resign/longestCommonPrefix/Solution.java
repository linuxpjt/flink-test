package com.naxions.resign.longestCommonPrefix;

public class Solution {
    public String longestCommonPrefix(String[] strs) {
        String value = strs[0];
        for (int i = 1; i < strs.length; i++) {
            value = publicString(value, strs[i]);
        }
        return value;
    }

    public String publicString(String str1, String str2) {
        StringBuilder s = new StringBuilder();
        int l = Math.min(str1.length(), str2.length());
        for (int i = 0; i < l; i++) {
            if (str1.charAt(i) == str2.charAt(i)) {
                s.append(str1.charAt(i));
            } else {
                return s.toString();
            }
        }
        return s.toString();
    }

    public static void main(String[] args) {
        Solution solution = new Solution();
        String s = solution.longestCommonPrefix(new String[]{"cir","car"});
        System.out.println(s);
    }
}
