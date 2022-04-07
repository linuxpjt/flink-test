package com.naxions.resign.TwoSum;

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
            if (map.containsKey(a) && j!=map.get(a)){
                b[0] = j;
                b[1] = map.get(a);
            }
        }
        return b;
    }

    public static void main(String[] args) {
        int[] a = new int[3];
        a[0] = 1;
        a[1] = 2;
        a[2] = 3;

        Solution2 s = new Solution2();

        int[] b = s.twoSum(a, 4);
        System.out.println(Arrays.toString(b));

    }
}
