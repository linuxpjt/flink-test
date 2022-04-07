package com.naxions.resign.removeDuplicates;

import java.util.Arrays;

public class Solution {
    public int removeDuplicates(int[] nums) {
        int n = 0;

        for (int i = 1; i < nums.length; i++) {
            if (nums[i] == nums[n]){

            } else {
//                n += 1;
                nums[++n] = nums[i];
            }
        }
        System.out.println(Arrays.toString(nums));
        return n+1;
//        System.out.println(n);
//        return Arrays.copyOfRange(nums,0,n+1);
    }

    public static void main(String[] args) {
        int[] a = new int[]{1,1,2,3,3,3,4};

        int i = new Solution().removeDuplicates(a);
        System.out.println(i);
    }
}
