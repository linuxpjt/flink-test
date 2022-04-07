package com.naxions.resign.removeElement;

import java.util.Arrays;

public class Solution {
    // [0,1,2,2,3,0,4,2]  2
    public int removeElement(int[] nums, int val) {
        int n = nums.length;
        int left = 0;
        for (int right = 0; right < n; right++) {
            if (nums[right] != val) {
                nums[left] = nums[right];
                left++;
            }
        }
        System.out.println(Arrays.toString(nums));
        return left;
    }

    public static void main(String[] args) {
        int[] a = new int[]{0,1,2,2,3,0,4,2};
        int m = new Solution().removeElement(a,2);
        System.out.println(m);
    }
}
