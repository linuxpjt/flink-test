package com.naxions.resign.reverse;

public class Solution1 {
    public int reverse(int x) {
        int rev = 0;
        while (x !=0) {
            if (rev < Integer.MIN_VALUE/10 || rev > Integer.MAX_VALUE/10) {
                return 0;
            }

            int temp = x%10;
            x = x/10;
            rev = rev*10 + temp;
        }

        return rev;
    }

    public static void main(String[] args) {
        int a = -3911;

        Solution1 solution1 = new Solution1();
        int res = solution1.reverse(a);
        System.out.println(res);
    }
}
