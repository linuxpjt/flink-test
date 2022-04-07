package com.naxions.resign.TwoSum.excr;

import java.util.Arrays;
import java.util.HashMap;

public class Solution2 {
    public int[] twoSum(int[] nums, int target) {
        int[] b = new int[2];
        HashMap<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            map.put(nums[i], i);
        }
        for (int j = 0; j < nums.length; j++) {
            int a = target - nums[j];
            if (map.containsKey(a) && j!=map.get(a)) {
                b[0]=j;
                b[1] = map.get(a);
            }
        }
        return b;
    }

    public static void main(String[] args) {
        int[] a = new int[]{1,2,3};
        Solution2 solution2 = new Solution2();
        int[] c = solution2.twoSum(a, 4);

        System.out.println(Arrays.toString(c));
    }
}
