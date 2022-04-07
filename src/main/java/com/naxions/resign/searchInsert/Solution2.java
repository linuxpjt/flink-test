package com.naxions.resign.searchInsert;

// 二分法查找
public class Solution2 {
    public int searchInsert(int[] nums, int target) {
        int n = nums.length;
        int l=0,r=n-1;
        while (l <= r) {
            int mid = (l + r) / 2;
            if (nums[mid] <target) {
                l = mid+1;
            } else r = mid-1;
        }
        return l;
    }

    public static void main(String[] args) {
        int[] a = {1,3,5,6};
        int n = new Solution2().searchInsert(a, 7);

        System.out.println(n);
    }
}