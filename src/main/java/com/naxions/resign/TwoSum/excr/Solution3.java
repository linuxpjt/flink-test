package com.naxions.resign.TwoSum.excr;

import java.util.Arrays;
import java.util.HashMap;

public class Solution3 {
    public int[] twoSum(int[] nums, int target){
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
        int[] a = new int[]{1,2,3};
        Solution3 solution3 = new Solution3();
        int[] c = solution3.twoSum(a, 4);

        System.out.println(Arrays.toString(c));
    }
}
