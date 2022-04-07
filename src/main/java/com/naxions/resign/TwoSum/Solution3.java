package com.naxions.resign.TwoSum;

import java.util.Arrays;
import java.util.HashMap;

public class Solution3 {
    public int[] twoSum(int[] nums, int target) {
        HashMap<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            if (map.containsKey(nums[i])) {
                return new int[]{map.get(nums[i]), i};
            }
            map.put(target - nums[i], i);
        }
        return new int[]{1, 2, 3};
    }

    public static void main(String[] args) {
        int[] a = new int[3];
        a[0] = 1;
        a[1] = 2;
        a[2] = 3;

        Solution3 s = new Solution3();

        int[] b = s.twoSum(a, 5);
        System.out.println(Arrays.toString(b));
    }
}
