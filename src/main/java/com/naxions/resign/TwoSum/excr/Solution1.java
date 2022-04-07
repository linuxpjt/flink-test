package com.naxions.resign.TwoSum.excr;

import java.util.Arrays;

public class Solution1 {
    public int[] twoSum(int[] nums, int target){
        int[] b = new int[2];
        for (int i = 0; i < nums.length; i++) {
            int a = target - nums[i];
            for (int j = i+1; j < nums.length; j++) {
                if (a == nums[j]) {
                    b[0] = i;
                    b[1] = j;
                }
            }
        }
        return b;
    }

    public static void main(String[] args) {
        int[] a = new int[]{1,2,2};
        Solution1 solution1 = new Solution1();
        int[] c = solution1.twoSum(a, 4);

        System.out.println(Arrays.toString(c));
    }
}
