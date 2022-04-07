package com.naxions.resign.ispalindrome;

public class Solution {
    public boolean isPalindrome(int x) {
        if (x<0 || x/10==0 && x!=0) {
            return false;
        }

        int reverseNum = 0;
        while (x > reverseNum) {
            reverseNum = reverseNum*10 + x%10;
            x /= 10;
        }

        return x==reverseNum || x==reverseNum/10;
    }


    public static void main(String[] args) {
        int a = 1234321;
        Solution solution = new Solution();
        boolean b = solution.isPalindrome(a);
        System.out.println(b);
    }
}
