package com.naxions.resign.searchInsert;

// 暴力查找
public class Solution {
    public int searchInsert(int[] nums, int target) {
        int index;
        int n = nums.length;
        if (nums[0] >= target) {
            return 0;
        }
        if (nums[n-1]==target){
            return n-1;
        }
        for (int i = 0; i < n-1; i++) {
            if (nums[i] == target) {
                return i;
            } else if (nums[i]<target && nums[i+1]>target){
                index = i+1;
                return index;
            }
        }
        return n;
    }

    public static void main(String[] args) {
        int[] a = {1,3,5,6};
        int n = new Solution().searchInsert(a, 7);

        System.out.println(n);
    }
}
