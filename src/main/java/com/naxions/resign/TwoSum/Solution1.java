package com.naxions.resign.TwoSum;

import java.util.Arrays;

public class Solution1 {
    public int[] twoSum(int[] nums, int target) {
        int[] a = new int[2];
        for (int i = 0; i < nums.length; i++) {
            for (int j=i+1; j<nums.length; j++) {
                if (nums[i]+nums[j] == target){
                    a[0] = i;
                    a[1] = j;
                }
            }
        }
        return a;
    }

    public static void main(String[] args) {
        int[] a = new int[3];
        a[0] = 1;
        a[1] = 2;
        a[2] = 3;

        Solution1 s = new Solution1();

        int[] b = s.twoSum(a, 3);
        System.out.println(Arrays.toString(b));
        System.out.println(b[0]);
        System.out.println(b[1]);
    }
}
